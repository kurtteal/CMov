package pt.utl.ist.cmov.csserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientHandler implements Runnable {
	private Socket clientSocket;
	private String arena;
	private Server server;
	private int clientId;

	// Constructor
	ClientHandler(Socket client, String arena, Server server, int clientId) {
		this.clientSocket = client;
		this.arena = arena;
		this.server = server;
		this.clientId = clientId;
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
					clientSocket.getInputStream()));
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			server.clients.put(clientId,out);
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
					if(!server.gameOngoing){ //se ainda nao existir um jogo a correr
						String playerName = command.split(" ")[1];
						playerName = playerName.substring(0, playerName.length()-1);//retirar o #
						server.clientsNames.put(clientId, playerName);
						server.reply(clientId, "0"); //Create successful
						server.gameOngoing = true;
					}else
						server.reply(clientId, "1"); //create failed
			//JOIN (no jogo a decorrer)
				}else if(command.startsWith("join")){ 
					//verificar se ha jogo a decorrer
					if(server.gameOngoing){
						//Eh preciso enviar aos jogadores que se juntam, a informacao
						//sobre quem eles sao no mapa (activePlayer -> clientId)
						//Join successful '2 <id>'
						server.reply(clientId, "2" + clientId); 
						String playerName = command.split(" ")[1];
						playerName = playerName.substring(0, playerName.length()-1);//retirar o #
						server.clientsNames.put(clientId, playerName);
						//envia a todos a nova lista
						server.broadcast("L" + server.clientsNames.toString()); 
					}else
						server.reply(clientId, "3"); //join failed
			//SET MAP <map number>
				}else if(command.startsWith("set_map")){
					mapNumber = command.charAt(8);
					server.broadcast("M" + mapNumber);
				}else if(command.startsWith("start")){
					server.broadcast("X");
				}else if(command.startsWith("leave_game")){
					server.clients.remove(clientId);
					server.clientsNames.remove(clientId);
					if(server.clients.isEmpty()){
						server.clt_id = 1;
						server.gameOngoing = false;
					}
					System.out.println("Received: " + command);
					break;
				}else{ //o resto sao updates de jogadores vindos do jogo
				
					// RELAY: Send broadcast to clients
					if(command != null && !command.equals("")){
						server.broadcast(command); //O proprio cliente ja envia com C...
					}
					System.out.println("Notifying clients!");
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
			clientSocket.close();
		} catch (IOException e) { e.printStackTrace(); }
	}
}

