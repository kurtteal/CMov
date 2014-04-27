package com.example.bomberman.model;

import java.util.Random;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;

import com.example.bomberman.MainGamePanel;
import com.example.bomberman.R;
import com.example.bomberman.csclient.ClientService;

public class Robot extends Bomberman{
	
	public int robotId; //used in multiplayer
	private boolean singleplayer;
	//os robots precisam de saber se sao eles que enviam a info
	//no caso do multiplayer
	private char playerId; 
	
	private ClientService service;
	
	public Robot(){
		super();
	}
	
	public Robot (Resources resources, int x, int y, int xMargin, int yMargin, MainGamePanel panel, char myself, int speed, int numColumns, int numLines, int robotId) {
		super(resources, x, y, xMargin, yMargin, panel, myself, numColumns, numLines);
		this.movementMargin = speed; //the faster it goes, the more margin it needs
		this.speed.setVelocity(speed);

		this.robotId = robotId;
		this.singleplayer = panel.activity.singleplayer;
		this.playerId = panel.activity.playerId;
		
		if(!singleplayer)
			service = new ClientService();
		
		this.bitmapUp = BitmapFactory.decodeResource(resources, R.drawable.robot_back);
		this.bitmapDown = BitmapFactory.decodeResource(resources, R.drawable.robot_front);
		this.bitmapLeft = BitmapFactory.decodeResource(resources, R.drawable.robot_left);
		this.bitmapRight = BitmapFactory.decodeResource(resources, R.drawable.robot_right);
		
		//this.speed.goUp(); //initial behaviour for robots
		startMoving();
	}
	
	public void startMoving(){
		if(playerId == '1'){
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		char[] surroundings = checkSurroundings();
		decideNewPath(surroundings);
	}
	
	//O robot resolve colisoes, escolhendo uma direccao qqer aleatoria
//	@Override
//	public void solveCollision(){
//		//check surroundings
//		char[] surroundings = checkSurroundings();
//		decideNewPath(surroundings);
//	}
	
	//Returns a char[4] with whats in the surroundings on the logic matrix
	//if I find a player in the surroundings, I kill it immediately!
	private char[] checkSurroundings(){
		int[] array = getPositionInMatrix();
		//Log.i("ARRAY:", array[0]+","+array[1]);
		int j = array[0];
		int i = array[1];
		
		//check for players in the surroundings
		char above = gc.readOverlayPosition(i-1, j);
		char below = gc.readOverlayPosition(i+1, j);
		char toTheLeft = gc.readOverlayPosition(i, j-1);
		char toTheRight = gc.readOverlayPosition(i, j+1);
		
		if(above != '-'){
			Bomberman bman = panel.getArena().getPlayerById(above);
			if(bman != null)
				bman.die();
		}
		if(below != '-'){
			Bomberman bman = panel.getArena().getPlayerById(below);
			if(bman != null)
				bman.die();
		}
		if(toTheLeft != '-'){
			Bomberman bman = panel.getArena().getPlayerById(toTheLeft);
			if(bman != null)
				bman.die();
		}
		if(toTheRight != '-'){
			Bomberman bman = panel.getArena().getPlayerById(toTheRight);
			if(bman != null)
				bman.die();
		}
		
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
			//Pick an available path randomly
			Random m = new Random();
			int selected = m.nextInt(numPossible);
			
			switch(possiblePaths[selected]){
				case 'U':
					//speed.goUp();
					if(singleplayer)
						oneSquareUp();
					else
						service.robotUp(robotId);
					break;
				case 'D':
					//speed.goDown();
					if(singleplayer)
						oneSquareDown();
					else
						service.robotDown(robotId);
					break;
				case 'L':
					//speed.goLeft();
					if(singleplayer)
						oneSquareLeft();
					else
						service.robotLeft(robotId);
					break;
				case 'R':
					//speed.goRight();
					if(singleplayer)
						oneSquareRight();
					else
						service.robotRight(robotId);
					break;
				default:
					break;
			}
		}
	}
	
	//New positions (pixels) for robots
	@Override
	protected void updatePixelPosition(){
		//Se estiver num cruzamento, obtem a vizinhanca, verifica se ha 
		//players na vizinhanca e decide aleatoriamente a nova direccao
		if(targetX != 0 && Math.abs(targetX - x) < movementMargin){ //se chegou ao destino
			x = targetX;
			targetX = 0;
			speed.setXStationary();
			if(!checkIfNextMove()){
				//check surroundings
				char[] surroundings = checkSurroundings();
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
				char[] surroundings = checkSurroundings();
				decideNewPath(surroundings);
				//checkIfPlanted();
				//will plant a bomb with a given probability if it is at an intersection
				//decideIfPlant();
			}
		}else{
			x += (speed.getVelocity() * speed.getxDirection()); 
			y += (speed.getVelocity() * speed.getyDirection());
		}	
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
		if(block != 'O' && block != 'W' && block != 'B') return true;
		else return false;
	}

}
