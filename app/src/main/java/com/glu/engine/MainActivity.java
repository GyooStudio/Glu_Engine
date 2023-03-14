package com.glu.engine;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NativeActivity;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.glu.engine.utils.Loader;
import com.glu.engine.vectors.Vector3f;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    GluSurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityManager am = ( ActivityManager ) getSystemService ( Context.ACTIVITY_SERVICE );
        ConfigurationInfo info = am.getDeviceConfigurationInfo();

        if(!(Float.parseFloat(info.getGlEsVersion()) >= 3.0f)){
            Log.e("Glu Engine",info.getGlEsVersion() + " is the maximum supported OpenGL version. 3.0 is required, exiting...");
            System.exit(-1);
        }else{
            Log.w("Glu Engine","OpenGL es " + info.getGlEsVersion() + " supported, higher than 3.0, proceeding.");
        }

        Loader.init(this);

        surfaceView = new GluSurfaceView(this,this);
        hideUI();

        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        SensorEventListener sel = new SensorEventListener() {
            private int accuracy = 0;
            float[] gravity = new float[3];
            float[] linear_acceleration = new float[3];
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                    float[] v = sensorEvent.values;

                    final float alpha = 0.9999f;
                    final float alphaB = 0.0f;

                    if (gravity[0] == 0f && gravity[1] == 0f && gravity[2] == 0f) {
                        gravity[0] = v[0];
                        gravity[1] = v[1];
                        gravity[2] = v[2];
                    }

                    gravity[0] = alpha * gravity[0] + (1 - alpha) * v[0];
                    gravity[1] = alpha * gravity[1] + (1 - alpha) * v[1];
                    gravity[2] = alpha * gravity[2] + (1 - alpha) * v[2];

                    linear_acceleration[0] = alphaB * linear_acceleration[0] + (1f - alphaB) * (v[0] - gravity[0]);
                    linear_acceleration[1] = alphaB * linear_acceleration[1] + (1f - alphaB) * (v[1] - gravity[1]);
                    linear_acceleration[2] = alphaB * linear_acceleration[2] + (1f - alphaB) * (v[2] - gravity[2]);

                    Vector3f dir = new Vector3f(linear_acceleration[0], linear_acceleration[1], linear_acceleration[2]);
                    surfaceView.scene.inputManager.onAccelerometerChanged(dir);
                    //Log.w("accelerometer","x: " + dir.x + " y: " + dir.y + " z: " + dir.z);
                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
                if(sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                    accuracy = i;
                    Log.w("accelerometer","accuracy : " + i);
                }
            }
        };
        sm.registerListener(sel, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        //sm.registerListener(sel, sm.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_GAME);

        setContentView(surfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w("OnResume","HasResumed");
        hideUI();
        if(surfaceView != null) {
            surfaceView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(surfaceView != null) {
            surfaceView.onPause();
        }
        hideUI();
        Log.w("OnPause","HasPaused");
    }

    @Override
    public void onConfigurationChanged(Configuration config){
        super.onConfigurationChanged(config);
        surfaceView.onConfigurationChanged(config);
        hideUI();
    }

    public void hideUI(){
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
    }
}