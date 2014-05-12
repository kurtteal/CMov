package com.example.bomberman;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.bomberman.model.Arena;
import com.example.bomberman.util.GameConfigs;

/**
 * @author impaler
 * This is the main surface that handles the on touch events and draws
 * the image to the screen.
 */
public class MainGamePanel extends SurfaceView implements
		SurfaceHolder.Callback {

	private static final String TAG = MainGamePanel.class.getSimpleName();
	public GameActivity activity;
	
	public GameThread thread;
	//private Bomberman bomberman; //modelo q s mexe
	private Arena arena;
	
	public MainGamePanel(Context context) {
		super(context);
		commonInit(context);
	}
	
    public MainGamePanel(Context context, AttributeSet attrs) {
        super(context, attrs);
		commonInit(context);
    }

    public MainGamePanel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
		commonInit(context);
    }

	private void commonInit(Context context){
		getHolder().addCallback(this);
		activity = (GameActivity) context;
		activity.setGamePanel(this);
		
    	GameConfigs gc = ((GameActivity)context).gc;      
		//Log.d("CONTEXT", matrix.getLine(1)); //ja funca
		arena = new Arena(getResources(), gc, this);
		// create the game loop thread
		thread = new GameThread(getHolder(), this);
		// make the GamePanel focusable so it can handle events
		setFocusable(true);
	}
	
	public void endGame(){
		activity.endGame();
	}
	
	public Arena getArena(){
		return arena;
	}
    
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	//Timer no longer starts when surface is created, especially for multiplayer,
	//it starts when the instance who created the game signals the start. See GameActivity.startTimer()
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// at this point the surface is created and
		// we can safely start the game loop
		thread.setRunning(true);
		thread.start();
		
	}


	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "Surface is being destroyed");
		// tell the thread to shut down and wait for it to finish
		// this is a clean shutdown
		boolean retry = true;
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
				// try again shutting down the thread
			}
		}
		Log.d(TAG, "Thread was shut down cleanly");
	}

	public void update() {
		// Update new positions and animations
		arena.update(System.currentTimeMillis());
//		bomberman.update(System.currentTimeMillis(), this);
	}
	
	//Aqui eh qdo sao desenhados, a ordem interessa, os ultimos ficam "por cima"
	protected void render(Canvas canvas) {
		// fills the canvas with black
		if(canvas != null){ //ha um bug em que ha umas (raras) vezes em q o canvas vem a null...lol
			canvas.drawColor(Color.BLACK);
			arena.draw(canvas);
		}
//		bomberman.draw(canvas);
	}
	

}
