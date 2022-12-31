package com.glu.engine.vectors;

public class Vector2f {
    public float x;
    public float y;

    public Vector2f(float x,float y){
        this.x = x;
        this.y = y;
    }

    public Vector2f(float xy){
        this.x = xy;
        this.y = xy;
    }

    public void add(Vector2f b){
        x += b.x;
        y += b.y;
    }

    public void sub(Vector2f b){
        x -= b.x;
        y -= b.y;
    }

    public void scale(float s){
        x *= s;
        y *= s;
    }

    public void multiply(Vector2f m){
        x *= m.x;
        y *= m.y;
    }

    public void divide(Vector2f d){
        x = x/d.x;
        y = y/d.y;
    }

    public void normalize(){
        x = x/length();
        y = y/length();
    }

    public static Vector2f add(Vector2f a, Vector2f b){
        return new Vector2f(a.x+b.x, a.y + b.y);
    }

    public static Vector2f sub(Vector2f a, Vector2f b){
        return new Vector2f(a.x-b.x, a.y - b.y);
    }

    public static Vector2f scale(Vector2f a, float s){
        return new Vector2f(a.x * s,a.y*s);
    }

    public static Vector2f multiply(Vector2f a, Vector2f b){
        return new Vector2f(a.x*b.x,a.y*b.y);
    }

    public static Vector2f divide(Vector2f a, Vector2f b){
        return new Vector2f(a.x/b.x,a.y/b.y);
    }

    public float length(){
        return (float) Math.sqrt(Math.pow(x,2.0)+Math.pow(y,2.0));
    }

    public static float distance(Vector2f a, Vector2f b){
        return (float) Math.sqrt(Math.pow(a.x-b.x,2.0)+Math.pow(a.y-b.y,2.0));
    }

    public static float dot(Vector2f a,Vector2f b){
        return (a.x*b.x)+(a.y*b.y);
    }

    public static Vector2f normalize(Vector2f a){
        return new Vector2f(a.x/a.length(),a.y/a.length());
    }

    public Vector2f copy(){
        return new Vector2f(x,y);
    }

    public Vector2f negative(){
        return new Vector2f(-x,-y);
    }
}
