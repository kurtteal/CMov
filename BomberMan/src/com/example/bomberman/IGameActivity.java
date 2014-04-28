package com.example.bomberman;

public interface IGameActivity {

	public void goLeft(char id);
	public void goRight(char id);
	public void goUp(char id);
	public void goDown(char id);
	public void plantBomb(char id);
	
	public void robotGoUp(int id);
	public void robotGoDown(int id);
	public void robotGoLeft(int id);
	public void robotGoRight(int id);
	
	public void startTimeOrder();
	public int getNumPlayers();
	public boolean getStartedTime();
	
}
