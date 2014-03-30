package com.example.bomberman;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MenuActivity extends Activity {

	ArrayList<String> users;
	TextView activeUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		users = new ArrayList<String>();
		users.add("Kurt");
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

			if (!users.contains(active))
				users.add(0, active);
			activeUser.setText(active);
			break;
		}
	}

	public void selectLevel(View v) {
		Intent intent = new Intent(MenuActivity.this, SelectMapActivity.class);
		startActivity(intent);
	}

	public void joinGame(View v) {
		// Intent intent = new Intent(CreateNoteActivity.this,
		// MainActivity.class);
		// startActivity(intent);
	}

	public void openSettings(View v) {
		Intent intent = new Intent(MenuActivity.this, SelectUserActivity.class);
		intent.putExtra("users", users);
		startActivityForResult(intent, 1);
	}

	
}
