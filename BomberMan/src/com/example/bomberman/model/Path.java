package com.example.bomberman.model;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.example.bomberman.R;

/**
 * This is a test droid that is dragged, dropped, moved, smashed against
 * the wall and done other terrible things with.
 * Wait till it gets a weapon!
 * 
 * @author impaler
 *
 */
public class Path {

	private Bitmap bitmap; // the actual bitmap (or the animation sequence)
	private int x; // the X coordinate (top left of the image)
	private int y; // the Y coordinate (top left of the image)
	private boolean blocked;

	private Rect sourceRect; // the rectangle to be drawn from the animation
								// bitmap
	private int frameNr = 5; // number of frames in animation

	private int spriteWidth; // the width of the sprite to calculate the cut out
								// rectangle
	private int spriteHeight; // the height of the sprite

	public Path(Resources resources, int x, int y) {
		this.bitmap = BitmapFactory.decodeResource(resources,
				R.drawable.walking_right);
		this.x = x;
		this.y = y;
		spriteWidth = bitmap.getWidth() / frameNr;
		spriteHeight = bitmap.getHeight();
		sourceRect = new Rect(0, 0, spriteWidth, spriteHeight);
	}

	// for collision checks
	public int getWidth() {
		return spriteWidth;
	}

	// for collision checks
	public int getHeight() {
		return spriteHeight;
	}

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
	
	public boolean isBlocked() {
		return blocked;
	}

	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}

	public void update(long gameTime) {
		// Path don't need to update their state.
	}

	// the draw method which draws the corresponding frame
	public void draw(Canvas canvas) {
		// where to draw the sprite
		Rect destRect = new Rect(getX(), getY(), getX() + spriteWidth, getY()
				+ spriteHeight);
		// pega no bitmap, corta pelo sourceRect e coloca em destRect
		canvas.drawBitmap(this.bitmap, sourceRect, destRect, null);
	}
	
}
