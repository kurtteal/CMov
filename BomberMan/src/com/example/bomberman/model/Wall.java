package com.example.bomberman.model;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.example.bomberman.R;
import com.example.bomberman.util.GameConfigs;

/**
 * This is a test droid that is dragged, dropped, moved, smashed against the
 * wall and done other terrible things with. Wait till it gets a weapon!
 * 
 * @author impaler
 * 
 */
public class Wall implements IDrawable{

	private Bitmap bitmap; // the actual bitmap (or the animation sequence)
	private int x; // the X coordinate (top left of the image)
	private int y; // the Y coordinate (top left of the image)

	public Wall(Resources resources, int x, int y, int i, int j) {
		//the wall will not use int i and int j, as it does not change the state of the gameMatrix
		this.bitmap = BitmapFactory.decodeResource(resources,
				R.drawable.wall);
		this.x = x;
		this.y = y;
	}
	
	public void setState(PathState state, char[][] matrix) {}

	// for collision checks
	public int getWidth() {
		return bitmap.getWidth();
	}

	// for collision checks
	public int getHeight() {
		return bitmap.getHeight();
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

	public void update(long gameTime, GameConfigs gm) {
		// Walls don't need to update their state.
	}

	// the draw method which draws the corresponding frame	
	public void draw(Canvas canvas) {
		canvas.drawBitmap(bitmap, x, y,null);
	}


}
