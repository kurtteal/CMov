package com.example.bomberman.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

public class GameConfigs implements Serializable {

	public char matrix[][];
	private int sizeX;
	private int sizeY;

	public String levelName;
	public int gameDuration;
	public int explosionTimeout;
	public int explosionDuration;
	public int explosionRange;
	public int robotSpeed;
	public int ptsPerRobot; //	Points per robot killed
	public int ptsPerPlayer; // Points per opponent killed
	  
	
	public int getSizeX(){
		return sizeX;
	}
	
	public int getSizeY(){
		return sizeY;
	}
	
	public void loadConfigs(InputStream is) throws IOException{

		BufferedReader input = null;

		// use buffering, reading one line at a time
		// FileReader always assumes default encoding is OK!
		input = new BufferedReader(new InputStreamReader(is));

		String line = input.readLine();
		if(line == null){
			input.close();
			throw new IOException("Ficheiro de config mal formatado!");
		}
		//obtem o tamanho da matriz e inicializa-a
		String[] dimensions = line.split(",");
		sizeX = Integer.parseInt(dimensions[0]); //linhas
		sizeY = Integer.parseInt(dimensions[1]); //colunas
		matrix = new char[sizeX][sizeY];
		
		//returns an empty String if two newlines appear in a row (this should never happen tho)
		int i,j;
		for(i=0; i< sizeX; i++){
			if((line = input.readLine()) != null){
				char[] charArray = line.toCharArray();
				for(j=0; j< sizeY; j++)
					matrix[i][j] = charArray[j]; //get element from file
			}
		}
		//Obtem os outros dados
		String[] array;
		line = input.readLine();
		array = line.split("/");
		levelName = array[0];
		
		line = input.readLine();
		array = line.split("/");
		gameDuration = Integer.parseInt(array[0]);

		line = input.readLine();
		array = line.split("/");
		explosionTimeout = Integer.parseInt(array[0]);
		
		line = input.readLine();
		array = line.split("/");
		explosionDuration = Integer.parseInt(array[0]);
		
		line = input.readLine();
		array = line.split("/");
		explosionRange = Integer.parseInt(array[0]);

		line = input.readLine();
		array = line.split("/");
		robotSpeed = Integer.parseInt(array[0]);
		
		line = input.readLine();
		array = line.split("/");
		ptsPerRobot = Integer.parseInt(array[0]);
		
		line = input.readLine();
		array = line.split("/");
		ptsPerPlayer = Integer.parseInt(array[0]);
		
		input.close();
	}
	
	//Debug
	public void showMatrixOnConsole(){
		int i,j;
		for(i=0; i< sizeX; i++){
			for(j=0; j< sizeY; j++)
				System.out.print(matrix[i][j]); //get element from file
			System.out.println("");
		}
	}
	
	//Debug
	public String getLine(int j){
		int i;
		char[] array = new char[sizeY];
		for(i=0; i < sizeY; i++){
			array[i] = matrix[j][i];
		}
		return new String(array);
	}
	
}
