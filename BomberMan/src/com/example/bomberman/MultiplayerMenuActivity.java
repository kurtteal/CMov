package com.example.bomberman;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.example.bomberman.csclient.ClientService;
import com.example.bomberman.util.GameConfigs;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MultiplayerMenuActivity extends Activity implements IMenuActivity, OnItemSelectedListener {

	Map<Integer, String> users;
	ArrayList<String> items;
	MyAdapter adapter;
	String localUser;
	
	private GameConfigs gc;
	private char playerId;
	
    private boolean connected;
    private ClientService service;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multiplayer_menu);
		localUser = getIntent().getStringExtra("playerName");
		
		//create the list of players, and add the local player
		users = new TreeMap<Integer, String>(); //treemap orders by key, by default
		users.put(1, localUser);
		//arraylist that will display the player list
		items = new ArrayList<String>();
		items.add("Number          Name");
	    Iterator it = users.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        items.add("Player " + pairs.getKey() + "          " + pairs.getValue());
	        it.remove(); 
	    }
		
	    //create an array adapter to bind the array to list view
	    adapter = new MyAdapter(this, items);
	    
		ListView scoreList = (ListView)findViewById(R.id.players_list);
	    /*bind the array adapter to the ListView */
	    scoreList.setAdapter(adapter);
	    
	    connected = false;
	    
	    //Spinner
		Spinner spinner = (Spinner) findViewById(R.id.levels_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> spin_adapter = ArrayAdapter.createFromResource(this,
		        R.array.levels_array, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		spin_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(spin_adapter);
		spinner.setOnItemSelectedListener(this);
	    
//	    Button newGameButton = (Button) findViewById(R.id.newgame_button);
//		newGameButton.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				newGame();
//			}
//		});
//	    Button joinGameButton = (Button) findViewById(R.id.joingame_button);
//		joinGameButton.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				joinGame();
//			}
//		});
	}
	

	
    public void onItemSelected(AdapterView<?> parent, View view, 
            int pos, long id) {
    	CharSequence mSelected = (CharSequence) parent.getItemAtPosition(pos);
		String selection = mSelected.toString();
		//Log.i("XXXId:", selection);
		//ir buscar o mapa correcto conforme o selecionado pelo user
		int selected = Integer.parseInt(selection.substring(4));
		//Log.i("SELECTED:", selection.substring(4));

		service.setMap(selected);
    }
    
	public void onNothingSelected(AdapterView parent) {
		Log.i("SelMapView", "Nothing is selected");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
	
	//Internal method
	private void updateItemsList(){
		items.clear();
		
		items.add("Number          Name");
	    Iterator it = users.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        items.add("Player " + pairs.getKey() + "          " + pairs.getValue());
	        it.remove(); 
	    }
	    runOnUiThread(new Runnable() {
	        public void run() {
	            // faz refresh ah ListView para mostrar o conteudo actualizado
	    	    if(adapter != null)
	    	    	adapter.notifyDataSetChanged();

	        }
	    });

	}

	//Button method
	public void newGame(View v) {
		if (!connected) {
			service = new ClientService();
			service.setMenuActivity(this);
			Log.d("Multi", "Im trying to connect");
			service.connect();
			service.createGame(localUser);
			connected = true;
		} else
			Toast.makeText(this,
					"Already connected to the server!",
					Toast.LENGTH_LONG).show();
	}

	//Button method
	public void joinGame(View v) {
		if (!connected) {
			service = new ClientService();
			service.setMenuActivity(this);
			service.connect();
			service.joinGame(localUser);
			connected = true;
		} else
			Toast.makeText(this,
					"Already connected to the server!",
					Toast.LENGTH_LONG).show();
	}
	
	//Button method
	public void startGame(View v) {
		service.startGame();
	}
	
	
	//Callback methods for the client service
	//=======================================
	public void createResponse(boolean result){
		if(result){
			playerId = '1';
			Log.d("Creation success", " ");
			runOnUiThread(new Runnable() {
		        public void run() {
					Spinner s = (Spinner) findViewById(R.id.levels_spinner);
					s.setVisibility(View.VISIBLE);
					Button b1 = (Button) findViewById(R.id.newgame_button);
					b1.setVisibility(View.GONE);
					Button b2 = (Button) findViewById(R.id.startgame_button);
					b2.setVisibility(View.VISIBLE);
					Button b3 = (Button) findViewById(R.id.joingame_button);
					b3.setClickable(false); 
		        }
		    });
		}
		else
			Log.d("Creation failed", " ");
	}
	public void joinResponse(boolean result, char playerId){
		if(result){
			this.playerId = playerId;
			Log.d("Join success, playerId: ", playerId+"");
			runOnUiThread(new Runnable() {
		        public void run() {
					TextView tv = (TextView) findViewById(R.id.level_name);
					tv.setVisibility(View.VISIBLE);
					Button b = (Button) findViewById(R.id.newgame_button);
					b.setClickable(false); 
		        }
		    });
		}
		else
			Log.d("Join failed", " ");
	}
	public void updatePlayerList(TreeMap<Integer, String> clientsNames){
		users = clientsNames;
		updateItemsList();
	}
	
	public void updateMap(char mapNumber){
		String mapSelected;
		switch(mapNumber){
		case 1:
			mapSelected = new String("map1");
			break;
		case 2:
			mapSelected = new String("map2");
			break;
		case 3:
			mapSelected = new String("map3");
			break;
		case 4:
			mapSelected = new String("map4");
			break;
		default:
			mapSelected = new String("map1");	
		}
		
		gc = new GameConfigs();
        AssetManager am = getAssets();
        try {
			InputStream is = am.open(mapSelected);
			gc.loadConfigs(is); //loads up the matrix from the map file
		} catch (IOException e) { e.printStackTrace(); }
		runOnUiThread(new Runnable() {
	        public void run() {
	            TextView t = (TextView)findViewById(R.id.level_name);
	            t.setText("Level name: "+gc.levelName);

	        }
	    });
	}

	public void startGameOrder() {
		Intent intent = new Intent(MultiplayerMenuActivity.this, GameActivity.class);
		intent.putExtra("gc", gc); //get number from select_map layout (the one selected)
		intent.putExtra("playerName", localUser);
		intent.putExtra("playerId", playerId + "");
		intent.putExtra("singleplayer", false);
		startActivity(intent);
	}
	
}