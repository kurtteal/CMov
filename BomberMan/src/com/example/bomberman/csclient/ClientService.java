package com.example.bomberman.csclient;

import java.util.TreeMap;

import com.example.bomberman.IGameActivity;
import com.example.bomberman.IMenuActivity;
import android.util.Log;

//Classe que encapsula o protocolo de comunicacao do lado do cliente.
//Vai chamar os asyncTasks necessarios para fazer connect e enviar os varios comandos
//mantem o playerId e a activity actual em que se encontra (do ultimo metodo que foi chamado)
//O asyncTask mantem 1 referencia para esta classe. E esta classe eh passada de activity em activity
//isolando o asyncTask, tanto do protocolo usado como de intera��es com v�rias activities.
public class ClientService {

	private static char playerId = '#';
	private static IMenuActivity menuActivity;
	private static IGameActivity gameActivity;
	private static TreeMap<Integer, String> clientsNames = new TreeMap<Integer, String>();
	private static TreeMap<Integer, String> participantsReady = new TreeMap<Integer, String>();

	public void setMenuActivity(IMenuActivity act){
		menuActivity = act;
	}

	public void setGameActivity(IGameActivity act){
		gameActivity = act;
	}

	public void setPlayerId(char id){
		playerId = id;
	}
	public char getPlayerId(){
		return playerId;
	}

	//Vai abrir o socket po servidor, na asyncTask ficam guardados (static), o socket,
	//e os buffers de entrada e saida para comunicacao
	public void connect(){
		try{
			//out = (new ClientConnectorTask("connect", MainActivity.this).execute("")).get();
			new ClientAsyncTask("connect", this).execute("");
		}catch(Exception e){	e.printStackTrace();	}
	}

