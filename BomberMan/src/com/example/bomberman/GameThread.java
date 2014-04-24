package com.example.bomberman;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * @author impaler
 *
 * The Main thread which contains the game loop. The thread must have access to 
 * the surface view and holder to trigger events every game tick.
 */
public class GameThread extends Thread {

	private static final String TAG = GameThread.class.getSimpleName();

	// Surface holder that can access the physical surface
	private SurfaceHolder surfaceHolder;
	// The actual view that handles inputs
	// and draws to the surface
	private MainGamePanel gamePanel;
	private Object pauseLock;

	// flag to hold game state 
	private boolean running;		// boolean for the whole game
	private boolean paused;			// boolean for pause/resume

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean getPaused(){
		return this.paused;
	}

	public GameThread(SurfaceHolder surfaceHolder, MainGamePanel gamePanel) {
		super();
		this.surfaceHolder = surfaceHolder;
		this.gamePanel = gamePanel;
		this.pauseLock = new Object();
		this.paused = false;
	}

	public void pauseThread(){
		//synchronized (pauseLock) {
		this.paused = true;
		//}
	}

	public void resumeThread(){
		synchronized (pauseLock) {
			this.paused = false;
			this.pauseLock.notifyAll();
		}
	}


	@Override
	public void run() {

		Canvas canvas;
		Log.d(TAG, "Starting game loop");
		while (running) {
			
			synchronized (pauseLock) {
				if (paused) {
					try {
						pauseLock.wait();
					} catch (InterruptedException e) {
						System.out.println("CAUGHT InterruptedException IN PAUSE THREAD");
						e.printStackTrace();
					}
				}
			}
			
			canvas = null;
			// try locking the canvas for exclusive pixel editing in the surface
			try {
				canvas = this.surfaceHolder.lockCanvas();
				synchronized (surfaceHolder) {
					// updates the state of all objects in the game
					this.gamePanel.update();
					// render state to the screen
					// draws the canvas on the panel
					this.gamePanel.render(canvas);				
				}
			} finally {
				// in case of an exception the surface is not left in an inconsistent state
				if (canvas != null) 
					surfaceHolder.unlockCanvasAndPost(canvas);	
			}

		}
	}

}
