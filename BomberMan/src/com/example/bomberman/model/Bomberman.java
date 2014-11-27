package com.example.bomberman.model;

import java.util.ArrayList;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import com.example.bomberman.GamePanel;
import com.example.bomberman.R;
import com.example.bomberman.model.components.Speed;
import com.example.bomberman.util.GameConfigs;

public class Bomberman { 
	
	GamePanel panel;
	GameConfigs gc;
	protected char myself; //serve para nao chocar com a sua propria posicao inicial
	private int numColumns;
	private int numLines;
	protected double movementMargin = 3;
	
	protected Bitmap bitmapUp; // the actual bitmap (or the animation sequence)
	protected Bitmap bitmapDown;
	protected Bitmap bitmapLeft;
	protected Bitmap bitmapRight;
	protected int x;			// the X coordinate (top left of the image)
	protected int y;			// the Y coordinate (top left of the image)
	protected int xMapMargin;
	protected int yMapMargin;
	public int i;	//overlay matrix coordinates
	public int j; //visivel na gameActivity
	protected Speed speed;	// the speed with its directions
	
	protected static final String TAG = Bomberman.class.getSimpleName();
	protected Rect sourceRect; // the rectangle to be drawn from the animation bitmap
	protected int frameNr = 3; // number of frames in animation
	protected int currentFrame; // the current frame
	protected long frameTicker; // the time of the last frame update

	protected int fps = 6; //da animacao, n eh do jogo
	protected int framePeriod = 1000 / fps; // milliseconds between each frame (1000/fps)
	protected int spriteWidth; // the width of the sprite to calculate the cut out rectangle
	protected int spriteHeight;   // the height of the sprite
	
	protected int targetX;
	protected int targetY; //the players will move square by square
	
	private boolean firstUpdate = true;
	private boolean isPaused = false;
	private ArrayList<Character> playersKilled;
	protected char nextMove = ' '; //players podem guardar o prox move para tornar o jogo mais responsivo 
	protected boolean justPlanted;
	protected int iBomb;
	protected int jBomb;
	
