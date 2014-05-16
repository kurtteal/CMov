package com.example.bomberman.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.UnknownHostException;

import pt.utl.ist.cmov.wifidirect.sockets.SimWifiP2pSocket;
import android.os.AsyncTask;
import android.util.Log;

public class ClientAsyncTask extends AsyncTask<String, Void, PrintWriter> {

	private static PrintWriter out;
	private static SimWifiP2pSocket clientWDSocket;
	private static BufferedReader in;
	private int serverPort = 10001;
	private String mode;
	private static NetworkService service;
	private static Thread thread;

	public ClientAsyncTask(String mode) {
		super();
		this.mode = mode;
	}

	public ClientAsyncTask(String mode, NetworkService service) {
		super();
		this.mode = mode;
		ClientAsyncTask.service = service;
	}

	public void closeSocket() {
		try {
			thread.interrupt();
			out.close();
			in.close();
			clientWDSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected PrintWriter doInBackground(String... strings) {
		if (strings.length <= 0) {
			return null;
		}
		if(mode.equals("connect")) {
			Log.d("ClientAsyncTask", "Connecting to " + strings[0]);
			try {
				clientWDSocket = new SimWifiP2pSocket(strings[0], serverPort);
				out = new PrintWriter(clientWDSocket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(
						clientWDSocket.getInputStream()));
				thread = new ClientListener(in, service);
				thread.start();
				Log.d("ClientAsyncTask", "Connected to " + strings[0]);
			} catch (UnknownHostException e) {
				Log.d("CONNECT TO SERVER","Error connecting to server - UnknownHostException");
				e.printStackTrace();
			} catch (IOException e) {
				Log.d("CONNECT TO SERVER","Error connecting to server - IOException");
				e.printStackTrace();
			}
			return out;
		}
		else if(mode.equals("send")) {
			if(out != null){
				out.println(strings[0]);
				return out;
			} else
				return null;
		}
		else {
			return null;
		}
	}

	public class ClientListener extends Thread {
		BufferedReader in;
		NetworkService service;

		public ClientListener(BufferedReader bfr, NetworkService service) {
			this.in = bfr;
			this.service = service;
		}

		@Override
		public void run() {
			while (true) {
				try {
					if(isInterrupted())
						break;
					String message = in.readLine();
					if(message == null){
						closeSocket();
						break;
					}
					service.processMessage(message);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
