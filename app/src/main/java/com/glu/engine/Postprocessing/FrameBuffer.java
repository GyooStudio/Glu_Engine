package com.glu.engine.Postprocessing;

import android.opengl.GLES30;
import android.util.Log;

import com.glu.engine.Objects.GTexture;

public class FrameBuffer {

    public int ID;
    public GTexture texture;
    public GTexture B;
    public GTexture C;
    public GTexture D;
    public int width;
    public int height;
    public boolean isMipmaped;

    public FrameBuffer(int w,int h,boolean HDR, boolean scene, boolean shadow, boolean Mipmaped){

        width = w;
        height = h;
        isMipmaped = Mipmaped;

        if(scene) {

            int[] frameBuffer = new int[1];
            GLES30.glGenFramebuffers(1,frameBuffer,0);
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,frameBuffer[0]);
            texture = new GTexture(w,h,false,true,HDR,false, Mipmaped);

            int[] depthBuffer = new int[1];
            GLES30.glGenRenderbuffers(1,depthBuffer,0);
            GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER,depthBuffer[0]);
            GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER,GLES30.GL_DEPTH_COMPONENT16,w,h);
            GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER,GLES30.GL_DEPTH_ATTACHMENT,GLES30.GL_RENDERBUFFER,depthBuffer[0]);

            B = new GTexture(w, h, false, true, true, false, Mipmaped);
            C = new GTexture(w, h, false, true, true, false, Mipmaped);
            D = new GTexture(w, h, false, true, true, false, Mipmaped);


            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, texture.ID, 0);
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT1, GLES30.GL_TEXTURE_2D, B.ID, 0);
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT2, GLES30.GL_TEXTURE_2D, C.ID, 0);
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT3, GLES30.GL_TEXTURE_2D, D.ID, 0);
            int[] drawBuffer = new int[]{
                    GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_COLOR_ATTACHMENT1,
                    GLES30.GL_COLOR_ATTACHMENT2, GLES30.GL_COLOR_ATTACHMENT3};
            GLES30.glDrawBuffers(4, drawBuffer, 0);

            if (GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) != GLES30.GL_FRAMEBUFFER_COMPLETE) {
                Log.e("frameBuffer", "frameBuffer failed " + GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER));
            }

            ID = frameBuffer[0];

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        }else if(shadow){

            int[] frameBuffer = new int[1];
            GLES30.glGenFramebuffers(1,frameBuffer,0);
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,frameBuffer[0]);
            texture = new GTexture(w,h,true,false,HDR,false,Mipmaped); //custom class for handling textures. It works fine.

            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, texture.ID, 0);
            int[] drawBuffer = new int[]{GLES30.GL_COLOR_ATTACHMENT0};
            GLES30.glDrawBuffers(1, drawBuffer, 0);

            int[] depthBuffer = new int[1];
            GLES30.glGenRenderbuffers(1,depthBuffer,0);
            GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER,depthBuffer[0]);
            GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER,GLES30.GL_DEPTH_COMPONENT16,w,h);
            GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER,GLES30.GL_DEPTH_ATTACHMENT,GLES30.GL_RENDERBUFFER,depthBuffer[0]);

            if (GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) != GLES30.GL_FRAMEBUFFER_COMPLETE) { // no log output on this one
                Log.e("frameBuffer", "frameBuffer failed " + GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER));
            }

            ID = frameBuffer[0];

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        }else{

            int[] frameBuffer = new int[1];
            GLES30.glGenFramebuffers(1,frameBuffer,0);
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,frameBuffer[0]);
            texture = new GTexture(w,h,false,true,HDR,false,Mipmaped); //custom class for handling textures. It works fine.

            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, texture.ID, 0);
            int[] drawBuffer = new int[]{GLES30.GL_COLOR_ATTACHMENT0};
            GLES30.glDrawBuffers(1, drawBuffer, 0);

            if (GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) != GLES30.GL_FRAMEBUFFER_COMPLETE) { // no log output on this one
                Log.e("frameBuffer", "frameBuffer failed " + GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER));
            }

            ID = frameBuffer[0];

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        }

    }
}

