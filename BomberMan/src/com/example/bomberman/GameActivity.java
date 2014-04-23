package com.example.bomberman;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.bomberman.csclient.ClientService;
import com.example.bomberman.model.Arena;
import com.example.bomberman.model.Bomberman;
import com.example.bomberman.util.GameConfigs;
import com.example.bomberman.util.ScoreBoard;

public class GameActivity extends Activity implements IGameActivity {
	/** Called when the activity is first created. */

	private static final String TAG = GameActivity.class.getSimpleName();
	protected GameConfigs gc;
	
	private MainGamePanel gamePanel;
	private TextView timeLeftView;
	private TextView scoreView;
	
	private String playerName;
	public String playerId; //visivel para a arena
	
	private Timer timeUpdater;
	private Handler mHandler;
	private int countDown;
	private int score;
	
	protected ClientService service;
	private boolean singleplayer = true;
	
	@Override
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
	    playerId = getIntent().getStringExtra("playerId");
	    singleplayer = getIntent().getExtras().getBoolean("singleplayer");
	    service = new ClientService();
	    service.setGameActivity(this);
	    
		setContentView(R.layout.activity_game);
		
		TextView playerNameView = (TextView)findViewById(R.id.activity_game_player_name);
		scoreView = (TextView)findViewById(R.id.activity_game_score);
		timeLeftView = (TextView)findViewById(R.id.activity_game_time_left);
		TextView playerCountView = (TextView)findViewById(R.id.activity_game_player_count);
		
		playerNameView.setText("Player:\n" + playerName);
		scoreView.setText("Score:\n0");
		timeLeftView.setText("Time left:\n" + gc.gameDuration);
		playerCountView.setText("# Players:\n" + "(todo)");
		
		// Timer setup, it will only start when the game starts.
		// The handler gets the message from the timer thread to update the UI.
		timeUpdater = new Timer();
		countDown = gc.gameDuration;
		mHandler = new Handler() {
		    public void handleMessage(Message msg) {
		    	if(countDown == 0)
		    		endGame();
		    	else{
		    		timeLeftView.setText("Time left:\n" + countDown);
//		    		if(firstTick){
//		    			gamePanel.getArena().setActivePlayer(playerId.charAt(0));
//		    			firstTick = false;
//		    		}
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

	
	public void movePlayerUp(View v){
		if(singleplayer){
			Arena arena = gamePanel.getArena();
			Bomberman bman = arena.getActivePlayer();
			bman.oneSquareUp();
		}else{
			service.goUp();
		}
	}

	public void movePlayerLeft(View v){
		if(singleplayer){
			Arena arena = gamePanel.getArena();
			Bomberman bman = arena.getActivePlayer();
			bman.oneSquareLeft();
		}else{
			service.goLeft();
		}
	}
	
	public void movePlayerDown(View v){
		if(singleplayer){
			Arena arena = gamePanel.getArena();
			Bomberman bman = arena.getActivePlayer();
			bman.oneSquareDown();
		}else{
			service.goDown();
		}
	}
	
	public void movePlayerRight(View v){
		if(singleplayer){
			Arena arena = gamePanel.getArena();
			Bomberman bman = arena.getActivePlayer();
			bman.oneSquareRight();
		}else{
			service.goRight();
		}
	}
	
	public void dropBomb(View v){
		if(singleplayer){
			Arena arena = gamePanel.getArena();
			Bomberman bman = arena.getActivePlayer();
			bman.plantBomb();
		}else{
			service.plantBomb();
		}
	}
	
	protected void startTimer() {
		// Start the time left timer.
		timeUpdater.scheduleAtFixedRate(new TimerTask() {
			  @Override
			  public void run() {
				  countDown--;
				  mHandler.obtainMessage().sendToTarget();
			  }
		}, 1000, 1000);
	}
	
	public void updateTimeLeft() {
		
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
	public void plantBomb(char id){
		Arena arena = gamePanel.getArena();
		Bomberman bman = arena.getPlayer(id);
		bman.plantBomb();
	}
	
	
}
