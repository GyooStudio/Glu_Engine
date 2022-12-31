package com.glu.engine.shader.compute;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;

import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector2f;
import com.glu.engine.vectors.Vector3f;
import com.glu.engine.vectors.Vector4f;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL;

public abstract class ComputeVertexShader {

    public int programID;
    public int shader;

    public String code;

    private int vaoID;
    private int transformFeedbackID;
    private int outputBufferID;

    private int VaryingsAmount;
    private int OutputVariableSize;
    private int PointsNumber;

    public ComputeVertexShader(String code){
        this.code = code;
    }

    public void buildShader(){
        shader = loadShader(code);
        programID = GLES30.glCreateProgram();
        GLES30.glAttachShader(programID,shader);

        GLES30.glLinkProgram(programID);
        GLES30.glValidateProgram(programID);
        final int[] compileStatus = new int[1];
        GLES30.glGetProgramiv(programID, GLES30.GL_LINK_STATUS, compileStatus, 0);
        if(compileStatus[0] == 0){
            Log.e("Program","Something occurred with the program");
            Log.e("Program","[HERE ->]" + GLES30.glGetProgramInfoLog(programID));

            System.exit(-1);
        }

        start();
        bindAttributes();
        getAllUniforms();
        stop();

        vaoID = createVAO();
        transformFeedbackID = createTransformFeedback();

        int[] bufferID = new int[1];
        GLES30.glGenBuffers(1,bufferID,0);
        outputBufferID = bufferID[0];
    }

    public void define(String argument,boolean is){
        if(!is) {
            code = code.replace("#define " + argument, " ");
        }
    }

    public abstract void bindAttributes();

    public void bindAttribute(int vbo, String attribute){
        GLES30.glBindAttribLocation(programID,vbo,attribute);
    }

    private int loadShader(String code){
        int shader = GLES30.glCreateShader(GLES20.GL_VERTEX_SHADER);

        GLES30.glShaderSource(shader,code);
        GLES30.glCompileShader(shader);
        final int[] compileStatus = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0);
        if(compileStatus[0] == 0){
            System.err.println("Could not compile compute vertex shader");
            System.err.println("[HERE ->]" + GLES30.glGetShaderInfoLog(shader));
            Log.e("Shader Source", GLES30.glGetShaderSource(shader));
            GLES30.glDeleteShader(shader);
            shader = 0;
            System.exit(-1);
        }

