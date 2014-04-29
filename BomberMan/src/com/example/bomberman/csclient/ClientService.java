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
	public void setMap(int mapNumber){
		String message = "set_map " + mapNumber + " " + playerId;
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}

	//Invocado pelo botao
	public void goLeft(){
		String message = "Cleft" + playerId; //client command left
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}

	//Invocado pelo botao
	public void goRight(){
		String message = "Cright" + playerId; //client command right
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}

	//Invocado pelo botao
	public void goUp(){
		String message = "Cup" + playerId; //client command up
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}

	//Invocado pelo botao
	public void goDown(){
		String message = "Cdown" + playerId; //client command down
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}

	//Invocado pelo botao
	public void plantBomb(){
		String message = "Cbomb" + playerId; //client command bomb
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

	protected void socketWasClosed(){
		//TODO chamar 1 excepcao na activity a explciar que o socket foi fechado no servidor
	}

	//Chamado pelos robots
	public void robotLeft(int robotId){
		String message = "Rleft" + robotId; //robot left
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}

	//Chamado pelos robots
	public void robotRight(int robotId){
		String message = "Rright" + robotId; //robot left
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}

	//Chamado pelos robots
	public void robotUp(int robotId){
		String message = "Rup" + robotId; //robot left
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}

	//Chamado pelos robots
	public void robotDown(int robotId){
		String message = "Rdown" + robotId; //robot left
		try{
			//out = (new ClientConnectorTask("send", out, MainActivity.this).execute(message)).get();
			new ClientAsyncTask("send").execute(message);
		}catch(Exception e){	e.printStackTrace();	}
	}

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
			joinNotSuccessful();
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
			preStartGameOrder();
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

	private void joinNotSuccessful(){
		//avisar a activity
		menuActivity.joinResponse(false, ' ');
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

	private void preStartGameOrder(){
		menuActivity.preStartGameOrder();
	}

	private void playerAction(String command){
		char executerId = command.charAt(command.length()-1);
		if(command.startsWith("up"))
			gameActivity.goUp(executerId);
		else if(command.startsWith("down"))
			gameActivity.goDown(executerId);
		else if(command.startsWith("left"))
			gameActivity.goLeft(executerId);
		else if(command.startsWith("right"))
			gameActivity.goRight(executerId);
		else if(command.startsWith("bomb"))
			gameActivity.plantBomb(executerId);
	}

	private void robotAction(String command){
		int executerId = Integer.parseInt(command.substring(command.length()-1));
		if(command.startsWith("up"))
			gameActivity.robotGoUp(executerId);
		else if(command.startsWith("down"))
			gameActivity.robotGoDown(executerId);
		else if(command.startsWith("left"))
			gameActivity.robotGoLeft(executerId);
		else if(command.startsWith("right"))
			gameActivity.robotGoRight(executerId);
	}

	private void checkIfAllReady(int id){
		Log.d("Participants | playerId", participantsReady.size()+" "+playerId);
		if(playerId == '1' && gameActivity.masterIsReady()){
			//juntar este id a uma lista
			//a string eh irrelevante, so nos interessa o facto d n haverem repetidos
			participantsReady.put(id, ""); 

			//verificar se ja recebi o set de todos os outros participantes
			if(participantsReady.size() == (gameActivity.getNumPlayers() - 1))
				startGame(); //envia o inicio do jogo pa tds
		}
	}

}
