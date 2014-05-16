package com.example.bomberman;

import java.util.ArrayList;

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
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bomberman.network.WDSimServiceConnection;
import com.example.bomberman.network.broadcastreceivers.WDSimBroadcastReceiver;

public class MenuActivity extends Activity implements PeerListListener, GroupInfoListener {

	private ArrayList<String> localUsers;
	private TextView activeUser;
	private boolean selectedUsername;
	private static boolean WDSimStarted;
	private boolean inGroup = false;
	private boolean isGroupOwner = false;
	private String serverAddress = "";
	private WDSimServiceConnection servConn = null;
	private WDSimBroadcastReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		localUsers = new ArrayList<String>();
		localUsers.add("Kurt");
		localUsers.add("b0x1");
		localUsers.add("Wo0d");
		activeUser = ((TextView) findViewById(R.id.activeTV));
		Intent intent = getIntent();
		if(intent != null) {
			WDSimStarted = intent.getBooleanExtra("WDSimStarted", false);
			inGroup = getIntent().getBooleanExtra("inGroup", false);
			isGroupOwner = getIntent().getBooleanExtra("isGO", false);
			serverAddress = getIntent().getStringExtra("serverAddress");
			String activeU = intent.getStringExtra("activePlayer");
			if(activeU == null){
				activeUser.setText("(None - Please go to Settings)");
				selectedUsername = false;
			} else {
				activeUser.setText(activeU);
				selectedUsername = true;
			}
		}
		
		/*
		 * Network related code
		 */
		if(!WDSimStarted)
			// initialize the WDSim API
			SimWifiP2pSocketManager.Init(getApplicationContext());

		// register broadcast receiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
		receiver = new WDSimBroadcastReceiver(this);
		registerReceiver(receiver, filter);

		servConn = new WDSimServiceConnection(this);
		if(servConn.getManager() == null) {
			Intent serviceIntent = new Intent(this, SimWifiP2pService.class);
			bindService(serviceIntent, (ServiceConnection) servConn,
					Context.BIND_AUTO_CREATE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	// Called when coming here from another activity with startForResults..
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if(resultCode == RESULT_OK) {
			String active = intent.getStringExtra("activeUser");
			if (!localUsers.contains(active))
				localUsers.add(0, active);
			activeUser.setText(active);
			selectedUsername = true;
		}
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
	
	/*
	 * Button methods.
	 */

	public void singlePlayer(View v) {
		if(!selectedUsername){
			Toast.makeText(this, "Please pick a Username first!",
					Toast.LENGTH_SHORT).show();
		} else{
			Intent intent = new Intent(MenuActivity.this, SelectMapActivity.class);
			intent.putExtra("playerName", activeUser.getText());
			unregisterReceiver(receiver);
			startActivity(intent);
		}
	}

	public void multiPlayer(View v) {
		if(!selectedUsername){
			Toast.makeText(this, "Please pick a Username first!",
					Toast.LENGTH_SHORT).show();
		} else{
			Intent intent = new Intent(MenuActivity.this, MultiplayerMenuActivity.class);
			intent.putExtra("playerName", activeUser.getText());
			intent.putExtra("serverAddress", serverAddress);
			intent.putExtra("inGroup", inGroup);
			intent.putExtra("isGO", isGroupOwner);
			unregisterReceiver(receiver);
			startActivity(intent);
		}
	}

	public void openSettings(View v) {
		Intent intent = new Intent(MenuActivity.this, SettingsActivity.class);
		intent.putExtra("users", localUsers);
		startActivityForResult(intent, 1);
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
			}
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
