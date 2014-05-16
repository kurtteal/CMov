package com.example.bomberman.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import android.content.Entity;
import android.os.DropBoxManager.Entry;
import android.text.BoringLayout;
import android.util.Log;

import com.example.bomberman.GameActivity;
import com.example.bomberman.MultiplayerMenuActivity;
import com.example.bomberman.model.Bomberman;
import com.example.bomberman.model.IDrawable;
import com.example.bomberman.model.Path;
import com.example.bomberman.model.PathState;
import com.example.bomberman.model.Robot;
import com.example.bomberman.model.Wall;
import com.example.bomberman.network.server.Server;
import com.example.bomberman.util.ScoreBoard;

/*
 * Network service class that encapsulates the application's communication and synchronization protocol.
 * Its subclasses will define how the messages are sent through the network.
 */
public class NetworkService {

	private static boolean isServer;
	private static Server server = null;
	private static char playerId = '#';
	private static MultiplayerMenuActivity menuActivity;
	private static GameActivity gameActivity;
	private static TreeMap<Integer, String> clientsNames = new TreeMap<Integer, String>();
	private static TreeMap<Integer, String> participantsReady = new TreeMap<Integer, String>();

	/*
	 * Getters and Setters
	 */

	public void enableServer() {
		NetworkService.isServer = true;
		NetworkService.server = new Server();
		NetworkService.server.start();
	}

	public void disableServer() {
		NetworkService.isServer = false;
		NetworkService.server = null;
	}

	public void resetServer(){
		NetworkService.server.resetServer();
	}

