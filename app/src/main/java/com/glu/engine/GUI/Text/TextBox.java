package com.glu.engine.GUI.Text;

import android.util.Log;

import com.glu.engine.Objects.RawModel;
import com.glu.engine.shader.TextShader;
import com.glu.engine.utils.Loader;
import com.glu.engine.vectors.Vector2f;

public class TextBox {
    public Font font;
    public Vector2f position;
    public Vector2f size;
    public Vector2f scale;
    public float rotation;
    public boolean show;
    private boolean canSetText = true;
    public boolean toUpdate = false;

    public RawModel textModel;
    public TextShader shader;

    public String text;
    public float charSpacing;
    public float lineSpacing;

    public enum Alignment {RIGHT, LEFT, CENTERED}
    public Alignment al = Alignment.RIGHT;
    public boolean returnToLine = true;

    public String name = "name";

    public int id = 0;

    public TextBox(Font font, TextShader shader, Vector2f size, Vector2f position, Vector2f scale, float rotation){
        this.font = font;
        this.shader = shader;
        this.position = position;
        this.size = size;
        this.scale = scale;
        this.rotation = rotation;
        text = "Text.";
        show = true;
    }

    public TextBox(Font font, TextShader shader){
        this.font = font;
        this.shader = shader;
        this.position = new Vector2f(0,0);
        this.size = new Vector2f(400,400);
        this.scale = new Vector2f(1,1);
        this.rotation = 0;
        text = "Text.";
        show = true;
    }

    public void setText(String text, float charSpacing, float lineSpacing, Alignment al){
        if(canSetText && text != this.text) {
            this.text = text;
            this.charSpacing = charSpacing;
            this.lineSpacing = lineSpacing;
            this.toUpdate = true;
            this.al = al;
        }
    }

