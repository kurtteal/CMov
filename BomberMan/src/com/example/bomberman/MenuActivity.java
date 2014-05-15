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
