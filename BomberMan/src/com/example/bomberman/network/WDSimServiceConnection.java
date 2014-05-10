package com.example.bomberman.network;

import pt.utl.ist.cmov.wifidirect.SimWifiP2pManager;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pManager.Channel;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;

import com.example.bomberman.GameActivity;
import com.example.bomberman.MultiplayerMenuActivity;

public class WDSimServiceConnection implements ServiceConnection {
	
	private Activity activity = null;
	private static SimWifiP2pManager mManager = null;
	private static Channel mChannel = null;
	private static Messenger mService = null;
	
	public WDSimServiceConnection(Activity act) {
		this.activity = act;
	}
	
	public SimWifiP2pManager getManager() {
		return mManager;
	}
	
	public Channel getChannel() {
		return mChannel;
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		mService = new Messenger(service);
		mManager = new SimWifiP2pManager(mService);
		mChannel = mManager.initialize(activity.getApplication(), activity.getMainLooper(), null);
		if(activity instanceof MultiplayerMenuActivity)
			((MultiplayerMenuActivity) activity).requestGroupInfo();
		//else
			//((GameActivity) activity).requestGroupInfo();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mService = null;
		mManager = null;
		mChannel = null;
	}

}
