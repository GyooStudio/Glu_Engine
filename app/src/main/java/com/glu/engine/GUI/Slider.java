package com.glu.engine.GUI;

import android.util.Log;

import com.glu.engine.Scene.Ressources;
import com.glu.engine.shader.ColorShader;
import com.glu.engine.utils.Maths;
import com.glu.engine.vectors.Vector2f;

public final class Slider {
    public float maxValue;
    public float minValue;
    public float currentValue;
    public float step;
    public int decimalNumber = 10;

    public Vector2f position;
    public Vector2f size;
    public float rotation;

    public GUIBase button;
    public GUIBase slider;

    public boolean isClicked = false;

    public int pointerClicked = 0;

    public String name = "name";

    public int id = 0;

    private Ressources ressources = Ressources.getRessources();

    public Slider(GUIBase button, GUIBase slider){
        maxValue = 1;
        minValue = 0;
        currentValue = 0.5f;
        step = 0;
        position = new Vector2f(0,0);
        size = new Vector2f(100,10);
        rotation = ((float) Math.PI / 180f);

        this.button = button;
        button.scale.set(0,new Vector2f(11,11));
        button.position.set(0,position);
        this.slider = slider;
        slider.scale.set(0,size);
        slider.position.set(0,position);
    }

    public Slider(GUIBase button, GUIBase slider,float min, float max){
        maxValue = max;
        minValue = min;
        currentValue = (max + min)/2;
        step = 0;
        position = new Vector2f(0,0);
        size = new Vector2f(100,10);
        rotation = ((float) Math.PI / 180f);

        this.button = button;
        button.scale.set(0,new Vector2f(11,11));
        button.position.set(0,position);
        this.slider = slider;
        slider.scale.set(0,size);
        slider.position.set(0,position);
    }

    public Slider(GUIBase button, GUIBase slider,float min, float max, float step){
        maxValue = max;
        minValue = min;
        currentValue = (max + min)/2;
        this.step = step;
        position = new Vector2f(0,0);
        size = new Vector2f(100,10);
        rotation = ((float) Math.PI / 180f);

        this.button = button;
        button.scale.set(0,new Vector2f(11,11));
        button.position.set(0,position);
        this.slider = slider;
        slider.scale.set(0,size);
        slider.position.set(0,position);
    }

    public void click(Vector2f click,int pointer){
        Vector2f pos = Vector2f.scale(Vector2f.sub(click,Vector2f.scale(ressources.viewport, 0.5f)),2f);
        Vector2f bpos = button.position.get(0);
        Vector2f bscale = button.scale.get(0);
        if(((pos.x>bpos.x-bscale.x)&&(pos.x<bpos.x+bscale.x))&&
                ((pos.y>bpos.y-bscale.y)&&(pos.y<bpos.y+bscale.y))){
            isClicked =true;
            pointerClicked = pointer;
        }
        if (isClicked && pointerClicked == pointer){
            Vector2f ptOne = new Vector2f((float) (position.x + Math.cos(rotation)*size.x), (float) (position.y + Math.sin(rotation)*size.x));
            Vector2f ptTwo = new Vector2f((float) (position.x + Math.cos(rotation+Math.PI)*size.x), (float) (position.y + Math.sin(rotation+Math.PI)*size.x));
            Vector2f finalPos = Maths.closestOnLine(ptOne,ptTwo,pos);
            button.position.set(0,finalPos);

            Vector2f a = Vector2f.sub(ptTwo,ptOne);
            Vector2f b = Vector2f.sub(finalPos,ptOne);
            if(step == 0) {
                currentValue = Maths.mix(maxValue, minValue, b.length() / a.length());
            }else{
                float tmpCurrentValue = Maths.mix(maxValue, minValue, b.length() / a.length());
                currentValue = Math.round((1/step)*tmpCurrentValue)*step;
            }

            long tmp = (long)( currentValue * Math.pow( 10, decimalNumber ) );
            currentValue = (float)( tmp / Math.pow( 10, decimalNumber ) );
        }
    }

    public void setSliderSize(Vector2f size){
        this.size = size;
        slider.scale.set(0,size);
    }

    public void setRotation(float rotation){
        this.rotation = rotation;
        slider.rotation.set(0,(float) Math.toDegrees(rotation));
        button.rotation.set(0,(float) Math.toDegrees(rotation));
    }

    public void setPosition(Vector2f position){
        this.position = position;
        button.position.set(0,Vector2f.add(Vector2f.sub(button.position.get(0),slider.position.get(0)),position));
        slider.position.set(0,position);
    }

    public void scale(float s){
        size.scale(s);
        button.scale.get(0).scale(s);
        button.position.set(0, Vector2f.add(Vector2f.scale(Vector2f.sub(button.position.get(0),slider.position.get(0)),s),slider.position.get(0)));
        slider.scale.get(0).scale(s);
    }

    public void release(int pointer){
        if(pointer == pointerClicked) {
            isClicked = false;
        }
    }

    public void hide(){
        slider.show.set(0,false);
        button.show.set(0,false);
    }

    public void show(){
        slider.show.set(0,true);
        button.show.set(0,true);
    }
}
