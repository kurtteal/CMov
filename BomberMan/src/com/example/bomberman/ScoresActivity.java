package com.example.bomberman;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.example.bomberman.util.ScoreBoard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;

public class ScoresActivity extends Activity {

	ScoreBoard scores;
	ArrayList<String> items;
	MyAdapter adapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scores);
		scores = (ScoreBoard)(getIntent().getSerializableExtra("scores"));
		

		items = new ArrayList<String>();
		items.add("Name          Score");
	    Iterator it = scores.getSortedMap().entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        items.add("Player " + pairs.getKey() + "          " + pairs.getValue());
	        it.remove(); 
	    }
		
	    //create an array adapter to bind the array to list view
	    adapter = new MyAdapter(this, items);
	    
		ListView scoreList = (ListView)findViewById(R.id.scores_list);
	    /*bind the array adapter to the ListView */
	    scoreList.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	public void leave(View v) {		
		Intent intent = new Intent(ScoresActivity.this, MenuActivity.class);
		startActivity(intent);
	}
}
