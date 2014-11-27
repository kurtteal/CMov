package com.example.bomberman;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.example.bomberman.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bomberman.network.BluetoothModule;
import com.example.bomberman.network.NetworkService;
import com.example.bomberman.util.GameConfigs;
import com.example.bomberman.util.MyAdapter;

public class MultiplayerMenuActivity extends Activity implements
	OnItemSelectedListener{


	private static GameConfigs gc;
	private static int numUsers;
	private static BluetoothModule bt;
	
	private TreeMap<Integer, String> users;
	private ArrayList<String> items;
	private MyAdapter adapter;
	private String localUser;
	private char playerId;
	private String mapSelected;
	private NetworkService service;

	private ArrayList<String> detectedDevices;
	
	private Button newGameBtn;
	private Button joinGameBtn;
	private Button startGameBtn;
	private Button readyBtn;
	private ListView deviceList;
	private TextView mapName;
	private Spinner mapSpinner;
	
	//Handler for the bluetooth messages that concern the interface
    public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			//Log.d("HANDLER", String.format("Handler.handleMessage(): msg=%s", msg.obj));
            // This is where main activity thread receives messages
			if(service != null)
				service.preProcessMessage(msg);
			super.handleMessage(msg);
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multiplayer_menu);
		
		newGameBtn = ((Button) findViewById(R.id.newgame_button));
		joinGameBtn = ((Button) findViewById(R.id.joingame_button));
		startGameBtn = ((Button) findViewById(R.id.start_button));
		readyBtn = ((Button) findViewById(R.id.ready_button));
		mapName = ((TextView) findViewById(R.id.level_name));
		
		// create the list of players, and add the local player
		localUser = getIntent().getStringExtra("playerName");
		users = new TreeMap<Integer, String>();
		users.put(1, localUser);
		numUsers = 1;

		//Stuff for the user interface list with the detected bt devices
		detectedDevices = new ArrayList<String>();
		deviceList = (ListView) findViewById(R.id.btList);
		adapter = new MyAdapter(this, detectedDevices);
		deviceList.setAdapter(adapter);
		deviceList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String selectedDevice = ((TextView) view.findViewById(R.id.text))
						.getText().toString();
				bt.connectToServer(selectedDevice); //its a name (maybe change to addr?)XXX
				deviceList.setVisibility(View.GONE);
			}
		});
		adapter.notifyDataSetChanged();

		// Spinner
		mapSpinner = (Spinner) findViewById(R.id.levels_spinner);
		
		ArrayAdapter<CharSequence> spin_adapter = ArrayAdapter
				.createFromResource(this, R.array.levels_array,
						android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		spin_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		mapSpinner.setAdapter(spin_adapter);
		mapSpinner.setOnItemSelectedListener(this);

		//Create the Bluetooth module
		bt = new BluetoothModule(this, mHandler);
		if(!bt.supportsBluetooth()){
			Toast.makeText(MultiplayerMenuActivity.this,
					"Bluetooth not available", Toast.LENGTH_LONG).show();
		}else{
			bt.enableBluetooth(); //enable in case its disabled
			String pairedList = bt.getPairedDevices();
			if(pairedList.isEmpty())
				pairedList = "No devices are currently paired!\n";
			Toast.makeText(MultiplayerMenuActivity.this,
					pairedList, Toast.LENGTH_SHORT).show();
		}
		service = new NetworkService();
		service.setBluetoothModule(bt);
		service.setMenuActivity(this);
		
	}

	// Map Spinner listener
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		CharSequence mSelected = (CharSequence) parent.getItemAtPosition(pos);
		String selection = mSelected.toString();
		int selected = Integer.parseInt(selection.substring(6));
		AssetManager am = getAssets();
		int maxPlayers = 0;
		try {
			InputStream is = am.open("map" + selected);
			maxPlayers = new GameConfigs().loadConfigs(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//service.setMap(selected, maxPlayers);
		service.send("map#"+selected);
		updateMap((""+selected).charAt(0));
	}

	public void onNothingSelected(AdapterView<?> parent) {
		Log.i("SelMapView", "Nothing is selected");
	}

//	private void updateItemsList() {
//		runOnUiThread(new Runnable() {
//			public void run() {
//				items.clear();
//				items.add("Number          Username");
//				Iterator<Entry<Integer, String>> it = users.entrySet()
//						.iterator();
//				while (it.hasNext()) {
//					Entry<Integer, String> pairs = (Entry<Integer, String>) it
//							.next();
//					items.add("Player " + pairs.getKey() + "          "
//							+ pairs.getValue());
//				}
//				if (adapter != null)
//					adapter.notifyDataSetChanged();
//			}
//		});
//	}

	/*
	 * Activity state changes.
	 */

	@Override
	public void onPause() {
		bt.unregisterBroadcastReceiver(); //for discovery purposes
	    super.onPause();
	}

	@Override
	public void onResume() {
		bt.registerBroadcastReceiver(); //for discovery purposes
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

	
	// Button listeners

	public void newGame(View v) {
		playerId = '1';
		bt.triggerDiscoverable();
		newGameBtn.setVisibility(View.GONE);
		joinGameBtn.setVisibility(View.GONE);
		mapSpinner.setVisibility(View.VISIBLE);
		startGameBtn.setVisibility(View.VISIBLE);
		bt.startServer();
	}
	
	public void joinGame(View v) {
		playerId = '2';
		bt.discover();
		newGameBtn.setVisibility(View.GONE);
		joinGameBtn.setVisibility(View.GONE);
		deviceList.setVisibility(View.VISIBLE);
		mapName.setVisibility(View.VISIBLE);
		readyBtn.setVisibility(View.VISIBLE);
	}
	
	public void startGame(View v){
		service.send("start");
		startGameOrder('a');
	}
	
	public void sendReady(View v){
		service.send("ready");
	}

//	/*
//	 * Callbacks for the network service.
//	 */
//
//	public void createResponse(boolean result) {
//		connected = true;
//		if (result) {
//			playerId = '1';
//			runOnUiThread(new Runnable() {
//				public void run() {
//					Spinner s = (Spinner) findViewById(R.id.levels_spinner);
//					s.setVisibility(View.VISIBLE);
//					Button b1 = (Button) findViewById(R.id.newgame_button);
//					b1.setVisibility(View.GONE);
//					Button b2 = (Button) findViewById(R.id.startgame_button);
//					b2.setVisibility(View.VISIBLE);
//					Button b3 = (Button) findViewById(R.id.joingame_button);
//					b3.setEnabled(false);
//				}
//			});
//		} else {
//			runOnUiThread(new Runnable() {
//				public void run() {
//					Toast.makeText(MultiplayerMenuActivity.this,
//							"There's a game running already!",
//							Toast.LENGTH_LONG).show();
//				}
//			});
//		}
//	}
//
//	public void joinResponse(boolean result, char playerId) {
//		if (result) {
//			connected = true;
//			this.playerId = playerId;
//			runOnUiThread(new Runnable() {
//				public void run() {
//					TextView tv = (TextView) findViewById(R.id.level_name);
//					tv.setVisibility(View.VISIBLE);
//					Button b = (Button) findViewById(R.id.newgame_button);
//					b.setEnabled(false);
//				}
//			});
//		} else {
//			// se falhou o campo playerId eh reciclado para indicar a causa da
//			// falha
//			char cause = playerId;
//			Log.d("Join failed", " ");
//			if (cause == '3') {
//				connected = true;
//				runOnUiThread(new Runnable() {
//					public void run() {
//						Toast.makeText(
//								MultiplayerMenuActivity.this,
//								"There's no game to join, create one yourself!",
//								Toast.LENGTH_LONG).show();
//					}
//				});
//			} else if (cause == '4') {
//				runOnUiThread(new Runnable() {
//					public void run() {
//						Toast.makeText(MultiplayerMenuActivity.this,
//								"The game is full, you can't join :(",
//								Toast.LENGTH_LONG).show();
//					}
//				});
//			} else if (cause == '5') {
//				runOnUiThread(new Runnable() {
//					public void run() {
//						Toast.makeText(
//								MultiplayerMenuActivity.this,
//								"Username already taken! Please select another.",
//								Toast.LENGTH_LONG).show();
//					}
//				});
//			} else {
//				runOnUiThread(new Runnable() {
//					public void run() {
//						Toast.makeText(MultiplayerMenuActivity.this,
//								"Can't join game. Try again later!",
//								Toast.LENGTH_LONG).show();
//					}
//				});
//			}
//		}
//	}

	public void updatePlayerList(TreeMap<Integer, String> clientsNames) {
		users = clientsNames;
		numUsers = clientsNames.size(); //DEPRECATED (I THINK)
		//updateItemsList();
	}

	public void startGameOrder(char mode) {
		boolean gameOngoing = false;
//		if (mode == '#') { // se estou a entrar a meio
//			gameOngoing = true;
//			// o meu num define o num d jogadores, pq acabei de entrar
//			numUsers = Character.getNumericValue(playerId);
//		}
		bt.unregisterBroadcastReceiver(); //for discovery purposes
		
		Intent intent = new Intent(MultiplayerMenuActivity.this,
				GameActivity.class);
		intent.putExtra("gc", gc);
		intent.putExtra("playerName", localUser);
		intent.putExtra("playerId", playerId + "");
		intent.putExtra("singleplayer", false);
		intent.putExtra("numPlayers", 2);
		intent.putExtra("gameOngoing", gameOngoing);
		intent.putExtra("maxPlayers", gc.getMaxPlayers());
		intent.putExtra("usersMap", users);
		//intent.putExtra("btModule", bt); //needs to be serializable
		//TODO nao da para serializar o bt, tenho de adaptar o network service para levar um bt
		//quer sera estatico e depois do outro lado basta instanciar um novo networkservice...
		startActivity(intent);
	}


	public void stopConnection(View v){
		//Depois tambem falta o broadcast receiver q recebe eventos do tipo alteraçao no bluetooth
		//para fazer a mesma coisa que aqui (porque um deles termina por botao, o outro termina pq
		//a ligacao fechou)... Nao eh preciso porque ele vai detectar que a ligaçao terminou e vai apanhar
		//uma excepcao q o vai fazer terminar a thread
		service.stopBluetoothConnection();
	}
	
	public void updateDetectedDevices(String nameAddr){
		detectedDevices.add(nameAddr.split("&")[0]);
		adapter.notifyDataSetChanged();
	}
	
	public void updateMap(char mapNumber) {
		mapSelected = "map" + mapNumber;
		gc = new GameConfigs();
		AssetManager am = getAssets();
		try {
			InputStream is = am.open(mapSelected);
			gc.loadConfigs(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		runOnUiThread(new Runnable() {
			public void run() {
				TextView t = (TextView) findViewById(R.id.level_name);
				t.setText("Level name: " + gc.levelName);
				Toast.makeText(MultiplayerMenuActivity.this,
						"Updating map", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	public void allowStart(){
		runOnUiThread(new Runnable() {
			public void run() {
				startGameBtn.setVisibility(View.VISIBLE);
				Toast.makeText(MultiplayerMenuActivity.this,
						"Showing start button", Toast.LENGTH_SHORT).show();
			}
		});
	}

}
