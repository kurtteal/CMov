package com.example.bomberman;

public interface IGameActivity {

	public void goUpOrder(char id, String i, String j);
	public void goDownOrder(char id, String i, String j);
	public void goLeftOrder(char id, String i, String j);
	public void goRightOrder(char id, String i, String j);
	public void plantBombOrder(char id, String i, String j);
	
	public void robotGoUp(int id, String i, String j);
	public void robotGoDown(int id, String i, String j);
	public void robotGoLeft(int id, String i, String j);
	public void robotGoRight(int id, String i, String j);
	
	public void startGameOrder();
	public int getNumPlayers();
	public int getMaxPlayers();
	public boolean masterIsReady();
	
	public void newPlayer(char newPlayerId);
	public int getCountDown();
	public void setCountDown(int clock);
	
}
