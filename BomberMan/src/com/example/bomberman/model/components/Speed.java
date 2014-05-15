package com.example.bomberman.model.components;

import java.util.Random;

public class Speed {

	public static final int DIRECTION_RIGHT = 1;
	public static final int DIRECTION_LEFT = -1;
	public static final int DIRECTION_UP = -1;
	public static final int DIRECTION_DOWN = 1;
	public static final int STILL = 0;

	private double velocity = 6;
	private int xDirection;
	private int yDirection;

	public Speed() {
		stayStill();
	}

	public double getVelocity() {
		return velocity;
	}

	public void setVelocity(double velo) {
		velocity = velo;
	}

	public int getxDirection() {
		return xDirection;
	}

	public int getyDirection() {
		return yDirection;
	}

	public void setXStationary() {
		xDirection = STILL;
	}

	public void setYStationary() {
		yDirection = STILL;
	}

	public void goUp() {
		xDirection = STILL;
		yDirection = DIRECTION_UP;
	}

	public void goDown() {
		xDirection = STILL;
		yDirection = DIRECTION_DOWN;
	}

	public void goRight() {
		xDirection = DIRECTION_RIGHT;
		yDirection = STILL;
	}

	public void goLeft() {
		xDirection = DIRECTION_LEFT;
		yDirection = STILL;
	}

	public void stayStill() {
		xDirection = STILL;
		yDirection = STILL;
	}

	// returns true if the bomberman is moving
	public boolean isNotZero() {
		if (xDirection != STILL || yDirection != STILL)
			return true;
		return false;
	}

	// changes the direction on the X axis (called when theres a colision)
	public void toggleXDirection() {
		xDirection = xDirection * -1;
	}

	// changes the direction on the Y axis (called when theres a colision)
	public void toggleYDirection() {
		yDirection = yDirection * -1;
	}

	public void toggleCurrentDirection() {
		if (xDirection != STILL)
			toggleXDirection();
		else if (yDirection != STILL)
			toggleYDirection();
	}

	// AI dos robots (lol)
	public void randomDirection() {
		Random r = new Random();
		int i = r.nextInt(2);
		int j = r.nextInt(2);
		if (i == 0) {
			if (j == 0)
				goUp();
			else
				goDown();
		} else {
			if (j == 0)
				goLeft();
			else
				goRight();
		}
	}

}
