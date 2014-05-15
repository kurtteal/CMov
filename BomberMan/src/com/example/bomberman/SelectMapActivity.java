package com.example.bomberman;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.bomberman.util.GameConfigs;

public class SelectMapActivity extends Activity implements OnItemSelectedListener {
	
	private GameConfigs gc;
	private String playerName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_map);
		
		Spinner spinner = (Spinner) findViewById(R.id.levels_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> spin_adapter = ArrayAdapter.createFromResource(this,
		        R.array.levels_array, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		spin_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(spin_adapter);
		spinner.setOnItemSelectedListener(this);
		
	    playerName = getIntent().getStringExtra("playerName");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.select_map, menu);
		return true;
	}
	
    public void onItemSelected(AdapterView<?> parent, View view, 
            int pos, long id) {
    	CharSequence mSelected = (CharSequence) parent.getItemAtPosition(pos);
		String selection = mSelected.toString();
		String mapSelected = "map" + selection.substring(6);
//		Log.d("SELECT MAP ACT", "THE selection IS " + selection);
//		Log.d("SELECT MAP ACT", "THE mapSelected IS " + mapSelected);
		gc = new GameConfigs();
        AssetManager am = getAssets();
        try {
			InputStream is = am.open(mapSelected);
			gc.loadConfigs(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
        TextView t;
        t = (TextView)findViewById(R.id.level_name);
        t.setText("Level name: "+ gc.levelName);
        
        t = (TextView)findViewById(R.id.game_duration);
        t.setText("Game duration: "+ gc.gameDuration);
        
        t = (TextView)findViewById(R.id.explosion_timeout);
        t.setText("Explosion timeout: "+ gc.explosionTimeout);
        
        t = (TextView)findViewById(R.id.explosion_duration);
        t.setText("Explosion duration: "+ gc.explosionDuration);
        
        t = (TextView)findViewById(R.id.explosion_range);
        t.setText("Explosion range: "+ gc.explosionRange);
        
        t = (TextView)findViewById(R.id.robot_speed);
        t.setText("Robot speed: "+ gc.robotSpeed);
        
        t = (TextView)findViewById(R.id.pts_per_robot);
        t.setText("Points per robot: "+ gc.ptsPerRobot);
        
        t = (TextView)findViewById(R.id.pts_per_opponent);
        t.setText("Points per opponent: "+ gc.ptsPerPlayer);
    }

	public void onNothingSelected(AdapterView<?> parent) {
		Log.i("SelMapView", "Nothing is selected");
	}

	public void startGame(View v) {		
		Intent intent = new Intent(SelectMapActivity.this, GameActivity.class);
		intent.putExtra("gc", gc);
		intent.putExtra("playerName", playerName);
		intent.putExtra("playerId", "1");
		intent.putExtra("singleplayer", true);
		intent.putExtra("numPlayers", 1);
		startActivity(intent);
	}

}
