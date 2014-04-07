package com.example.bomberman;

import java.io.InputStream;

import com.example.bomberman.util.GameConfigs;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

public class GameActivity extends Activity {
    /** Called when the activity is first created. */
	
	private static final String TAG = GameActivity.class.getSimpleName();
	protected GameConfigs matrix;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // requesting to turn the title OFF
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        // making it full screen
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // set our MainGamePanel as the View
        //setContentView(new MainGamePanel(this));

        matrix = (GameConfigs)getIntent().getSerializableExtra("matrix");

        setContentView(R.layout.activity_game);
        Log.d(TAG, "View added");
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
    
    
}