	//Metodo para teste
	public void send(String message){
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message + playerId);
		}catch(Exception e){	e.printStackTrace();	}
	}

	//Invocado pelo botao
	public void createGame(String playerName){
		String message = "create " + playerName + playerId;
		Log.d("Msg enviada: ", message);
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}

	//Invocado pelo botao
	public void joinGame(String playerName){
		String message = "join " + playerName + playerId;
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}

	//Invocado pelo botao (todos fazem load da arena e esperam pela ordem de start)
	public void preStartGame(){
		String message = "start " + playerId;
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}

	//Invocado pelo botao
	public void leaveGame(){
		String message = "leave_game" + playerId;
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}

	//Invocado pelo botao
	public void setMap(int mapNumber, int maxPlayers){
		String message = "set_map " + mapNumber + " " + playerId + " " + maxPlayers;
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}

	//Invocado pelo botao
	public void goUp(int i, int j){
		String message = "Cup" + "/" + playerId + "/" + i + "/" + j; //client command up
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}

	//Invocado pelo botao
	public void goDown(int i, int j){
		String message = "Cdown" + "/" + playerId + "/" + i + "/" + j;//client command down
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}
	
	//Invocado pelo botao
	public void goLeft(int i, int j){
		String message = "Cleft" + "/" + playerId + "/" + i + "/" + j; //client command left
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}

	//Invocado pelo botao
	public void goRight(int i, int j){
		String message = "Cright" + "/" + playerId + "/" + i + "/" + j; //client command right
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}

	//Invocado pelo botao
	public void plantBomb(int i, int j){
		String message = "Cbomb" + "/" + playerId + "/" + i + "/" + j; //client command bomb
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}

	public void startGame(){
		String message = "T" + playerId; //start the timer
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}
	
	//Quem se joina a meio, envia
	public void midJoin(){
		String message = "mid_join_ready " + playerId;
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}
	
	//O game master depois de receber um join a meio, envia o clock atual
	//para o ultimo que entrou ficar atualizado
	private void sendCurrentClock(char newPlayerId, int clock){
		String message = "clock " + clock + newPlayerId;
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}

	protected void socketWasClosed(){
		//TODO chamar 1 excepcao na activity a explciar que o socket foi fechado no servidor
	}

	//Chamado pelos robots
	public void robotUp(int robotId, int i, int j){
		String message = "Rup" + "/" + robotId + "/" + i + "/" + j; //robot left
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}

	//Chamado pelos robots
	public void robotDown(int robotId, int i, int j){
		String message = "Rdown" + "/" + robotId + "/" + i + "/" + j; //robot left
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}
	
	//Chamado pelos robots
	public void robotLeft(int robotId, int i, int j){
		String message = "Rleft" + "/" + robotId + "/" + i + "/" + j; //robot left
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}

	//Chamado pelos robots
	public void robotRight(int robotId, int i, int j){
		String message = "Rright" + "/" + robotId + "/" + i + "/" + j; //robot left
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}

	//Chamado pelos participantes que nao sao game master, enquanto o master nao
	//esta pronto
	public void imSet(){
		String message = "S" + playerId; 
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){ e.printStackTrace(); }
	}


	//Metodo invocado pelo ClientListener depois da resposta do servidor:	
	protected void processMessage(String message){
		//verificar que tipo de mensagem se trata	
		char type = message.charAt(0);
		switch(type){
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
		case 'L': //lista de jogadores actualizada
			String[] players = message.substring(2, message.length()-1).split(",");
			Log.d("Plist updt: ", this.playerId + " " + players[0]);
			updatePlayerList(players);
			break;
		case 'M': //mapa escolhido
			updateMap(message.charAt(1));
			break;
		case 'X': //preStartGame (gameActivity loading arena)
			preStartGameOrder(message);
			break;
		case 'S': //other participants sending 'im set' msgs to the game master
			checkIfAllReady(message.charAt(1));
			break;
		case 'T': //startGame
			gameActivity.startGameOrder();
			break;
		case 'C': //comando de outro jogador (ou do proprio)
			playerAction(message.substring(1));
			break;
		case 'R': //comando de um robot
			robotAction(message.substring(1));
			break;
		case 'N': //new player N <playerId>
			newPlayer(message.substring(1));
			break;
		case 'Y': //Y <clock> remaining in secs
			updateClock(message.substring(1));
			break;
		default:
			break;	
		}
	}

	//Metodos internos
	private void createSuccessful(){
		setPlayerId('1'); //quem criou eh o 1
		//avisar a activity
		menuActivity.createResponse(true);
	}

	private void createNotSuccessful(){
		//avisar a activity
		menuActivity.createResponse(false);
	}

	private void joinSuccessful(char playerId){
		setPlayerId(playerId);
		//avisar a activity
		menuActivity.joinResponse(true, playerId);
	}

	private void joinNotSuccessful(char cause){
		//avisar a activity
		menuActivity.joinResponse(false, cause);
	}

	private void updatePlayerList(String[] players){
		for(String player : players){
			String[] data = player.split("="); 
			clientsNames.put(Integer.parseInt(data[0].trim()), data[1]);
		}
		//avisar a activity
		menuActivity.updatePlayerList(clientsNames);
		//http://stackoverflow.com/questions/4369537/update-ui-from-thread
		//new ResponseAsyncTask(activity, clientsNames).execute(" ");
	}

	private void updateMap(char mapNumber){
		menuActivity.updateMap(mapNumber);
	}

	private void preStartGameOrder(String message){
		char mode = message.charAt(1);
		menuActivity.preStartGameOrder(mode);
	}

	private void playerAction(String command){ //ex: up/3/13/42
		String[] params = command.split("/");
		char executerId = params[1].charAt(0);
		String iPos = params[2];
		String jPos = params[3];
		if(command.startsWith("up"))
			gameActivity.goUpOrder(executerId, iPos, jPos);
		else if(command.startsWith("down"))
			gameActivity.goDownOrder(executerId, iPos, jPos);
		else if(command.startsWith("left"))
			gameActivity.goLeftOrder(executerId, iPos, jPos);
		else if(command.startsWith("right"))
			gameActivity.goRightOrder(executerId, iPos, jPos);
		else if(command.startsWith("bomb"))
			gameActivity.plantBombOrder(executerId, iPos, jPos);
	}

	private void robotAction(String command){
		String[] params = command.split("/");
		int executerId = Integer.parseInt(params[1]);
		String iPos = params[2];
		String jPos = params[3];
		if(command.startsWith("up"))
			gameActivity.robotGoUp(executerId, iPos, jPos);
		else if(command.startsWith("down"))
			gameActivity.robotGoDown(executerId, iPos, jPos);
		else if(command.startsWith("left"))
			gameActivity.robotGoLeft(executerId, iPos, jPos);
		else if(command.startsWith("right"))
			gameActivity.robotGoRight(executerId, iPos, jPos);

	}

	private void checkIfAllReady(char id){
		Log.d("Participants | playerId", participantsReady.size()+" "+playerId);
		if(playerId == '1' && gameActivity.masterIsReady()){
			//juntar este id a uma lista
			//a string eh irrelevante, so nos interessa o facto d n haverem repetidos
			participantsReady.put(Character.getNumericValue(id), ""); 

			//verificar se ja recebi o set de todos os outros participantes
			if(participantsReady.size() == (gameActivity.getNumPlayers() - 1))
				startGame(); //envia o inicio do jogo pa tds
		}
	}
	
	//Quando um novo player entra, eh pq o servidor deixou (ainda havia lugares vagos)
	//Os unicos que recebem esta msg sao todos menos o que entra
	private void newPlayer(String id){
		char newPlayerId = id.charAt(0);
		gameActivity.newPlayer(newPlayerId);
		if(playerId == '1'){
			//TODO enviar msg com o clock actual para o newPlayerId
			int currentCount = gameActivity.getCountDown();
			sendCurrentClock(newPlayerId, currentCount); //metodo do proprio servico
		}
	}
	
	private void updateClock(String clock){
		gameActivity.setCountDown(Integer.parseInt(clock));
	}

}
