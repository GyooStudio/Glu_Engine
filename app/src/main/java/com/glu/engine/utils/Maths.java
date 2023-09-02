package com.glu.engine.utils;

import android.opengl.Matrix;
import android.util.Log;

import com.glu.engine.Scene.Camera;
import com.glu.engine.Scene.Scene;
import com.glu.engine.Scene.SunLight;
import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector2f;
import com.glu.engine.vectors.Vector3f;

public class Maths {

    public static Matrix4f createTransformationMatrix(Vector3f translation, Vector3f rotation, Vector3f scale){
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        Matrix.translateM(matrix.mat,0,translation.x,translation.y,translation.z);
        Matrix.rotateM(matrix.mat,0,rotation.x,1.0f,0.0f,0.0f);
        Matrix.rotateM(matrix.mat,0,rotation.y,0.0f,1.0f,0.0f);
        Matrix.rotateM(matrix.mat,0,rotation.z,0.0f,0.0f,1.0f);
        Matrix.scaleM(matrix.mat, 0,scale.x,scale.y,scale.z);
        return matrix;
    }

    public static Matrix4f createTransformationMatrix(Vector2f translation, float rotation, Vector2f scale){
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        Matrix.translateM(matrix.mat,0,translation.x,translation.y,0);
        Matrix.rotateM(matrix.mat,0,rotation,0.0f,0.0f,1.0f);
        Matrix.scaleM(matrix.mat, 0,scale.x,scale.y,1.0f);
        return matrix;
    }

    public Matrix4f createTranslationMatrix(Vector3f translation){
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        Matrix.translateM(matrix.mat,0,translation.x,translation.y,0);
        return matrix;
    }

    public Matrix4f createScaleMatrix(Vector3f scale){
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        Matrix.scaleM(matrix.mat, 0,scale.x,scale.y,1.0f);
        return matrix;
    }

    public static Matrix4f createRotationMatrix(Vector3f rotation){
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        Matrix.rotateM(matrix.mat,0,rotation.x,1.0f,0.0f,0.0f);
        Matrix.rotateM(matrix.mat,0,rotation.y,0.0f,1.0f,0.0f);
        Matrix.rotateM(matrix.mat,0,rotation.z,0.0f,0.0f,1.0f);
        return matrix;
    }

    public static Matrix4f generateViewMatrix(Camera camera){
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        Matrix.rotateM(matrix.mat,0,-camera.getRotation().x,1.0f,0.0f,0.0f);
        Matrix.rotateM(matrix.mat,0,-camera.getRotation().y,0.0f,1.0f,0.0f);
        Matrix.rotateM(matrix.mat,0,-camera.getRotation().z,0.0f,0.0f,1.0f);
        Matrix.translateM(matrix.mat,0,-camera.getPosition().x,-camera.getPosition().y,-camera.getPosition().z);
        return matrix;
    }

    public static float lessThan(float a, float b){
        if(a<b){
            return 1.0f;
        }
        return 0.0f;
    }

    public static Vector2f closestOnLine(Vector2f ptOne, Vector2f ptTwo, Vector2f ptTest){
        float m;
        if(ptOne.x-ptTwo.x != 0) {
            m = (ptOne.y - ptTwo.y) / (ptOne.x - ptTwo.x);
        }else{
            m = 0;
        }
        float b = ptOne.y - (m*ptOne.x);

        float _b = ptTest.y-(ptTest.x*(-1/m));

        float f = Math.min(Math.max(ptOne.x,ptTwo.x),Math.max(Math.min(ptOne.x,ptTwo.x),(_b-b)/(((m*m)+1)/m)));

        return new Vector2f(f, f*m+b);
    }

    public static float mix(float a, float b, float m){
        return (a*(1-m))+(b*m);
    }

    public static Matrix4f[] generateSunTransformMatrix(Scene scene){
        Matrix4f proj = new Matrix4f();
        proj.setIdentity();
        Matrix.perspectiveM(proj.mat,0,scene.FOV,1f,Scene.NEAR_PLANE,scene.sunLight.shadowDist);
        Matrix4f camTransform = generateViewMatrix(scene.camera);
        float[] res = new float[16];
        Matrix.multiplyMM(res,0, proj.mat, 0,camTransform.mat,0);
        proj.mat = res;
        Matrix.invertM(res, 0,proj.mat,0);
        proj.mat = res;
        Matrix4f sunView = Maths.createRotationMatrix(Vector3f.lookAt(new Vector3f(0),scene.sunLight.direction,0).negative());

        Vector3f[] NDC = new Vector3f[]{
                new Vector3f(-1f,-1f,-1f),
                new Vector3f(1f,-1f,-1f),
                new Vector3f(-1f,1f,-1f),
                new Vector3f(1f,1f,-1f),
                new Vector3f(-1f,-1f,1f),
                new Vector3f(1f,-1f,1f),
                new Vector3f(-1f,1f,1f),
                new Vector3f(1f,1f,1f),
        };

        Vector3f min = new Vector3f( Float.MAX_VALUE);
        Vector3f max = new Vector3f( -Float.MAX_VALUE);
        Vector3f center = new Vector3f(0);

        for(int i = 0; i < 8; i++){
            float[] vector = new float[]{NDC[i].x,NDC[i].y,NDC[i].z,1f};
            float[] result = new float[]{0f,0f,0f,0f};

            Matrix.multiplyMV(result,0,proj.mat,0,vector,0);
            result[0] = result[0]/result[3];
            result[1] = result[1]/result[3];
            result[2] = result[2]/result[3];
            result[3] = 1f;
            Matrix.multiplyMV(vector,0,sunView.mat,0,result,0);
            center.add(new Vector3f(vector[0],vector[1],vector[2]));

            min.x = Float.min(vector[0],min.x);
            min.y = Float.min(vector[1],min.y);
            min.z = Float.min(vector[2],min.z);
            max.x = Float.max(vector[0],max.x);
            max.y = Float.max(vector[1],max.y);
            max.z = Float.max(vector[2],max.z);
        }

        center.scale(1f/8f);

        /*float[] vector = new float[]{0f,0f,-1f,1f};
        float[] result = new float[]{0f,0f,0f,0f};
        Matrix.multiplyMV(result,0,sunView.mat,0,vector,0);

        center = new Vector3f(result[0],result[1],result[2]);*/

        Matrix4f transform = Maths.createTransformationMatrix(center.negative(),Vector3f.lookAt(new Vector3f(0),scene.sunLight.direction,0).negative(),new Vector3f(1f));

        proj.mat = new float[]{
                2f/(max.x-min.x),0f,0f,0f,
                0f,2f/(max.y-min.y),0f,0f,
                0f,0f,-2f/(2f*scene.sunLight.shadowDist),0f,
                0f,0f,0f,1f
        };

        return new Matrix4f[]{transform,proj};
    }
}
