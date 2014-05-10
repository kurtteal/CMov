package com.example.bomberman.network.broadcastreceivers;

import pt.utl.ist.cmov.wifidirect.SimWifiP2pBroadcast;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.bomberman.MultiplayerMenuActivity;

public class MenuBroadcastReceiver extends BroadcastReceiver {

	private MultiplayerMenuActivity activity;

	public MenuBroadcastReceiver(MultiplayerMenuActivity activity) {
		super();
		this.activity = activity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

			// This action is triggered when the WDSim service changes state:
			// - creating the service generates the WIFI_P2P_STATE_ENABLED event
			// - destroying the service generates the WIFI_P2P_STATE_DISABLED
			// event

			int state = intent.getIntExtra(
					SimWifiP2pBroadcast.EXTRA_WIFI_STATE, -1);
			if (state == SimWifiP2pBroadcast.WIFI_P2P_STATE_ENABLED) {
				// Wifi Direct foi ligado.
			} else {
				// Wifi Direct foi desligado.
			}
		} else if (SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION
				.equals(action)) {

		} else if (SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION
				.equals(action)) {
			activity.requestGroupInfo();

		} else if (SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION
				.equals(action)) {
		}
	}

}
