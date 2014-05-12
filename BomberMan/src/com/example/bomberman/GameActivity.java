package com.example.bomberman;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import pt.utl.ist.cmov.wifidirect.SimWifiP2pBroadcast;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pDevice;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pDeviceList;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pInfo;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pManager.GroupInfoListener;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pManager.PeerListListener;
import pt.utl.ist.cmov.wifidirect.service.SimWifiP2pService;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.bomberman.model.Arena;
import com.example.bomberman.model.Bomberman;
import com.example.bomberman.model.Robot;
import com.example.bomberman.network.NetworkService;
import com.example.bomberman.network.WDSimServiceConnection;
import com.example.bomberman.network.broadcastreceivers.GameBroadcastReceiver;
import com.example.bomberman.util.GameConfigs;
import com.example.bomberman.util.ScoreBoard;

public class GameActivity extends Activity implements PeerListListener, GroupInfoListener {

	private static final String TAG = GameActivity.class.getSimpleName();
	protected GameConfigs gc;

	private MainGamePanel gamePanel;
	private TextView playerNameView;
	private TextView timeLeftView;
	private TextView scoreView;
	private TextView playerCountView;
	private Button toggleStateButton;
	private Button upButton;
	private Button leftButton;
	private Button rightButton;
	private Button downButton;
	private Button bombButton;

	private String playerName;
	public char playerId; //visivel para a arena

	private Timer timeUpdater;
	private Timer sendImSetTimer;
	private Handler mHandler;
	private int countDown;
	private int score;
	private int numPlayers;
	private int maxPlayers;

	protected NetworkService service;
	public boolean singleplayer = true; //visivel para os robots
	private boolean gameMasterIsReady = false;
	private boolean gameOngoing = false;

	private boolean WDSimEnabled;
	private boolean inGroup = false;
	private WDSimServiceConnection servConn = null;
	
	@SuppressLint("HandlerLeak") @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WDSimEnabled = getIntent().getBooleanExtra("WDState", false);
		gc = (GameConfigs)getIntent().getSerializableExtra("gc");
		playerName = getIntent().getStringExtra("playerName");
		playerId = getIntent().getStringExtra("playerId").charAt(0);
		singleplayer = getIntent().getExtras().getBoolean("singleplayer");
		numPlayers = getIntent().getExtras().getInt("numPlayers");
		if(!singleplayer) {
			gameOngoing = getIntent().getExtras().getBoolean("gameOngoing");
			maxPlayers = getIntent().getExtras().getInt("maxPlayers");
		}
		
		service = new NetworkService();
		service.setGameActivity(this);

		setContentView(R.layout.activity_game);

		playerNameView = (TextView)findViewById(R.id.activity_game_player_name);
		scoreView = (TextView)findViewById(R.id.activity_game_score);
		timeLeftView = (TextView)findViewById(R.id.activity_game_time_left);
		playerCountView = (TextView)findViewById(R.id.activity_game_player_count);
		toggleStateButton = (Button)findViewById(R.id.toggleStateBtn);
		upButton = (Button)findViewById(R.id.up);
		leftButton = (Button)findViewById(R.id.left);
		rightButton = (Button)findViewById(R.id.right);
		downButton = (Button)findViewById(R.id.down);
		bombButton = (Button)findViewById(R.id.bomb);

		playerNameView.setText("Player:\n" + playerName);
		scoreView.setText("Score:\n0");
		timeLeftView.setText("Time left:\n" + gc.gameDuration);
		playerCountView.setText("# Players:\n" + numPlayers);

