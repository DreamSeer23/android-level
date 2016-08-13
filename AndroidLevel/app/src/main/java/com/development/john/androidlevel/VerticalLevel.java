/**
 * Name: John Cerreta & Andrew Uriarte
 * File: VerticalLevel.java
 * Purpose: The main activity of the project. Displayed when the phone is held upwards in portrait mode.
 */
package com.development.john.androidlevel;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * Class for the VerticalLevel
 */
public class VerticalLevel extends AppCompatActivity implements SensorEventListener{

    private long lastUpdate;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private boolean locked = false;

    private LevelView level;
    private TextView x;
    private ToggleButton lockButton;

    private int offset = 0;
    private float lastRoll;

    // Gravity rotational data
    private float gravity[];
    // Magnetic rotational data
    private float magnetic[]; //for magnetic rotational data
    private float accels[] = new float[3];
    private float mags[] = new float[3];
    private float[] values = new float[3];

    // azimuth, pitch and roll
    private float azimuth;
    private float pitch;
    private float roll;
    private float balance;
    private float tmp;

    @Override
    /**
     * onCreate method, called when the app is created. Sets up the app.
     * Coded by Andrew
     * @param savedInstanceState default code
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vertical_level);
        initializeViews();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null)
        {
            //got accelerometer
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_NORMAL);

        }



    }

    /**
     * Initializes the views used by the activity
     */
    public void initializeViews(){
        level = (LevelView) findViewById(R.id.Level);
        x = (TextView) findViewById(R.id.xView);
        lockButton = (ToggleButton) findViewById(R.id.lockButton);

        lockButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                      if (isChecked) {
                                                          locked = true;
                                                      } else {
                                                          locked = false;
                                                      }
                                                  }
                                              });

                Typeface myTypeface = Typeface.createFromAsset(this.getAssets(), "digital-7.ttf");
                x.setTypeface(myTypeface);
                x.setTextColor(Color.BLUE);
    }


    /**
     * Returns the rotation of the phone based on orientation
     * @return 0 if in portrait, 90 in landscape, 180 in reverse portrait, 270 in reverse landscape
     */
            private int getScreenRotationOnPhone() {

                final Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

                switch (display.getRotation()) {
                    case Surface.ROTATION_0: {
                        return 0;
                    }
                    //right side up
                    case Surface.ROTATION_90: {
                        return 90;
                    }
                    case Surface.ROTATION_180: {
                        return 180;
                    }
                    //left side up
                    case Surface.ROTATION_270: {
                        return 270;
                    }
                    default:
                        return -1;
                }
            }

            @Override
            /**
             * onSensorChanged method, used to detect when sensor (Accelerometer) has detected change
             * Coded by John and Andrew
             * @param SensorEvent contains information capture by a sensor changing
             */
            public void onSensorChanged(SensorEvent event) {

                //Differentiate between sensor
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        mags = event.values.clone();
                        break;
                    case Sensor.TYPE_ACCELEROMETER:
                        accels = event.values.clone();
                        break;
                }

                //Once both sensors have been updated
                if (mags != null && accels != null) {
                    gravity = new float[16];
                    magnetic = new float[16];
                    //get rotation matrix using accelerometer and geomagnetic sensor values
                    boolean success = SensorManager.getRotationMatrix(gravity, magnetic, accels, mags);

                    float[] outR = new float[16];

                    sensorManager.remapCoordinateSystem(gravity, sensorManager.AXIS_X, sensorManager.AXIS_Z, outR);

                    sensorManager.getOrientation(outR, values);

                    tmp = (float) Math.sqrt(outR[8] * outR[8] + outR[9] * outR[9]);
                    tmp = (tmp == 0 ? 0 : outR[8] / tmp);

                    //Meat and potatoes right here, how the rotation is interpreted
                    if (success) {
                        azimuth = accels[1];
                        //azimuth = (float) Math.toDegrees(values[0]);
                        pitch = (float) Math.toDegrees(values[1]);
                        roll = (float) Math.toDegrees(values[2]); //Most important value
                        balance = (float) Math.toDegrees(Math.asin(tmp));
                        mags = null;
                        accels = null;
                    }
                }

                long time = System.currentTimeMillis();
                float deltaRoll = Math.abs(roll - lastRoll);

                //Only update every .2s since accelerometer is sensitive. Also, only if degree change is > .5.
                if ((time - lastUpdate) > 200 && deltaRoll > .5) {
                    lastUpdate = time;

                    //Force orientation changes, so the animation can line up properly
                    //Depending on orientation, display must be offset
                    //Won't change orientation if locked
                    if (!locked) {
                        if (pitch > 45 || (lastRoll < 45 && lastRoll > -45)) {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            if(pitch > 45)
                                level.setOrientation(2);
                            else
                                level.setOrientation(0);
                            offset = 0;
                        } else if (lastRoll < -45) {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            level.setOrientation(1);
                            offset = -90;
                        } else if (lastRoll > 45) {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                            level.setOrientation(1);
                            offset = 90;
                        }
                    }

                    //temps used to display info/animate the images
                    lastRoll = roll;
                    float drawRoll = roll;
                    int setter;
                    String display;
                    //Show bubble based on a dampening equation (Just plug and play)
                    switch (getScreenRotationOnPhone()) {
                        case 0:
                            //if held up right
                            if(pitch < 45){
                                setter = (int) (drawRoll / 90 * level.getWidth() / 1.5);
                                if(setter < -level.getWidth()/3)
                                    setter = -level.getWidth()/3;
                                else if(setter > level.getWidth()/3)
                                    setter = level.getWidth()/3;
                                level.setBubbleX(-setter);

                                display = String.format("%02.1f", (-roll + offset));
                                x.setText(display);
                            }
                            //if flat on a surface
                            else
                            {
                                setter = (int) (balance / 90 * level.getWidth() / 1.5);
                                if(setter > level.getWidth()/3)
                                    setter = level.getWidth()/3;
                                else if (setter < -level.getWidth()/3)
                                    setter = - level.getWidth()/3;
                                level.setBubbleX(setter);
                                setter = (int) (azimuth / 10 * level.getHeight() / 3);
                                if(setter > 4.5*level.getHeight()/30)
                                    setter = (int)(4.5 * level.getHeight()/30);
                                else if (setter < -4.5 * level.getHeight()/30)
                                    setter = (int) (-4.5 * level.getHeight()/30);
                                level.setLandingY(-setter);

                                display = String.format("X: %02.1f\nY: %02.1f", (balance), (azimuth*9));
                                x.setText(display);
                            }
                            break;
                        //right side of phone up
                        case 90:
                            drawRoll += 90;
                            setter = (int) (drawRoll / 90 * level.getWidth() / 2.5);
                            if(setter < -level.getWidth()/5)
                                setter = -level.getWidth()/5;
                            else if(setter > level.getWidth()/5)
                                setter = level.getWidth()/5;
                            level.setBubbleX(-setter);

                            display = String.format("%02.1f", (-roll + offset));
                            x.setText(display);
                            break;
                        //left side of phone up
                        case 270:
                            drawRoll -= 90;
                            setter = (int) (drawRoll / 90 * level.getWidth() / 2.5);
                            if(setter > level.getWidth()/5)
                                setter = level.getWidth()/5;
                            else if(setter < -level.getWidth()/5)
                                setter = -level.getWidth()/5;
                            level.setBubbleX(-setter);

                            display = String.format("%02.1f", (-roll + offset));
                            x.setText(display);
                            break;
                    }

                }


            }

            @Override
            //Had to be overridden
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }

            @Override
            public void onConfigurationChanged(Configuration newConfig) {
                super.onConfigurationChanged(newConfig);
                // Checks the orientation of the screen


            }


        }
