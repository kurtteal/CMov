package com.example.bomberman.util;

public class GameMatrix {

	private char matrix[][];
	int sizeX;
	int sizeY;
	
	public GameMatrix(int sizeX, int sizeY){
		matrix = new char[sizeX][sizeY];
		this.sizeX = sizeX;
		this.sizeY = sizeY;
	}
	  
	public int getSizeX(){
		return sizeX;
	}
	
	public int getSizeY(){
		return sizeY;
	}
	
	public void fillMatrix() {
		int i,j;
		for(i=0; i< sizeY; i++)
			for(j=0; j< sizeX; j++){
				char element = '0';//TODO get element from file
				matrix[i][j] = element;
			}
	}
	
	//Called from the GameThread
	public void drawMap(){
		//TODO
	}
	
}
