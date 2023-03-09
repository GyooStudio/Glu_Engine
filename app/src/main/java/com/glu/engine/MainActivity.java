package com.glu.engine;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NativeActivity;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.glu.engine.utils.Loader;

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