package com.example.bomberman;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import pt.utl.ist.cmov.wifidirect.SimWifiP2pBroadcast;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pDevice;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pDeviceList;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pInfo;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pManager.GroupInfoListener;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pManager.PeerListListener;
import pt.utl.ist.cmov.wifidirect.service.SimWifiP2pService;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.bomberman.model.Arena;
import com.example.bomberman.model.Bomberman;
import com.example.bomberman.model.Robot;
import com.example.bomberman.network.NetworkService;
import com.example.bomberman.network.WDSimServiceConnection;
import com.example.bomberman.network.broadcastreceivers.WDSimBroadcastReceiver;
import com.example.bomberman.util.GameConfigs;
import com.example.bomberman.util.ScoreBoard;

public class GameActivity extends Activity implements PeerListListener, GroupInfoListener {

	protected GameConfigs gc;
	private GamePanel gamePanel;
	private TextView playerNameView;
	private TextView timeLeftView;
	private TextView scoreView;
	private TextView playerCountView;
	private Button toggleStateButton;
	private Button quitButton;
	private Button upButton;
	private Button leftButton;
	private Button rightButton;
	private Button downButton;
	private Button bombButton;
	private TreeMap<Integer, String> users = new TreeMap<Integer, String>();
	private String playerName;
	public char playerId; //visivel para a arena
	private Thread timerThread;
	private Timer sendImSetTimer;
	private static int countDown;
	private int score;
	private int numPlayers;
	private int maxPlayers;
	protected NetworkService service;
	public boolean singleplayer = true; //visivel para os robots
	private boolean gameMasterIsReady = false;
	private boolean gameOngoing = false;
	private boolean inGroup = false;
	private boolean unpausing = false;
	private boolean isGroupOwner = false;
	private String serverAddress = null;
	private WDSimServiceConnection servConn = null;
	private WDSimBroadcastReceiver receiver;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gc = (GameConfigs)getIntent().getSerializableExtra("gc");
		playerName = getIntent().getStringExtra("playerName");
		playerId = getIntent().getStringExtra("playerId").charAt(0);
		singleplayer = getIntent().getExtras().getBoolean("singleplayer");
		numPlayers = getIntent().getExtras().getInt("numPlayers");

		if(!singleplayer) {
			gameOngoing = getIntent().getExtras().getBoolean("gameOngoing");
			maxPlayers = getIntent().getExtras().getInt("maxPlayers");
			HashMap<Integer, String> receivedMap = (HashMap<Integer, String>)getIntent().getExtras().getSerializable("usersMap");
			users.putAll(receivedMap);
		} else {
			users.put(1, playerName);
		}

		setContentView(R.layout.activity_game);

		playerNameView = (TextView)findViewById(R.id.activity_game_player_name);
		scoreView = (TextView)findViewById(R.id.activity_game_score);
		timeLeftView = (TextView)findViewById(R.id.activity_game_time_left);
		playerCountView = (TextView)findViewById(R.id.activity_game_player_count);
		toggleStateButton = (Button)findViewById(R.id.toggleStateBtn);
		quitButton = (Button)findViewById(R.id.quit);
		upButton = (Button)findViewById(R.id.up);
		leftButton = (Button)findViewById(R.id.left);
		rightButton = (Button)findViewById(R.id.right);
		downButton = (Button)findViewById(R.id.down);
		bombButton = (Button)findViewById(R.id.bomb);

		playerNameView.setText("Username:\n" + playerName);
		scoreView.setText("Score:\n0");
		timeLeftView.setText("Time left:\n" + gc.gameDuration);
		playerCountView.setText("# Players:\n" + numPlayers);

		// Timer setup, it will only start when the game starts.
		countDown = gc.gameDuration;
		
		/*
		 * Network related code.
		 */
		
		service = new NetworkService();
		service.setGameActivity(this);

		// register broadcast receiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
		receiver = new WDSimBroadcastReceiver(this);
		registerReceiver(receiver, filter);

