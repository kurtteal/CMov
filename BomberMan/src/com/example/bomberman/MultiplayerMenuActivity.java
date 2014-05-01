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
	
	private static GameConfigs gc;
	private char playerId;
	private String mapSelected;
	
    private boolean connected;
    private ClientService service;
    private static int numUsers;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multiplayer_menu);
		localUser = getIntent().getStringExtra("playerName");
		
		//create the list of players, and add the local player
		users = new TreeMap<Integer, String>(); //treemap orders by key, by default
		users.put(1, localUser);
		numUsers = 1;
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
	

	//Choosing the map will call this method
    public void onItemSelected(AdapterView<?> parent, View view, 
            int pos, long id) {
    	CharSequence mSelected = (CharSequence) parent.getItemAtPosition(pos);
		String selection = mSelected.toString();
		//Log.i("XXXId:", selection);
		//ir buscar o mapa correcto conforme o selecionado pelo user
		int selected = Integer.parseInt(selection.substring(4));
		//Log.i("SELECTED:", selection.substring(4));

        AssetManager am = getAssets();
        int maxPlayers = 0;
        try {
			InputStream is = am.open("map"+selected);
			maxPlayers = new GameConfigs().loadConfigs(is); //loads up the matrix from the map file
			//Log.i("Updating Map", "" + gc.levelName + " " + mapSelected);
		} catch (IOException e) { e.printStackTrace(); }
		
		service.setMap(selected, maxPlayers);
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
	    runOnUiThread(new Runnable() {
	        public void run() {
	    		items.clear();
	    		
	    		items.add("Number          Name");
	    	    Iterator it = users.entrySet().iterator();
	    	    while (it.hasNext()) {
	    	        Map.Entry pairs = (Map.Entry)it.next();
	    	        items.add("Player " + pairs.getKey() + "          " + pairs.getValue());
	    	        it.remove(); 
	    	    }
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
			connected = true;
		}
		service.createGame(localUser);
	}

	//Button method
	public void joinGame(View v) {
		if (!connected) {
			service = new ClientService();
			service.setMenuActivity(this);
			service.connect();
			connected = true;
		} 
		service.joinGame(localUser);
	}
	
	//Button method
	public void startGame(View v) {
		service.preStartGame();
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
					b3.setEnabled(false); 
		        }
		    });
		}
		else{
			Log.d("Creation failed", " ");
			runOnUiThread(new Runnable() {
		        public void run() {
					Toast.makeText(MultiplayerMenuActivity.this,
							"There's a game running already!",
							Toast.LENGTH_LONG).show();
		        }
			});
		}
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
					b.setEnabled(false); 
		        }
		    });
		}
		else{
			//se falhou o campo playerId eh reciclado para indicar a causa da falha
			char cause = playerId; 
			Log.d("Join failed", " ");
			if(cause == '3')
				runOnUiThread(new Runnable() {
			        public void run() {
						Toast.makeText(MultiplayerMenuActivity.this,
								"There's no game to join, create one yourself!",
								Toast.LENGTH_LONG).show();
			        }
				});
			else
				runOnUiThread(new Runnable() {
			        public void run() {
						Toast.makeText(MultiplayerMenuActivity.this,
								"The game is full, you can't join :(",
								Toast.LENGTH_LONG).show();
			        }
				});
		}
	}
	public void updatePlayerList(TreeMap<Integer, String> clientsNames){
		users = clientsNames;
		numUsers = clientsNames.size();
		updateItemsList();
	}
	
	public void updateMap(char mapNumber){  
		switch(mapNumber){
		case '1':
			mapSelected = new String("map1");
			break;
		case '2':
			mapSelected = new String("map2");
			break;
		case '3':
			mapSelected = new String("map3");
			break;
		case '4':
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
			//Log.i("Updating Map", "" + gc.levelName + " " + mapSelected);
		} catch (IOException e) { e.printStackTrace(); }
		runOnUiThread(new Runnable(){
			public void run() {
				TextView t = (TextView) findViewById(R.id.level_name);
				t.setText("Level name: " + gc.levelName);
			}
		});
	}

	//Goes to gameActivity
	public void preStartGameOrder(char mode) {
		boolean gameOngoing = false;
		if(mode == '#'){ //se estou a entrar a meio
			gameOngoing = true;
			//o meu num define o num d jogadores, pq acabei de entrar
			numUsers = Character.getNumericValue(playerId); 
		}
		Intent intent = new Intent(MultiplayerMenuActivity.this, GameActivity.class);
		intent.putExtra("gc", gc); //get number from select_map layout (the one selected)
		intent.putExtra("playerName", localUser);
		intent.putExtra("playerId", playerId + "");
		intent.putExtra("singleplayer", false);
		intent.putExtra("numPlayers", numUsers);
		intent.putExtra("gameOngoing", gameOngoing);
		intent.putExtra("maxPlayers", gc.getMaxPlayers());
		startActivity(intent);
	}
	
}