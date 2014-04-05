package com.example.bomberman.model;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import com.example.bomberman.MainGamePanel;
import com.example.bomberman.R;
import com.example.bomberman.model.components.Speed;
import com.example.bomberman.util.GameMatrix;

/**
 * This is a test droid that is dragged, dropped, moved, smashed against
 * the wall and done other terrible things with.
 * Wait till it gets a weapon!
 * 
 * @author impaler
 *
 */
public class Bomberman { 

	MainGamePanel panel;
	protected int iArena;
	protected int jArena; //this player's coordinates in the arena matrix
	protected int initialX;
	protected int initialY; //these will be the base for collision calculations
	protected int myself; //serve para nao chocar com a sua propria posicao inicial
	
	protected Bitmap bitmapRight;	// the actual bitmap (or the animation sequence)
	protected Bitmap bitmapLeft;
	protected int x;			// the X coordinate (top left of the image)
	protected int y;			// the Y coordinate (top left of the image)
	protected Speed speed;	// the speed with its directions
	
	protected static final String TAG = Bomberman.class.getSimpleName();
	protected Rect sourceRect; // the rectangle to be drawn from the animation bitmap
	protected int frameNr = 5; // number of frames in animation
	protected int currentFrame; // the current frame
	protected long frameTicker; // the time of the last frame update

	protected int fps = 5; //da animacao, n eh do jogo
	protected int framePeriod = 1000 / fps; // milliseconds between each frame (1000/fps)
	protected int spriteWidth; // the width of the sprite to calculate the cut out rectangle
	protected int spriteHeight;   // the height of the sprite
	
	public Bomberman (Resources resources, int x, int y, MainGamePanel panel, int i, int j, int myself) {
		this.myself = myself;
		iArena = i;
		jArena = j;
		this.panel = panel;
		this.bitmapRight = BitmapFactory.decodeResource(resources, R.drawable.walking_right);
		this.bitmapLeft = BitmapFactory.decodeResource(resources, R.drawable.walking_left);
		initialX = x;
		initialY = y;
		this.x = x;
		this.y = y;
		this.speed = new Speed();
		//speed.stayStill();
		currentFrame = 0;
		spriteWidth = bitmapRight.getWidth() / frameNr;
		spriteHeight = bitmapRight.getHeight();
		sourceRect = new Rect(0, 0, spriteWidth, spriteHeight);
		frameTicker = 0l;
	}
	
	public Bomberman(){} //is needed so that Robot can extend this class
	
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

	//transforms pixel coordinates into matrix entries for the logic matrix
	//decisao sobre mapeamento depende da direccao em que me estou a mover
	public void checkAndResolveCollision(char[][] matrix){

		int i,j;
		int width = getWidth();
		int height = getHeight();
		//Log.d("COORDS", "x,y = "+x+","+y+", width,height ="+width+","+height);
		if(speed.getxDirection() == speed.DIRECTION_RIGHT){ 
			i=x/width;
			if(x%width != 0) i++;
			j=y/width;
			//teste de colisao
			if(matrix[j][i]!= '-' && matrix[j][i]!= myself){
				//eh preciso voltar a po-lo numa posicao sem colisao (ligeiramente atras)
				x = (x/width)*width; //divisao inteira!! nao se anulam as operacoes!!
				y = j*height;
				speed.toggleXDirection();
			}
		}
		else if(speed.getxDirection() == speed.DIRECTION_LEFT){ 
			i=x/width;
			j=y/width;
			//teste de colisao
			if(matrix[j][i]!= '-' && matrix[j][i]!= myself){
				//eh preciso voltar a po-lo numa posicao sem colisao (ligeiramente atras)
				x = (i+1)*width; 
				y = j*height;
				speed.toggleXDirection();
			}
		}
		else if(speed.getyDirection() == speed.DIRECTION_DOWN){ 
			i=x/height;
			j=y/height;
			if(y%height != 0) j++;
			//teste de colisao
			if(matrix[j][i]!= '-' && matrix[j][i]!= myself){
				//eh preciso voltar a po-lo numa posicao sem colisao (ligeiramente atras)
				x = (i)*width; 
				y = (y/height)*height;
				speed.toggleXDirection();
			}
		}
		else if(speed.getyDirection() == speed.DIRECTION_UP){
			i=x/height;
			j=y/height;
			if(matrix[j][i]!= '-' && matrix[j][i]!= myself){
				//eh preciso voltar a po-lo numa posicao sem colisao (ligeiramente atras)
				x = (i)*width; 
				y = (j+1)*height;
				speed.toggleXDirection();
			}
		}			

	}
	
	
	/**
	 * Method which updates the bomberman's internal state every tick
	 */
	public void update(long gameTime, GameMatrix gm) { //TODO check against the gameMatrix for collisions
		
		int i,j;	
		int[] result;
		// check collision with right wall if heading right
		if (speed.getxDirection() != Speed.STILL || speed.getyDirection() != Speed.STILL) {
			checkAndResolveCollision(gm.matrix);
				//Log.d("UPDATE", "i,j = "+i+","+j+", matrix[i,j] ="+gm.matrix[i][j]);
			
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
