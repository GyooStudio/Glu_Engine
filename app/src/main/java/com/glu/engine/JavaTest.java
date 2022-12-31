package com.glu.engine;

import com.glu.engine.GUI.GUIBase;

public class JavaTest {
    public int a;
    public GUIBase b;

    JavaTest(int a, GUIBase b){
        this.a = a;
        this.b = b;
    }

    public int getA(){
        return a;
    }

    public void setA(int a){
        this.a = a;
    }
}
