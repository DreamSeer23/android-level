/**
 * Name: John Cerreta & Andrew Uriarte
 * File: LevelView.java
 * Purpose: Shows the level/bubble and animates them. Used in every activity.
 */
package com.development.john.androidlevel;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by John on 4/6/2016.
 */
public class LevelView extends ImageView {


    private int frameRate = 30;
    private int bubbleX;
    private int landingY;
    public static final int BUBBLE_Y = 0;
    private int orientation = 0;
    private Handler h;
    private Paint p;
    boolean pause, initialized, lsRes;

    Bitmap portraitResized, landscapeResized, bubbleResized, flatResized, fbubbleResized, portraitBackground, landscapeBackground, flatBackground, bubble, flatBubble;

    /**
     * Basic constructor, sets up instance fields
     * @param context the activity this view is in
     * @param attrs basic layout params for the view
     */
    public LevelView(Context context, AttributeSet attrs) { //constructor for LevelView, coded by John
        super(context, attrs);
        p = new Paint();
        h = new Handler();
        bubbleX = this.getWidth() - 5;
        landingY = this.getHeight()/2;
        initialized = lsRes = false;


        //instantiates everything needed
    }


    /**
     * Initializes all the images we will be using. Uses quite a bit of memory so can't be done in constructor.
     */
    public void initializeBitmaps()
    {
        portraitBackground = BitmapFactory.decodeResource(getResources(), R.drawable.rect_level_graphic);
        landscapeBackground = BitmapFactory.decodeResource(getResources(), R.drawable.level_horizontal_blank2);
        flatBackground = BitmapFactory.decodeResource(getResources(), R.drawable.level_flat_blank2);
        bubble = BitmapFactory.decodeResource(getResources(), R.drawable.bubble_horizontal);
        flatBubble = BitmapFactory.decodeResource(getResources(), R.drawable.flat_bubble);
        portraitResized = Bitmap.createScaledBitmap(portraitBackground, this.getWidth(), this.getHeight(), true);
        bubbleResized = Bitmap.createScaledBitmap(bubble, 350, 350, true);
        landscapeResized = Bitmap.createScaledBitmap(landscapeBackground, this.getHeight() + getStatusBarHeight(), this.getWidth(), true);
        flatResized = Bitmap.createScaledBitmap(flatBackground, this.getWidth(), this.getHeight(), true);
        fbubbleResized = Bitmap.createScaledBitmap(flatBubble, this.getWidth(), this.getHeight(), true);
        initialized = true;
    }

    /**
     * Returns the Status Bar Height of the phone so that the images can account for the offset
     * @return status bar height
     */
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * Used so that main class can communicate when the orientation has changed to this view
     * @param i Either 0, 1, or 2. Represents which type of level to be drawn
     */
    public void setOrientation(int i)
    {
        orientation = i;
    }


    private Runnable r = new Runnable() { //sets up runnable for refreshing of screen
        @Override
        public void run() {
            invalidate();
        }
    };

    /**
     * Standard onDraw method
     * @param canvas the canvas where this is being drawn on
     */
    @Override
    protected void onDraw(Canvas canvas) { //Coded by Andrew
        if(!initialized)
            initializeBitmaps();

        //animates based on orientation
        switch(orientation) {
            case 0:
                canvas.drawBitmap(portraitResized, 0, 0, p);
                canvas.drawBitmap(bubbleResized, this.getWidth()/2 - 175 + bubbleX, this.getHeight()/2 - 210, p);
                break;
            case 1:
                canvas.drawBitmap(landscapeResized, 0, 0, p);
                canvas.drawBitmap(bubbleResized, this.getWidth() / 2 - 175 + bubbleX, this.getHeight() / 2 - 170, p);
                break;
            case 2:
                canvas.drawBitmap(flatResized, 0, 0, p);
                canvas.drawBitmap(fbubbleResized, 0 + bubbleX, 0 + landingY, p);
        }

        h.postDelayed(r, frameRate);
    }

    //setters that allow the main class to communicate the info of the sensors
    public void setBubbleX(int x)
    {
        bubbleX = x;
    }
    public void setLandingY(int y) { landingY = y; }
}
