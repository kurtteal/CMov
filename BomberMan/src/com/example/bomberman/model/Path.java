package com.example.bomberman.model;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.example.bomberman.R;

/**
 * This is a test droid that is dragged, dropped, moved, smashed against the
 * wall and done other terrible things with. Wait till it gets a weapon!
 * 
 * @author impaler
 * 
 */
public class Path implements IDrawable{

	private Bitmap bitmap; // the actual bitmap (or the animation sequence)
	private int x; // the X coordinate (top left of the image)
	private int y; // the Y coordinate (top left of the image)

	private Rect sourceRect; // the rectangle to be drawn from the animation
								// bitmap
	private int frameNr = 5; // number of frames in animation

	private int currentFrame; // the current frame
	private long frameTicker; // the time of the last frame update
	private int fps = 5; //da animacao, n eh do jogo
	private int framePeriod = 1000 / fps; // milliseconds between each frame
	
	private int spriteWidth; // the width of the sprite to calculate the cut out
								// rectangle
	private int spriteHeight; // the height of the sprite
	
	private PathState state;

	public Path(Resources resources, int x, int y, PathState state) {
		if(state == PathState.FLOOR){
			bitmap = BitmapFactory.decodeResource(resources,
				R.drawable.floor);
			frameNr = 1;
		}
		else if(state == PathState.OBSTACLE){
			bitmap = BitmapFactory.decodeResource(resources,
					R.drawable.obstacle);
				frameNr = 1;
		}
		else if(state == PathState.EXPLOSION){
			bitmap = BitmapFactory.decodeResource(resources,
					R.drawable.explosion);
				frameNr = 4;
		}
		else if(state == PathState.BOMB){
			bitmap = BitmapFactory.decodeResource(resources,
					R.drawable.bomb);
				frameNr = 2;
		}
		
		this.x = x;
		this.y = y;
		spriteWidth = bitmap.getWidth() / frameNr;
		spriteHeight = bitmap.getHeight();
		sourceRect = new Rect(0, 0, spriteWidth, spriteHeight);		
		currentFrame = 0;
		frameTicker = 0l;
	}

	// for collision checks
	public int getWidth() {
		return spriteWidth;
	}

	// for collision checks
	public int getHeight() {
		return spriteHeight;
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

	public PathState getState() {
		return state;
	}

	public void setState(PathState state) {
		this.state = state;
		
	}

	public void update(long gameTime) {
		//New frame
		if (gameTime > frameTicker + framePeriod) {
			frameTicker = gameTime;
			// increment the frame
			currentFrame++;
			if (currentFrame >= frameNr) {
				currentFrame = 0;
			}
		}
		// define the rectangle to cut out sprite
		this.sourceRect.left = currentFrame * spriteWidth;
		this.sourceRect.right = this.sourceRect.left + spriteWidth;
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
