package com.example.bomberman.model;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.example.bomberman.MainGamePanel;
import com.example.bomberman.R;
import com.example.bomberman.util.GameConfigs;

/**
 * This is a test droid that is dragged, dropped, moved, smashed against the
 * wall and done other terrible things with. Wait till it gets a weapon!
 * 
 * @author impaler
 * 
 */
public class Path implements IDrawable{
	
	char bombOwner;
	
	private GameConfigs gc;
	public int iArena;
	public int jArena;
	private IDrawable[][] pixelMatrix; //for the explosions to update the other nearby objects
	private MainGamePanel panel;
	private int numColumns;
	private int numLines;
	
	private Bitmap bitmap; // the actual bitmap (or the animation sequence)
	private Bitmap floorBitmap;
	private Bitmap obstacleBitmap;
	private Bitmap bombBitmap;
	private Bitmap explosionBitmap;
	
	private int floorFrameNr = 1;
	private int obstacleFrameNr = 1;
	private int bombFrameNr = 2;
	private int explosionFrameNr = 4;
	
	private int x; // the X coordinate (top left of the image)
	private int y; // the Y coordinate (top left of the image)

	private Rect sourceRect; // the rectangle to be drawn from the animation
								// bitmap
	private int frameNr; // number of frames in animation

	private int currentFrame; // the current frame
	private long frameTicker; // the time of the last frame update
	private int fps = 5; //da animacao, n eh do jogo
	private int framePeriod = 1000 / fps; // milliseconds between each frame
	
	private int spriteWidth; // the width of the sprite to calculate the cut out
								// rectangle
	private int spriteHeight; // the height of the sprite
	
	private PathState state;
	private long bombInitialTime;
	private long explosionInitialTime;
	private int bombTime; //14 vem do ficheiro map escolhido (esta nos assets)
	private int explosionTime; // 7
	private int explosionRange; // range 2 
	
	private boolean firstUpdate = true; //These 2 attributes exist only because the bitmaps cant be
	private PathState initialState;		//expanded in the constructor, so its done on the first update instead
	
	//TODO bombas e explosoes precisam de ter o id (myself) do owner para atribuicao de pts

	public Path(Resources resources, int x, int y, PathState state, int i, int j, int numColumns, int numLines, MainGamePanel panel) {
		iArena = i;
		jArena = j; //Each component knows its coordinates on the gameMatrix
		this.pixelMatrix = panel.getArena().pixelMatrix;
		this.initialState = state;
		this.panel = panel;
		this.gc = panel.getArena().gc;
		this.numColumns = numColumns;
		this.numLines = numLines;
		floorBitmap = BitmapFactory.decodeResource(resources, R.drawable.floor);
		obstacleBitmap = BitmapFactory.decodeResource(resources, R.drawable.obstacle);
		bombBitmap = BitmapFactory.decodeResource(resources, R.drawable.bomb);
		explosionBitmap = BitmapFactory.decodeResource(resources, R.drawable.explosion);
		
		bombOwner = 'R'; //owner de alguma bomba que ja vinha desenhada no mapa inicial
		
		this.x = x;
		this.y = y;
		
		bombTime = panel.getArena().gc.explosionTimeout;
		explosionTime = panel.getArena().gc.explosionDuration;
		this.explosionRange = panel.getArena().gc.explosionRange;

		frameTicker = 0l;
	}
	
	public char getOwner(){
		return bombOwner;
	}

	private void explodePrematurely(){
		bombInitialTime = 0; //vai fazer com q no proximo tick (update), esta bomba va explodir
	}
	
	// for collision checks
	public int getWidth() {
		//return spriteWidth;
		return panel.getWidth()/numColumns;
	}

