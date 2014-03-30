package com.example.bomberman;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SelectUserActivity extends Activity {

	ListView listView;
	ArrayList<String> users;
	MyAdapter adapter;
	EditText input;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_user);
		
		// Obtem o intent da view que a chamou
		Intent intent = getIntent();
		
	    users = (ArrayList<String>)intent.getSerializableExtra("users");
	    listView = (ListView)findViewById(R.id.userList);
	    input = (EditText)findViewById(R.id.newUserInput);
		
	    //create an array adapter to bind the array to list view
	    adapter = new MyAdapter(this, users);
	 
	    /*bind the array adapter to the ListView */
	    listView.setAdapter(adapter);
	    
	    //When I click on an element, it goes to the ReadNoteActivity
	    listView.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View view,
	            int position, long id) {

	            // selected user
	        	String selectedUser = ((TextView) view.findViewById(R.id.texto)).getText().toString();
	        	Intent intent = new Intent(SelectUserActivity.this, MenuActivity.class);
	            intent.putExtra("activeUser", selectedUser);
	            setResult(RESULT_OK, intent);
	            finish();
	        }
	      });
	          
        // faz refresh ah ListView para mostrar o conteudo actualizado
		adapter.notifyDataSetChanged();
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
    	if(!selectedUser.equals("")){
	    	users.add(0, selectedUser);
	    	adapter.notifyDataSetChanged();
    	}
    }

}
