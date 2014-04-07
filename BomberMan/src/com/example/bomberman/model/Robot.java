package com.example.bomberman.model;

import java.util.Random;

import android.content.res.Resources;
import android.util.Log;

import com.example.bomberman.MainGamePanel;

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
	
	public Robot (Resources resources, int x, int y, MainGamePanel panel, char myself, int speed) {
		super(resources, x, y ,panel, myself);
		this.speed.setVelocity(speed);
		this.speed.goUp();
	}
	
	//O robot resolve colisoes, escolhendo uma direccao qqer aleatoria
	@Override
	public void solveCollision(char[][] matrix){
		decideNewPath(matrix);
	}
	
	//Recebe a matriz de estados, olha para as posicoes ah sua volta
	//e ve dessas quais as que tem caminho livre, depois decide aleatoriamente
	//entre os caminhos livres
	private void decideNewPath(char[][] matrix){
		int[] array = getPositionInMatrix();
		Log.i("ARRAY:", array[0]+","+array[1]);
		int i = array[0];
		int j = array[1];
		//check surroundings
		char above = matrix[j-1][i];
		char below = matrix[j+1][i];
		char toTheLeft = matrix[j][i-1];
		char toTheRight = matrix[j][i+1];
		
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
	protected void updatePixelPosition(char[][] matrix){
		//Se estiver num cruzamento, decide aleatoriamente a nova direccao
		if( y%getHeight() == 0 && Math.abs(x - getPositionInMatrix()[0]*getWidth()) < 3 ){ //horizontal
			x = getPositionInMatrix()[0]*getWidth();
			decideNewPath(matrix);
		} else if( x%getWidth() == 0 && Math.abs(y - getPositionInMatrix()[1]*getHeight()) < 3 ){ //horizontal
			y = getPositionInMatrix()[1]*getHeight();
			decideNewPath(matrix);
		}
		x += (speed.getVelocity() * speed.getxDirection()); 
		y += (speed.getVelocity() * speed.getyDirection());
			
	}
	
	private boolean pathIsFree(char block){
		if(block != 'O' && block != 'W' && block != 'B') return true;
		else return false;
	}

}
