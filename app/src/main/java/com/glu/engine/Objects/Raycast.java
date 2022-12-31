package com.glu.engine.Objects;

import com.glu.engine.vectors.Vector3f;

import java.util.ArrayList;

public class Raycast {
    public Vector3f pos;
    public Vector3f dir;
    public ArrayList<Vector3f> hitpos = new ArrayList<>();
    public int numberOfHits;

    private float hitDist;
    private float hitDepth;

    public ArrayList<Entity> hitEntities = new ArrayList<>();
    public ArrayList<Integer> hitEntitiesInstanceIndex = new ArrayList<>();

    public boolean hit;

    public Raycast(Vector3f pos, Vector3f dir){
        this.pos = pos;
        this.dir = Vector3f.normalize(dir);
        hit = false;
        numberOfHits = 0;
    }

    Raycast(){
        hit = false;
        numberOfHits = 0;
    }

    public void addHitPos(Vector3f pos){
        numberOfHits ++;
        if(!hit){
            hit = true;
        }
        hitpos.add(pos);
    }

    public void addHitPos(int index, Vector3f pos){
        numberOfHits ++;
        if(!hit){
            hit = true;
        }
        hitpos.add(index,pos);
    }

    public void addHitEntity(Entity e, int instanceIndex){
        hitEntities.add(e);
        hitEntitiesInstanceIndex.add(instanceIndex);
    }

    public void addHitEntity(int index, Entity e, int instanceIndex){
        hitEntities.add(index, e);
        hitEntitiesInstanceIndex.add(index, instanceIndex);
    }

    public float getHitDist(){
        hitDist = Vector3f.distance(pos,hitpos.get(0));
        return hitDist;
    }
}