        return shader;
    }

    public abstract void getAllUniforms();

    public int getUniformLocation(String uniform){
        return GLES30.glGetUniformLocation(programID,uniform);
    }

    public void loadUniformFloat(int location, float value){
        GLES30.glUniform1f(location,value);
    }

    public void loadUniformVector(int location, Vector3f vector){
        GLES30.glUniform3f(location,vector.x,vector.y,vector.z);
    }

    public void loadUniformVector(int location, Vector2f vector){
        GLES30.glUniform2f(location,vector.x,vector.y);
    }

    public void loadUniformVector(int location, Vector4f vector){
        GLES30.glUniform4f(location,vector.x,vector.y,vector.z,vector.w);
    }

    public void loadUniformBoolean(int location, boolean bool){
        if(bool){
            GLES30.glUniform1i(location,1);
        }else{
            GLES30.glUniform1i(location,0);
        }
    }

    public void loadUniformMatix(int location, Matrix4f matrix){
        GLES30.glUniformMatrix4fv(location,1,false,matrix.mat,0);
    }

    public void loadUniformTexture(int location, int textureUnit){
        GLES30.glUniform1i(location, textureUnit);
    }

    public void loadUniformInt(int location,int number){
        GLES30.glUniform1i(location,number);
    }

    public void start(){
        GLES30.glUseProgram(programID);
    }

    public void stop(){
        GLES30.glUseProgram(0);
    }

    private static int createVAO(){
        int[] vaoID =new int[1];
        GLES30.glGenVertexArrays(1,vaoID,0);
        GLES30.glBindVertexArray(vaoID[0]);
        return  vaoID[0];
    }

    private static int createTransformFeedback(){
        int[] trFdbckID = new int[1];
        GLES30.glGenTransformFeedbacks(1, trFdbckID,0);
        GLES30.glBindTransformFeedback(GLES30.GL_TRANSFORM_FEEDBACK,trFdbckID[0]);
        return trFdbckID[0];
    }

    public void loadVaryingInt(int index, int[] data){
        int[] vboID = new int[1];
        GLES30.glGenBuffers(1,vboID,0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,vboID[0]);
        IntBuffer intBuffer = makeIntBuffer(data);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,data.length*4,intBuffer,GLES30.GL_STATIC_DRAW);
        GLES30.glVertexAttribPointer(index, 1, GLES30.GL_FLOAT, false, 0,0 );
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,0);
    }

    public void loadVaryingFloat(int index, float[] data){
        GLES30.glBindVertexArray(vaoID);
        storeDataInVAO(index, 1, data);
        GLES30.glBindVertexArray(0);
    }

    public void loadVaryingVector2f(int index, float[] vec){
        GLES30.glBindVertexArray(vaoID);
        storeDataInVAO(index, 2, vec);
        GLES30.glBindVertexArray(0);
    }

    public void loadVaryingVector3f(int index, float[] vec){
        GLES30.glBindVertexArray(vaoID);
        storeDataInVAO(index, 3, vec);
        GLES30.glBindVertexArray(0);
    }

    public void loadVaryingVector4f(int index, float[] vec){
        GLES30.glBindVertexArray(vaoID);
        storeDataInVAO(index, 4, vec);
        GLES30.glBindVertexArray(0);
    }

    private static void storeDataInVAO(int index, int dimensions, float[] data){
        int[] vboID = new int[1];
        GLES30.glGenBuffers(1,vboID,0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,vboID[0]);
        FloatBuffer floatBuffer = makeFloatBuffer(data);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,data.length*4,floatBuffer,GLES30.GL_STATIC_DRAW);
        GLES30.glVertexAttribPointer(index, dimensions, GLES30.GL_FLOAT, false, 0,0 );
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,0);
    }

    public static FloatBuffer makeFloatBuffer(float[] array){
        FloatBuffer floatBuffer = ByteBuffer.allocateDirect(array.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        floatBuffer.put(array).position(0);
        //Log.w("makeFloatBuffer", array.length * 4 +" bytes buffer created");
        return floatBuffer;
    }

    public static IntBuffer makeIntBuffer(int[] data){
        IntBuffer buffer = ByteBuffer.allocateDirect(data.length*Integer.BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();
        buffer.put(data).position(0);
        //Log.w("makeIntBuffer", data.length * 2 +" bytes buffer created");
        return buffer;
    }

    public void prepareToCompute(int varyingsAmount, int outputVariableSize){
        VaryingsAmount = varyingsAmount;
        OutputVariableSize = outputVariableSize;

        start();
        GLES30.glBindVertexArray(vaoID);
        for (int i = 0; i < varyingsAmount; i++) {
            GLES30.glEnableVertexAttribArray(i);
        }

        GLES30.glBindTransformFeedback(GLES30.GL_TRANSFORM_FEEDBACK,transformFeedbackID);
        GLES30.glBindBuffer(GLES30.GL_TRANSFORM_FEEDBACK,outputBufferID);
        GLES30.glBufferData(GLES30.GL_TRANSFORM_FEEDBACK,outputVariableSize*4,null,GLES30.GL_DYNAMIC_READ);

        GLES30.glBeginTransformFeedback(GLES30.GL_POINTS);
    }

    public void Render(int pointsNumber){
        PointsNumber = pointsNumber;

        GLES30.glDrawArrays(GLES30.GL_POINTS,0,pointsNumber);
        GLES30.glEndTransformFeedback();
        GLES30.glBindVertexArray(0);
        GLES30.glBindTransformFeedback(GLES30.GL_TRANSFORM_FEEDBACK,0);
    }

    public float[] retreiveDataAsFloat(){
        int error = GLES30.glGetError();
        GLES30.glUnmapBuffer(GLES30.GL_TRANSFORM_FEEDBACK_BUFFER);
        error = GLES30.glGetError();
        Buffer i = GLES30.glMapBufferRange(GLES30.GL_TRANSFORM_FEEDBACK_BUFFER,0,4*4,GLES30.GL_MAP_READ_BIT);
        //Log.w("retreiveData","" + i);
        error = GLES30.glGetError();
        switch(error){
            case GLES30.GL_INVALID_ENUM: Log.e("GLError", "INVALID ENUM");
                break;
            case GLES30.GL_INVALID_VALUE: Log.e("GLError", "INVALID VALUE");
                break;
            case GLES30.GL_INVALID_OPERATION: Log.e("GLError", "INVALID OPERATION");
                break;
            case GLES30.GL_INVALID_FRAMEBUFFER_OPERATION: Log.e("GLError", "INVALID FRAMEBUFFER OPERATION");
                break;
            case GLES30.GL_OUT_OF_MEMORY: Log.e("GLError", "INVALID OUT OF MEMORY");
                break;
            case GLES30.GL_NO_ERROR: Log.e("GLError", "NO ERROR");
                break;
        }
        //Log.w("retreiveData","" + );
        //GLES30.glFlushMappedBufferRange(GLES30.GL_TRANSFORM_FEEDBACK_BUFFER,0,OutputVariableSize*4);
        System.exit(-1);
        return null; //floatBuffer.array();
    }

    public int[] retreiveDataAsInt(){
        IntBuffer intBuffer = (IntBuffer) GLES30.glMapBufferRange(GLES30.GL_TRANSFORM_FEEDBACK_BUFFER,0,OutputVariableSize,GLES30.GL_MAP_READ_BIT);
        return intBuffer.array();
    }
}
