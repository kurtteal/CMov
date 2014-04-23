package pt.utl.ist.cmov.csserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Server {
    private ServerSocket serverSocket;   
    int serverPort = 4442;
    
	String arena = "lol";
    public int clt_id = 1;
    public boolean gameOngoing = false;
    final int max_players = 3;
	public HashMap<Integer, PrintWriter> clients = new HashMap<Integer, PrintWriter>();
	public HashMap<Integer, String> clientsNames = new HashMap<Integer, String>();
	
	//Para fechar o socket
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
	
	//BROADCAST: Notifies all clients of the update
	public void broadcast(String line){
		for(PrintWriter out : clients.values()){
			System.out.println("Sending the update: '" + line + "'");
			out.println(line);
		}
	}
	
	//Ansers just this client in particular
	public void reply(int clientId, String answer){
		clients.get(clientId).println(answer);
	}

	public void run() throws UnknownHostException{
	    try {
	        serverSocket = new ServerSocket(serverPort);
	    } catch (IOException e) {
	        System.out.println("Could not listen on port: " + serverPort);
	    }
	
	    System.out.println("Server started. Listening to the port " + serverPort);
	    InetAddress addr = InetAddress.getLocalHost();
	    System.out.println("IP of server: " + addr.getCanonicalHostName());
	    	
	
		while (true) {
			ClientHandler w;
			try {
				// server.accept returns a client connection
				if(clt_id <= max_players){ // max 3 jogadores
					w = new ClientHandler(serverSocket.accept(), arena, this, clt_id++);
					System.out.println("New worker thread launched!");
					Thread t = new Thread(w);
					t.start();
				}
			} catch (IOException e) {
				System.out.println("Accept failed: " + serverPort);
				System.exit(-1);
			}
		}
	}
	
    public static void main(String[] args) throws UnknownHostException {
    	new Server().run();
    }
}
