package com.example.bomberman.network;

import java.util.ArrayList;
import java.util.TreeMap;

import android.util.Log;

import com.example.bomberman.GameActivity;
import com.example.bomberman.MultiplayerMenuActivity;
import com.example.bomberman.network.server.Server;

/*
 * Network service class that encapsulates the application's communication and synchronization protocol.
 * Its subclasses will define how the messages are sent through the network.
 */
public class NetworkService {
	
	private static boolean WDSimEnabled;
	private static boolean isServer;
	private static Thread serverThread = null;
	private static Server server = null;
	private static ArrayList<String> addresses = null;
	private static char playerId = '#';
	private static MultiplayerMenuActivity menuActivity;
	private static GameActivity gameActivity;
	private static TreeMap<Integer, String> clientsNames = new TreeMap<Integer, String>();
	private static TreeMap<Integer, String> participantsReady = new TreeMap<Integer, String>();
	
	/*
	 * Getters and Setters
	 */
	
	public void enableWDSim() {
		WDSimEnabled = true; 
	}
	
	public void enableServer() {
		NetworkService.isServer = true;
		server = new Server();
		serverThread = new Thread(server);
		serverThread.start();
		Log.d("NetService", "STARTED SERVER THREAD CARALHO");
	}
	
	public boolean usingWDSim() {
		return WDSimEnabled;
	}
	
	public boolean isServer() {
		if(WDSimEnabled)
			return isServer;
		else
			return false;
	}
	
	public void setMenuActivity(MultiplayerMenuActivity act) {
		menuActivity = act;
	}

	public void setGameActivity(GameActivity act) {
		gameActivity = act;
	}
	
	public void setPlayerId(char id) {
		playerId = id;
	}
	
	public MultiplayerMenuActivity getMenuActivity() {
		return menuActivity;
	}
	
	public GameActivity getGameActivity() {
		return gameActivity;
	}

	public char getPlayerId() {
		return playerId;
	}
	
	public void setAddresses(ArrayList<String> addresses) {
		NetworkService.addresses = addresses;
	}

	
	/*
	 * Communication primitives.
	 */
	