	public Bomberman (Resources resources, int x, int y, int xMargin, int yMargin, GamePanel panel, char myself, int numColumns, int numLines) {
		this.myself = myself;
		this.panel = panel;
		this.gc = panel.getArena().gc;
		if(myself == '1'){
			this.bitmapUp = BitmapFactory.decodeResource(resources, R.drawable.white_back);
			this.bitmapDown = BitmapFactory.decodeResource(resources, R.drawable.white_front);
			this.bitmapLeft = BitmapFactory.decodeResource(resources, R.drawable.white_left);
			this.bitmapRight = BitmapFactory.decodeResource(resources, R.drawable.white_right);
		}else if(myself == '2'){
			this.bitmapUp = BitmapFactory.decodeResource(resources, R.drawable.blue_back);
			this.bitmapDown = BitmapFactory.decodeResource(resources, R.drawable.blue_front);
			this.bitmapLeft = BitmapFactory.decodeResource(resources, R.drawable.blue_left);
			this.bitmapRight = BitmapFactory.decodeResource(resources, R.drawable.blue_right);
		}else if(myself == '3'){
			this.bitmapUp = BitmapFactory.decodeResource(resources, R.drawable.red_back);
			this.bitmapDown = BitmapFactory.decodeResource(resources, R.drawable.red_front);
			this.bitmapLeft = BitmapFactory.decodeResource(resources, R.drawable.red_left);
			this.bitmapRight = BitmapFactory.decodeResource(resources, R.drawable.red_right);
		}else{
			this.bitmapUp = BitmapFactory.decodeResource(resources, R.drawable.green_back);
			this.bitmapDown = BitmapFactory.decodeResource(resources, R.drawable.green_front);
			this.bitmapLeft = BitmapFactory.decodeResource(resources, R.drawable.green_left);
			this.bitmapRight = BitmapFactory.decodeResource(resources, R.drawable.green_right);
		}
		xMapMargin = xMargin;
		yMapMargin = yMargin;
		
		this.x = x;
		this.y = y;
		
		this.numColumns = numColumns;
		this.numLines = numLines;
		
		currentFrame = 0;
		playersKilled = new ArrayList<Character>();
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
	
	public String getPlayerId(){
		return myself+"";
	}
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
	
	public boolean getIsPaused(){
		return this.isPaused;
	}
	
	public void setIsPaused(boolean state){
		this.isPaused = state;
	}
	
	public boolean getIfKilledPlayer(char playerId){
		Log.d("MENU ACT", "O PLAYERS KILLED ESTA " + playersKilled);
		Log.d("MENU ACT", "O PLAYER ID E " + playerId);
		return this.playersKilled.contains(playerId);
	}
	
	public void addPlayerKilled(char playerId){
		this.playersKilled.add(playerId);
	}
	
	public char getMyself(){
		return this.myself;
	}
	
	public void setInitialPosition(int i, int j){
		this.x = xMapMargin + j * getWidth();
		this.y = yMapMargin + i * getHeight();
	}
	
	//Receives the sent coordinates, and updates the position
	//(in pixel map: x, y) to the sent values, the overlay and the i, j
	protected void correctThePosition(int newI, int newJ){
		x = newJ*getWidth() + xMapMargin;
		y = newI*getHeight() + yMapMargin;
		gc.writeOverlayPosition(i, j, '-');
		gc.writeOverlayPosition(newI, newJ, myself);
		i = newI;
		j = newJ;
	}
	
	public void plantBomb(String iPos, String jPos){

		if(iPos != null && jPos != null){
			int newI = Integer.parseInt(iPos);
			int newJ = Integer.parseInt(jPos);
			if(newI != i || newJ != j){ //there was lag, and we need to set new positions
				correctThePosition(newI, newJ);
				speed.stayStill();
			}
		}

		panel.getArena().plantBomb(i, j, myself);
		justPlanted = true;
		iBomb = i;
		jBomb = j;
	}
	
	
	//PLAYER MOVEMENT BEHAVIOUR
	//Move one square to the left
	public void oneSquareLeft(String iPos, String jPos){
		if(iPos != null && jPos != null){
			int iSent = Integer.parseInt(iPos);
			int jSent = Integer.parseInt(jPos);
			if(iSent != i || jSent != j){ //there was lag, and we need to set new positions 
				correctThePosition(iSent, jSent);
				speed.stayStill();
			}
		}
		if(!isMoving() || speed.getxDirection() == Speed.DIRECTION_RIGHT){
			speed.goLeft(); 
			targetX = xMapMargin + j*getWidth() - getWidth(); 
		}
		else
			nextMove = 'L';
	}

	public void oneSquareRight(String iPos, String jPos){
		if(iPos != null && jPos != null){
			int iSent = Integer.parseInt(iPos);
			int jSent = Integer.parseInt(jPos);
			if(iSent != i || jSent != j){ //there was lag, and we need to set new positions 
				correctThePosition(iSent, jSent);
				speed.stayStill();
			}
		}
		if (!isMoving() || speed.getxDirection() == Speed.DIRECTION_LEFT) {
			speed.goRight();
			targetX = xMapMargin + j * getWidth() + getWidth();
			//Log.d("Myself targetx x:",myself +" " + targetX+ " "+x);
		}
		else
			nextMove = 'R';
	}

	public void oneSquareUp(String iPos, String jPos){
		if(iPos != null && jPos != null){
			int iSent = Integer.parseInt(iPos);
			int jSent = Integer.parseInt(jPos);
			if(iSent != i || jSent != j){ //there was lag, and we need to set new positions 
				correctThePosition(iSent, jSent);
				speed.stayStill();
			}
		}
		//se tiver parado ou a ir na direcao oposta muda de direcao
		if (!isMoving() || speed.getyDirection() == Speed.DIRECTION_DOWN) {
			speed.goUp();
			targetY = yMapMargin + i * getHeight() - getHeight();
		}
		else
			nextMove = 'U';
	}
	
	public void oneSquareDown(String iPos, String jPos){
		if(iPos != null && jPos != null){
			int iSent = Integer.parseInt(iPos);
			int jSent = Integer.parseInt(jPos);
			if(iSent != i || jSent != j){ //there was lag, and we need to set new positions 
				correctThePosition(iSent, jSent);
				speed.stayStill();
			}
		} 
		if (!isMoving() || speed.getyDirection() == Speed.DIRECTION_UP) {
			speed.goDown();
			targetY = yMapMargin + i * getHeight() + getHeight();
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
		int collisionI, collisionJ;
		int width = getWidth();
		int height = getHeight();
		//Log.d("COORDS", "x,y = "+x+","+y+", width,height ="+width+","+height);
		
		//A maneira como se detecta e de como se corrige uma colisao depende
		//da direccao em que o boneco viaja. Nao ha colisoes entre players, ou
		//entre players e robots (ha deps a condicao de morte se estiver perto do robot)
		if(speed.getxDirection() == Speed.DIRECTION_RIGHT){ 
			collisionJ=(x-xMapMargin)/width;
			if((x-xMapMargin)%width != 0) collisionJ++;
			collisionI=(y-yMapMargin)/height;
			//Log.d("UPDATE", "i,j = "+i+","+j+", matrix[i,j] ="+gm.matrix[i][j]);
			//teste de colisao: se vai contra blocos, ou se player chegou ao target
			char block = gc.readLogicPosition(collisionI, collisionJ);
//			if(myself == '1') //PROBLEMA ESTA A ESCREVER 1 POR CIMA DO B TODO
//				Log.d("BLOCK", "block = " + block);
//			int[] coords = getPositionInMatrix();
//			int iCurrent = coords[1];
//			int jCurrent = coords[0];
			if(block == 'B' && justPlanted && iBomb == i && jBomb == j){ 
				//Se eu acabei de por a bomba e estou em cima dela, nao ha colisao
			}
			else if(block == 'O' || block == 'W' || block == 'B'){
				//eh preciso voltar a po-lo numa posicao sem colisao (ligeiramente atras)
				x = xMapMargin + ((x-xMapMargin)/width)*width; //divisao inteira!! nao se anulam as operacoes!!
				y = yMapMargin + collisionI*height;
				collision = true;
			}
		}
		else if(speed.getxDirection() == Speed.DIRECTION_LEFT){ 
			collisionJ=(x-xMapMargin)/width;
			collisionI=(y-yMapMargin)/height;
			//teste de colisao: se vai contra blocos, ou se player chegou ao target
			char block = gc.readLogicPosition(collisionI, collisionJ);
//			int[] coords = getPositionInMatrix();
//			int iCurrent = coords[1];
//			int jCurrent = coords[0];
			if(block == 'B' && justPlanted && iBomb == i && jBomb == j){ 
				//Se eu acabei de por a bomba e estou em cima dela, nao ha colisao
			}
			else if(block == 'O' || block == 'W' || block == 'B'){
				//eh preciso voltar a po-lo numa posicao sem colisao (ligeiramente atras)
				x = xMapMargin + (collisionJ+1)*width; 
				y = yMapMargin + collisionI*height;
				collision = true;
			}
		}
		else if(speed.getyDirection() == Speed.DIRECTION_DOWN){ 
			collisionJ=(x-xMapMargin)/width;
			collisionI=(y-yMapMargin)/height;
			if((y-yMapMargin)%height != 0) collisionI++;
			//teste de colisao: se vai contra blocos, ou se player chegou ao target
			char block = gc.readLogicPosition(collisionI, collisionJ);
//			int[] coords = getPositionInMatrix();
//			int iCurrent = coords[1];
//			int jCurrent = coords[0];
			if(block == 'B' && justPlanted && iBomb == i && jBomb == j){ 
				//Se eu acabei de por a bomba e estou em cima dela, nao ha colisao
			}
			else if(block == 'O' || block == 'W' || block == 'B'){
				//eh preciso voltar a po-lo numa posicao sem colisao (ligeiramente atras)
				x = xMapMargin + (collisionJ)*width; 
				y = yMapMargin + ((y-yMapMargin)/height)*height;
				collision = true;
			}
		}
		else if(speed.getyDirection() == Speed.DIRECTION_UP){
			collisionJ=(x-xMapMargin)/width;
			collisionI=(y-yMapMargin)/height;
			//teste de colisao: se vai contra blocos, ou se player chegou ao target
			char block = gc.readLogicPosition(collisionI, collisionJ);
//			int[] coords = getPositionInMatrix();
//			int iCurrent = coords[1];
//			int jCurrent = coords[0];
			if(block == 'B' && justPlanted && iBomb == i && jBomb == j){ 
				//Se eu acabei de por a bomba e estou em cima dela, nao ha colisao
			}
			else if(block == 'O' || block == 'W' || block == 'B'){
				//eh preciso voltar a po-lo numa posicao sem colisao (ligeiramente atras)
				x = xMapMargin + (collisionJ)*width; 
				y = yMapMargin + (collisionI+1)*height;
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
//	public int[] getPositionInMatrix(){
//		int[] resultado = new int[2];
//
//		//Log.d("getPosMat", "x, x/getWidth() = " + x + x/getWidth());
//		resultado[0] = (x-xMapMargin)/getWidth();
//		if((x-xMapMargin)%getWidth() >= getWidth()/2) resultado[0]++;
//		resultado[1] = (y-yMapMargin)/getHeight();
//		if((y-yMapMargin)%getHeight() >= getHeight()/2) resultado[1]++;
//			
//		return resultado;
//	}
	
	//Converte coordenadas de pixeis, em coords da matriz logica, nao para colisao,
	//mas para obter a posicao actual na matriz de estados
	//x,y -> j,i (porque j sao linhas e i colunas)
	//se tiver entre 2 blocos, conta o bloco mais proximo
	public void updateMatrixCoordinates(){
		i = (y-yMapMargin)/getHeight();
		if((y-yMapMargin)%getHeight() >= getHeight()/2) i++;
		j = (x-xMapMargin)/getWidth();
		if((x-xMapMargin)%getWidth() >= getWidth()/2) j++;
	}
	
	//Verifica se existe um nextMove para fazer
	protected boolean checkIfNextMove(){
		if(nextMove != ' '){
			switch(nextMove){
				case 'U':
					oneSquareUp(null, null);
					break;
				case 'D':
					oneSquareDown(null, null);
					break;
				case 'L':
					oneSquareLeft(null, null);
					break;
				case 'R':
					oneSquareRight(null, null);
					break;
			}
			nextMove = ' ';
			return true;
		}
		return false;
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
		//int[] coords = getPositionInMatrix();
		if(justPlanted && (i != iBomb || j != jBomb)){
			justPlanted = false;
			iBomb = 0;
			jBomb = 0;
		}
	}
	
	//Verifica a sua posicao na matrix logica
	protected void checkPositionChange(){
		//Get old coordinate positions from pixel positions
		int oldI = i;
		int oldJ = j;
		
		//Update pixel positions
		updatePixelPosition(); //x,y
		updateMatrixCoordinates(); //i,j
		
		//Se mudou de coords, significa que abandonou o bloco antigo
		if(oldJ != j || oldI != i ){
			gc.writeOverlayPosition(oldI, oldJ, '-'); //antigo bloco agora eh chao
			//se a nova posicao eh 1 explosao, morre
			if(gc.readLogicPosition(i, j) == 'E'){
				gc.writeOverlayPosition(i, j, '-');
				die();
			}
			//cc, o novo bloco agora contem o proprio
			else{ 
				if(myself != 'R')			// Para o robot nao reescrever a pos dos players na overlay matrix.
					gc.writeOverlayPosition(i, j, myself); 
			}
		}
		
	}
	
	//Method that expands the bitmaps
	private void expandBitmaps(){
		//expansao do bitmap para ocupar td o mapa
		//Log.d("ONUPDATE", "panelWidth, newWidth = " + panel.getWidth() + "," + frameNr*panel.getWidth()/19);
		bitmapUp = Bitmap.createScaledBitmap(bitmapUp, frameNr*panel.getWidth()/numColumns, panel.getHeight()/numLines, false);
		bitmapDown = Bitmap.createScaledBitmap(bitmapDown, frameNr*panel.getWidth()/numColumns, panel.getHeight()/numLines, false);
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
			updateMatrixCoordinates(); //inicializacao do i,j
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
//		int[] currentPos = getPositionInMatrix();
//		int j = currentPos[0];
//		int i = currentPos[1];
		if(gc.readLogicPosition(i,j)=='E'){
			gc.writeOverlayPosition(i,j,'-');
			die();
		}
		
	}

	// the draw method which draws the corresponding frame
	public void draw(Canvas canvas) {
		// where to draw the sprite
		Rect destRect = new Rect(getX(), getY(), getX() + spriteWidth, getY() + spriteHeight);
		// pega no bitmap, corta pelo sourceRect e coloca em destRect
		Bitmap bitmap = bitmapDown; //default virado para a frente
		if(speed.getxDirection() != 0){
			bitmap = speed.getxDirection() == Speed.DIRECTION_RIGHT ? bitmapRight : bitmapLeft;
		}else if(speed.getyDirection() == -1){ //ir para cima
			bitmap = bitmapUp;
		}
		canvas.drawBitmap(bitmap, sourceRect, destRect, null);
	}
	
}
