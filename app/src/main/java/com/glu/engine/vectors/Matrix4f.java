package com.glu.engine.vectors;

import android.opengl.Matrix;
import android.util.Log;

/* index:
* 0 ; 1 ; 2 ; 3
* 4 ; 5 ; 6 ; 7
* 8 ; 9 ; 10; 11
* 12; 13; 14; 15
*
* use : x + y*4
* */

public class Matrix4f {
    public float[] mat;

    public void set(float[] mat){
        if(mat.length == 16) {
            this.mat = mat;
        }else{
            Log.e("Matrix4f.set()","float mat[] needs to be 16 in length");
        }
    }

    public Matrix4f(){
        mat = new float[16];
        setIdentity();
    }

    public void setIdentity(){
        mat = new float[]
                {1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f};
    }

    public Matrix4f returnIndentity(){
        mat = new float[]
                       {1.0f, 0.0f, 0.0f, 0.0f,
                        0.0f, 1.0f, 0.0f, 0.0f,
                        0.0f, 0.0f, 1.0f, 0.0f,
                        0.0f, 0.0f, 0.0f, 1.0f};
        return this;
    }

    public static Vector3f MultiplyMV(Matrix4f m, Vector3f v){
        float[] vec = new float[]{v.x,v.y,v.z,1.0f};
        float[] res = new float[4];
        Matrix.multiplyMV(res,0,m.mat,0,vec,0);
        return new Vector3f(res[0]/res[3],res[1]/res[3],res[2]/res[3]);
    }

    public static Matrix4f MultiplyMM(Matrix4f m, Matrix4f mb){
        Matrix4f res = new Matrix4f();
        Matrix.multiplyMM(res.mat,0,m.mat,0,mb.mat,0);
        return res;
    }

    public void inverse(){
        Matrix.invertM(mat,0,mat,0);
    }

    public static Matrix4f inverse(Matrix4f mat){
        Matrix4f resMat = new Matrix4f();
        Matrix.invertM(resMat.mat,0,mat.mat,0);
        return resMat;
    }



    public void rotate(Vector3f rotation){
        Matrix4f rot = new Matrix4f();
        Matrix.rotateM(rot.mat,0,rotation.x,1.0f,0.0f,0.0f);
        Matrix.rotateM(rot.mat,0,rotation.y,0.0f,1.0f,0.0f);
        Matrix.rotateM(rot.mat,0,rotation.z,0.0f,0.0f,1.0f);
        this.mat = Matrix4f.MultiplyMM(rot,this).mat;
    }

    public void translate(Vector3f translation){
        Matrix.translateM(mat, 0, mat, 0, translation.x,translation.y,translation.z);
    }

    public void scale(Vector3f scale){
        Matrix.scaleM(mat, 0, mat, 0, scale.x,scale.y,scale.z);
    }

    public Matrix4f copy(){
        Matrix4f r = new Matrix4f();
        for (int i = 0; i < mat.length; i++) {
            r.mat[i] = mat[i];
        }
        return r;
    }
}
