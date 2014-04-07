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
import com.example.bomberman.util.GameConfigs;

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
	protected int initialX;
	protected int initialY; //these will be the base for collision calculations
	protected char myself; //serve para nao chocar com a sua propria posicao inicial
	
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
	
	protected int targetX;
	protected int targetY; //the players will move square by square
	
	public Bomberman (Resources resources, int x, int y, MainGamePanel panel, char myself) {
		this.myself = myself;
		this.panel = panel;
		this.bitmapRight = BitmapFactory.decodeResource(resources, R.drawable.walking_right);
		this.bitmapLeft = BitmapFactory.decodeResource(resources, R.drawable.walking_left);
		initialX = x; //possivelmente para a expansao do mapa pelo ecra inteiro
		initialY = y;
		this.x = x;
		this.y = y;
		
		currentFrame = 0;
		spriteWidth = bitmapRight.getWidth() / frameNr;
		spriteHeight = bitmapRight.getHeight();
		sourceRect = new Rect(0, 0, spriteWidth, spriteHeight);
		frameTicker = 0l;
		this.speed = new Speed();
		if(myself != 'R'){ oneSquareDown(); oneSquareLeft();}
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
	
	//PLAYER BEHAVIOUR
	//Move one square to the left
	public void oneSquareLeft(){ 
		if(!isMoving()){
			speed.goLeft(); 
			targetX = getPositionInMatrix()[0]*getWidth() - getWidth(); 
		}
	}

	public void oneSquareRight() {
		if (!isMoving()) {
			speed.goRight();
			targetX = getPositionInMatrix()[0] * getWidth() + getWidth();
		}
	}

	public void oneSquareUp() {
		if (!isMoving()) {
			speed.goUp();
			targetY = getPositionInMatrix()[1] * getHeight() - getHeight();
		}
	}
	public void oneSquareDown(){ 
		if (!isMoving()) {
			speed.goDown();
			targetY = getPositionInMatrix()[1] * getHeight() + getHeight();
			//Log.d("GODOWN", "targetY,y,newPos = " + targetY + "," + y + getPositionInMatrix()[1] * getHeight());
		}
	}
	
	public char getId(){
		return myself;
	}

	public void die(){
		//Avisar a arena que este elemento está fora de jogo
		panel.getArena().elementHasDied(this);
	}
	
	//transforms pixel coordinates into matrix entries for the logic matrix
	//decisao sobre mapeamento depende da direccao em que me estou a mover
	public void checkCollision(char[][] matrix){

		boolean collision = false;
		int i,j;
		int width = getWidth();
		int height = getHeight();
		//Log.d("COORDS", "x,y = "+x+","+y+", width,height ="+width+","+height);
		
		//A maneira como se detecta e de como se corrige uma colisao depende
		//da direccao em que o boneco viaja. Nao ha colisoes entre players, ou
		//entre players e robots (ha deps a condiçao de morte se estiver perto do robot)
		if(speed.getxDirection() == Speed.DIRECTION_RIGHT){ 
			i=x/width;
			if(x%width != 0) i++;
			j=y/height;
			//Log.d("UPDATE", "i,j = "+i+","+j+", matrix[i,j] ="+gm.matrix[i][j]);
			//teste de colisao: se vai contra blocos, ou se player chegou ao target
			if(matrix[j][i]== 'O' || matrix[j][i]== 'W' || matrix[j][i]== 'B'){
				//eh preciso voltar a po-lo numa posicao sem colisao (ligeiramente atras)
				x = (x/width)*width; //divisao inteira!! nao se anulam as operacoes!!
				y = j*height;
				collision = true;
			}
		}
		else if(speed.getxDirection() == Speed.DIRECTION_LEFT){ 
			i=x/width;
			j=y/height;
			//teste de colisao: se vai contra blocos, ou se player chegou ao target
			if(matrix[j][i]== 'O' || matrix[j][i]== 'W' || matrix[j][i]== 'B'){
				//eh preciso voltar a po-lo numa posicao sem colisao (ligeiramente atras)
				x = (i+1)*width; 
				y = j*height;
				collision = true;
			}
		}
		else if(speed.getyDirection() == Speed.DIRECTION_DOWN){ 
			i=x/width;
			j=y/height;
			if(y%height != 0) j++;
			//teste de colisao: se vai contra blocos, ou se player chegou ao target
			if(matrix[j][i]== 'O' || matrix[j][i]== 'W' || matrix[j][i]== 'B'){
				//eh preciso voltar a po-lo numa posicao sem colisao (ligeiramente atras)
				x = (i)*width; 
				y = (y/height)*height;
				collision = true;
			}
		}
		else if(speed.getyDirection() == Speed.DIRECTION_UP){
			i=x/width;
			j=y/height;
			//teste de colisao: se vai contra blocos, ou se player chegou ao target
			if(matrix[j][i]== 'O' || matrix[j][i]== 'W' || matrix[j][i]== 'B'){
				//eh preciso voltar a po-lo numa posicao sem colisao (ligeiramente atras)
				x = (i)*width; 
				y = (j+1)*height;
				collision = true;
			}
		}	
		if(collision)
			solveCollision(matrix); //robots solve it differently (they override this)
	}
	
	//Os players simplesmente nao avancam
	public void solveCollision(char[][] matrix){
		//speed.toggleCurrentDirection();
		speed.stayStill();
		targetX = 0;
		targetY = 0; 
	}
	
	//Se chegou aqui é porque ja fez deteccao de colisoes
	//Converte coordenadas de pixeis, em coords da matriz logica, nao para colisao,
	//mas para obter a posiçao actual
	protected int[] getPositionInMatrix(){
		int[] resultado = new int[2];

		resultado[0]=x/getWidth();
		if(x%getWidth() >= getWidth()/2) resultado[0]++;
		resultado[1]=y/getHeight();
		if(y%getHeight() >= getHeight()/2) resultado[1]++;
			
		return resultado;
	}
	
	//if player is close to targetX or Y he moves automatically there
	//to avoid going past the target and having to come back due to spare
	//updates
	protected void updatePosition(char[][] matrix){
		if( myself!='R'){ //New positions (pixels) for players
			if(targetX != 0 && Math.abs(targetX - x) < 3){
				x = targetX;
				targetX = 0;
				speed.setXStationary();
			}
			else if(targetY != 0 && Math.abs(targetY - y) < 3){
				y = targetY; 
				targetY = 0;
				speed.setYStationary();
			}
			else{
				x += (speed.getVelocity() * speed.getxDirection()); 
				y += (speed.getVelocity() * speed.getyDirection());
			}
		}
	}
	
	//Verifica a sua posicao na matrix logica
	private void checkPositionChange(char[][] matrix){
		//Get old coordinate positions
		int[] oldPositions = getPositionInMatrix();
		

		updatePosition(matrix);
		
		//Calculate new positions (coordenates)
		int[] newPositions = getPositionInMatrix();
		//Se mudou de coords, significa que abandonou o bloco antigo
		if(oldPositions[0] != newPositions[0] || oldPositions[1] != newPositions[1] ){
			matrix[oldPositions[1]][oldPositions[0]] = '-'; //antigo bloco agora eh chao
			//se a nova posicao eh 1 explosao, morre
			if(matrix[newPositions[1]][newPositions[0]] == 'E'){
				die();
			}
			else{ //o novo bloco agora contem o proprio
				matrix[newPositions[1]][newPositions[0]] = myself; 
			}
		}
		
	}
	
	/**
	 * Method which updates the bomberman's internal state every tick
	 */
	public void update(long gameTime, GameConfigs gm) {
		
		if (isMoving()) {
			// check collision and resolve the colision
			checkCollision(gm.matrix);
				
			// update the gm matrix with 'floor' if left and 'myself' if arrived at new block
			checkPositionChange(gm.matrix);
			
//			x += (speed.getVelocity() * speed.getxDirection()); 
//			y += (speed.getVelocity() * speed.getyDirection());
			
			//Prepare new frame
			if (gameTime > frameTicker + framePeriod) {
				frameTicker = gameTime;
				// increment the frame
				currentFrame++;
				if (currentFrame >= frameNr) {
					currentFrame = 0;
				}
			}
			// define the rectangle to cut out sprite
			sourceRect.left = currentFrame * spriteWidth;
			sourceRect.right = sourceRect.left + spriteWidth;
		}
		else{ //se esta parado, ver se alguma explosao nova o atinge
			int[] currentPos = getPositionInMatrix();
			int x = currentPos[0];
			int y = currentPos[1];
			if(gm.matrix[y][x]=='E')
				die();
		}
	}

	// the draw method which draws the corresponding frame
	public void draw(Canvas canvas) {
		// where to draw the sprite
		Rect destRect = new Rect(getX(), getY(), getX() + spriteWidth, getY() + spriteHeight);
		// pega no bitmap, corta pelo sourceRect e coloca em destRect
		Bitmap bitmap = speed.getxDirection() == Speed.DIRECTION_RIGHT ? bitmapRight : bitmapLeft;
		canvas.drawBitmap(bitmap, sourceRect, destRect, null);
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
