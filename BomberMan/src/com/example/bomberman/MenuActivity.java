package com.example.bomberman;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class MenuActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
	
    public void chooseUserMenu(View v) {
        //Intent intent = new Intent(CreateNoteActivity.this, MainActivity.class);
        //startActivity(intent);
    }
    
    public void newGameMenu(View v) {
        //Intent intent = new Intent(CreateNoteActivity.this, MainActivity.class);
        //startActivity(intent);
    }
    
    public void joinGameMenu(View v) {
        //Intent intent = new Intent(CreateNoteActivity.this, MainActivity.class);
        //startActivity(intent);
    }

}
