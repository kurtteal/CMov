package com.example.bomberman;

import java.util.ArrayList;

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

import com.example.bomberman.util.MyAdapter;

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
		adapter = new MyAdapter(this, users);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String selectedUser = ((TextView) view.findViewById(R.id.text))
						.getText().toString();
				Intent intent = new Intent(SettingsActivity.this,
						MenuActivity.class);
				intent.putExtra("activeUser", selectedUser);
				setResult(RESULT_OK, intent);
				finish();
			}
		});

		adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.select_user, menu);
		return true;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	public void newUser(View v) {
		String selectedUser = input.getText().toString();
		if (selectedUser.trim().length() > 0) {
			input.setText("");
			users.add(0, selectedUser);
			adapter.notifyDataSetChanged();
		} else {
			Toast.makeText(this, "Invalid Username! Please try again.",
					Toast.LENGTH_SHORT).show();
		}
	}

}
