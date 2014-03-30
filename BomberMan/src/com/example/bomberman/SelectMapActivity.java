package com.example.bomberman;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SelectMapActivity extends Activity {

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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.select_map, menu);
		return true;
	}

	public void startGame(View v) {
		Intent intent = new Intent(SelectMapActivity.this, GameActivity.class);
		startActivity(intent);
	}

}