	public void connect() {
		try {
			new ClientAsyncTask("connect", this, addresses).execute("");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void send(String message) {
		try {
			new ClientAsyncTask("send").execute(message);
			Log.d("NetService", "Client sent" + message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void closeConnection() {
		// TODO blabla granchinho's shit ...
	}
	
	/*
	 * Button methods, they build the messages and give the send order.
	 */
	
	public void createGame(String playerName) {
		String message = "create " + playerName + playerId;
		send(message);
	}
	
	public void joinGame(String playerName) {
		String message = "join " + playerName + playerId;
		send(message);
	}
	
	public void preStartGame() {
		String message = "start " + playerId;
		send(message);
	}
	
	public void startGame() {
		String message = "T" + getPlayerId();
		send(message);
	}
	
	public void midJoin() {
		String message = "mid_join_ready " + playerId;
		send(message);
	}
	
	public void leaveGame() {
		String message = "leave_game" + playerId;
		send(message);
	}
	
	public void setMap(int mapNumber, int maxPlayers) {
		String message = "set_map " + mapNumber + " " + playerId + " " + maxPlayers;
		send(message);
	}

	/*
	 * Player commands
	 */
	
	public void goUp(int i, int j) {
		String message = "Cup" + "/" + playerId + "/" + i + "/" + j;
		send(message);
	}

	public void goDown(int i, int j) {
		String message = "Cdown" + "/" + playerId + "/" + i + "/" + j;
		send(message);
	}

	public void goLeft(int i, int j) {
		String message = "Cleft" + "/" + playerId + "/" + i + "/" + j;
		send(message);
	}

	public void goRight(int i, int j) {
		String message = "Cright" + "/" + playerId + "/" + i + "/" + j;
		send(message);
	}

	public void plantBomb(int i, int j) {
		String message = "Cbomb" + "/" + playerId + "/" + i + "/" + j;
		send(message);
	}

	/*
	 * Robot commands
	 */
	
	public void robotUp(int robotId, int i, int j) {
		String message = "Rup" + "/" + robotId + "/" + i + "/" + j;
		send(message);
	}

	public void robotDown(int robotId, int i, int j) {
		String message = "Rdown" + "/" + robotId + "/" + i + "/" + j;
		send(message);
	}

	public void robotLeft(int robotId, int i, int j) {
		String message = "Rleft" + "/" + robotId + "/" + i + "/" + j;
		send(message);
	}

	public void robotRight(int robotId, int i, int j) {
		String message = "Rright" + "/" + robotId + "/" + i + "/" + j;
		send(message);
	}

	/*
	 * Internal methods.
	 */
	
	private void createSuccessful() {
		setPlayerId('1'); // quem criou eh o 1
		menuActivity.createResponse(true);
	}

	private void createNotSuccessful() {
		menuActivity.createResponse(false);
	}

	private void joinSuccessful(char playerId) {
		setPlayerId(playerId);
		menuActivity.joinResponse(true, playerId);
	}

	private void joinNotSuccessful(char cause) {
		menuActivity.joinResponse(false, cause);
	}

	private void updatePlayerList(String[] players) {
		for (String player : players) {
			String[] data = player.split("=");
			clientsNames.put(Integer.parseInt(data[0].trim()), data[1]);
		}
		menuActivity.updatePlayerList(clientsNames);
	}

	private void updateMap(char mapNumber) {
		menuActivity.updateMap(mapNumber);
	}

	private void preStartGameOrder(String message) {
		char mode = message.charAt(1);
		menuActivity.preStartGameOrder(mode);
	}

	private void playerAction(String command) { // ex: up/3/13/42
		String[] params = command.split("/");
		char executerId = params[1].charAt(0);
		String iPos = params[2];
		String jPos = params[3];
		if (command.startsWith("up"))
			gameActivity.goUpOrder(executerId, iPos, jPos);
		else if (command.startsWith("down"))
			gameActivity.goDownOrder(executerId, iPos, jPos);
		else if (command.startsWith("left"))
			gameActivity.goLeftOrder(executerId, iPos, jPos);
		else if (command.startsWith("right"))
			gameActivity.goRightOrder(executerId, iPos, jPos);
		else if (command.startsWith("bomb"))
			gameActivity.plantBombOrder(executerId, iPos, jPos);
	}

	private void robotAction(String command) {
		String[] params = command.split("/");
		int executerId = Integer.parseInt(params[1]);
		String iPos = params[2];
		String jPos = params[3];
		if (command.startsWith("up"))
			gameActivity.robotGoUp(executerId, iPos, jPos);
		else if (command.startsWith("down"))
			gameActivity.robotGoDown(executerId, iPos, jPos);
		else if (command.startsWith("left"))
			gameActivity.robotGoLeft(executerId, iPos, jPos);
		else if (command.startsWith("right"))
			gameActivity.robotGoRight(executerId, iPos, jPos);
	}

	private void checkIfAllReady(char id) {
		Log.d("Participants | playerId", participantsReady.size() + " "
				+ playerId);
		if (playerId == '1' && gameActivity.masterIsReady()) {
			// juntar este id a uma lista
			// a string eh irrelevante, so nos interessa o facto d n haverem
			// repetidos
			participantsReady.put(Character.getNumericValue(id), "");
			// verificar se ja recebi o set de todos os outros participantes
			if (participantsReady.size() == (gameActivity.getNumPlayers() - 1))
				startGame(); // envia o inicio do jogo pa tds
		}
	}
	
	// Quando um novo player entra, eh pq o servidor deixou (ainda havia lugares
	// vagos)
	// Os unicos que recebem esta msg sao todos menos o que entra
	private void newPlayer(String id) {
		char newPlayerId = id.charAt(0);
		gameActivity.newPlayer(newPlayerId);
		if (playerId == '1') {
			// TODO enviar msg com o clock actual para o newPlayerId
			int currentCount = gameActivity.getCountDown();
			sendCurrentClock(newPlayerId, currentCount); // metodo do proprio
															// servico
		}
	}
	
	private void updateClock(String clock) {
		gameActivity.setCountDown(Integer.parseInt(clock));
	}
	
	/*
	 * Other synchronization methods.
	 */
	
	// Chamado pelos participantes que nao sao game master, enquanto o master
	// nao esta pronto
	public void imSet() {
		String message = "S" + playerId;
		send(message);
	}
	
	// O game master depois de receber um join a meio, envia o clock atual
	// para o ultimo que entrou ficar atualizado
	private void sendCurrentClock(char newPlayerId, int clock) {
		String message = "clock " + clock + newPlayerId;
		send(message);
	}
	
	/*
	 * The message processing method. Invoked after a message is received.
	 */
	public void processMessage(String message) {
		char type = message.charAt(0);
		switch (type) {
		case '0':
			createSuccessful();
			break;
		case '1':
			createNotSuccessful();
			break;
		case '2':
			char playerId = message.charAt(1);
			joinSuccessful(playerId);
			break;
		case '3':
			joinNotSuccessful('3');
			break;
		case '4':
			joinNotSuccessful('4');
			break;
		case 'L': // lista de jogadores actualizada
			String[] players = message.substring(2, message.length() - 1)
					.split(",");
			Log.d("Plist updt: ", NetworkService.playerId + " " + players[0]);
			updatePlayerList(players);
			break;
		case 'M': // mapa escolhido
			updateMap(message.charAt(1));
			break;
		case 'X': // preStartGame (gameActivity loading arena)
			preStartGameOrder(message);
			break;
		case 'S': // other participants sending 'im set' msgs to the game master
			checkIfAllReady(message.charAt(1));
			break;
		case 'T': // startGame
			gameActivity.startGameOrder();
			break;
		case 'C': // comando de outro jogador (ou do proprio)
			playerAction(message.substring(1));
			break;
		case 'R': // comando de um robot
			robotAction(message.substring(1));
			break;
		case 'N': // new player N <playerId>
			newPlayer(message.substring(1));
			break;
		case 'Y': // Y <clock> remaining in secs
			updateClock(message.substring(1));
			break;
		default:
			break;
		}
	}
	
}
