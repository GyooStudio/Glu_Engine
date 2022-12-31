package com.glu.engine.GUI;

import com.glu.engine.shader.ColorShader;
import com.glu.engine.vectors.Vector2f;
import com.glu.engine.vectors.Vector4f;

import java.util.ArrayList;

public class ColorSquare extends GUIBase{

    public ArrayList<Vector4f> color = new ArrayList<>();
    public float radius;

    public ColorSquare(Vector2f screenDimensions, Vector4f color){
        super(screenDimensions);
        this.color.add(color);
        radius = 0.0f;
    }

    public ColorSquare(Vector2f screenDimensions){
        super(screenDimensions);
        this.color.add(new Vector4f(0,0,0,1));
        radius = 0.0f;
    }

    public ColorSquare copy(){
        ColorSquare a = new ColorSquare(screenDimensions);
        a.color = (ArrayList<Vector4f>) color.clone();
        a.radius = radius;
        a.copy(this);
        return a;
    }

    @Override
    public void addInstance(){
        super.addInstance();
        color.add(new Vector4f(1,1,1,1));
    }

    public void addInstance(Vector2f pos, float rot, Vector2f scale){
        super.addInstance(pos,rot,scale);
        color.add(new Vector4f(1,1,1,1));
    }

    public void addInstance(Vector2f pos, float rot, Vector2f scale, Vector4f color){
        super.addInstance(pos,rot,scale);
        color.add(color);
    }

    @Override
    public void instance(int index){
        super.instance(index);
        color.add(color.get(index));
    }

    @Override
    public void removeInstance(int index){
        super.removeInstance(index);
        color.remove(index);
    }

}
