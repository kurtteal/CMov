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
	private Handler mHandler;
	private int countDown;
	private int score;
	
	protected ClientService service;
	public boolean singleplayer = true; //visivel para os robots
	
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
		playerCountView.setText("# Players:\n" + "(todo)");
		
		// Timer setup, it will only start when the game starts.
		// The handler gets the message from the timer thread to update the UI.
		countDown = gc.gameDuration;
		mHandler = new Handler() {
		    public void handleMessage(Message msg) {
		    	if(countDown == 0)
		    		endGame();
		    	else{
		    		timeLeftView.setText("Time left:\n" + countDown);
		    		score = gamePanel.getArena().scores.get(playerId);
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
	public void startTime(){
		if(singleplayer){
			startTimer();
		}else{
			//in multiplayer buttons become disabled until timer starts
			runOnUiThread(new Runnable() {
		        public void run() {
		        	disableControlButtons();
					toggleStateButton.setEnabled(false);
		        }
		    });
			
			//Quem inicia o jogo (id=1) atrasa-s 2 secs propositadamente
			//e so depois envia a ordem de inicio do jogo (isto eh feito para dar
			//tempo aos outros participantes de fazerem o seu load do jogo e estarem
			//prontos a comunicar)
			if(playerId == '1'){
				Timer waitBeforeStart = new Timer();
				waitBeforeStart.schedule(new TimerTask() {			
					@Override
					public void run() {
						service.startTime();
					}
					
				}, 100);
			}
		}
	}
	
	public void movePlayerUp(View v){
		if(singleplayer){
			this.goUp('1');
		}else{
			service.goUp();
		}
	}
	
	public void movePlayerLeft(View v){
		if(singleplayer){
			this.goLeft('1');
		} else{
			service.goLeft();
		}
	}
	
	public void movePlayerDown(View v){
		if(singleplayer){
			this.goDown('1');
		} else{
			service.goDown();
		}
	}
	
	public void movePlayerRight(View v){
		if(singleplayer){
			this.goRight('1');
		} else{
			service.goRight();
		}
	}
	
	public void dropBomb(View v){
		if(singleplayer){
			this.plantBomb('1');
		} else{
			service.plantBomb();
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
	public void goUp(char id){
		Arena arena = gamePanel.getArena();
		Bomberman bman = arena.getPlayer(id);
		bman.oneSquareUp();
	}
	
	public void goDown(char id){
		Arena arena = gamePanel.getArena();
		Bomberman bman = arena.getPlayer(id);
		bman.oneSquareDown();
	}
	
	public void goLeft(char id){
		Arena arena = gamePanel.getArena();
		Bomberman bman = arena.getPlayer(id);
		bman.oneSquareLeft();
	}
	
	public void goRight(char id){
		Arena arena = gamePanel.getArena();
		Bomberman bman = arena.getPlayer(id);
		bman.oneSquareRight();
	}

	public void plantBomb(char id){
		Arena arena = gamePanel.getArena();
		Bomberman bman = arena.getPlayer(id);
		bman.plantBomb();
	}
	
	public void robotGoUp(int id){
		Arena arena = gamePanel.getArena();
		Robot bman = arena.getRobot(id);
		bman.oneSquareUp();
	}
	
	public void robotGoDown(int id){
		Arena arena = gamePanel.getArena();
		Robot bman = arena.getRobot(id);
		bman.oneSquareDown();
	}
	
	public void robotGoLeft(int id){
		Arena arena = gamePanel.getArena();
		Robot bman = arena.getRobot(id);
		bman.oneSquareLeft();
	}
	
	public void robotGoRight(int id){
		Arena arena = gamePanel.getArena();
		Robot bman = arena.getRobot(id);
		bman.oneSquareRight();
	}
	
	public void startTimeOrder(){
		runOnUiThread(new Runnable() {
	        public void run() {
	    		enableControlButtons();
	        	toggleStateButton.setEnabled(true);
	        }
	    });
		startTimer();
	}
	
}
