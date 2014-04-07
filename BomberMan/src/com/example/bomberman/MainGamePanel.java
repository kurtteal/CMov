package com.example.bomberman;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.bomberman.model.Bomberman;
import com.example.bomberman.model.components.Speed;

/**
 * @author impaler
 * This is the main surface that handles the on touch events and draws
 * the image to the screen.
 */
public class MainGamePanel extends SurfaceView implements
		SurfaceHolder.Callback {

	private static final String TAG = MainGamePanel.class.getSimpleName();
	
	private GameThread thread;
	private Bomberman bman; //modelo q s mexe

	private void commonInit(){
		// create droid and load bitmap : <bitmap, xInitial, yInitial>
		bman = new Bomberman(getResources(), 50, 50);
		// create the game loop thread
		thread = new GameThread(getHolder(), this);
		// make the GamePanel focusable so it can handle events
		setFocusable(true);
	}
	
	public MainGamePanel(Context context) {
		super(context);
		// adding the callback (this) to the surface holder to intercept events
		getHolder().addCallback(this);
		commonInit();
	}
    public MainGamePanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
		commonInit();
    }

    public MainGamePanel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
		commonInit();
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
		} if (event.getAction() == MotionEvent.ACTION_MOVE) {
			// the gestures
			if (bman.isTouched()) {
				// the droid was picked up and is being dragged
				bman.setX((int)event.getX());
				bman.setY((int)event.getY());
			}
		} if (event.getAction() == MotionEvent.ACTION_UP) {
			// touch was released
			if (bman.isTouched()) {
				bman.setTouched(false);
			}
		}
		return true;
	}

	protected void render(Canvas canvas) {
		// fills the canvas with black
		canvas.drawColor(Color.BLACK);
		bman.draw(canvas);
	}
	
	public void update() {
		// check collision with right wall if heading right
		if (bman.getSpeed().getxDirection() == Speed.DIRECTION_RIGHT
				&& bman.getX() + bman.getWidth() >= getWidth()) {
			bman.getSpeed().toggleXDirection();
		}
		// check collision with left wall if heading left
		if (bman.getSpeed().getxDirection() == Speed.DIRECTION_LEFT
				&& bman.getX() <= 0) {
			bman.getSpeed().toggleXDirection();
		}
		// check collision with bottom wall if heading down
		if (bman.getSpeed().getyDirection() == Speed.DIRECTION_DOWN
				&& bman.getY() + bman.getHeight() >= getHeight()) {
			bman.getSpeed().toggleYDirection();
		}
		// check collision with top wall if heading up
		if (bman.getSpeed().getyDirection() == Speed.DIRECTION_UP
				&& bman.getY() <= 0) {
			bman.getSpeed().toggleYDirection();
		}
		// Update the lone droid
		bman.update(System.currentTimeMillis());
	}


}
