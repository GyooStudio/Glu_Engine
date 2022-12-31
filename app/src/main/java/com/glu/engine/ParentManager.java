package com.glu.engine;

import com.glu.engine.vectors.Vector2f;
import com.glu.engine.vectors.Vector3f;

import java.util.ArrayList;

public class ParentManager {

    public ArrayList<Vector2f> guiPos;
    public ArrayList<Float> guiRot;
    public ArrayList<Vector2f> guiScale;
    public ArrayList<Integer> guiParent;
    public ArrayList<Vector3f> objectPos;
    public ArrayList<Vector3f> objectRot;
    public ArrayList<Vector3f> objectScale;
    public ArrayList<Integer> objectParent;

    ParentManager(){
        guiPos.add(null);
        guiRot.add(null);
        guiScale.add(null);
        guiParent.add(null);
        objectPos.add(null);
        objectRot.add(null);
        objectScale.add(null);
        objectParent.add(null);
    }

    public int addObject(Vector3f pos, Vector3f rot, Vector3f scale){
        objectPos.add(pos);
        objectScale.add(scale);
        objectRot.add(rot);
        objectParent.add(0);
        return objectParent.size() - 1;
    }

    public void parentObjectTo(int object, int parent){
        objectParent.set(object,parent);
    }

    public void unparentObject(int object){
        objectParent.set(object,0);
    }

    //public

    //public void unparentObjectKeepTransform(int object){
        //Vector3f pos = objectPos.get(ob)
    //}

    public int addGui(Vector2f pos, float rot, Vector2f scale){
        guiPos.add(pos);
        guiScale.add(scale);
        guiRot.add(rot);
        guiParent.add(0);
        return guiParent.size() - 1;
    }

    public void parentGuiTo(int gui, int parent){
        guiParent.set(gui,parent);
    }

    public void unparentGui(int gui){
        guiParent.set(gui,0);
    }

}
