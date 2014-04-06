package com.example.bomberman;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import com.example.bomberman.util.GameConfigs;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class SelectMapActivity extends Activity implements OnItemSelectedListener {
	
	private GameConfigs matrix;
	
    public void onItemSelected(AdapterView<?> parent, View view, 
            int pos, long id) {
    	CharSequence mSelected = (CharSequence) parent.getItemAtPosition(pos);
		String selection = mSelected.toString();
		//Log.i("XXXId:", selection);
		//ir buscar o mapa correcto conforme o selecionado pelo user
		int selected = Integer.parseInt(selection.substring(4));
		//Log.i("SELECTED:", selection.substring(4));
		String mapSelected;
		switch(selected){
			case 1:
				mapSelected = new String("map1");
				break;
			case 2:
				mapSelected = new String("map2");
				break;
			case 3:
				mapSelected = new String("map3");
				break;
			default:
				mapSelected = new String("map1");
				
		}
		matrix = new GameConfigs();
        AssetManager am = getAssets();
        try {
			InputStream is = am.open(mapSelected);
			matrix.loadConfigs(is); //loads up the matrix from the map file
		} catch (IOException e) { e.printStackTrace(); }
        TextView t;
        t = (TextView)findViewById(R.id.level_name);
        t.setText("Level name: "+matrix.levelName);
        
        t = (TextView)findViewById(R.id.game_duration);
        t.setText("Game duration: "+matrix.gameDuration);
        
        t = (TextView)findViewById(R.id.explosion_timeout);
        t.setText("Explosion timeout: "+matrix.explosionTimeout);
        
        t = (TextView)findViewById(R.id.explosion_duration);
        t.setText("Explosion duration: "+matrix.explosionDuration);
        
        t = (TextView)findViewById(R.id.explosion_range);
        t.setText("Explosion range: "+matrix.explosionRange);
        
        t = (TextView)findViewById(R.id.robot_speed);
        t.setText("Robot speed: "+matrix.robotSpeed);
        
        t = (TextView)findViewById(R.id.pts_per_robot);
        t.setText("Points per robot: "+matrix.ptsPerRobot);
        
        t = (TextView)findViewById(R.id.pts_per_opponent);
        t.setText("Ponts per opponent: "+matrix.ptsPerPlayer);
    }

	public void onNothingSelected(AdapterView parent) {
		Log.i("SelMapView", "Nothing is selected");
	}
	
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
	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.select_map, menu);
		return true;
	}

	public void startGame(View v) {		
		Intent intent = new Intent(SelectMapActivity.this, GameActivity.class);
		intent.putExtra("matrix", matrix); //get number from select_map layout (the one selected)
		startActivity(intent);
	}

}
