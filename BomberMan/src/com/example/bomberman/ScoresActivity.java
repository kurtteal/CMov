package com.example.bomberman;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;

import com.example.bomberman.util.MyAdapter;
import com.example.bomberman.util.ScoreBoard;

public class ScoresActivity extends Activity {

	private ScoreBoard scores;
	private TreeMap<Integer, String> users = new TreeMap<Integer, String>();
	private ArrayList<String> items;
	private ArrayList<String> items_values;
	private String activePlayer;
	private MyAdapter adapter;
	private MyAdapter adapter_values;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scores);

		scores = (ScoreBoard) getIntent().getSerializableExtra("scores");
		activePlayer = getIntent().getStringExtra("playerName");

		items = new ArrayList<String>();
		items_values = new ArrayList<String>();
		items.add("Username");
		items_values.add("Score");
		Iterator<Entry<String, Integer>> it = scores.getSortedMap().entrySet()
				.iterator();
		HashMap<Integer, String> receivedMap = (HashMap<Integer, String>) getIntent()
				.getExtras().getSerializable("usersMap");
		users.putAll(receivedMap);

		while (it.hasNext()) {
			Entry<String, Integer> pairs = (Entry<String, Integer>) it.next();
			items.add(users.get(Integer.parseInt(pairs.getKey())));
			items_values.add(Integer.toString(pairs.getValue()));
			it.remove();
		}

		ListView scoreList = (ListView) findViewById(R.id.scores_list);
		ListView scoreListValues = (ListView) findViewById(R.id.scores_list_values);
		adapter = new MyAdapter(this, items);
		adapter_values = new MyAdapter(this, items_values);
		scoreList.setAdapter(adapter);
		scoreListValues.setAdapter(adapter_values);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
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

	public void leave(View v) {
		Intent intent = new Intent(ScoresActivity.this, MenuActivity.class);
		Log.d("ENDGAME USER", "Username is: " + activePlayer);
		intent.putExtra("activePlayer", activePlayer);
		startActivity(intent);
	}

}
