package com.example.bomberman.model;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.example.bomberman.GamePanel;
import com.example.bomberman.R;
import com.example.bomberman.util.GameConfigs;

public class Wall implements IDrawable{

	private Bitmap bitmap; // the actual bitmap (or the animation sequence)
	private int x; // the X coordinate (top left of the image)
	private int y; // the Y coordinate (top left of the image)
	private GamePanel panel;
	private int numColumns;
	private int numLines;
	
	private boolean firstUpdate = true;

	public Wall(Resources resources, int x, int y, int numColumns, int numLines, GamePanel panel) {
		this.panel = panel;
		//the wall will not use int i and int j, as it does not change the state of the gameMatrix
		this.bitmap = BitmapFactory.decodeResource(resources, R.drawable.wall);
		this.x = x;
		this.y = y;
		this.numColumns = numColumns;
		this.numLines = numLines;
	}
	
	public void setState(PathState state, char owner) {}

	// for collision checks
	public int getWidth() {
		//return bitmap.getWidth();
		return panel.getWidth()/numColumns;
	}

	// for collision checks
	public int getHeight() {
		//return bitmap.getHeight();
		return panel.getHeight()/numLines;
	}
	// for collision checks
	public int getRightBorder() { return x+getWidth(); }
	public int getLeftBorder() { return x; }
	public int getUpBorder() { return y; }
	public int getDownBorder() { return y+getHeight(); }

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	private void expandBitmap(){
		//expansao do bitmap
		bitmap = Bitmap.createScaledBitmap(bitmap, panel.getWidth()/numColumns, panel.getHeight()/numLines, false);
	}
	
	public void update(long gameTime, GameConfigs gm) {
		if(firstUpdate){
			expandBitmap();
			firstUpdate = false;
		}
		// Walls don't need to update their state.
	}

	// the draw method which draws the corresponding frame	
	public void draw(Canvas canvas) {
		canvas.drawBitmap(bitmap, x, y,null);
	}


}
