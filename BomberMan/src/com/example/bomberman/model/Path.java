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
public class Path implements IDrawable{
	
	public int iArena;
	public int jArena;
	private IDrawable[][] arena; //for the explosions to update the other nearby objects

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
	private Resources resources;
	private long bombInitialTime;
	private long explosionInitialTime;
	private int bombTime; //14 vem do ficheiro map escolhido (esta nos assets)
	private int explosionTime; // 7
	private int explosionRange; // range 2 
	
	//TODO bombas e explosoes precisam de ter o id (myself) do owner para atribuicao de pts

	public Path(Resources resources, int x, int y, PathState state, int i, int j, Arena arenaHolder) {
		iArena = i;
		jArena = j; //Each component knows its coordinates on the gameMatrix
		this.arena = arenaHolder.arena;
		this.resources = resources;
		setState(state, null); //na configuracao inicial nao ha actualizacoes ah matrix, so leitura
			
		this.x = x;
		this.y = y;
		
		bombTime = arenaHolder.gc.explosionTimeout;
		explosionTime = arenaHolder.gc.explosionDuration;
		this.explosionRange = arenaHolder.gc.explosionRange;

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

	//Muda o estado. 
	public void setState(PathState state, char[][] matrix) {
		this.state = state;
		if(state == PathState.FLOOR){
			bitmap = BitmapFactory.decodeResource(resources,
				R.drawable.floor);
			frameNr = 1;
			if(matrix != null)
				matrix[iArena][jArena] = '-';
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
			explosionInitialTime = System.currentTimeMillis();
			if(matrix != null){ 
				matrix[iArena][jArena] = 'E';
			}
		}
		else if(state == PathState.BOMB){
			bitmap = BitmapFactory.decodeResource(resources,
					R.drawable.bomb);
			frameNr = 2;
			bombInitialTime = System.currentTimeMillis();
			if(matrix != null)
				matrix[iArena][jArena] = 'B';
		}
		spriteWidth = bitmap.getWidth() / frameNr;
		spriteHeight = bitmap.getHeight();
		sourceRect = new Rect(0, 0, spriteWidth, spriteHeight);		
		currentFrame = 0;
	}

	//Nos casos especiais em que muda de um estado para outro, pode precisar de
	//actualizar a matriz de estados.
	public void update(long gameTime, GameConfigs gm) {
		if(state == PathState.EXPLOSION || state == PathState.BOMB){
			//if bomb check if its time to explode
			if(state == PathState.BOMB){
				if(gameTime > bombInitialTime + bombTime*1000){
					setState(PathState.EXPLOSION, gm.matrix);
					//indica para cada 1 das direcçoes se nessa direcao ja houve 1 bloqueio
					//para nao passar por cima das walls
					boolean[] directionsNotBlocked = {true, true, true, true};
					//Faz o setState tb nos blocos ao lado, se nao forem wall
					for(int i=1; i<= explosionRange; i++){
						if(directionsNotBlocked[0]){
							if(gm.matrix[iArena+i][jArena]=='O' || gm.matrix[iArena+i][jArena]=='W') 
								directionsNotBlocked[0]=false;
							if(gm.matrix[iArena+i][jArena]!='W')
								arena[iArena+i][jArena].setState(PathState.EXPLOSION, gm.matrix);
						}
						if(directionsNotBlocked[1]){
							if(gm.matrix[iArena-i][jArena]=='O' || gm.matrix[iArena-i][jArena]=='W')
								directionsNotBlocked[1]=false;
							if(gm.matrix[iArena-i][jArena]!='W')
								arena[iArena-i][jArena].setState(PathState.EXPLOSION, gm.matrix);
						}
						if(directionsNotBlocked[2]){
							if(gm.matrix[iArena][jArena+i]=='O' || gm.matrix[iArena][jArena+i]=='W')
								directionsNotBlocked[2]=false;
							if(gm.matrix[iArena][jArena+i]!='W')
								arena[iArena][jArena+i].setState(PathState.EXPLOSION, gm.matrix);
						}
						if(directionsNotBlocked[3]){
							if(gm.matrix[iArena][jArena-i]=='O' || gm.matrix[iArena][jArena-i]=='W') 
								directionsNotBlocked[3]=false;
							if(gm.matrix[iArena][jArena-i]!='W')
								arena[iArena][jArena-i].setState(PathState.EXPLOSION, gm.matrix);
						}				
					}
				}
			}else { //se for explosao, ve se ja eh tempo de terminar a explosao
				if(gameTime > explosionInitialTime + explosionTime*1000){
					setState(PathState.FLOOR, gm.matrix);
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