		// Timer setup, it will only start when the game starts.
		// The handler gets the message from the timer thread to update the UI.
		countDown = gc.gameDuration;
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				if(countDown == 0)
					endGame();
				else{
					timeLeftView.setText("Time left:\n" + countDown);
					Arena arna= gamePanel.getArena();
					ScoreBoard scb = arna.scores;
					score = scb.get(playerId);
					scoreView.setText("Score:\n" + score);
				}
			}
		};
		
		/*
		 * Network related code.
		 */
		
		service = new NetworkService();
		service.setGameActivity(this);
		
		if(WDSimEnabled) {
			// register broadcast receiver
			IntentFilter filter = new IntentFilter();
			filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
			filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
			filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
			filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
			GameBroadcastReceiver receiver = new GameBroadcastReceiver(this);
			registerReceiver(receiver, filter);
			
			servConn = new WDSimServiceConnection(this);
			Intent intent = new Intent(this, SimWifiP2pService.class);
            bindService(intent, (ServiceConnection) servConn, Context.BIND_AUTO_CREATE);
		}
	}

	public void setGamePanel(MainGamePanel gPanel) {
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
	protected void onDestroy() {
		Log.d(TAG, "Destroying...");
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "Stopping...");
		super.onStop();
	}

	//The arena will call this on its first update
	//This method starts the timer and enables the control buttons, effectively starting the game!
	public void startGame(){
		if(singleplayer){
			startTimer();
		}else{
			//in multiplayer buttons become disabled until timer starts
			if(gameOngoing){
				//participantReady = true;
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
		// Start the time left timer.
		timeUpdater = new Timer();
		timeUpdater.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				GameActivity.this.countDown--;
				mHandler.obtainMessage().sendToTarget();
			}
		}, 1000, 1000);
	}

	public void toggleGameState(View v){
		boolean isPaused = gamePanel.thread.getPaused();
		if(isPaused){
			toggleStateButton.setText("Pause");
			enableControlButtons();
			startTimer();
			gamePanel.thread.resumeThread();
		} else {
			toggleStateButton.setText("Play");
			disableControlButtons();
			timeUpdater.cancel();
			gamePanel.thread.pauseThread();
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
		gamePanel.thread.setRunning(false);
		this.finish();
	}

	public void endGame(){
		ScoreBoard scores = gamePanel.getArena().scores;
		Intent intent = new Intent(GameActivity.this, ScoresActivity.class);
		intent.putExtra("scores", scores);
		gamePanel.thread.setRunning(false);
		service.stopServer();
		startActivity(intent);
	}

	/*
	 * Callbacks for the Network Service.
	 */
	
	public void goUpOrder(char id, String i, String j){
		Arena arena = gamePanel.getArena();
		Bomberman bman = arena.getPlayer(id);
		bman.oneSquareUp(i, j);
	}

	public void goDownOrder(char id, String i, String j){
		Arena arena = gamePanel.getArena();
		Bomberman bman = arena.getPlayer(id);
		bman.oneSquareDown(i, j);
	}

	public void goLeftOrder(char id, String i, String j){
		Arena arena = gamePanel.getArena();
		Bomberman bman = arena.getPlayer(id);
		bman.oneSquareLeft(i, j);
	}

	public void goRightOrder(char id, String i, String j){
		Arena arena = gamePanel.getArena();
		Bomberman bman = arena.getPlayer(id);
		bman.oneSquareRight(i, j);
	}

	public void plantBombOrder(char id, String i, String j){
		Arena arena = gamePanel.getArena();
		Bomberman bman = arena.getPlayer(id);
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
	
	/*
	 * WDSim related code
	 */

	@Override
	public void onGroupInfoAvailable(SimWifiP2pDeviceList devices,
			SimWifiP2pInfo groupInfo) {
		inGroup = groupInfo.askIsConnected();
		if(inGroup) {
			ArrayList<String> addresses = new ArrayList<String>();
			for(SimWifiP2pDevice d : devices.getDeviceList()) {
				String[] split = d.virtDeviceAddress.split(":");
				addresses.add(split[0]);
				Log.d("IPs", split[0]);
			}
		}
	}

	@Override
	public void onPeersAvailable(SimWifiP2pDeviceList arg0) {
		// TODO Auto-generated method stub
		
	}

}
