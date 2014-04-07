package com.example.bomberman;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.bomberman.model.Arena;
import com.example.bomberman.model.Bomberman;
import com.example.bomberman.model.Path;
import com.example.bomberman.model.PathState;
import com.example.bomberman.model.Wall;
import com.example.bomberman.model.components.Speed;
import com.example.bomberman.util.GameConfigs;

/**
 * @author impaler
 * This is the main surface that handles the on touch events and draws
 * the image to the screen.
 */
public class MainGamePanel extends SurfaceView implements
		SurfaceHolder.Callback {

	private static final String TAG = MainGamePanel.class.getSimpleName();
	
	private GameThread thread;
	//private Bomberman bomberman; //modelo q s mexe
	private Arena arena;
	
	public MainGamePanel(Context context) {
		super(context);
		// adding the callback (this) to the surface holder to intercept events
		getHolder().addCallback(this);
		commonInit(context);
	}
    public MainGamePanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
		commonInit(context);
    }

    public MainGamePanel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
		commonInit(context);
    }

	private void commonInit(Context context){
		// create droid and load bitmap : <bitmap, xInitial, yInitial>

		//bomberman = new Bomberman(getResources(), 50, 50);
    	GameConfigs matrix = ((GameActivity)context).matrix;      
		Log.d("CONTEXT", matrix.getLine(1)); //ja funca
		arena = new Arena(getResources(), matrix, this);
		// create the game loop thread
		thread = new GameThread(getHolder(), this);
		// make the GamePanel focusable so it can handle events
		setFocusable(true);
	}
	
	public Arena getArena(){
		return arena;
	}
    
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

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
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// delegating event handling to the droid
	//		droid.handleActionDown((int)event.getX(), (int)event.getY());
			
			// check if in the lower part of the screen we exit
			if (event.getY() > getHeight() - 50) {
				thread.setRunning(false);
				((Activity)getContext()).finish();
			} else {
				Log.d(TAG, "Coords: x=" + event.getX() + ",y=" + event.getY());
			}
		} 
		return true;
	}

	public void update() {
		
		// Update new positions and animations
		arena.update(System.currentTimeMillis());
//		bomberman.update(System.currentTimeMillis(), this);
	}
	
	//Aqui eh qdo sao desenhados, a ordem interessa, os ultimos ficam "por cima"
	protected void render(Canvas canvas) {
		// fills the canvas with black
		canvas.drawColor(Color.BLACK);
		arena.draw(canvas);
//		bomberman.draw(canvas);
	}
	

}
