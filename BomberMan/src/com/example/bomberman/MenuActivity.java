package com.example.bomberman;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MenuActivity extends Activity {

	private ArrayList<String> localUsers;
	private TextView activeUser;
	private boolean WDSimEnabled;

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
			WDSimEnabled = intent.getBooleanExtra("WDState", false);
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
			WDSimEnabled = intent.getBooleanExtra("WDState", false);
		}
	}

	/*
	 * Button methods.
	 */
	
	public void singlePlayer(View v) {
		Intent intent = new Intent(MenuActivity.this, SelectMapActivity.class);
    	intent.putExtra("playerName", activeUser.getText());
		startActivity(intent);
	}

	public void multiPlayer(View v) {
		Intent intent = new Intent(MenuActivity.this, MultiplayerMenuActivity.class);
    	intent.putExtra("playerName", activeUser.getText());
    	intent.putExtra("WDState", WDSimEnabled);
		startActivity(intent);
	}

	public void openSettings(View v) {
		Intent intent = new Intent(MenuActivity.this, SettingsActivity.class);
		intent.putExtra("users", localUsers);
		intent.putExtra("WDState", WDSimEnabled);
		startActivityForResult(intent, 1);
	}
	
}
