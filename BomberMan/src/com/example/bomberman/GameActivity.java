package com.example.bomberman;

import java.util.Timer;
import java.util.TimerTask;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import com.example.bomberman.csclient.ClientService;
import com.example.bomberman.model.Arena;
import com.example.bomberman.model.Bomberman;
import com.example.bomberman.model.Robot;
import com.example.bomberman.util.GameConfigs;
import com.example.bomberman.util.ScoreBoard;

public class GameActivity extends Activity implements IGameActivity {
	/** Called when the activity is first created. */

	private static final String TAG = GameActivity.class.getSimpleName();
	protected GameConfigs gc;

	private MainGamePanel gamePanel;
	private TextView timeLeftView;
	private TextView scoreView;
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

	protected ClientService service;
	public boolean singleplayer = true; //visivel para os robots
	private boolean gameMasterIsReady = false;

	@SuppressLint("HandlerLeak") @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requesting to turn the title OFF
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		// making it full screen
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// set our MainGamePanel as the View
		//setContentView(new MainGamePanel(this));

		gc = (GameConfigs)getIntent().getSerializableExtra("gc");
		playerName = getIntent().getStringExtra("playerName");
		playerId = getIntent().getStringExtra("playerId").charAt(0);
		singleplayer = getIntent().getExtras().getBoolean("singleplayer");
		numPlayers = getIntent().getExtras().getInt("numPlayers");
		service = new ClientService();
		service.setGameActivity(this);

		setContentView(R.layout.activity_game);

		TextView playerNameView = (TextView)findViewById(R.id.activity_game_player_name);
		scoreView = (TextView)findViewById(R.id.activity_game_score);
		timeLeftView = (TextView)findViewById(R.id.activity_game_time_left);
		TextView playerCountView = (TextView)findViewById(R.id.activity_game_player_count);
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
					Log.d("SCORE SCB", "PLAYER ID:" + playerId);
					score = scb.get(playerId);
					Log.d("SCORE DEPOIS DO SCB", "SCORE:" + score);
					scoreView.setText("Score:\n" + score);
					//Log.d("ACTIVITY", "PlayerId " + playerId + " Score: " + score);
				}
			}
		};

		Log.d(TAG, "View added");
	}

	public void setGamePanel(MainGamePanel gPanel) {
		this.gamePanel = gPanel;
	}

	public int getNumPlayers(){
		return this.numPlayers;
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
		if(singleplayer){ //em single ou multiplayer (eu posso começar 1 jogo multi sozinho)
			startTimer();
		}else{
			//in multiplayer buttons become disabled until timer starts
			runOnUiThread(new Runnable() {
				public void run() {
					disableControlButtons();
					toggleStateButton.setEnabled(false);
				}
			});
			if(numPlayers == 1){//nao tenho que esperar pela resposta de ninguem
				startGameOrder();
				return;
			}

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
			//From this point onwards, when player 1 receives the 'im set' messages from all participants
			//he will start the game, as everyone is ready by then. (This is done on the service!)
			gameMasterIsReady = true;
		}
	}

	public void movePlayerUp(View v){
		if(singleplayer){
			this.goUpOrder('1', null, null);
		}else{
			Bomberman b = gamePanel.getArena().getPlayer(playerId);
			int[] currentPos = b.getPositionInMatrix();
			int j = currentPos[0];
			int i = currentPos[1];
			service.goUp(i,j);
		}
	}

	public void movePlayerLeft(View v){
		if(singleplayer){
			this.goLeftOrder('1', null, null);
		} else{
			Bomberman b = gamePanel.getArena().getPlayer(playerId);
			int[] currentPos = b.getPositionInMatrix();
			int j = currentPos[0];
			int i = currentPos[1];
			service.goLeft(i,j);
		}
	}

	public void movePlayerDown(View v){
		if(singleplayer){
			this.goDownOrder('1', null, null);
		} else{
			Bomberman b = gamePanel.getArena().getPlayer(playerId);
			int[] currentPos = b.getPositionInMatrix();
			int j = currentPos[0];
			int i = currentPos[1];
			service.goDown(i,j);
		}
	}

	public void movePlayerRight(View v){
		if(singleplayer){
			this.goRightOrder('1', null, null);
		} else{
			Bomberman b = gamePanel.getArena().getPlayer(playerId);
			int[] currentPos = b.getPositionInMatrix();
			int j = currentPos[0];
			int i = currentPos[1];
			service.goRight(i,j);
		}
	}

	public void dropBomb(View v){
		if(singleplayer){
			this.plantBombOrder('1', null, null);
		} else{
			Bomberman b = gamePanel.getArena().getPlayer(playerId);
			int[] currentPos = b.getPositionInMatrix();
			int j = currentPos[0];
			int i = currentPos[1];
			service.plantBomb(i,j);
		}
	}

	public void startTimer() {
		// Start the time left timer.
		timeUpdater = new Timer();
		timeUpdater.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				countDown--;
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

	//After the local player dies, we disable all
	//buttons except quit
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
		startActivity(intent);
	}

	//Callback methods for the client service
	//=======================================
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

}
