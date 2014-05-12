package com.example.bomberman;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;

import com.example.bomberman.util.MyAdapter;
import com.example.bomberman.util.ScoreBoard;

public class ScoresActivity extends Activity {

	private ScoreBoard scores;
	private ArrayList<String> items;
	private MyAdapter adapter;
	private boolean WDSimEnabled;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scores);
		
		scores = (ScoreBoard) getIntent().getSerializableExtra("scores");
		WDSimEnabled = getIntent().getBooleanExtra("WDState", false);

		items = new ArrayList<String>();
		items.add("Name          Score");
	    Iterator<Entry<String, Integer>> it = scores.getSortedMap().entrySet().iterator();
	    while (it.hasNext()) {
	    	Entry<String, Integer> pairs = (Entry<String, Integer>) it.next();
	        items.add("Player " + pairs.getKey() + "          " + pairs.getValue());
	        it.remove(); 
	    }
		
	    ListView scoreList = (ListView)findViewById(R.id.scores_list);
		adapter = new MyAdapter(this, items);
	    scoreList.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	public void leave(View v) {		
		Intent intent = new Intent(ScoresActivity.this, MenuActivity.class);
		intent.putExtra("WDState", WDSimEnabled);
		startActivity(intent);
	}
	
}
