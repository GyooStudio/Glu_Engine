package com.glu.engine.Scene;

import com.glu.engine.vectors.Vector3f;

public class Light implements Comparable<Light>{
    public Vector3f position;
    public Vector3f color;
    public float intensity;
    public float radius;

    public String name = "name";

    public Light(Vector3f p, Vector3f c, float i){
        position = p;
        color = c;
        intensity = i;
        radius = (i/0.2f)*( i/0.2f);
    }

    @Override
    public int compareTo(Light obj){return (int)(this.radius*10.0 - obj.radius*10.0);}

    public Light copy(){
        return new Light(position,color,intensity);
    }
}
