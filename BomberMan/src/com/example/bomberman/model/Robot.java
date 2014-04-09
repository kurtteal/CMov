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
	
	//Recebe a matriz de estados, olha para as posicoes ah sua volta
	//e ve dessas quais as que tem caminho livre, depois decide aleatoriamente
	//entre os caminhos livres
	private void decideNewPath(){
		int[] array = getPositionInMatrix();
		Log.i("ARRAY:", array[0]+","+array[1]);
		int i = array[0];
		int j = array[1];
		//check surroundings
		char above = gc.readPosition(j-1, i);
		char below = gc.readPosition(j+1, i);
		char toTheLeft = gc.readPosition(j, i-1);
		char toTheRight = gc.readPosition(j, i+1);
		
		//Log.d("ROBOT", "U,D,L,R = "+above+","+below+","+toTheLeft+","+toTheRight);
		
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
		//Se estiver num cruzamento, decide aleatoriamente a nova direccao
		if( (y-yMapMargin)%getHeight() == 0 && Math.abs((x-xMapMargin) - getPositionInMatrix()[0]*getWidth()) < movementMargin ){ //horizontal
			x = xMapMargin + getPositionInMatrix()[0]*getWidth();
			decideNewPath();
		} else if( (x-xMapMargin)%getWidth() == 0 && Math.abs((y-yMapMargin) - getPositionInMatrix()[1]*getHeight()) < movementMargin ){ //horizontal
			y = yMapMargin + getPositionInMatrix()[1]*getHeight();
			decideNewPath();
		}
		x += (speed.getVelocity() * speed.getxDirection()); 
		y += (speed.getVelocity() * speed.getyDirection());
			
	}
	
	private boolean pathIsFree(char block){
		if(block != 'O' && block != 'W' && block != 'B') return true;
		else return false;
	}

}