	public boolean isServer() {
		return isServer;
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

	/*
	 * Communication primitives.
	 */

	public void connect(String address) {
		try {
			// The server runs on a separate thread so there's
			// no guarantee that it already ran before the main thread reaches the following
			// lines of code to send the creation message.
			if(isServer){
				Log.d("NetService", "Server state is" + Server.ready);
				while(!Server.ready) {
					Thread.yield();
				}
			}
			new ClientAsyncTask("connect", this).execute(address);
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
		// TODO ...
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
		String message = "mid_join_ready " + gameActivity.getActivePlayer();
		send(message);
	}

	public void leaveGame() {
		String message = "leave_game " + playerId;
		send(message);
	}

	public void setMap(int mapNumber, int maxPlayers) {
		String message = "set_map " + mapNumber + " " + playerId + " " + maxPlayers;
		send(message);
	}

	public void pauseGame() {
		String message = "pause_game " + playerId;
		send(message);
	}

	public void resumeGame() {
		String message = "resume_game " + playerId;
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
	
	private void updateGameUsers(String[] players) {
		//clientsNames = new TreeMap<Integer, String>();
		for (String player : players) {
			String[] data = player.split("=");
			clientsNames.put(Integer.parseInt(data[0].trim()), data[1]);
		}
		gameActivity.updatePlayerList(clientsNames);
	}

	private void updateMap(char mapNumber) {
		menuActivity.updateMap(mapNumber);
	}

	private void startGameOrder(String message) {
		Log.d("START GAME ORDER", "A MESSAGE E" + message);
		char mode = message.charAt(1);
		menuActivity.startGameOrder(mode);
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
			ScoreBoard scoreBrd = gameActivity.getGamePanel().getArena().scores;
			char current_matrix[][] = gameActivity.getGamePanel().getArena().getGC().matrix;
			String deadElementsList = gameActivity.getGamePanel().getArena().getDeadElementsIds();
			String playersPosition = gameActivity.getGamePanel().getArena().getPlayersPositions(id);
			//String userNamesList = gameActivity.getUsersMap();
			//Log.d("TESTT", "IN NEWPLAYER SERVICE, O USERNAMES LIST E" + userNamesList.toString());
			
			sendCurrentInfo(newPlayerId, currentCount, scoreBrd, current_matrix, deadElementsList, playersPosition); // metodo do proprio
			// servico
		}
	}

	private void pausePlayer(String id) {
		char pausePlayerId = id.charAt(0);
		gameActivity.pausePlayer(pausePlayerId);
	}

	private void resumePlayer(String id) {
		char resumePlayerId = id.charAt(0);
		gameActivity.resumePlayer(resumePlayerId);
	}

	private void quitPlayer(String id) {
		char quitPlayerId = id.charAt(0);
		gameActivity.quitPlayer(quitPlayerId);
	}

	private void updateInfo(int clock, String scoreBrd, String currentMatrix, String deadElementsList, String playersPosition) {

		//HashMap<String, Integer> newBoard = new HashMap<String, Integer>();
		ScoreBoard newBoard = new ScoreBoard();
		int numLines = gameActivity.getGamePanel().getArena().getGC().getNumLines();
		int numColumns = gameActivity.getGamePanel().getArena().getGC().getNumColumns();
		char[][] newMatrix = new char[numLines][numColumns];
		
		ArrayList<String> deadRobotsIds = new ArrayList<String>();
		ArrayList<String> deadPlayersIds = new ArrayList<String>();
		
		String[] boardSplitted = scoreBrd.split("&");

		String[] matrixSplitted = currentMatrix.split("&");

		String[] deadElementsSplitted = deadElementsList.split("&");

		//		StringBuilder sb = new StringBuilder();
		//		for(String s : boardSplitted){
		//			sb.append(s);
		//			sb.append("XXX");
		//		}
		//		Log.d("UPDATE INFO", "BOARD SPLITTED E "+ sb.toString());

		for(String keyValue: boardSplitted){
			String[] keyValueSplitted = keyValue.split(",");
			newBoard.add(keyValueSplitted[0], Integer.parseInt(keyValueSplitted[1]));
		}

		for(String matrixVals: matrixSplitted){
			String[] matrixValsSplitted = matrixVals.split(",");
			newMatrix[Integer.parseInt(matrixValsSplitted[0])][Integer.parseInt(matrixValsSplitted[1])] = matrixValsSplitted[2].charAt(0); 
		}
		
		if(!(deadElementsSplitted[0].equals("no deads"))){
			for(String deadVal: deadElementsSplitted){
				String[] deadValsSplitted = deadVal.split(",");
				if(deadValsSplitted[0] == "P"){
					Log.d("DEADVALS", "PLAYER:" + deadValsSplitted[1]);
					deadPlayersIds.add(deadValsSplitted[1]);
				} else{
					deadRobotsIds.add(deadValsSplitted[1]);
					Log.d("DEADVALS", "ROBOT:" + deadValsSplitted[1]);
				}
			}
		}

		gameActivity.setCountDown(clock);
		gameActivity.getGamePanel().getArena().setScoreBoard(newBoard);
		gameActivity.getGamePanel().getArena().setListsAndMatrix(newMatrix, deadRobotsIds, deadPlayersIds, playersPosition);
		gameActivity.getGamePanel().getArena().setUpdatedGameInfo();
		
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
	private void sendCurrentInfo(char newPlayerId, int clock, ScoreBoard scoreBrd, char current_matrix[][], String newDeadList, String playersPosition) {

		String newScoreBoard = "";
		String newCurrentMatrix = "";

		for (java.util.Map.Entry<String, Integer> entry : scoreBrd.entrySet()){
			newScoreBoard += entry.getKey() + "," + entry.getValue();
			newScoreBoard += "&";
		}

		for(int i = 0; i < current_matrix.length; i++){
			for(int j = 0; j < current_matrix[i].length; j++){
				newCurrentMatrix += i + "," + j + "," + current_matrix[i][j];
				newCurrentMatrix += "&";
			}
		}

		String message = "info " + "#" +  clock + "#" + newScoreBoard + "#" + newCurrentMatrix + "#" + newDeadList + "#" + playersPosition + "#" + newPlayerId;
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
		case '5':
			joinNotSuccessful('5');
			break;
		case 'L': // lista de jogadores actualizada
			String[] players = message.substring(2, message.length() - 1)
			.split(",");
			Log.d("Plist updt", NetworkService.playerId + " " + players[0] + "|" + players[1]);
			updatePlayerList(players);
			break;
		case 'K':
			String[] playrs = message.substring(2, message.length() - 1)
			.split(",");
			updateGameUsers(playrs);
			break;
		case 'M': // mapa escolhido
			updateMap(message.charAt(1));
			break;
		case 'X': // preStartGame (gameActivity loading arena)
			startGameOrder(message);
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
		case 'P': // Pause do jogador com o id <playerId>
			pausePlayer(message.substring(1));
			break;
		case 'Q': // Resume do jogador com o id <playerId> (Q pois R já estava usado...)
			resumePlayer(message.substring(1));
			break;
		case 'U': // Resume do jogador com o id <playerId> (Q pois R já estava usado...)
			quitPlayer(message.substring(1));
			break;
		case 'Y': // Y <clock> remaining in secs
			String[] messageSplitted = message.split("#");
			int clock = Integer.parseInt(messageSplitted[1]);
			String scoreBrd = messageSplitted[2];
			String currentMatrix = messageSplitted[3];
			String deadsList = messageSplitted[4];
			String playersPositions = messageSplitted[5];
			//String userNamesMap = messageSplitted[6];
			updateInfo(clock, scoreBrd, currentMatrix, deadsList, playersPositions);
			break;
		default:
			break;
		}
	}
}
