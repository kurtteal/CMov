package com.example.bomberman.model;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import com.example.bomberman.MainGamePanel;
import com.example.bomberman.R;
import com.example.bomberman.model.components.Speed;
import com.example.bomberman.util.GameConfigs;

public class Bomberman { 
	
	MainGamePanel panel;
	GameConfigs gc;
	protected char myself; //serve para nao chocar com a sua propria posicao inicial
	private int numColumns;
	private int numLines;
	protected int movementMargin = 3;
	
	protected Bitmap bitmapRight;	// the actual bitmap (or the animation sequence)
	protected Bitmap bitmapLeft;
	protected int x;			// the X coordinate (top left of the image)
	protected int y;			// the Y coordinate (top left of the image)
	protected int xMapMargin;
	protected int yMapMargin;
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
	
	private boolean firstUpdate = true;
	private char nextMove = ' '; //players podem guardar o prox move para tornar o jogo mais responsivo 
	protected boolean justPlanted;
	protected int iBomb;
	protected int jBomb;
	
	public Bomberman (Resources resources, int x, int y, int xMargin, int yMargin, MainGamePanel panel, char myself, int numColumns, int numLines) {
		this.myself = myself;
		this.panel = panel;
		this.gc = panel.getArena().gc;
		this.bitmapRight = BitmapFactory.decodeResource(resources, R.drawable.walking_right);
		this.bitmapLeft = BitmapFactory.decodeResource(resources, R.drawable.walking_left);
		xMapMargin = xMargin;
		yMapMargin = yMargin;
		
		this.x = x;
		this.y = y;
		this.numColumns = numColumns;
		this.numLines = numLines;
		
		currentFrame = 0;
		spriteWidth = bitmapRight.getWidth() / frameNr;
		spriteHeight = bitmapRight.getHeight();
		sourceRect = new Rect(0, 0, spriteWidth, spriteHeight);
		frameTicker = 0l;
		this.speed = new Speed();
		
		justPlanted = false;
		iBomb = 0;
		jBomb = 0;
		// DEBUG if(myself != 'R'){ oneSquareDown(); oneSquareLeft();}
	}
	
	public Bomberman(){} //is needed so that Robot can extend this class
	
