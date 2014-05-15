package com.example.bomberman.model;

import java.util.Random;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.bomberman.GamePanel;
import com.example.bomberman.R;
import com.example.bomberman.network.NetworkService;

public class Robot extends Bomberman{

	private double xAcum = 0;
	private double yAcum = 0;
	public int robotId; //used in multiplayer
	private boolean singleplayer;
	//os robots precisam de saber se sao eles que enviam a info
	//no caso do multiplayer
	private char playerId; 

	private NetworkService service;

	private int checkPlayersCounter = 0;

	public Robot(){
		super();
	}

	public Robot (Resources resources, int x, int y, int xMargin, int yMargin, GamePanel panel, char myself, double speed, int numColumns, int numLines, int robotId) {
		super(resources, x, y, xMargin, yMargin, panel, myself, numColumns, numLines);
		this.movementMargin = speed; //the faster it goes, the more margin it needs
		this.speed.setVelocity(speed);

		this.robotId = robotId;
		this.singleplayer = panel.activity.singleplayer;
		this.playerId = panel.activity.playerId;

		if(!singleplayer)
			service = new NetworkService();

		this.bitmapUp = BitmapFactory.decodeResource(resources, R.drawable.robot_back);
		this.bitmapDown = BitmapFactory.decodeResource(resources, R.drawable.robot_front);
		this.bitmapLeft = BitmapFactory.decodeResource(resources, R.drawable.robot_left);
		this.bitmapRight = BitmapFactory.decodeResource(resources, R.drawable.robot_right);

		//this.speed.goUp(); //initial behaviour for robots
		if(singleplayer){
			startMoving();
		}
	}

	public void startMoving(){
		//em multiplayer os robots sao come�ados depois do first update da arena
		//onde este metodo ja foi invocado e portanto ja existe i e j, aqui eh preciso
		//invocar este metodo no construtor antes de mandar mexer os robots
		updateMatrixCoordinates(); 
		char[] surroundings = getSurroundings();
		decideNewPath(surroundings);
	}

	//O robot resolve colisoes, escolhendo uma direccao qqer aleatoria
	@Override
	public void solveCollision(){
		//check surroundings
		speed.stayStill();		//porque o robot so muda de direçao de estiver parado (caso contrario vai para next move)
		targetX = 0;
		targetY = 0;
		char[] surroundings = getSurroundings();
		decideNewPath(surroundings);
		if(nextMove != ' ')
			Log.d("decideNewPath", "ROBOTID: "+ robotId+ " , O NEXT MOVE E " + nextMove);
	}


	//check for players in the surroundings, killing them if any was found
	private void checkForPlayers(){
		char current = gc.readOverlayPosition(i, j);
		char above = gc.readOverlayPosition(i-1, j);
		char below = gc.readOverlayPosition(i+1, j);
		char toTheLeft = gc.readOverlayPosition(i, j-1);
		char toTheRight = gc.readOverlayPosition(i, j+1);


		if(current != '-'){
			Bomberman bman = panel.getArena().getPlayer(current);
			if(bman != null && !bman.getIsPaused()){
				gc.writeOverlayPosition(i,j,'-');
				bman.die();
			}
		}
		if(above != '-'){
			Bomberman bman = panel.getArena().getPlayer(above);
			if(bman != null && !bman.getIsPaused()){
				gc.writeOverlayPosition(i,j,'-');
				bman.die();
			}
		}
		if(below != '-'){
			Bomberman bman = panel.getArena().getPlayer(below);
			if(bman != null && !bman.getIsPaused()){
				gc.writeOverlayPosition(i,j,'-');
				bman.die();
			}
		}
		if(toTheLeft != '-'){
			Bomberman bman = panel.getArena().getPlayer(toTheLeft);
			if(bman != null && !bman.getIsPaused()){
				gc.writeOverlayPosition(i,j,'-');
				bman.die();
			}
		}
		if(toTheRight != '-'){
			Bomberman bman = panel.getArena().getPlayer(toTheRight);
			if(bman != null && !bman.getIsPaused()){
				gc.writeOverlayPosition(i,j,'-');
				bman.die();
			}
		}
	}

	//Returns a char[4] with whats in the surroundings on the logic matrix
	//if I find a player in the surroundings, I kill it immediately!
	private char[] getSurroundings(){
		//		int[] array = getPositionInMatrix();
		//		//Log.i("ARRAY:", array[0]+","+array[1]);
		//		int j = array[0];
		//		int i = array[1];

		char[] directions = new char[4];

		//fill the directions array			
		directions[0] = gc.readLogicPosition(i-1, j);
		directions[1] = gc.readLogicPosition(i+1, j);
		directions[2] = gc.readLogicPosition(i, j-1);
		directions[3] = gc.readLogicPosition(i, j+1);

		return directions;
	}

