package com.example.bomberman.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

public class GameConfigs implements Serializable {

	private static final long serialVersionUID = 3721119759990402565L;
	
	public char matrix[][]; //onde estao walls e paths
	public char overlay[][]; //onde estao os players e robots
	
	private int numLines;
	private int numColumns;

	public String levelName;
	public int gameDuration;
	public double explosionTimeout;
	public int explosionDuration;
	public int explosionRange;
	public double robotSpeed;
	public int ptsPerRobot;
	public int ptsPerPlayer;
	public int maxPlayers;
	  
	
	public int getNumLines() {
		return numLines;
	}
	
	public int getNumColumns() {
		return numColumns;
	}
	
	public int getMaxPlayers() {
		return maxPlayers;
	}
	
	public int loadConfigs(InputStream is) throws IOException {
		BufferedReader input = new BufferedReader(new InputStreamReader(is));

		String line = input.readLine();
		if(line == null) {
			input.close();
			throw new IOException("Ficheiro de config mal formatado!");
		}
		
		//obtem o tamanho da matriz e inicializa-a
		String[] dimensions = line.split(",");
		numLines = Integer.parseInt(dimensions[0]);
		numColumns = Integer.parseInt(dimensions[1]);
		matrix = new char[numLines][numColumns];
		overlay = new char[numLines][numColumns];
		
		//returns an empty String if two newlines appear in a row (this should never happen tho)
		int i,j;
		for(i=0; i< numLines; i++) {
			if((line = input.readLine()) != null) {
				char[] charArray = line.toCharArray();
				for(j=0; j< numColumns; j++) {
					matrix[i][j] = charArray[j]; //get element from file
					if(charArray[j] != '-' && charArray[j] != 'W' && charArray[j] != 'O' && charArray[j] != 'B' && charArray[j] != 'E')
						overlay[i][j] = charArray[j]; //player or robot
					else
						overlay[i][j] = '-';
				}
			}
		}
		
		String[] array;
		line = input.readLine();
		array = line.split("/");
		levelName = array[0];
		
		line = input.readLine();
		array = line.split("/");
		gameDuration = Integer.parseInt(array[0]);

		line = input.readLine();
		array = line.split("/");
		explosionTimeout = Double.parseDouble(array[0]);
		
		line = input.readLine();
		array = line.split("/");
		explosionDuration = Integer.parseInt(array[0]);
		
		line = input.readLine();
		array = line.split("/");
		explosionRange = Integer.parseInt(array[0]);

		line = input.readLine();
		array = line.split("/");
		robotSpeed = Double.parseDouble(array[0]);
		
		line = input.readLine();
		array = line.split("/");
		ptsPerRobot = Integer.parseInt(array[0]);
		
		line = input.readLine();
		array = line.split("/");
		ptsPerPlayer = Integer.parseInt(array[0]);
		
		line = input.readLine();
		array = line.split("/");
		maxPlayers = Integer.parseInt(array[0]);
		
		input.close();
		return maxPlayers;
	}
	
	public char readLogicPosition(int i, int j) {
		synchronized(matrix) {
			return matrix[i][j];
		}
	}
	
	public void writeLogicPosition(int i, int j, char value) {
		synchronized(matrix) {
			matrix[i][j] = value;
		}
	}
	
	public char readOverlayPosition(int i, int j) {
		synchronized(overlay) {
			return overlay[i][j];
		}
	}
	
	//A matriz overlay serve para poder escrever cada player no mapa na posicao 
	//correcta sem escrever por cima de uma bomba ou de uma explosao
	public void writeOverlayPosition(int i, int j, char value) {
		synchronized(overlay) {
			overlay[i][j] = value;
		}
	}
	
}
