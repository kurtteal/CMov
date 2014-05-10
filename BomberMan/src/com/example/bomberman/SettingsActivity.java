package com.example.bomberman;

import java.util.ArrayList;

import com.example.bomberman.util.MyAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SettingsActivity extends Activity {

	private ListView listView;
	private ToggleButton wifiButton;
	private ArrayList<String> users;
	private MyAdapter adapter;
	private EditText input;
	private boolean WDSimEnabled;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		WDSimEnabled = getIntent().getBooleanExtra("WDState", false);
		users = (ArrayList<String>) getIntent().getSerializableExtra("users");
	    
	    input = (EditText) findViewById(R.id.newUserInput);
	    listView = (ListView) findViewById(R.id.userList);
	    wifiButton = (ToggleButton) findViewById(R.id.WDEnableButton);
	    
	    // create an array adapter to bind the array to list view
	    adapter = new MyAdapter(this, users);
	    listView.setAdapter(adapter);
	    
	    // when I click on an element, it goes back to the MenuActivity
	    listView.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View view,
	            int position, long id) {
	        	String selectedUser = ((TextView) view.findViewById(R.id.texto)).getText().toString();
	        	Intent intent = new Intent(SettingsActivity.this, MenuActivity.class);
	            intent.putExtra("activeUser", selectedUser);
	            intent.putExtra("WDState", WDSimEnabled);
	            setResult(RESULT_OK, intent);
	            finish();
	        }
	      });
	          
        // faz refresh ah ListView para mostrar o conteudo actualizado
		adapter.notifyDataSetChanged();
		
		wifiButton.setChecked(WDSimEnabled);
		toggleWifiDirect(null);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.select_user, menu);
		return true;
	}
	
    public void newUser(View v) {
    	String selectedUser = input.getText().toString();
    	input.setText("");
    	if(!selectedUser.equals("")) {
	    	users.add(0, selectedUser);
	    	adapter.notifyDataSetChanged();
    	}
    }
    
    public void toggleWifiDirect(View v) {
    	WDSimEnabled = wifiButton.isChecked();
    	if(WDSimEnabled)
    		wifiButton.setText("Wifi Direct ON");
    	else
    		wifiButton.setText("Wifi Direct OFF");
    }

}
