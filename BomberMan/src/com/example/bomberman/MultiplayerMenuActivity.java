package com.example.bomberman;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import pt.utl.ist.cmov.wifidirect.SimWifiP2pBroadcast;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pDevice;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pDeviceList;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pInfo;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pManager.GroupInfoListener;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pManager.PeerListListener;
import pt.utl.ist.cmov.wifidirect.service.SimWifiP2pService;
import pt.utl.ist.cmov.wifidirect.sockets.SimWifiP2pSocketManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bomberman.network.NetworkService;
import com.example.bomberman.network.WDSimServiceConnection;
import com.example.bomberman.network.broadcastreceivers.MenuBroadcastReceiver;
import com.example.bomberman.util.GameConfigs;
import com.example.bomberman.util.MyAdapter;

public class MultiplayerMenuActivity extends Activity implements
OnItemSelectedListener, PeerListListener, GroupInfoListener {

	private TreeMap<Integer, String> users;
	private ArrayList<String> items;
	private MyAdapter adapter;
	private String localUser;
	private static GameConfigs gc;
	private char playerId;
	private String mapSelected;
	private boolean connected;
	private NetworkService service;
	private static int numUsers;

	private boolean inGroup = false;
	private boolean isGroupOwner = false;
	private String serverAddress = null;
	private WDSimServiceConnection servConn = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multiplayer_menu);

		// create the list of players, and add the local player
		localUser = getIntent().getStringExtra("playerName");
		users = new TreeMap<Integer, String>(); // treemap orders by key, by
		// default
		users.put(1, localUser);
		numUsers = 1;

		// arraylist that will display the player list
		items = new ArrayList<String>();
		items.add("Number          Username");
		Iterator<Entry<Integer, String>> it = users.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, String> pairs = (Entry<Integer, String>) it.next();
			items.add("Player " + pairs.getKey() + "          "
					+ pairs.getValue());
			// it.remove();
		}

		// create an array adapter to bind the array to list view
		ListView scoreList = (ListView) findViewById(R.id.players_list);
		adapter = new MyAdapter(this, items);
		scoreList.setAdapter(adapter);

		// Spinner
		Spinner spinner = (Spinner) findViewById(R.id.levels_spinner);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> spin_adapter = ArrayAdapter
				.createFromResource(this, R.array.levels_array,
						android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		spin_adapter
		.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(spin_adapter);
		spinner.setOnItemSelectedListener(this);

		/*
		 * Network related code.
		 */

		service = new NetworkService();
		service.setMenuActivity(this);
		connected = false;

		// initialize the WDSim API
		SimWifiP2pSocketManager.Init(getApplicationContext());

		// register broadcast receiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
		MenuBroadcastReceiver receiver = new MenuBroadcastReceiver(this);
		registerReceiver(receiver, filter);

		servConn = new WDSimServiceConnection(this);
		Intent intent = new Intent(this, SimWifiP2pService.class);
		bindService(intent, (ServiceConnection) servConn,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	/*
	 * Listeners associated to the Spinner.
	 */

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

		service.setMap(selected, maxPlayers);
	}

	public void onNothingSelected(AdapterView<?> parent) {
		Log.i("SelMapView", "Nothing is selected");
	}

	private void updateItemsList() {
		runOnUiThread(new Runnable() {
			public void run() {
				items.clear();
				items.add("Number          Username");
				Iterator<Entry<Integer, String>> it = users.entrySet()
						.iterator();
				while (it.hasNext()) {
					Entry<Integer, String> pairs = (Entry<Integer, String>) it
							.next();
					items.add("Player " + pairs.getKey() + "          "
							+ pairs.getValue());
					// it.remove();
				}
				if (adapter != null)
					adapter.notifyDataSetChanged();
			}
		});
	}

	
	@Override
	public void onPause() {
	    super.onPause();  // Always call the superclass method first

	    // Release the Camera because we don't need it when paused
	    // and other activities might need to use it.
	    Toast.makeText(this, "ON PAUSE - MULTIPLAYER ACT",
				Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onResume() {
	    super.onResume();  // Always call the superclass method first

	    // Get the Camera instance as the activity achieves full user focus
	    Toast.makeText(this, "ON RESUME - MULTIPLAYER ACT",
				Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onStop() {
	    super.onStop();  // Always call the superclass method first

	    // Save the note's current draft, because the activity is stopping
	    // and we want to be sure the current note progress isn't lost.
	    Toast.makeText(this, "ON STOP - MULTIPLAYER ACT",
				Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onStart() {
	    super.onStart();  // Always call the superclass method first
	    
	    // The activity is either being restarted or started for the first time
	    // so this is where we should make sure that GPS is enabled
	    Toast.makeText(this, "ON START - MULTIPLAYER ACT",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onRestart() {
	    super.onRestart();  // Always call the superclass method first
	    
	    Toast.makeText(this, "ON RESTART - MULTIPLAYER ACT",
				Toast.LENGTH_SHORT).show(); 
	}
	
	/*
	 * Listeners associate to the Buttons.
	 */

	public void newGame(View v) {
		requestGroupInfo();
		if (inGroup && isGroupOwner) {
			Log.d("BOMBERMAN", "Enabling bomberman server.");
			service.enableServer();
		} else {
			Toast.makeText(this, "Not in a group or not the GO.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (!connected) {
			if (serverAddress == null) {
				Toast.makeText(this, "Can't find the server.",
						Toast.LENGTH_SHORT).show();
				return;
			}
			service.connect(serverAddress);
		}
		service.createGame(localUser);
	}

	public void joinGame(View v) {
		if (!inGroup) {
			Toast.makeText(this, "Join a Wi-Fi Direct group first.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (!connected) {
			if (serverAddress == null) {
				Toast.makeText(this, "Can't find the server.",
						Toast.LENGTH_SHORT).show();
				return;
			}
			service.disableServer();
			service.connect(serverAddress);
		}
		service.joinGame(localUser);
	}

	public void startGame(View v) {
		service.preStartGame();
	}

	/*
	 * Callbacks for the network service.
	 */

	public void createResponse(boolean result) {
		connected = true;
		if (result) {
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
		} else {
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

	public void joinResponse(boolean result, char playerId) {
		if (result) {
			connected = true;
			this.playerId = playerId;
			Log.d("Join success, playerId: ", playerId + "");
			runOnUiThread(new Runnable() {
				public void run() {
					TextView tv = (TextView) findViewById(R.id.level_name);
					tv.setVisibility(View.VISIBLE);
					Button b = (Button) findViewById(R.id.newgame_button);
					b.setEnabled(false);
					Button b2 = (Button) findViewById(R.id.joingame_button);
					b2.setEnabled(false);
				}
			});
		} else {
			// se falhou o campo playerId eh reciclado para indicar a causa da
			// falha
			char cause = playerId;
			Log.d("Join failed", " ");
			if (cause == '3') {
				connected = true;
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(
								MultiplayerMenuActivity.this,
								"There's no game to join, create one yourself!",
								Toast.LENGTH_LONG).show();
					}
				});
			} else if (cause == '4') {
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(MultiplayerMenuActivity.this,
								"The game is full, you can't join :(",
								Toast.LENGTH_LONG).show();
					}
				});
			} else if (cause == '5') {
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(
								MultiplayerMenuActivity.this,
								"Username already taken! Please select another.",
								Toast.LENGTH_LONG).show();
					}
				});
			} else {
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(MultiplayerMenuActivity.this,
								"Can't join game. Try again later!",
								Toast.LENGTH_LONG).show();
					}
				});
			}
		}
	}

	public void updatePlayerList(TreeMap<Integer, String> clientsNames) {
		users = clientsNames;
		numUsers = clientsNames.size();
		updateItemsList();
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
			}
		});
	}

	public void startGameOrder(char mode) {
		boolean gameOngoing = false;
		if (mode == '#') { // se estou a entrar a meio
			gameOngoing = true;
			// o meu num define o num d jogadores, pq acabei de entrar
			numUsers = Character.getNumericValue(playerId);
		}
		// unbindService(servConn);
		Intent intent = new Intent(MultiplayerMenuActivity.this,
				GameActivity.class);
		intent.putExtra("gc", gc);
		intent.putExtra("playerName", localUser);
		intent.putExtra("playerId", playerId + "");
		intent.putExtra("singleplayer", false);
		intent.putExtra("numPlayers", numUsers);
		intent.putExtra("gameOngoing", gameOngoing);
		intent.putExtra("maxPlayers", gc.getMaxPlayers());
		intent.putExtra("usersMap", users);
		startActivity(intent);
	}

	/*
	 * WDSim Listeners callbacks.
	 */

	@Override
	public void onGroupInfoAvailable(SimWifiP2pDeviceList devices,
			SimWifiP2pInfo groupInfo, String goName) {
		inGroup = groupInfo.askIsConnected();
		isGroupOwner = groupInfo.askIsGO();
		if (inGroup) {
			for (SimWifiP2pDevice d : devices.getDeviceList()) {
				String[] split = d.virtDeviceAddress.split(":");
				if (d.deviceName.equals(goName))
					serverAddress = split[0];
				Log.d("IPs", split[0]);
			}
			Log.d("MultiplayerMenu", "Server address: " + serverAddress + " - "
					+ goName);
		}
	}

	public void requestGroupInfo() {
		servConn.getManager().requestGroupInfo(servConn.getChannel(),
				(GroupInfoListener) this);
	}

	@Override
	public void onPeersAvailable(SimWifiP2pDeviceList peers) {
	}

}
