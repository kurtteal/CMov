package com.example.bomberman.model;

import com.example.bomberman.util.GameMatrix;

import android.graphics.Canvas;

// for collision checks, and arena drawing
public interface IDrawable {
	public int getWidth();
	public int getHeight();
	public int getRightBorder();
	public int getLeftBorder(); 
	public int getUpBorder();
	public int getDownBorder();
	
	public void update(long gameTime, GameMatrix gm);
	public void draw(Canvas canvas);
}
