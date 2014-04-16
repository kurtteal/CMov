package com.example.bomberman.model;

import java.util.Random;

import android.content.res.Resources;
import android.util.Log;

import com.example.bomberman.MainGamePanel;
import com.example.bomberman.util.GameConfigs;

/**
 * This is a test droid that is dragged, dropped, moved, smashed against
 * the wall and done other terrible things with.
 * Wait till it gets a weapon!
 * 
 * @author impaler
 *
 */
public class Robot extends Bomberman{
	
	public Robot(){
		super();
	}
	
	public Robot (Resources resources, int x, int y, int xMargin, int yMargin, MainGamePanel panel, char myself, int speed, int numColumns, int numLines) {
		super(resources, x, y, xMargin, yMargin, panel, myself, numColumns, numLines);
		this.movementMargin = speed; //the faster it goes, the more margin it needs
		this.speed.setVelocity(speed);
		this.speed.goUp(); //initial behaviour for robots
	}
	
	//O robot resolve colisoes, escolhendo uma direccao qqer aleatoria
	@Override
	public void solveCollision(){
		decideNewPath();
	}
	
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
	private void decideNewPath(){
		int[] array = getPositionInMatrix();
		//Log.i("ARRAY:", array[0]+","+array[1]);
		int j = array[0];
		int i = array[1];
		//check surroundings
		char[] surroundings = checkSurroundings();
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
		int selected = m.nextInt(4);
		
		switch(possiblePaths[selected]){
			case 'U':
				speed.goUp();
				break;
			case 'D':
				speed.goDown();
				break;
			case 'L':
				speed.goLeft();
				break;
			case 'R':
				speed.goRight();
				break;
			default:
				break;
		}
	}
	
	//New positions (pixels) for robots
	@Override
	protected void updatePixelPosition(){
		int[] coords = getPositionInMatrix();
		int j = coords[1];
		int i = coords[0];
		//Se estiver num cruzamento, decide aleatoriamente a nova direccao
		//e verifica se ha player na vizinhanca 
		if( (y-yMapMargin)%getHeight() == 0 && Math.abs((x-xMapMargin) - i*getWidth()) < movementMargin ){ //horizontal
			x = xMapMargin + i*getWidth();
			decideNewPath();
			//checkIfPlanted();
			//will plant a bomb with a given probability if it is at an intersection
			//decideIfPlant();
			
		} else if( (x-xMapMargin)%getWidth() == 0 && Math.abs((y-yMapMargin) - j*getHeight()) < movementMargin ){ //horizontal
			y = yMapMargin + j*getHeight();
			decideNewPath();
			//checkIfPlanted();
			//will plant a bomb with a given probability if it is at an intersection
			//decideIfPlant();
		}
		x += (speed.getVelocity() * speed.getxDirection()); 
		y += (speed.getVelocity() * speed.getyDirection());
			
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
