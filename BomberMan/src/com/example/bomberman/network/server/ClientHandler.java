package com.example.bomberman.network.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import android.util.Log;
import pt.utl.ist.cmov.wifidirect.sockets.SimWifiP2pSocket;

public class ClientHandler implements Runnable {

	private SimWifiP2pSocket clientWDSocket = null;
	private Server server = null;
	private int clientId;

	private static char maxPlayers = '1';

	public ClientHandler(SimWifiP2pSocket client, Server server, int clientId) {
		this.clientWDSocket = client;
		this.server = server;
		this.clientId = clientId; //ids sao atribuidos pela ordem d contacto com o server
	}

	// PROTOCOLO no servidor, respostas possiveis:
	// 0 - Create successful
	// 1 - Create failed
	// 2 - Join successful
	// 3 - Join failed
	// L - Update to the players list
	// M - new map selected
	// C - general commands from other players
	public void run() {
		String command;
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			in = new BufferedReader(new InputStreamReader(
					clientWDSocket.getInputStream()));
			out = new PrintWriter(clientWDSocket.getOutputStream(), true);
		} catch (IOException e) {
			System.out.println("in or out failed");
			System.exit(-1);
		}

		while (true) {
			char mapNumber; 
			try {
				command = in.readLine();
				//System.out.println("Depois do read");
				if(command == null){
					System.out.println("Client '" + clientId + "' died, ending handler thread for that client!");
					break;
				}
				//CREATE <nickname>
				if(command.startsWith("create")){ 
					//se ainda nao existir um jogo a comecar ou a correr
					if(!Server.gameStarting && !Server.gameOngoing){
						Server.clients.put(clientId,out); //entra na lista de broadcast
						String playerName = command.split(" ")[1];
						playerName = playerName.substring(0, playerName.length()-1);//retirar o #
						Server.clientsNames.put(clientId, playerName);
						server.reply(clientId, "0"); //Create successful
						Server.gameStarting = true;
					}else
						server.reply(clientId, out, "1"); //create failed
					//JOIN (no jogo a decorrer)
				}else if(command.startsWith("join")){ 
					System.out.println("Someone joining: " + clientId + " " + Character.getNumericValue(maxPlayers));
					//verificar se ha jogo a decorrer
					if(Server.gameStarting && clientId <= Character.getNumericValue(maxPlayers)){
						String playerName = command.split(" ")[1];
						playerName = playerName.substring(0, playerName.length()-1);//retirar o #
						boolean foundSameUser = false;
						for(int i = 0 ; i < Server.clientsNames.size() ; i++) {
							if(Server.clientsNames.valueAt(i).equals(playerName))
								foundSameUser = true;
						}
						if(foundSameUser)
							server.reply(clientId, out, "5"); // Check if username exists 
						else{
							Server.clients.put(clientId,out); //entra na lista de broadcast
							//Eh preciso enviar aos jogadores que se juntam, a informacao
							//sobre quem eles sao no mapa (activePlayer -> clientId)
							//Join successful '2 <id>'
							server.reply(clientId, "2" + clientId); 
							server.reply(clientId, "M" + Server.mapSelected);
							Server.clientsNames.put(clientId, playerName);
							//envia a todos a nova lista
							server.broadcast("L" + Server.clientsNames.toString());
						}
					}else if(Server.gameOngoing){
						if(clientId <= Character.getNumericValue(maxPlayers)){
							server.reply(clientId, out, "2" + clientId); 
							server.reply(clientId, out, "M" + Server.mapSelected);
							server.reply(clientId, out, "X#"); //fazer load do mapa
						}else
							server.reply(clientId, out, "4"); //game full, join failed
						//servidor nao sabe se o player cabe no mapa, esse check eh feito client-side
					}else
						server.reply(clientId, out, "3"); //no game, join failed
				}else if(command.startsWith("pause_game")){ 
					Log.d("CLT HANDLER", "Someone pausing: " + clientId);
					server.broadcast("P" + clientId);
					//WHEN MID JOINER is ready to receive commands from the game
				}else if(command.startsWith("leave_game")){ 
					Log.d("CLT HANDLER", "Someone leaving: " + clientId);
					Server.clients.remove(clientId);
					Server.clientsNames.remove(clientId);
					server.broadcast("U" + clientId);
					//WHEN MID JOINER is ready to receive commands from the game
				}else if(command.startsWith("resume_game")){ 
					Log.d("CLT HANDLER", "Someone resuming: " + clientId);
					server.broadcast("Q" + clientId);
					//WHEN MID JOINER is ready to receive commands from the game
				}else if(command.startsWith("mid_join_ready")){
					server.broadcast("N" + clientId);
					String playerName = command.split(" ")[1];
					Log.d("TESTT", "NO SERVER; PLAYER NAME E " +playerName);
					Server.clientsNames.put(clientId, playerName);
					server.reply(clientId, out, "K" + Server.clientsNames.toString()); //fazer load do mapa
					server.broadcast("K" + Server.clientsNames.toString());
					Server.clients.put(clientId,out); //entra na lista de broadcast
				}else if(command.startsWith("info")){
					String[] commandSplitted = command.split("#");
					int clock = Integer.parseInt(commandSplitted[1]);
					String scoreBrd = commandSplitted[2];
					String currentMatrix = commandSplitted[3];
					String deadsList = commandSplitted[4];
					String playersPositions = commandSplitted[5];
					//String userNamesMap = commandSplitted[6];
					int playerId = Integer.parseInt(commandSplitted[6]);
					server.reply(playerId, "Y" + "#" +  clock + "#" + scoreBrd + "#" + currentMatrix + "#" + deadsList + "#" + playersPositions);
					//SET MAP <map number>
				}else if(command.startsWith("set_map")){
					mapNumber = command.charAt(8);
					maxPlayers = command.charAt(12);
					System.out.println("New map " +mapNumber + " max players: "+ maxPlayers);
					Server.mapSelected = mapNumber;
					server.broadcast("M" + mapNumber);
				}else if(command.startsWith("start")){
					server.broadcast("X" + clientId);
					Server.gameStarting = false;
					Server.gameOngoing = true;
				}else if(command.startsWith("leave_game")){
					Server.clients.remove(clientId);
					Server.clientsNames.remove(clientId);
					if(Server.clients.size() == 0){
						Server.clt_id = 1;
						Server.gameOngoing = false;
					}
					break;
				}else{ //o resto sao updates de jogadores vindos do jogo
					// RELAY: Send broadcast to clients
					if(command != null && !command.equals(""))
						server.broadcast(command); //O proprio cliente ja envia com C...
				}
				System.out.println("Received: " + command);
			} catch (IOException e) {
				System.out.println("Read failed");
				e.printStackTrace();
				System.exit(-1);
			}
		}
		//Close socket to the client
		try {
			out.close();
			in.close();
			clientWDSocket.close();
		} catch (IOException e) { e.printStackTrace(); }
	}

}
