package com.glu.engine.GUI.Text;

import android.util.Log;

import com.glu.engine.Objects.GTexture;

public class Font {
    public GTexture textureAtlas;
    public int charNumber;
    public int atlasCharWidth;
    public int cellWidth;
    public int textureAtlasWidth;
    public int padding;
    public int[] ranges;

    private float[] charWidth;
    private float[] charHeight;
    private float[] charDropDown;
    private float[] charFarLeft;

    public Font(GTexture textureAtlas, int textureAtlasWidth,float[] charHeight, float[] charWidth, float[] charDropDown, float[] charFarLeft, int charNumber, int atlasCharWidth, int cellWidth, int padding, int[] ranges){
        this.textureAtlas = textureAtlas;
        this.charNumber = charNumber;
        this.atlasCharWidth = atlasCharWidth;
        this.cellWidth = cellWidth;
        this.charWidth = charWidth;
        this.textureAtlasWidth = textureAtlasWidth;
        this.charHeight = charHeight;
        this.charDropDown = charDropDown;
        this.padding = padding;
        this.charFarLeft = charFarLeft;
        this.ranges = ranges;
    }

    public float getCharWidth(int index) {
        try {
            int offset = 0;
            for (int i = 0; i < ranges.length; i++) {
                if (index > ranges[i] && index < ranges[i + 1]) {
                    return charWidth[index - ranges[i] + offset];
                } else {
                    offset += ranges[i + 1] - ranges[i];
                }
                i++;
            }
            return 0;
        }catch (Exception e){
            Log.e("getCharWidth", "You probably didn't order your ranges properly. Please make sure your ranges are in order from smallest to largest.");
            Log.e("getCharWidth", e.getMessage());
            return 0;
        }
    }

    public float getCharHeight(int index) {
        try {
            int offset = 0;
            for (int i = 0; i < ranges.length; i++) {
                if (index > ranges[i] && index < ranges[i + 1]) {
                    return charHeight[index - ranges[i] + offset];
                } else {
                    offset += ranges[i + 1] - ranges[i];
                }
                i++;
            }
            return 0;
        }catch (Exception e){
            Log.e("getCharHeight", "You probably didn't order your ranges properly. Please make sure your ranges are in order from smallest to largest.");
            Log.e("getCharHeight", e.getMessage());
            return 0;
        }
    }

    public float getCharDropDown(int index) {
        try {
            int offset = 0;
            for (int i = 0; i < ranges.length; i++) {
                if (index > ranges[i] && index < ranges[i + 1]) {
                    return charDropDown[index - ranges[i] + offset];
                } else {
                    offset += ranges[i + 1] - ranges[i];
                }
                i++;
            }
            return 0;
        }catch (Exception e){
            Log.e("getCharDropDown", "You probably didn't order your ranges properly. Please make sure your ranges are in order from smallest to largest.");
            Log.e("getCharDropDown", e.getMessage());
            return 0;
        }
    }

    public float getCharFarLeft(int index) {
        try {
            int offset = 0;
            for (int i = 0; i < ranges.length; i++) {
                if (index > ranges[i] && index < ranges[i + 1]) {
                    return charFarLeft[index - ranges[i] + offset];
                } else {
                    offset += ranges[i + 1] - ranges[i];
                }
                i++;
            }
            return 0;
        }catch (Exception e){
            Log.e("getCharDropDown", "You probably didn't order your ranges properly. Please make sure your ranges are in order from smallest to largest.");
            Log.e("getCharDropDown", e.getMessage());
            return 0;
        }
    }
}
