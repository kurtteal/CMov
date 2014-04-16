package com.example.bomberman;

import java.io.InputStream;
import java.util.Map;

import com.example.bomberman.model.Arena;
import com.example.bomberman.model.Bomberman;
import com.example.bomberman.model.PathState;
import com.example.bomberman.util.GameConfigs;
import com.example.bomberman.util.ScoreBoard;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class GameActivity extends Activity {
	/** Called when the activity is first created. */

	private static final String TAG = GameActivity.class.getSimpleName();
	protected GameConfigs gc;
	private MainGamePanel gamePanel;

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

		setContentView(R.layout.activity_game);
		Log.d(TAG, "View added");
	}


	public void setGamePanel(MainGamePanel gPanel){
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
		Arena arena = gamePanel.getArena();
		Bomberman bman = arena.getActivePlayer();
		bman.oneSquareUp();
	}

	public void movePlayerLeft(View v){
		Arena arena = gamePanel.getArena();
		Bomberman bman = arena.getActivePlayer();
		bman.oneSquareLeft();
	}
	
	public void movePlayerDown(View v){
		Arena arena = gamePanel.getArena();
		Bomberman bman = arena.getActivePlayer();
		bman.oneSquareDown();
	}
	
	public void movePlayerRight(View v){
		Arena arena = gamePanel.getArena();
		Bomberman bman = arena.getActivePlayer();
		bman.oneSquareRight();
	}
	
	public void dropBomb(View v){
		Arena arena = gamePanel.getArena();
		Bomberman bman = arena.getActivePlayer();
		bman.plantBomb();
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
	
}
