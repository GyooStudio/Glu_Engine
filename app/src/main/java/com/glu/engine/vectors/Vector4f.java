package com.glu.engine.vectors;

public class Vector4f {

    public float x;
    public float y;
    public float z;
    public float w;

    public Vector4f(float x,float y,float z,float w){
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vector4f(float xyzw){
        this.x = xyzw;
        this.y = xyzw;
        this.z = xyzw;
        this.w = xyzw;
    }

    public void add(Vector4f b){
        x += b.x;
        y += b.y;
        z += b.z;
        w += b.w;
    }

    public void sub(Vector4f b){
        x -= b.x;
        y -= b.y;
        z -= b.z;
        w -= b.w;
    }

    public void scale(float s){
        x *= s;
        y *= s;
        z *= s;
        w *= s;
    }

    public void multiply(Vector4f m){
        x *= m.x;
        y *= m.y;
        z *= m.z;
        w *= m.w;
    }

    public void divide(Vector4f d){
        x = x/d.x;
        y = y/d.y;
        z = z/d.z;
        w = w/d.w;
    }

    public void normalize(){
        x = x/length();
        y = y/length();
        z = z/length();
        w = w/length();
    }

    public float length(){
        return (float) Math.sqrt(Math.pow(x,2.0)+Math.pow(y,2.0)+Math.pow(z,2.0)+Math.pow(w,2.0));
    }

    public Vector4f copy(){
        return new Vector4f(x,y,z,w);
    }

    public static Vector4f add(Vector4f a, Vector4f b){
        return new Vector4f(a.x+b.x, a.y + b.y, a.z+b.z, a.w+b.w);
    }

    public static Vector4f sub(Vector4f a, Vector4f b){
        return new Vector4f(a.x-b.x, a.y - b.y, a.z-b.z, a.w-b.w);
    }

    public static Vector4f scale(Vector4f a, float s){
        return new Vector4f(a.x * s,a.y*s, a.z*s, a.w*s);
    }

    public static Vector4f multiply(Vector4f a, Vector4f b){
        return new Vector4f(a.x*b.x,a.y*b.y, a.z*b.z, a.w*b.w);
    }

    public static Vector4f divide(Vector4f a, Vector4f b){
        return new Vector4f(a.x/b.x,a.y/b.y, a.y/b.z, a.w/b.w);
    }

    public static float dot(Vector4f a,Vector4f b){
        return (a.x*b.x)+(a.y*b.y)+(a.z*b.z)+(a.w*b.w);
    }

    public static Vector4f normalize(Vector4f a){
        return new Vector4f(a.x/a.length(),a.y/a.length(),a.z/a.length(), a.w/a.length());
    }

    public static float distance(Vector4f a, Vector4f b){
        return (float) Math.sqrt(Math.pow(a.x-b.x,2.0)+Math.pow(a.y-b.y,2.0)+Math.pow(a.z-b.z,2.0)+Math.pow(a.w-b.w,2.0));
    }

    public Vector4f negative(){
        return new Vector4f(-x,-y,-z,-w);
    }
}
