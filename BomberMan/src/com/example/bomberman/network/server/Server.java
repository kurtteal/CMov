package com.example.bomberman.network.server;

import java.io.IOException;
import java.io.PrintWriter;

import pt.utl.ist.cmov.wifidirect.sockets.SimWifiP2pSocketServer;
import android.util.Log;
import android.util.SparseArray;

public class Server implements Runnable {
	
	private static SimWifiP2pSocketServer serverSocket;
    private static int serverPort = 10001;
    
	public static char mapSelected;
    public static int clt_id = 1;
    public static boolean gameStarting = false;
    public static boolean gameOngoing = false;
	public static SparseArray<PrintWriter> clients = new SparseArray<PrintWriter>();
	public static SparseArray<String> clientsNames = new SparseArray<String>();
	
	// Para fechar o socket
	protected void finalize() {
		// Objects created in run method are finalized when
		// program terminates and thread exits
		try {
			serverSocket.close();
		} catch (IOException e) {
			System.out.println("Could not close socket");
			System.exit(-1);
		}
	}
	
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

	@Override
	public void run() {
	    try {
	        serverSocket = new SimWifiP2pSocketServer(serverPort);
	        Log.d("BOMBERMAN SERVER", "created server socket");
	    } catch (IOException e) {
	        Log.d("BOMBERMAN SERVER", "Could not listen on port: " + serverPort);
	    }	
	
		while (true) {
			try {
				ClientHandler w = new ClientHandler(serverSocket.accept(), this, clt_id++);
				Thread t = new Thread(w);
				t.start();
			} catch (IOException e) {
				System.out.println("Accept failed: " + serverPort);
				System.exit(-1);
			}
		}
	}

}
