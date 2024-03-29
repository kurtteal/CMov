package com.example.bomberman.network.broadcastreceivers;

import pt.utl.ist.cmov.wifidirect.SimWifiP2pBroadcast;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pDeviceList;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pInfo;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pManager.GroupInfoListener;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class WDSimBroadcastReceiver extends BroadcastReceiver {
	
	private Activity activity;

    public WDSimBroadcastReceiver(Activity activity) {
        super();
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            int state = intent.getIntExtra(SimWifiP2pBroadcast.EXTRA_WIFI_STATE, -1);
            if (state == SimWifiP2pBroadcast.WIFI_P2P_STATE_ENABLED) {
        		
            } else {
        		
            }
        } else if (SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

        } else if (SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION.equals(action)) {
        	SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent
					.getSerializableExtra(SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
			SimWifiP2pDeviceList devices = (SimWifiP2pDeviceList) intent
					.getSerializableExtra(SimWifiP2pBroadcast.EXTRA_DEVICE_LIST);
			String goName = intent.getStringExtra("goName");
			((GroupInfoListener) activity).onGroupInfoAvailable(devices, ginfo, goName);
			
        } else if (SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION.equals(action)) {
        	SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent
					.getSerializableExtra(SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
			SimWifiP2pDeviceList devices = (SimWifiP2pDeviceList) intent
					.getSerializableExtra(SimWifiP2pBroadcast.EXTRA_DEVICE_LIST);
			String goName = intent.getStringExtra("goName");
			((GroupInfoListener) activity).onGroupInfoAvailable(devices, ginfo, goName);
			
        }
    }
    
}
