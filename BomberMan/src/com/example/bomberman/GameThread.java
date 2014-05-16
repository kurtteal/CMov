package com.example.bomberman;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GameThread extends Thread {

	private SurfaceHolder surfaceHolder;
	private GamePanel gamePanel;
	private Object pauseLock;

	private boolean running;	// boolean for the whole game
	private boolean paused;		// boolean for pause/resume

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean getPaused(){
		return this.paused;
	}

	public GameThread(SurfaceHolder surfaceHolder, GamePanel gamePanel) {
		super();
		this.surfaceHolder = surfaceHolder;
		this.gamePanel = gamePanel;
		this.pauseLock = new Object();
		this.paused = false;
	}

	public void pauseThread(){
		this.paused = true;
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
			
			if(isInterrupted())
				break;
			
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
