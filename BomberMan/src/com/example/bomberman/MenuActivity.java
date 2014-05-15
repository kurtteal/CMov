package com.example.bomberman;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MenuActivity extends Activity {

	private ArrayList<String> localUsers;
	private TextView activeUser;
	private boolean selectedUsername;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		localUsers = new ArrayList<String>();
		localUsers.add("Kurt");
		localUsers.add("b0x1");
		localUsers.add("Wo0d");
		activeUser = ((TextView) findViewById(R.id.activeTV));
		Intent intent = getIntent();
		if(intent != null) {
			String activeU = intent.getStringExtra("activePlayer");
			//Log.d("ENDGAME USER", "NO ONCREATE DO MENUACT, O USER E" + activeU);
			if(activeU == null){
				activeUser.setText("(None - Please go to Settings)");
				selectedUsername = false;
			} else {
				activeUser.setText(activeU);
				selectedUsername = true;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	// Called when coming here from another activity with startForResults..
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if(resultCode == RESULT_OK) {
			String active = intent.getStringExtra("activeUser");
			if (!localUsers.contains(active))
				localUsers.add(0, active);
			activeUser.setText(active);
			selectedUsername = true;
		}
	}

	@Override
	public void onPause() {
	    super.onPause();  // Always call the superclass method first

	    // Release the Camera because we don't need it when paused
	    // and other activities might need to use it.
	    Toast.makeText(this, "ON PAUSE - MENU ACT",
				Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onResume() {
	    super.onResume();  // Always call the superclass method first

	    // Get the Camera instance as the activity achieves full user focus
	    Toast.makeText(this, "ON RESUME - MENU ACT",
				Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onStop() {
	    super.onStop();  // Always call the superclass method first

	    // Save the note's current draft, because the activity is stopping
	    // and we want to be sure the current note progress isn't lost.
	    Toast.makeText(this, "ON STOP - MENU ACT",
				Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onStart() {
	    super.onStart();  // Always call the superclass method first
	    
	    // The activity is either being restarted or started for the first time
	    // so this is where we should make sure that GPS is enabled
	    Toast.makeText(this, "ON START - MENU ACT",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onRestart() {
	    super.onRestart();  // Always call the superclass method first
	    
	    Toast.makeText(this, "ON RESTART - MENU ACT",
				Toast.LENGTH_SHORT).show(); 
	}
	
	/*
	 * Button methods.
	 */

	public void singlePlayer(View v) {
		if(!selectedUsername){
			Toast.makeText(this, "Please pick a Username first!",
					Toast.LENGTH_SHORT).show();
		} else{
			Intent intent = new Intent(MenuActivity.this, SelectMapActivity.class);
			intent.putExtra("playerName", activeUser.getText());
			startActivity(intent);
		}
	}

	public void multiPlayer(View v) {
		if(!selectedUsername){
			Toast.makeText(this, "Please pick a Username first!",
					Toast.LENGTH_SHORT).show();
		} else{
			Intent intent = new Intent(MenuActivity.this, MultiplayerMenuActivity.class);
			intent.putExtra("playerName", activeUser.getText());
			startActivity(intent);
		}
	}

	public void openSettings(View v) {
		Intent intent = new Intent(MenuActivity.this, SettingsActivity.class);
		intent.putExtra("users", localUsers);
		startActivityForResult(intent, 1);
	}

}