	// for collision checks
	public int getHeight() {
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

	public PathState getState() {
		return state;
	}

	//Muda o estado. 
	public void setState(PathState state, char owner) {
		this.state = state;
		if(state == PathState.FLOOR){
			bitmap = floorBitmap;
			frameNr = floorFrameNr;
			gc.writeLogicPosition(iArena, jArena, '-');
		}
		else if(state == PathState.OBSTACLE){
			bitmap = obstacleBitmap;
			frameNr = obstacleFrameNr;
		}
		else if(state == PathState.EXPLOSION){
			bitmap = explosionBitmap;
			frameNr = explosionFrameNr;
			explosionInitialTime = System.currentTimeMillis();
			gc.writeLogicPosition(iArena, jArena, 'E');
			//se o owner eh player, avisa a arena que este ja pode por mais 1 bomba
			bombOwner = owner;
			if(bombOwner != 'R')
				panel.getArena().updatePlayerBomb(bombOwner);
		}
		else if(state == PathState.BOMB){
			bitmap = bombBitmap;
			frameNr = bombFrameNr;
			bombOwner = owner;
			bombInitialTime = System.currentTimeMillis();
			gc.writeLogicPosition(iArena, jArena, 'B');
		}
	
		currentFrame = 0;
	}
	
	private void expandBitmaps(){
		//expansao do bitmap		
		floorBitmap = Bitmap.createScaledBitmap(floorBitmap, floorFrameNr*panel.getWidth()/numColumns, panel.getHeight()/numLines, false);
		obstacleBitmap = Bitmap.createScaledBitmap(obstacleBitmap, obstacleFrameNr*panel.getWidth()/numColumns, panel.getHeight()/numLines, false);
		bombBitmap = Bitmap.createScaledBitmap(bombBitmap, bombFrameNr*panel.getWidth()/numColumns, panel.getHeight()/numLines, false);
		explosionBitmap = Bitmap.createScaledBitmap(explosionBitmap, explosionFrameNr*panel.getWidth()/numColumns, panel.getHeight()/numLines, false);
		spriteWidth = getWidth();
		spriteHeight = getHeight();
		sourceRect = new Rect(0, 0, spriteWidth, spriteHeight);
		
		//na configuracao inicial
		setState(initialState, ' '); 
	}

	//Nos casos especiais em que muda de um estado para outro, pode precisar de
	//actualizar a matriz de estados.
	public void update(long gameTime, GameConfigs gc) {
				
		if(firstUpdate){
			expandBitmaps();
			firstUpdate = false;
		}
		
		if(state == PathState.EXPLOSION || state == PathState.BOMB){
			
			//if bomb check if its time to explode
			if(state == PathState.BOMB){
				if(gameTime > bombInitialTime + bombTime*1000){
					setState(PathState.EXPLOSION, bombOwner);
					//indica para cada 1 das direcçoes se nessa direcao ja houve 1 bloqueio
					//para nao passar por cima das walls
					boolean[] directionsNotBlocked = {true, true, true, true};
					//Faz o setState tb nos blocos ao lado, se nao forem wall
					for(int i=1; i<= explosionRange; i++){
						if(directionsNotBlocked[0]){
							char block = gc.readPosition(iArena+i, jArena);
							if(block == 'O' || block == 'W') 
								directionsNotBlocked[0]=false;
							if(block == 'B')
								gc.writeLogicPosition(iArena+i, jArena, 'E');
							else if(block != 'W')
								panel.getArena().writeState(iArena+i, jArena, PathState.EXPLOSION, bombOwner);
						}
						if(directionsNotBlocked[1]){
							char block = gc.readPosition(iArena-i, jArena);
							if(block == 'O' || block == 'W') 
								directionsNotBlocked[1]=false;
							if(block == 'B')
								gc.writeLogicPosition(iArena-i, jArena, 'E');
							else if(block != 'W')
								panel.getArena().writeState(iArena-i, jArena, PathState.EXPLOSION, bombOwner);
						}
						if(directionsNotBlocked[2]){
							char block = gc.readPosition(iArena, jArena+i);
							if(block == 'O' || block == 'W') 
								directionsNotBlocked[2]=false;
							if(block == 'B')
								gc.writeLogicPosition(iArena, jArena + i, 'E');
							else if(block != 'W')
								panel.getArena().writeState(iArena, jArena+i, PathState.EXPLOSION, bombOwner);
						}
						if(directionsNotBlocked[3]){
							char block = gc.readPosition(iArena, jArena-i);
							if(block == 'O' || block == 'W') 
								directionsNotBlocked[3]=false;
							if(block == 'B')
								gc.writeLogicPosition(iArena, jArena-i, 'E');
							else if(block != 'W')
								panel.getArena().writeState(iArena, jArena-i, PathState.EXPLOSION, bombOwner);
						}				
					}
				}else{ //verifica se esta a ser tocado por uma explosao
					if( gc.readPosition(iArena, jArena) == 'E' )
						explodePrematurely();
				}
			}else { //se for explosao, ve se ja eh tempo de terminar a explosao
				if(gameTime > explosionInitialTime + explosionTime*1000){
					setState(PathState.FLOOR, ' ');
				}
			}
			
			//So nos estados de explosao ou bomba eh q tem q preocupar com o novo frame
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