	//for collision checks
	public int getWidth(){
		//return spriteWidth;
		return panel.getWidth()/numColumns;
	}
	//for collision checks
	public int getHeight(){
		//return spriteHeight;
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
	
	public Speed getSpeed() {
		return speed;
	}

	public void setSpeed(Speed speed) {
		this.speed = speed;
	}
	
	public boolean isMoving(){
		return speed.isNotZero();
	}
	
	public void plantBomb(){
		int[] coords = getPositionInMatrix();
		int i = coords[1];
		int j = coords[0];
		panel.getArena().plantBomb(i, j, myself);
		justPlanted = true;
		iBomb = i;
		jBomb = j;
	}
	
	
	//PLAYER MOVEMENT BEHAVIOUR
	//Move one square to the left
	public void oneSquareLeft(){ 
		if(!isMoving() || speed.getxDirection() == Speed.DIRECTION_RIGHT){
			speed.goLeft(); 
			targetX = xMapMargin + getPositionInMatrix()[0]*getWidth() - getWidth(); 
		}
		else 
			nextMove = 'L';
	}

	public void oneSquareRight() {
		if (!isMoving() || speed.getxDirection() == Speed.DIRECTION_LEFT) {
			speed.goRight();
			targetX = xMapMargin + getPositionInMatrix()[0] * getWidth() + getWidth();
		}
		else
			nextMove = 'R';
	}

	public void oneSquareUp() {
		//se tiver parado ou a ir na direcao oposta muda de direcao
		if (!isMoving() || speed.getyDirection() == Speed.DIRECTION_DOWN) {
			speed.goUp();
			targetY = yMapMargin + getPositionInMatrix()[1] * getHeight() - getHeight();
		}
		else	
			nextMove = 'U';
	}
	
	public void oneSquareDown(){ 
		if (!isMoving() || speed.getyDirection() == Speed.DIRECTION_UP) {
			speed.goDown();
			targetY = yMapMargin + getPositionInMatrix()[1] * getHeight() + getHeight();
			//Log.d("GODOWN", "targetY,y,newPos = " + targetY + "," + y + getPositionInMatrix()[1] * getHeight());
		}
		else
			nextMove = 'D';
	}
	
	public char getId(){
		return myself;
	}

	public void die(){
		//Avisar a arena que este elemento esta fora de jogo
		panel.getArena().elementHasDied(this);
	}
	
	//transforms pixel coordinates into matrix entries for the logic matrix
	//decisao sobre mapeamento depende da direccao em que me estou a mover
	public void checkCollision(){

		boolean collision = false;
		int i,j;
		int width = getWidth();
		int height = getHeight();
		//Log.d("COORDS", "x,y = "+x+","+y+", width,height ="+width+","+height);
		
		//A maneira como se detecta e de como se corrige uma colisao depende
		//da direccao em que o boneco viaja. Nao ha colisoes entre players, ou
		//entre players e robots (ha deps a condicao de morte se estiver perto do robot)
		if(speed.getxDirection() == Speed.DIRECTION_RIGHT){ 
			i=(x-xMapMargin)/width;
			if((x-xMapMargin)%width != 0) i++;
			j=(y-yMapMargin)/height;
			//Log.d("UPDATE", "i,j = "+i+","+j+", matrix[i,j] ="+gm.matrix[i][j]);
			//teste de colisao: se vai contra blocos, ou se player chegou ao target
			char block = gc.readLogicPosition(j, i);
//			if(myself == '1') //PROBLEMA ESTA A ESCREVER 1 POR CIMA DO B TODO
//				Log.d("BLOCK", "block = " + block);
			int[] coords = getPositionInMatrix();
			int iCurrent = coords[1];
			int jCurrent = coords[0];
			if(block == 'B' && justPlanted && iBomb == iCurrent && jBomb == jCurrent){ 
				//Se eu acabei de por a bomba e estou em cima dela, nao ha colisao
			}
			else if(block == 'O' || block == 'W' || block == 'B'){
				//eh preciso voltar a po-lo numa posicao sem colisao (ligeiramente atras)
				x = xMapMargin + ((x-xMapMargin)/width)*width; //divisao inteira!! nao se anulam as operacoes!!
				y = yMapMargin + j*height;
				collision = true;
			}
		}
		else if(speed.getxDirection() == Speed.DIRECTION_LEFT){ 
			i=(x-xMapMargin)/width;
			j=(y-yMapMargin)/height;
			//teste de colisao: se vai contra blocos, ou se player chegou ao target
			char block = gc.readLogicPosition(j, i);
			int[] coords = getPositionInMatrix();
			int iCurrent = coords[1];
			int jCurrent = coords[0];
			if(block == 'B' && justPlanted && iBomb == iCurrent && jBomb == jCurrent){ 
				//Se eu acabei de por a bomba e estou em cima dela, nao ha colisao
			}
			else if(block == 'O' || block == 'W' || block == 'B'){
				//eh preciso voltar a po-lo numa posicao sem colisao (ligeiramente atras)
				x = xMapMargin + (i+1)*width; 
				y = yMapMargin + j*height;
				collision = true;
			}
		}
		else if(speed.getyDirection() == Speed.DIRECTION_DOWN){ 
			i=(x-xMapMargin)/width;
			j=(y-yMapMargin)/height;
			if((y-yMapMargin)%height != 0) j++;
			//teste de colisao: se vai contra blocos, ou se player chegou ao target
			char block = gc.readLogicPosition(j, i);
			int[] coords = getPositionInMatrix();
			int iCurrent = coords[1];
			int jCurrent = coords[0];
			if(block == 'B' && justPlanted && iBomb == iCurrent && jBomb == jCurrent){ 
				//Se eu acabei de por a bomba e estou em cima dela, nao ha colisao
			}
			else if(block == 'O' || block == 'W' || block == 'B'){
				//eh preciso voltar a po-lo numa posicao sem colisao (ligeiramente atras)
				x = xMapMargin + (i)*width; 
				y = yMapMargin + ((y-yMapMargin)/height)*height;
				collision = true;
			}
		}
		else if(speed.getyDirection() == Speed.DIRECTION_UP){
			i=(x-xMapMargin)/width;
			j=(y-yMapMargin)/height;
			//teste de colisao: se vai contra blocos, ou se player chegou ao target
			char block = gc.readLogicPosition(j, i);
			int[] coords = getPositionInMatrix();
			int iCurrent = coords[1];
			int jCurrent = coords[0];
			if(block == 'B' && justPlanted && iBomb == iCurrent && jBomb == jCurrent){ 
				//Se eu acabei de por a bomba e estou em cima dela, nao ha colisao
			}
			else if(block == 'O' || block == 'W' || block == 'B'){
				//eh preciso voltar a po-lo numa posicao sem colisao (ligeiramente atras)
				x = xMapMargin + (i)*width; 
				y = yMapMargin + (j+1)*height;
				collision = true;
			}
		}	
		if(collision)
			solveCollision(); //robots solve it differently (they override this)
	}
	
	//Os players simplesmente nao avancam
	public void solveCollision(){
		//speed.toggleCurrentDirection();
		speed.stayStill();
		targetX = 0;
		targetY = 0; 
	}
	
	//Se chegou aqui eh porque ja fez deteccao de colisoes
	//Converte coordenadas de pixeis, em coords da matriz logica, nao para colisao,
	//mas para obter a posicao actual na matriz de estados
	//x,y -> j,i (porque j sao linhas e i colunas)
	public int[] getPositionInMatrix(){
		int[] resultado = new int[2];

		//Log.d("getPosMat", "x, x/getWidth() = " + x + x/getWidth());
		resultado[0]=(x-xMapMargin)/getWidth();
		if((x-xMapMargin)%getWidth() >= getWidth()/2) resultado[0]++;
		resultado[1]=(y-yMapMargin)/getHeight();
		if((y-yMapMargin)%getHeight() >= getHeight()/2) resultado[1]++;
			
		return resultado;
	}
	
	//Verifica se existe um nextMove para fazer
	private void checkIfNextMove(){
		if(nextMove != ' '){
			switch(nextMove){
				case 'U':
					oneSquareUp();
					break;
				case 'D':
					oneSquareDown();
					break;
				case 'L':
					oneSquareLeft();
					break;
				case 'R':
					oneSquareRight();
					break;
			}
			nextMove = ' ';
		}
	}
	
	//If player is close to targetX or Y he moves automatically there
	//to avoid going past the target and having to come back due to sparse
	//updates
	protected void updatePixelPosition(){
		if( myself!='R'){ //New positions (pixels) for players
			if(targetX != 0 && Math.abs(targetX - x) < movementMargin){ //se chegou ao destino
				x = targetX;
				targetX = 0;
				speed.setXStationary();
				checkIfNextMove();
				//Quando chego a um novo bloco vou ver se tinha plantado uma bomba no anterior
				//Log.d("BOMBCOLISION", "iJustPlanted = " + iJustPlanted);
				checkIfPlanted();
			}
			else if(targetY != 0 && Math.abs(targetY - y) < movementMargin){
				y = targetY; 
				targetY = 0;
				speed.setYStationary();
				checkIfNextMove();
				//Quando chego a um novo bloco vou ver se tinha plantado uma bomba no anterior
				checkIfPlanted();
			}
			else{
				x += (speed.getVelocity() * speed.getxDirection()); 
				y += (speed.getVelocity() * speed.getyDirection());
			}
		}
	}
	
	//Se pus uma bomba num bloco que nao o actual, devo poder "passar por cima" da bomba que
	//acabei de por, ate chegar a um novo bloco. Quando chegar ao novo bloco volto a "ligar"
	//o controlo de colisoes.
	protected void checkIfPlanted(){
		int[] coords = getPositionInMatrix();
		if(justPlanted && (coords[1] != iBomb || coords[0] != jBomb)){
			justPlanted = false;
			iBomb = 0;
			jBomb = 0;
		}
	}
	
	//Verifica a sua posicao na matrix logica
	protected void checkPositionChange(){
		//Get old coordinate positions from pixel positions
		int[] oldPositions = getPositionInMatrix();
		
		//Update pixel positions
		updatePixelPosition();
		
		//Calculate new coordinate positions from pixel positions
		int[] newPositions = getPositionInMatrix();
		
		//Se mudou de coords, significa que abandonou o bloco antigo
		if(oldPositions[0] != newPositions[0] || oldPositions[1] != newPositions[1] ){
			gc.writeOverlayPosition(oldPositions[1], oldPositions[0], '-'); //antigo bloco agora eh chao
			//se a nova posicao eh 1 explosao, morre
			if(gc.readLogicPosition(newPositions[1], newPositions[0]) == 'E'){
				die();
			}
			else{ //o novo bloco agora contem o proprio
				gc.writeOverlayPosition(newPositions[1], newPositions[0], myself); 
			}
		}
		
	}
	
	//Method that expands the bitmaps
	private void expandBitmaps(){
		//expansao do bitmap para ocupar td o mapa
		//Log.d("ONUPDATE", "panelWidth, newWidth = " + panel.getWidth() + "," + frameNr*panel.getWidth()/19);
		bitmapRight = Bitmap.createScaledBitmap(bitmapRight, frameNr*panel.getWidth()/numColumns, panel.getHeight()/numLines, false);
		bitmapLeft = Bitmap.createScaledBitmap(bitmapLeft, frameNr*panel.getWidth()/numColumns, panel.getHeight()/numLines, false);
		spriteWidth = bitmapRight.getWidth() / frameNr;
		spriteHeight = bitmapRight.getHeight();
		//Log.d("SPRITEDIM", "spriteWidth, bitmapRight_width = " + spriteWidth + bitmapRight.getWidth());
		sourceRect = new Rect(0, 0, spriteWidth, spriteHeight);
	}
	
	/**
	 * Method which updates the bomberman's internal state every tick
	 */
	public void update(long gameTime) {
		
		if(firstUpdate){
			expandBitmaps();
			firstUpdate = false;
		}
		if (isMoving()) {
			// check collision and resolve the colision
			checkCollision();
				
			// update the gm matrix with 'floor' if left and 'myself' if arrived at new block
			checkPositionChange();
			
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
		//ver se alguma explosao nova o atinge (mesmo que esteja parado)
		int[] currentPos = getPositionInMatrix();
		int j = currentPos[0];
		int i = currentPos[1];
		if(gc.readLogicPosition(i,j)=='E')
			die();
		
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
