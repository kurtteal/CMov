package com.example.bomberman;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MenuActivity extends Activity {

	ArrayList<String> localUsers;
	TextView activeUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		localUsers = new ArrayList<String>();
		localUsers.add("Kurt");
		localUsers.add("b0x1");
		activeUser = ((TextView) findViewById(R.id.activeTV));
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
		// Bundle extras = intent.getExtras();

		switch (resultCode) {
		case RESULT_OK:
			String active = intent.getStringExtra("activeUser");

			if (!localUsers.contains(active))
				localUsers.add(0, active);
			activeUser.setText(active);
			break;
		}
	}

	public void singlePlayer(View v) {
		Intent intent = new Intent(MenuActivity.this, SelectMapActivity.class);
		// Pass the player name to the next activities.
    	intent.putExtra("playerName", activeUser.getText());
		startActivity(intent);
	}

	public void multiPlayer(View v) {
		Intent intent = new Intent(MenuActivity.this, MultiplayerMenuActivity.class);
		// Pass the player name to the next activities.
    	intent.putExtra("playerName", activeUser.getText());
		startActivity(intent);
	}

	public void openSettings(View v) {
		Intent intent = new Intent(MenuActivity.this, SelectUserActivity.class);
		intent.putExtra("users", localUsers);
		startActivityForResult(intent, 1);
	}

	
}
