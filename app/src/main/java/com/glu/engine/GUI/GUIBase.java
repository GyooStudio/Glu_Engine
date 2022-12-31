package com.glu.engine.GUI;

import com.glu.engine.Objects.RawModel;
import com.glu.engine.utils.Loader;
import com.glu.engine.vectors.Vector2f;
import com.glu.engine.vectors.Vector4f;

import java.util.ArrayList;

public abstract class GUIBase {

    public ArrayList<Vector2f> position = new ArrayList<>();
    public ArrayList<Float> rotation = new ArrayList<Float>();
    public ArrayList<Vector2f> scale = new ArrayList<>();
    public ArrayList<Boolean> show = new ArrayList<>();
    public final float[] positions = new float[]{
            -1, 1, -1,
            -1,-1, -1,
            1, 1, -1,
            1,-1, -1};
    public Vector2f screenDimensions;

    public String name = "name";

    public int number = 1;

    public RawModel model;

    public GUIBase(Vector2f screenDimensions){
        this.position.add(new Vector2f(0,0));
        this.rotation.add(new Float(0));
        this.scale.add(new Vector2f(1,1));
        show.add(true);
        this.screenDimensions = screenDimensions;
    }

    public void copy(GUIBase a){
        position = new ArrayList<>(a.position);
        rotation = new ArrayList<>(a.rotation);
        scale = new ArrayList<>(a.scale);
        show = new ArrayList<>(a.show);
        model = a.model;
        screenDimensions = a.screenDimensions.copy();
    }

    public void makeModel(){
        model = Loader.loadToVAO(positions);
    }

    public void addInstance(){
        position.add(new Vector2f(0,0));
        rotation.add(new Float(0));
        scale.add(new Vector2f(1,1));
        show.add(new Boolean(true));
        number ++;
    }

    public void addInstance(Vector2f pos, float rot, Vector2f scale){
        position.add(pos);
        rotation.add(new Float(rot));
        scale.add(scale);
        show.add(true);
        number ++;
    }

    public void instance(int index){
        position.add(position.get(index));
        rotation.add(rotation.get(index));
        scale.add(scale.get(index));
        show.add(show.get(index));
        number ++;
    }

    public void removeInstance(int index){
        position.remove(index);
        rotation.remove(index);
        scale.remove(index);
        show.remove(index);
        number --;
    }

}
