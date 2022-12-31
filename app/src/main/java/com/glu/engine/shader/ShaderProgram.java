package com.glu.engine.shader;

import android.opengl.GLES30;
import android.util.Log;

import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector2f;
import com.glu.engine.vectors.Vector3f;
import com.glu.engine.vectors.Vector4f;

public abstract class ShaderProgram {

        public int programID;
        public int vertexShader;
        public int fragmentShader;

        public String vertCode;
        public String fragCode;

        public boolean isShaderBuilt = false;

        public ShaderProgram(String vertCode, String fragCode){
            this.vertCode = vertCode;
            this.fragCode = fragCode;
        }

        public void buildShader(){
            vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertCode);
            fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER,fragCode);
            programID = GLES30.glCreateProgram();
            GLES30.glAttachShader(programID,vertexShader);
            GLES30.glAttachShader(programID,fragmentShader);

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

            isShaderBuilt = true;
        }

        public void define(int code, String argument,boolean is){
            if(!is) {
                if (code == GLES30.GL_VERTEX_SHADER) {
                    vertCode = vertCode.replace("#define " + argument, " ");
                }else if(code == GLES30.GL_FRAGMENT_SHADER){
                    fragCode = fragCode.replace("#define " + argument, " ");
                }
            }
        }

        public abstract void bindAttributes();

        public void bindAttribute(int vbo, String attribute){
            GLES30.glBindAttribLocation(programID,vbo,attribute);
        }

        private int loadShader(int type, String code){
            int shader = GLES30.glCreateShader(type);

            GLES30.glShaderSource(shader,code);
            GLES30.glCompileShader(shader);
            final int[] compileStatus = new int[1];
            GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0);
            if(compileStatus[0] == 0){
                System.err.println("Could not compile shader type " + type);
                //frag = 35632
                //vert = 35633
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
}