    public void makeText(){
        canSetText = false;
        long timer = System.currentTimeMillis();

        Log.w("makeText", "" + text);

        float[] pos = new float[text.length()*6*2];
        float[] UV = new float[text.length()*6*2];

        float scaler = 30f/ (font.cellWidth - font.padding);
        Vector2f cursorPos;
        Vector2f cursorUV;
        Vector2f charSize;
        float charDropDown;

        String line;
        String word;
        int wordSize;
        int lineSize;
        boolean isFirstWord = true;
        boolean hasCompletedWord;
        boolean hasCompletedLine;

        switch (al){
            case LEFT:
                cursorPos = new Vector2f(-size.x, size.y-(font.cellWidth*scaler));
                break;
            case RIGHT:
                cursorPos = new Vector2f(size.x, size.y-(font.cellWidth*scaler));
                break;
            case CENTERED:
                cursorPos = new Vector2f(0, size.y-(font.cellWidth*scaler));
                break;
            default:
                cursorPos = new Vector2f(0,0);
                break;
        }

        for (int j = 1; j < text.length(); j++) {

            hasCompletedLine = false;
            line = "";
            lineSize = 0;
            j--;

            while (!hasCompletedLine) {
                word = "";
                hasCompletedWord = false;

                while (!hasCompletedWord) {
                    if (j < text.length()) {
                        char a = text.charAt(j);
                        if (a == ' ' || a == '-' || a == ',' || a == '.' || a == '!' || a == '?' || a == '/' || a == ':' || a == ';') {
                            hasCompletedWord = true;
                            word += text.charAt(j);
                        }else if(a == '\n'){
                            word += text.charAt(j);
                            hasCompletedWord = true;
                            hasCompletedLine = true;
                        } else {
                            word += text.charAt(j);
                        }
                        j++;
                    } else {
                        hasCompletedWord = true;
                        hasCompletedLine = true;
                    }
                }

                wordSize = 0;
                for (int i = 0; i < word.length(); i++) {
                    wordSize += (float) font.getCharWidth(word.charAt(i)) * scaler;
                }

                if (lineSize + wordSize > size.x && !isFirstWord && returnToLine) {
                    hasCompletedLine = true;
                    j -= word.length();
                }else if(lineSize + wordSize > size.x && isFirstWord && returnToLine){
                    line += word;
                    lineSize += wordSize;
                    hasCompletedLine = true;
                }else{
                    line += word;
                    lineSize += wordSize;
                }

                if (isFirstWord) {
                    isFirstWord = false;
                }
            }

            switch (al){
                case LEFT:
                    cursorPos.x = -(size.x/2);
                    break;
                case RIGHT:
                    cursorPos.x = (size.x/2) - lineSize;
                    break;
                case CENTERED:
                    cursorPos.x = -(lineSize/2);
                    break;
            }

            for (int i = 0; i < line.length(); i++) {
                int charIndex = line.charAt(i);
                //Log.w("makeText", "making " + line.charAt(i));
                charDropDown = font.getCharDropDown(charIndex);
                cursorUV = new Vector2f((float) Math.floorMod(charIndex, font.atlasCharWidth) / (float) font.atlasCharWidth + ((float)font.getCharFarLeft(charIndex) / (float) font.textureAtlasWidth), ((float) Math.floor(charIndex / font.atlasCharWidth) / (float) font.atlasCharWidth) + ((float) charDropDown / (float) font.textureAtlasWidth));
                charSize = new Vector2f(font.getCharWidth(charIndex), font.getCharHeight(charIndex));
                int n = (j-line.length()) + i;
                if(charIndex == ' ') {
                    charSize.x = font.getCharWidth('_');
                } else if(charIndex =='\n'){
                    switch (al){
                        case LEFT:
                            cursorPos.x = -(size.x/2);
                            break;
                        case RIGHT:
                            cursorPos.x = (size.x/2) - lineSize;
                            break;
                        case CENTERED:
                            cursorPos.x = -(lineSize/2);
                            break;
                    }
                    isFirstWord = true;
                }else {

                    pos[(((n * 6) + 0) * 2) + 0] = cursorPos.x;
                    pos[(((n * 6) + 0) * 2) + 1] = cursorPos.y - (charDropDown * scaler);
                    UV[(((n * 6) + 0) * 2) + 0] = cursorUV.x;
                    UV[(((n * 6) + 0) * 2) + 1] = cursorUV.y;

                    pos[(((n * 6) + 1) * 2) + 0] = cursorPos.x + (charSize.x * scaler);
                    pos[(((n * 6) + 1) * 2) + 1] = cursorPos.y - (charDropDown * scaler);
                    UV[(((n * 6) + 1) * 2) + 0] = cursorUV.x + (charSize.x / font.textureAtlasWidth);
                    UV[(((n * 6) + 1) * 2) + 1] = cursorUV.y;

                    pos[(((n * 6) + 2) * 2) + 0] = cursorPos.x;
                    pos[(((n * 6) + 2) * 2) + 1] = cursorPos.y + ((charSize.y - charDropDown) * scaler);
                    UV[(((n * 6) + 2) * 2) + 0] = cursorUV.x;
                    UV[(((n * 6) + 2) * 2) + 1] = cursorUV.y - (charSize.y / font.textureAtlasWidth);

                    pos[(((n * 6) + 3) * 2) + 0] = cursorPos.x + (charSize.x * scaler);
                    pos[(((n * 6) + 3) * 2) + 1] = cursorPos.y - (charDropDown * scaler);
                    UV[(((n * 6) + 3) * 2) + 0] = cursorUV.x + (charSize.x / font.textureAtlasWidth);
                    UV[(((n * 6) + 3) * 2) + 1] = cursorUV.y;

                    pos[(((n * 6) + 4) * 2) + 0] = cursorPos.x;
                    pos[(((n * 6) + 4) * 2) + 1] = cursorPos.y + ((charSize.y - charDropDown) * scaler);
                    UV[(((n * 6) + 4) * 2) + 0] = cursorUV.x;
                    UV[(((n * 6) + 4) * 2) + 1] = cursorUV.y - (charSize.y / font.textureAtlasWidth);

                    pos[(((n * 6) + 5) * 2) + 0] = cursorPos.x + (charSize.x * scaler);
                    pos[(((n * 6) + 5) * 2) + 1] = cursorPos.y + ((charSize.y - charDropDown) * scaler);
                    UV[(((n * 6) + 5) * 2) + 0] = cursorUV.x + (charSize.x / font.textureAtlasWidth);
                    UV[(((n * 6) + 5) * 2) + 1] = cursorUV.y - (charSize.y / font.textureAtlasWidth);
                }
                cursorPos.x += (charSize.x * scaler) + charSpacing;
            }
            cursorPos.y -= (float) (font.cellWidth - font.padding) * scaler + lineSpacing;
        }

        //Log.w("makeText", (System.currentTimeMillis()-timer) + " milliseconds to make text model of : " + text);
        canSetText = true;
        toUpdate = false;

        this.textModel = Loader.loadTextToVAO(pos,UV);
    }
}