		servConn = new WDSimServiceConnection(this);
		if(servConn.getManager() == null) {
			Log.d("SERVCON", "ESTOU NO IF3 == NULL");
			Intent intent = new Intent(this, SimWifiP2pService.class);
			bindService(intent, (ServiceConnection) servConn,
					Context.BIND_AUTO_CREATE);
		}


	}

	public void setGamePanel(GamePanel gPanel) {
		this.gamePanel = gPanel;
	}

	public int getNumPlayers(){
		return this.numPlayers;
	}

	public int getMaxPlayers(){
		return this.maxPlayers;
	}

	public int getCountDown(){
		return countDown;
	}

	public GamePanel getGamePanel(){
		return this.gamePanel;
	}

	public String getActivePlayer(){
		return this.playerName;
	}

	//O game master responde a um join tardio com o tempo actual do jogo
	//este jogador vai actualizar o tempo e comecar o seu timer
	public void setCountDown(int clock){
		countDown = clock;
		startTimer();
	}

	public boolean masterIsReady(){
		return this.gameMasterIsReady;
	}

	@Override
	public void onPause() {
		super.onPause();
		gamePanel.pause();
		toggleGameState(null);
		unpausing = true;
	}

	@Override
	public void onResume() {
		super.onResume();
		if(unpausing){
			gamePanel.resume();
			toggleGameState(null);
		}
	}
	
	@Override
	public void onBackPressed(){
		this.toggleGameState(null);
	}

	public void updatePlayerList(TreeMap<Integer, String> clientsNames) {
		users = clientsNames;
	}

	//The arena will call this on its first update
	//This method starts the timer and enables the control buttons, effectively starting the game!
	public void startGame(){
		if(singleplayer){
			startTimer();
		}else{
			//in multiplayer buttons become disabled until timer starts
			if(gameOngoing){
				service.midJoin();
				//o timer so vai comecar quando o game master enviar o clock actual do jogo
			}else{
				if(numPlayers == 1){//nao tenho que esperar pela resposta de ninguem
					gameMasterIsReady = true;
					startTimer();
					gamePanel.getArena().startRobots();
					return;
				}
				runOnUiThread(new Runnable() {
					public void run() {
						disableControlButtons();
						toggleStateButton.setEnabled(false);
					}
				});

				//each participant will send "im set" to the party leader every 2 secs, until lider starts
				if(playerId != '1'){
					sendImSetTimer = new Timer();
					sendImSetTimer.schedule(new TimerTask() {   
						@Override
						public void run() {
							service.imSet();
						}
					}, 2000, 2000);
				}
				//The game master is now ready to accept 'im set' messages
				//From this point onwards, when player 1 receives the 'im set' messages from all participants
				//he will start the game, as everyone is ready by then. (This is done on the service!)
				else
					gameMasterIsReady = true;
			}
		}
	}

	public void movePlayerUp(View v){
		if(singleplayer){
			this.goUpOrder('1', null, null);
		}else{
			Bomberman bomber = gamePanel.getArena().getPlayer(playerId);
			service.goUp(bomber.i, bomber.j);
		}
	}

	public void movePlayerDown(View v){
		if(singleplayer){
			this.goDownOrder('1', null, null);
		} else{
			Bomberman bomber = gamePanel.getArena().getPlayer(playerId);
			service.goDown(bomber.i, bomber.j);
		}
	}

	public void movePlayerLeft(View v){
		if(singleplayer){
			this.goLeftOrder('1', null, null);
		} else{
			Bomberman bomber = gamePanel.getArena().getPlayer(playerId);
			service.goLeft(bomber.i, bomber.j);
		}
	}

	public void movePlayerRight(View v){
		if(singleplayer){
			this.goRightOrder('1', null, null);
		} else{
			Bomberman bomber = gamePanel.getArena().getPlayer(playerId);
			service.goRight(bomber.i, bomber.j);
		}
	}

	public void dropBomb(View v){
		if(singleplayer){
			this.plantBombOrder('1', null, null);
		} else{
			Bomberman bomber = gamePanel.getArena().getPlayer(playerId);
			service.plantBomb(bomber.i, bomber.j);
		}
	}

	public void startTimer() {
		if(timerThread == null || timerThread.getState() == Thread.State.TERMINATED){
			timerThread = new TimerThread();
			timerThread.start();
		}
	}
	
	public void disableQuitButton(View v){
		this.quitButton.setEnabled(false);
	}

	public void enableQuitButton(View v){
		this.quitButton.setEnabled(true);
	}
	
	public void toggleGameState(View v){
		if(singleplayer){
			// In single player pause the whole game (i.e. the thread).
			if(gamePanel.thread.getPaused()){
				toggleStateButton.setText("Pause");
				enableControlButtons();
			} else {
				toggleStateButton.setText("Play");
				disableControlButtons();
				timerThread.interrupt();
			}
		} else{
			// In multiplayer stops drawing and checking collisions for this player
			Bomberman bman = gamePanel.getArena().getPlayer(playerId);
			if (bman != null){
				if(bman.getIsPaused()){
					//if the player paused we want to "unpause" it
					toggleStateButton.setText("Pause");
					enableControlButtons();
					service.resumeGame();
				} else{
					//else we want to pause it
					toggleStateButton.setText("Play");
					disableControlButtons();
					service.pauseGame();
				}
			}

		}
	}

	private void enableControlButtons(){
		upButton.setEnabled(true);
		leftButton.setEnabled(true);
		rightButton.setEnabled(true);
		downButton.setEnabled(true);
		bombButton.setEnabled(true);
	}

	public void disableControlButtons(){
		upButton.setEnabled(false);
		leftButton.setEnabled(false);
		rightButton.setEnabled(false);
		downButton.setEnabled(false);
		bombButton.setEnabled(false);
	}

	//After the local player dies, we disable all buttons except quit
	public void disableControlsAfterDeath(){
		runOnUiThread(new Runnable() {
			public void run() {
				disableControlButtons();
				//disable pause button as well
				toggleStateButton.setEnabled(false);
			}
		});
	}

	public void quitGame(View v){
		if(!singleplayer)
			service.leaveGame();
		timerThread.interrupt();
		gamePanel.thread.setRunning(false);
		Intent intent = new Intent(GameActivity.this, MenuActivity.class);
		intent.putExtra("activePlayer", playerName);
		unregisterReceiver(receiver);
		startActivity(intent);
	}

	public void endGame(){
		ScoreBoard scrs = gamePanel.getArena().scores;
		Intent intent = new Intent(GameActivity.this, ScoresActivity.class);
		intent.putExtra("scores", scrs);
		intent.putExtra("playerName", playerName);
		intent.putExtra("usersMap", users);
		gamePanel.thread.setRunning(false);
		timerThread.interrupt();
		if(service.isServer())
			service.resetServer();
		unregisterReceiver(receiver);
		startActivity(intent);
	}

	/*
	 * Callbacks for the Network Service.
	 */

	public void goUpOrder(char id, String i, String j){
		Arena arena = gamePanel.getArena();
		Bomberman bman = arena.getPlayer(id);
		if(bman != null)
			bman.oneSquareUp(i, j);
	}

	public void goDownOrder(char id, String i, String j){
		Arena arena = gamePanel.getArena();
		Bomberman bman = arena.getPlayer(id);
		if(bman != null)
			bman.oneSquareDown(i, j);
	}

	public void goLeftOrder(char id, String i, String j){
		Arena arena = gamePanel.getArena();
		Bomberman bman = arena.getPlayer(id);
		if(bman != null)
			bman.oneSquareLeft(i, j);
	}

	public void goRightOrder(char id, String i, String j){
		Arena arena = gamePanel.getArena();
		Bomberman bman = arena.getPlayer(id);
		if(bman != null)
			bman.oneSquareRight(i, j);
	}

	public void plantBombOrder(char id, String i, String j){
		Arena arena = gamePanel.getArena();
		Bomberman bman = arena.getPlayer(id);
		if(bman != null)
			bman.plantBomb(i, j);
	}

	public void robotGoUp(int id, String i, String j){
		Arena arena = gamePanel.getArena();
		Robot robot = arena.getRobot(id);
		if (robot != null)
			robot.oneSquareUp(i, j);
	}

	public void robotGoDown(int id, String i, String j){
		Arena arena = gamePanel.getArena();
		Robot robot = arena.getRobot(id);
		if (robot != null)
			robot.oneSquareDown(i, j);
	}

	public void robotGoLeft(int id, String i, String j){
		Arena arena = gamePanel.getArena();
		Robot robot = arena.getRobot(id);
		if (robot != null)
			robot.oneSquareLeft(i, j);
	}

	public void robotGoRight(int id, String i, String j){
		Arena arena = gamePanel.getArena();
		Robot robot = arena.getRobot(id);
		if (robot != null)
			robot.oneSquareRight(i, j);
	}

	public void startGameOrder(){
		if(playerId != '1'){
			sendImSetTimer.cancel(); //stop spamming 'im set' msgs
		}
		runOnUiThread(new Runnable() {
			public void run() {
				enableControlButtons();
				toggleStateButton.setEnabled(true);
				startTimer();
				gamePanel.getArena().startRobots();
			}
		});
	}

	//Quando entra um jogador a meio
	public void newPlayer(char newPlayerId){
		gamePanel.getArena().newPlayer(newPlayerId);
		runOnUiThread(new Runnable() {
			public void run() {
				numPlayers++;
				playerCountView.setText("# Players:\n" + numPlayers);
			}
		});
	}

	public void pausePlayer(char pausePlayerId){
		Arena arena = gamePanel.getArena();
		Bomberman bman = arena.getPlayer(pausePlayerId);
		if(bman != null)
			bman.setIsPaused(true);
	}

	public void resumePlayer(char resumePlayerId){
		Arena arena = gamePanel.getArena();
		Bomberman bman = arena.getPlayer(resumePlayerId);
		if(bman != null)
			bman.setIsPaused(false);
	}

	public void quitPlayer(char quitPlayerId){
		Bomberman bomber = gamePanel.getArena().getPlayer(quitPlayerId);
		if(bomber != null){
			numPlayers -= 1;
			gamePanel.getArena().removeElement(bomber);
			gamePanel.getArena().scores.remove(quitPlayerId+"");
		}
	}
	
	public void serverHasLeft(char newPlayerId){
		onPause();
		disableControlsAfterDeath();
		disableQuitButton(null);
		playerId = newPlayerId;
		
		ScoreBoard newScores = new ScoreBoard();
		ScoreBoard scores = gamePanel.getArena().scores;
		scores.remove("1");
		for(Entry<String, Integer> keyVal : scores.entrySet()){
			newScores.add((Integer.parseInt(keyVal.getKey()) - 1)+ "", keyVal.getValue());
		}
		gamePanel.getArena().setScoreBoard(newScores);
		
		TreeMap<Integer, String> newUsersMap = new TreeMap<Integer, String>();
		users.remove(1);
		for(Entry<Integer, String> idUserName : users.entrySet()){
			newUsersMap.put(idUserName.getKey() - 1, idUserName.getValue());		
		}
		users = newUsersMap;	
	}
	
	public void unsuspendGame() {
		onResume();
		enableControlButtons();
		enableQuitButton(null);
	}

	/*
	 * WDSim related code
	 */

	@Override
	public void onGroupInfoAvailable(SimWifiP2pDeviceList devices,
			SimWifiP2pInfo groupInfo, String goName) {
		inGroup = groupInfo.askIsConnected();
		isGroupOwner = groupInfo.askIsGO();
		if(inGroup) {
			if(playerId == '1' && isGroupOwner)
				service.enableServer();
			for(SimWifiP2pDevice d : devices.getDeviceList()) {
				String[] split = d.virtDeviceAddress.split(":");
				if (d.deviceName.equals(goName))
					serverAddress = split[0];
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			service.connect(serverAddress);
			service.rejoin();
		}
	}
	
	public void requestGroupInfo() {
		servConn.getManager().requestGroupInfo(servConn.getChannel(),
				(GroupInfoListener) this);
	}

	@Override
	public void onPeersAvailable(SimWifiP2pDeviceList arg0) {
	}

	/*
	 * Timer Thread class. It's responsible for the count down of the
	 * time left and also updates the UI (time left and score).
	 */

	public class TimerThread extends Thread {
		long startTime;

		public TimerThread() {
			super();
			startTime = System.currentTimeMillis();
		}

		public void run() {
			while(true) {
				if(isInterrupted())
					break;
				if(System.currentTimeMillis() - startTime >= 1000){
					countDown--;
					runOnUiThread(new Runnable() {
						public void run() {
							if(countDown == 0)
								endGame();
							else{
								timeLeftView.setText("Time left:\n" + countDown);
								Arena arna= gamePanel.getArena();
								ScoreBoard scb = arna.scores;
								score = scb.get(playerId);
								scoreView.setText("Score:\n" + score);
								playerCountView.setText("# Players:\n" + numPlayers);
							}
						}});
					startTime = System.currentTimeMillis();
				}
			}
		}

	}

}
