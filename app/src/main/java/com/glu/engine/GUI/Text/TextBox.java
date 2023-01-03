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

    public TextBox(Font font, Vector2f size, Vector2f position, Vector2f scale, float rotation){
        this.font = font;
        this.position = position;
        this.size = size;
        this.scale = scale;
        this.rotation = rotation;
        text = "Text.";
        show = true;
    }

    public TextBox(Font font){
        this.font = font;
        this.position = new Vector2f(0,0);
        this.size = new Vector2f(400,400);
        this.scale = new Vector2f(1,1);
        this.rotation = 0;
        text = "Text.";
        show = true;
    }

    public void setText(String text, float charSpacing, float lineSpacing, Alignment al){
        //while(!canSetText){}
        if(canSetText && text != this.text) {
            this.text = text;
            this.charSpacing = charSpacing;
            this.lineSpacing = lineSpacing;
            this.toUpdate = true;
            this.al = al;
        }
    }

    public void makeText(){
        canSetText = false; // make sure the text isn't changing while building
        long timer = System.currentTimeMillis(); // just a timer

        Log.w("makeText", "" + text); // logging

        float[] pos = new float[text.length()*6*2]; //the vertices's final positions
        float[] UV = new float[text.length()*6*2]; // the vertices's final UV coordinates

        float scaler = 30f / (font.cellWidth - font.padding);
        Vector2f cursorPos; //we will be building from the cursor's position, moving it at each character
        Vector2f cursorUV; // the cursor's position on the texture atlas
        Vector2f charSize; // the current character's size
        float charDropDown; // the current character's bounding boxe's min y with respect to the bottom line ( how much lower than ____ is the min y of the bounding box?)

        String line; // current line
        String word; // We will build the line word by word, this enables us to cut between words when we need to come back to line
        int wordSize; // the visual length of the word
        int lineSize; // the visual length of the line
        boolean isFirstWord = true; // is it the first word of the line?
        boolean hasCompletedWord;
        boolean hasCompletedLine;

        //setup cursor position according to the alignment
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
            isFirstWord = true;
            line = "";
            lineSize = 0;
            j--;

            // build a line
            while (!hasCompletedLine) {
                word = "";
                hasCompletedWord = false;

                // build a word
                while (!hasCompletedWord) {
                    if (j < text.length()) { //just make sure we didn't hit the end of the text
                        char a = text.charAt(j);
                        // these characters are tested to split the line ( [space] - , . ! ? / : ;)
                        if (a == ' ' || a == '-' || a == ',' || a == '.' || a == '!' || a == '?' || a == '/' || a == ':' || a == ';') {
                            hasCompletedWord = true;
                            word += text.charAt(j);
                        }else if(a == '\n'){ // check for return to line
                            word += text.charAt(j);
                            hasCompletedWord = true;
                            hasCompletedLine = true;
                        } else { // otherwise, just add the character to the line
                            word += text.charAt(j);
                        }
                        j++;
                    } else {
                        //if we hit the end of the text
                        hasCompletedWord = true;
                        hasCompletedLine = true;
                    }
                }

                //get the word's visual length
                wordSize = 0;
                for (int i = 0; i < word.length(); i++) {
                    wordSize += (float) font.getCharWidth(word.charAt(i)) * scaler;
                }

                //check to see if we should return to line
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
