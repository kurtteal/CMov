package com.example.bomberman.network.server;

import java.io.IOException;
import java.io.PrintWriter;

import pt.utl.ist.cmov.wifidirect.sockets.SimWifiP2pSocketServer;
import android.util.Log;
import android.util.SparseArray;

public class Server extends Thread {
	
	private static SimWifiP2pSocketServer serverSocket;
    private static int serverPort = 10001;
    public static boolean ready = false;
	public static char mapSelected;
    public static int clt_id = 1;
    public static boolean gameStarting = false;
    public static boolean gameOngoing = false;
	public static SparseArray<PrintWriter> clients = new SparseArray<PrintWriter>();
	public static SparseArray<String> clientsNames = new SparseArray<String>();
	
	// BROADCAST: Notifies all clients of the update
	public void broadcast(String line){
		for(int i = 0 ; i < clients.size() ; i++) {
			clients.valueAt(i).println(line);
		}
	}
	
	// Answers just this client in particular
	public void reply(int clientId, String answer) {
		clients.get(clientId).println(answer);
	}
	
	public void reply(int clientId, PrintWriter out, String answer) {
		out.println(answer);
	}
	
	public void resetServer(){
		try {
			Log.d("BOMBERMAN SERVER", "Resetting server state ...");
			if (serverSocket != null)
				serverSocket.close();
			Server.gameOngoing = false;
			Server.gameStarting = false;
			Server.clients = new SparseArray<PrintWriter>();
			Server.clientsNames = new SparseArray<String>();
		} catch (IOException e) {
			Log.e("BOMBERMAN SERVER", "Error while closing socket");
			System.exit(-1);
		}
	}

	@Override
	public void run() {
	    try {
	        serverSocket = new SimWifiP2pSocketServer(serverPort);
	        Log.d("BOMBERMAN SERVER", "Listening on port " + serverPort);
	    } catch (IOException e) {
	        Log.d("BOMBERMAN SERVER", "Could not listen on port: " + serverPort);
	    }
		while (true) {
			try {
				ready = true;
				ClientHandler w = new ClientHandler(serverSocket.accept(), this, clt_id++);
				Log.d("BOMBERMAN_SERVER", "ACCEPTED CLIENT CONNECTION");
				Thread t = new Thread(w);
				t.start();
			} catch (IOException e) {
				System.out.println("Accept failed: " + serverPort);
				System.exit(-1);
			}
		}
	}

}
