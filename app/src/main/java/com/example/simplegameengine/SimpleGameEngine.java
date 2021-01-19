package com.example.simplegameengine;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SimpleGameEngine extends Activity {
    GameView gameView;

    int width, height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameView = new GameView(this);
        setContentView(gameView);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        width = size.x;
        height = size.y;
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    class GameView extends SurfaceView implements Runnable {

        //thread
        Thread gameThread = null;

        //surface holder
        SurfaceHolder ourHolder;

        //bolean set and unset when game is running or not
        volatile boolean playing;

        //canvas and paint
        Canvas canvas;
        Paint paint;

        //variable tracks the game frame rate
        long fps;

        //help calculate fps
        private long timeThisFrame;

        //declare Bitmap object
        Bitmap bitmapDuck;

        //duck start not moving
        boolean isMoving = false;
        boolean forward = true;

        //duck walk at 150 pixel per second
        float walkSpeedPerSecond = 100;

        //start position from left
        float duckXPosition = 5;

        public GameView (Context context) {
            super(context);
            ourHolder = getHolder();
            paint = new Paint();
            bitmapDuck = BitmapFactory.decodeResource(this.getResources(), R.drawable.duck);
            playing = true;
        }
     @Override
     public void run() {
            while (playing) {
                //capture curent time
                long startFrameTime = System.currentTimeMillis();
                //update frame
                update();
                //draw frame
                draw(startFrameTime);

                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if(timeThisFrame > 0) {
                    fps = 1000 / timeThisFrame;
                }
            }
     }
     public void update() {
            if(isMoving) {
               if(forward) {
                   if(duckXPosition >= (getScreenWidth()-100)) {
                       forward = false;
                   } else {
                       duckXPosition = duckXPosition + (walkSpeedPerSecond/fps);
                   }
               } else {
                   if(duckXPosition <= 20) {
                       forward = true;
                   } else{
                       duckXPosition = duckXPosition - (walkSpeedPerSecond/fps);
                   }
               }
            }
     }
     public void draw(long startFrameTime) {
            if(ourHolder.getSurface().isValid()) {
                canvas = ourHolder.lockCanvas();
                canvas.drawColor(Color.argb(255, 220, 200, 189));
                paint.setColor(Color.argb(255, 217,169, 113));

                paint.setTextSize(35);
                canvas.drawText("FPS: " + fps, 20,40,paint);
                canvas.drawText("Height: " + height +" Width: "+ width, 20,80,paint);
                canvas.drawText("Duck Height: " + bitmapDuck.getHeight() + " Duck Width: " + bitmapDuck.getWidth(), 20,120,paint);
                canvas.drawText("Duck Start Position: " + startFrameTime, 20,160,paint);

                canvas.drawBitmap(bitmapDuck, duckXPosition, 300, paint);
                ourHolder.unlockCanvasAndPost(canvas);
            }
     }
     public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error: ", "joining thread");
            }
     }
     public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
     }
     @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            switch(motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                //player touch the screen
                case MotionEvent.ACTION_DOWN:
                     //set isMoving so duck will moved in update method
                    isMoving = true;
                    break;

                    //player remove finger from screen
                case MotionEvent.ACTION_UP:
                    isMoving = false;
                    break;
            }
            return true;
     }
    }
    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }
    }
