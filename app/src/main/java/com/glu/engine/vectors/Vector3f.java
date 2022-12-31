package com.glu.engine.vectors;

public class Vector3f {
    public float x;
    public float y;
    public float z;

    public Vector3f(float x,float y,float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f(Vector3f copy){
        this.x = copy.x;
        this.y = copy.y;
        this.z = copy.z;
    }

    public Vector3f(float xyz){
        this.x = xyz;
        this.y = xyz;
        this.z = x;
    }

    public void add(Vector3f b){
        x += b.x;
        y += b.y;
        z += b.z;
    }

    public void sub(Vector3f b){
        x -= b.x;
        y -= b.y;
        z -= b.z;
    }

    public void scale(float s){
        x *= s;
        y *= s;
        z *= s;
    }

    public void multiply(Vector3f m){
        x *= m.x;
        y *= m.y;
        z *= m.z;
    }

    public void divide(Vector3f d){
        x = x/d.x;
        y = y/d.y;
        z = z/d.z;
    }

    public void normalize(){
        x = x/length();
        y = y/length();
        z = z/length();
    }

    public float length(){
        return (float) Math.sqrt(x*x+y*y+z*z);
    }

    public Vector3f cross(Vector3f c){
        Vector3f r = new Vector3f(0,0,0);
        r.x = (y*c.z)-(z*c.y);
        r.y = (x*c.z)-(z*c.x);
        r.z = (x*c.y)-(y*c.x);
        return r;
    }

    public static float distance(Vector3f a, Vector3f b){
        return (float) Math.sqrt((a.x-b.x) * (a.x-b.x) + (a.y-b.y) * (a.y-b.y) + (a.z-b.z) * (a.z-b.z));
    }

    public static Vector3f add(Vector3f a, Vector3f b){
        return new Vector3f(a.x+b.x, a.y + b.y, a.z+b.z);
    }

    public static Vector3f sub(Vector3f a, Vector3f b){
        return new Vector3f(a.x-b.x, a.y - b.y, a.z-b.z);
    }

    public static Vector3f scale(Vector3f a, float s){
        return new Vector3f(a.x * s,a.y*s, a.z*s);
    }

    public static Vector3f multiply(Vector3f a, Vector3f b){
        return new Vector3f(a.x*b.x,a.y*b.y, a.z*b.z);
    }

    public static Vector3f divide(Vector3f a, Vector3f b){
        return new Vector3f(a.x/b.x,a.y/b.y, a.y/b.z);
    }

    public static float dot(Vector3f a,Vector3f b){
        return (a.x*b.x)+(a.y*b.y)+(a.z*b.z);
    }

    public static Vector3f normalize(Vector3f a){
        return new Vector3f(a.x/a.length(),a.y/a.length(),a.z/a.length());
    }

    public static Vector3f lookAt(Vector3f pos, Vector3f lookPos,float roll){
        Vector3f a = Vector3f.sub(pos,lookPos);
        Vector3f b = new Vector3f(0,0,0);
        Vector3f c = Vector3f.normalize(a);
        b.x = (float)Math.toDegrees(Math.asin(-c.y));
        b.y = (float)Math.toDegrees(Math.atan2(a.x,a.z));
        b.z = roll;
        return b;
    }

    public static Vector3f cross(Vector3f a,Vector3f b){
        Vector3f r = new Vector3f(0,0,0);
        r.x = (a.y*b.z)-(a.z*b.y);
        r.y = (a.x*b.z)-(a.z*b.x);
        r.z = (a.x*b.y)-(a.y*b.x);
        return r;
    }

    public Vector3f copy(){
        return new Vector3f(x,y,z);
    }

    public Vector3f negative(){
        return new Vector3f(-x,-y,-z);
    }
}
