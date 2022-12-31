package com.glu.engine.Objects;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.ETC1;
import android.opengl.ETC1Util;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL;

public class GTexture {
    public int ID;
    public boolean hasTransparency;
    public Bitmap bmp;
    public boolean isDirty = true;
    public boolean isMipmaped = false;

    public String name = "name";

    public GTexture(int ID,boolean transparency){
        this.ID = ID;
        this.hasTransparency = transparency;
        isDirty = false;
    }

    public GTexture(Bitmap bmp){
        this.bmp = Bitmap.createBitmap(bmp);
        ID = 0;
        hasTransparency = bmp.hasAlpha();
        isDirty =true;
    }

    public GTexture(int w,int h,boolean mono,boolean alpha, boolean HDR, boolean depth, boolean Mipmaped) {
        int[] tex = new int[1];
        GLES30.glGenTextures(1, tex, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, tex[0]);
        isMipmaped = Mipmaped;
        if (Mipmaped) {
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_NEAREST);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D,GLES30.GL_TEXTURE_MAX_LEVEL, 3);
        }else{
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        }
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_R, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

        if(depth) {
            FloatBuffer floatBuffer = ByteBuffer.allocateDirect(w * h * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_DEPTH_COMPONENT, w, h, 0, GLES30.GL_DEPTH_COMPONENT, GLES30.GL_UNSIGNED_INT, floatBuffer);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_COMPARE_FUNC, GLES30.GL_LEQUAL);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D,GLES30.GL_TEXTURE_COMPARE_MODE,GLES30.GL_NONE);
        }else{
            if (mono) {
                if (HDR) {
                    FloatBuffer floatBuffer = ByteBuffer.allocateDirect(w * h * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
                    GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_R16F, w, h, 0, GLES30.GL_RED, GLES30.GL_FLOAT, floatBuffer);
                } else {
                    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(w * h).order(ByteOrder.nativeOrder());
                    GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_R8, w, h, 0, GLES30.GL_RED, GLES30.GL_UNSIGNED_BYTE, byteBuffer);
                }
            } else if (alpha) {
                if (HDR) {
                    FloatBuffer floatBuffer = ByteBuffer.allocateDirect(w * h * 4 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
                    GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA16F, w, h, 0, GLES30.GL_RGBA, GLES30.GL_FLOAT, floatBuffer);
                } else {
                    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(w * h * 4).order(ByteOrder.nativeOrder());
                    GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, w, h, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, byteBuffer);
                }
            } else {
                if (HDR) {
                    FloatBuffer floatBuffer = ByteBuffer.allocateDirect(w * h * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
                    GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGB16F, w, h, 0, GLES30.GL_RGB, GLES30.GL_FLOAT, floatBuffer);
                } else {
                    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(w * h * 3).order(ByteOrder.nativeOrder());
                    GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGB, w, h, 0, GLES30.GL_RGB, GLES30.GL_UNSIGNED_BYTE, byteBuffer);
                }
            }
        }
        if(Mipmaped) {
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        }
        ID = tex[0];
        hasTransparency = alpha;
        isDirty =false;
    }

    public void makeTexture(){
        if(!bmp.isRecycled()) {
            int[] tex = new int[1];
            GLES30.glGenTextures(1, tex, 0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, tex[0]);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D,0,bmp,0);
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
            ID = tex[0];
            Log.w("makeTexture","texture ID : "+ID);
            bmp.recycle();
        }
        isDirty = false;
    }
}
