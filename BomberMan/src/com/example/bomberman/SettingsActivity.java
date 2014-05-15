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
import android.widget.Toast;

public class SettingsActivity extends Activity {

	private ListView listView;
	private ArrayList<String> users;
	private MyAdapter adapter;
	private EditText input;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		users = (ArrayList<String>) getIntent().getSerializableExtra("users");

		input = (EditText) findViewById(R.id.newUserInput);
		listView = (ListView) findViewById(R.id.userList);

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
		if(selectedUser.trim().length() > 0){
			input.setText("");
			users.add(0, selectedUser);
			adapter.notifyDataSetChanged();
		} else {
			Toast.makeText(this, "Invalid Username! Please try again.",
					Toast.LENGTH_SHORT).show();
		}
			
	}

}