	//Obtem a matriz de estados, olha para as posicoes ah sua volta
	//e ve dessas quais as que tem caminho livre, depois decide aleatoriamente
	//entre os caminhos livres
	//Este metodo so eh chamado quando o robot chegar a um bloco
	private void decideNewPath(char[] surroundings){
		//int[] array = getPositionInMatrix();
		//Log.i("ARRAY:", array[0]+","+array[1]);
		//int j = array[0];
		//int i = array[1];

		//Single ou multi, a instancia de jogo com playerId = 1 eh quem envia comandos.
		//Se tiver em multi e nao for o playerId 1 os robots vao-s mexer com comandos
		//vindos do servidor
		//Log.i("Robot:", "playerId= " + playerId + " robotId= " + robotId);
		if(playerId == '1'){
			char above = surroundings[0];
			char below = surroundings[1];
			char toTheLeft = surroundings[2];
			char toTheRight = surroundings[3];
			Log.d("decideNewPath", "ROBOT ID: "+robotId+" | above is:" + above);
			Log.d("decideNewPath", "ROBOT ID: "+robotId+" | below is:" + below);
			Log.d("decideNewPath", "ROBOT ID: "+robotId+" | left is:"+ toTheLeft);
			Log.d("decideNewPath", "ROBOT ID: "+robotId+" | right is:"+ toTheRight);


			int numPossible = 0;
			char[] possiblePaths = new char[4];
			//fill the possible paths array
			if(pathIsFree(above)){
				possiblePaths[numPossible] = 'U';
				numPossible++;
			}
			if(pathIsFree(below)){
				possiblePaths[numPossible] = 'D';
				numPossible++;
			}
			if(pathIsFree(toTheLeft)){
				possiblePaths[numPossible] = 'L';
				numPossible++;
			}
			if(pathIsFree(toTheRight)){
				possiblePaths[numPossible] = 'R';
				numPossible++;
			}
			if (numPossible > 0){
				Log.d("decideNewPath", "ENTREI NO IF E O numPossible is:" + numPossible);
				//Pick an available path randomly
				Random m = new Random();
				int selected = m.nextInt(numPossible);

				switch(possiblePaths[selected]){
				case 'U':
					//speed.goUp();
					if(singleplayer)
						oneSquareUp(null, null);
					else{
						//	int[] coords = getPositionInMatrix();
						//	int i = coords[1];
						//	int j = coords[0];
						Log.d("decideNewPath", "A NOVA DIRECAO E UP");
						service.robotUp(robotId, i, j);
					}
					break;
				case 'D':
					//speed.goDown();
					if(singleplayer)
						oneSquareDown(null, null);
					else{
						//	int[] coords = getPositionInMatrix();
						//	int i = coords[1];
						//	int j = coords[0];
						Log.d("decideNewPath", "A NOVA DIRECAO E DOWN");
						service.robotDown(robotId, i, j);
					}
					break;
				case 'L':
					//speed.goLeft();
					if(singleplayer)
						oneSquareLeft(null, null);
					else{
						//	int[] coords = getPositionInMatrix();
						//	int i = coords[1];
						//	int j = coords[0];
						Log.d("decideNewPath", "A NOVA DIRECAO E LEFT");
						service.robotLeft(robotId, i, j);
					}
					break;
				case 'R':
					//speed.goRight();
					if(singleplayer)
						oneSquareRight(null, null);
					else{
						//	int[] coords = getPositionInMatrix();
						//	int i = coords[1];
						//	int j = coords[0];
						Log.d("decideNewPath", "A NOVA DIRECAO E RIGHT");
						service.robotRight(robotId, i, j);
					}
					break;
				default:
					break;
				}
			} else {
				if(singleplayer)
					oneSquareUp(null, null);
				else{
					service.robotUp(robotId, i, j);
				}
			}
		}
	}

	//New positions (pixels) for robots
	@Override
	protected void updatePixelPosition(){
		//Se estiver num cruzamento, obtem a vizinhanca, 
		//e decide aleatoriamente a nova direccao

		if(targetX != 0 && Math.abs(targetX - x) < movementMargin){ //se chegou ao destino
			x = targetX;
			targetX = 0;
			speed.setXStationary();
			if(!checkIfNextMove()){
				//check surroundings
				char[] surroundings = getSurroundings();

				decideNewPath(surroundings);
				//checkIfPlanted();
				//will plant a bomb with a given probability if it is at an intersection
				//decideIfPlant();
			}
		} else if(targetY != 0 && Math.abs(targetY - y) < movementMargin){
			y = targetY; 
			targetY = 0;
			speed.setYStationary();
			if(!checkIfNextMove()){

				//check surroundings
				char[] surroundings = getSurroundings();
				decideNewPath(surroundings);
				//checkIfPlanted();
				//will plant a bomb with a given probability if it is at an intersection
				//decideIfPlant();
			}
		}else{ //continua a mexer-se caso contrario

			if(speed.getVelocity() >= 1){
				x += (speed.getVelocity() * speed.getxDirection()); 
				y += (speed.getVelocity() * speed.getyDirection());
				Log.d("ROBOT SPEED", "NO IF, ROBOT"+ robotId + " | X="+x +" | Y="+y);
			} else{
				xAcum += (speed.getVelocity() * speed.getxDirection());
				yAcum += (speed.getVelocity() * speed.getyDirection());
				if(xAcum >= 1){
					x += xAcum;
					xAcum -= 1;
				} else if(xAcum <= -1){
					x += xAcum;
					xAcum += 1;
				}

				if(yAcum >= 1){
					y += yAcum;
					yAcum -= 1;
				} else if(yAcum <= -1){
					y += yAcum;
					yAcum += 1;
				}
			}
		}	
		//verifica se ha players na vizinhanca
		checkPlayersCounter++;
		if(checkPlayersCounter%10 == 0) //faz o check a cada 5 updates
			checkForPlayers();
	}

	//	//actualiza a variavel de estado que diz se pos uma bomba recentemente (num bloco anterior)
	//	@Override
	//	protected void checkIfPlanted(){
	//		if(justPlanted)
	//			justPlanted = false;
	//	}
	//	
	//	private void decideIfPlant(){
	//		if(Math.random() > 0.95) //5% prob de por bomba num cruzamento
	//			plantBomb();
	//	}

	private boolean pathIsFree(char block){
		if(block != 'O' && block != 'W' && block != 'B' && block != 'E') return true;
		else return false;
	}

}
