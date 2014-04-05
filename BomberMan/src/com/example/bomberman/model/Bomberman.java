package com.example.bomberman.model;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.example.bomberman.MainGamePanel;
import com.example.bomberman.R;
import com.example.bomberman.model.components.Speed;

/**
 * This is a test droid that is dragged, dropped, moved, smashed against
 * the wall and done other terrible things with.
 * Wait till it gets a weapon!
 * 
 * @author impaler
 *
 */
public class Bomberman implements IDrawable { 

	MainGamePanel panel;
	public int iArena;
	public int jArena; //this player's coordinates in the arena matrix
	
	
	private Bitmap bitmapRight;	// the actual bitmap (or the animation sequence)
	private Bitmap bitmapLeft;
	private int x;			// the X coordinate (top left of the image)
	private int y;			// the Y coordinate (top left of the image)
	private Speed speed;	// the speed with its directions
	
	private static final String TAG = Bomberman.class.getSimpleName();
	private Rect sourceRect; // the rectangle to be drawn from the animation bitmap
	private int frameNr = 5; // number of frames in animation
	private int currentFrame; // the current frame
	private long frameTicker; // the time of the last frame update

	private int fps = 5; //da animacao, n eh do jogo
	private int framePeriod = 1000 / fps; // milliseconds between each frame (1000/fps)
	private int spriteWidth; // the width of the sprite to calculate the cut out rectangle
	private int spriteHeight;   // the height of the sprite
	
	public Bomberman (Resources resources, int x, int y, MainGamePanel panel, int i, int j) {
		iArena = i;
		jArena = j;
		this.panel = panel;
		this.bitmapRight = BitmapFactory.decodeResource(resources, R.drawable.walking_right);
		this.bitmapLeft = BitmapFactory.decodeResource(resources, R.drawable.walking_left);
		this.x = x;
		this.y = y;
		this.speed = new Speed();
		speed.stayStill();
		currentFrame = 0;
		spriteWidth = bitmapRight.getWidth() / frameNr;
		spriteHeight = bitmapRight.getHeight();
		sourceRect = new Rect(0, 0, spriteWidth, spriteHeight);
		frameTicker = 0l;
	}
	//for collision checks
	public int getWidth(){
		return spriteWidth;
	}
	//for collision checks
	public int getHeight(){
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
	
	public Speed getSpeed() {
		return speed;
	}

	public void setSpeed(Speed speed) {
		this.speed = speed;
	}
	
	public boolean isMoving(){
		return speed.isNotZero();
	}

	/**
	 * Method which updates the bomberman's internal state every tick
	 */
	public void update(long gameTime) {
		// check collision with right wall if heading right
		if (speed.getxDirection() == Speed.DIRECTION_RIGHT
				&& getX() + getWidth() >= panel.getWidth()) {
			speed.toggleXDirection();
		}
		// check collision with left wall if heading left
		if (speed.getxDirection() == Speed.DIRECTION_LEFT
				&& getX() <= 0) {
			speed.toggleXDirection();
		}
		// check collision with bottom wall if heading down
		if (speed.getyDirection() == Speed.DIRECTION_DOWN
				&& getY() + getHeight() >= panel.getHeight()) {
			speed.toggleYDirection();
		}
		// check collision with top wall if heading up
		if (speed.getyDirection() == Speed.DIRECTION_UP
				&& getY() <= 0) {
			speed.toggleYDirection();
		}
		
		//New positions
		x += (speed.getVelocity() * speed.getxDirection()); 
		y += (speed.getVelocity() * speed.getyDirection());
		
		//New frame
		if (isMoving() && gameTime > frameTicker + framePeriod) {
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
		Rect destRect = new Rect(getX(), getY(), getX() + spriteWidth, getY() + spriteHeight);
		// pega no bitmap, corta pelo sourceRect e coloca em destRect
		Bitmap bitmap = speed.getxDirection() == speed.DIRECTION_RIGHT ? bitmapRight : bitmapLeft;
		canvas.drawBitmap(bitmap, sourceRect, destRect, null);
//		canvas.drawBitmap(bitmap, 20, 150, null);
//		Paint paint = new Paint();
//		paint.setARGB(50, 0, 255, 0);
//		canvas.drawRect(20 + (currentFrame * destRect.width()), 150, 20 + (currentFrame * destRect.width()) + destRect.width(), 150 + destRect.height(),  paint);
	}
	
//	public void draw(Canvas canvas) {
//		canvas.drawBitmap(bitmap, x - (bitmap.getWidth() / 2), y - (bitmap.getHeight() / 2), null);
//	}
	
	/**
	 * Handles the {@link MotionEvent.ACTION_DOWN} event. If the event happens on the 
	 * bitmap surface then the touched state is set to <code>true</code> otherwise to <code>false</code>
	 * @param eventX - the event's X coordinate
	 * @param eventY - the event's Y coordinate
	 */
//	public void handleActionDown(int eventX, int eventY) {
//		if (eventX >= (x - bitmap.getWidth() / 2) && (eventX <= (x + bitmap.getWidth()/2))) {
//			if (eventY >= (y - bitmap.getHeight() / 2) && (y <= (y + bitmap.getHeight() / 2))) {
//				// droid touched
//				setTouched(true);
//			} else {
//				setTouched(false);
//			}
//		} else {
//			setTouched(false);
//		}
//
//	}
}
