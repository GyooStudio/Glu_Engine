package com.glu.engine.actionManager;

import android.util.Log;

import com.glu.engine.GUI.Button;
import com.glu.engine.GUI.Slider;
import com.glu.engine.Scene.Ressources;
import com.glu.engine.vectors.Vector2f;

import java.util.ArrayList;


/**
* ActionManager is a class that takes track of a point array for multiple touch inputs called
* "pointers", extracts useful information from those points and process this information to
* detect some movement types, called "actions", that are visible in the //action ID.
*
* those action IDs can then be used by an input manager to use them throughout code.
*
* They are stored in an array, so multiple actions can be read at the same time.
*
* Multi-touch is supported, but not handled as multi-touch. ActionManager will only read each
* action individually, as multi-touch may require influence from the UI, which is a task for an
* input manager.
*
* ISSUE: when performing multiple actions on multiple fingers at the same time, the program crashes.
*
*/
public final class ActionManager {
    private static ActionManager actionManager;

    public static final int MAX_POINTERS = 20;
    public int pointerNumber = 0;
    public long timeOfLastRecording;
    public boolean hasManuallyUpdated = false;

    final boolean[] pointerIndices = new boolean[MAX_POINTERS];
    final ArrayList<ArrayList<Vector2f>> actionTrack = new ArrayList<>();
    public ArrayList<Button> buttons = new ArrayList<>();
    public ArrayList<Slider> sliders = new ArrayList<>();
    private Ressources ressources;

    /** action ID*/
    public final ArrayList<ArrayList<actionType>> actions = new ArrayList<>();
    public enum actionType{
        TOUCH,
        LONG_TOUCH,
        NO_ACTION,
        SWIPE,
        SCROLL,
        CIRCLE
    }


    /**action variables*/
    public final Vector2f[] startPosition = new Vector2f[MAX_POINTERS];
    public final Vector2f[] averagePosition = new Vector2f[MAX_POINTERS];
    public final Vector2f[] averageDirectionVector = new Vector2f[MAX_POINTERS];
    public final Vector2f[] lastPoint = new Vector2f[MAX_POINTERS];
    public final Vector2f[] circleCenter = new Vector2f[MAX_POINTERS];
    public final Vector2f[] previousPoint = new Vector2f[MAX_POINTERS];
    public final Vector2f[] velocity = new Vector2f[MAX_POINTERS];
    public final float[] averageDistanceTravelled = new float[MAX_POINTERS];
    public final float[] averageDirectionVectorFollow = new float[MAX_POINTERS];
    public final float[] averageDistanceFromAveragePosition = new float[MAX_POINTERS];
    public final float[] averageCircleFollow = new float[MAX_POINTERS];
    public final float[] circleMaxDistance = new float[MAX_POINTERS];
    public final float[] circleMinDistance = new float[MAX_POINTERS];
    public final float[] circleDistanceDifference = new float[MAX_POINTERS];
    public final float[] distanceTravelled = new float[MAX_POINTERS];
    public final boolean[] hasSetLongTouch = new boolean[MAX_POINTERS];
    public final boolean[] isHasSetScroll = new boolean[MAX_POINTERS];
    public final boolean[] hasSetCircle = new boolean[MAX_POINTERS];
    public final boolean[] hasDoneCircle = new boolean[MAX_POINTERS];
    public final long[] timer = new long[MAX_POINTERS];
    public final long[] time = new long[MAX_POINTERS];

    public final boolean[] isTouching = new boolean[MAX_POINTERS];

    public synchronized static ActionManager getActionManager(){
        if(actionManager != null){
            return actionManager;
        }else{
            return new ActionManager();
        }
    }

    private ActionManager(){
        ressources = Ressources.getRessources();

        for (int i = 0; i < MAX_POINTERS; i++) {
            actionTrack.add(new ArrayList<>());
            actions.add(new ArrayList<>());
        }
    }

    /**starts an action to track*/
    public synchronized void addAction(int index){
        pointerNumber++;
        pointerIndices[index] = true;
        actionTrack.get(index).clear();
        isTouching[index] = true;
        hasSetLongTouch[index] = false;
        isHasSetScroll[index] = false;
        hasSetCircle[index] = false;
        hasDoneCircle[index] = false;
        timer[index] = System.currentTimeMillis();
        timeOfLastRecording = System.currentTimeMillis();
        hasManuallyUpdated = false;
        Log.w("actionManager", "addedAction " + index + ". actionNumber: " + pointerNumber + " isTouching: " + isTouching[index]);
    }

    /** add a point to an action */
    public void addPoint(int index,float x,float y){
        actionTrack.get(index).add(new Vector2f(x, ressources.viewport.y - y));

        boolean succeeded = false;
        int attempts = 0;
        while (!succeeded && attempts < 20) {
            try {
                processAction(index);
                succeeded = true;
            } catch (Exception e) {
                attempts ++;
                Log.e("addPoint", "something went wrong : " + e.getMessage());
                e.printStackTrace();
            }
        }
        timeOfLastRecording = System.currentTimeMillis();
        hasManuallyUpdated = false;
    }

    /** stop an on-going action */
    public void stopAction(int index){
        boolean succeeded = false;
        int attempts = 0;
        while (!succeeded && attempts < 20) {
            try {
                processAction(index);
                pointerIndices[index] = false;
                isTouching[index] = false;
                Log.w("actionManager", "stopped action " + index + ". actionNumber: " + pointerNumber + " isTouching: " + isTouching[index]);
                processAction(index);
                timeOfLastRecording = System.currentTimeMillis();
                hasManuallyUpdated = false;
                pointerNumber--;
                succeeded = true;
            } catch (Exception e) {
                attempts++;
                Log.e("stopAction", "something went wrong!");
                e.printStackTrace();
            }
        }
        if (attempts == 20) {
            pointerNumber--;
            pointerIndices[index] = false;
            isTouching[index] = false;
            Log.w("actionManager", "stopped action " + index + ". actionNumber: " + pointerNumber + " isTouching: " + isTouching[index]);
            timeOfLastRecording = System.currentTimeMillis();
            hasManuallyUpdated = false;
        }
    }

    public void manualUpdate(){
        long time = System.currentTimeMillis()-timeOfLastRecording;
        if(time > 41 && !hasManuallyUpdated){ // if it has been more than a 24th of a second (2.5 60fps frames and 1.25 30fps frames) since the last update.
            for(int i = 0; i < pointerNumber; i ++){
                if(lastPoint[i] != null) {
                    addPoint(i, lastPoint[i].x, ressources.viewport.y - lastPoint[i].y);
                }
            }
            hasManuallyUpdated = true;
            Log.w("actionManager","Updated after " + time + " milliseconds");
            timeOfLastRecording = System.currentTimeMillis();
        }
    }

    /** readAction extracts all the useful information about the array of points given by the system*/
    private void readAction(int index){
        if(actionTrack.get(index).size() > 0) {
            startPosition[index] = actionTrack.get(index).get(0);
            previousPoint[index] = actionTrack.get(index).get(Math.max(actionTrack.get(index).size() - 2, 0));
            lastPoint[index] = actionTrack.get(index).get(actionTrack.get(index).size() - 1);
            distanceTravelled[index] = Vector2f.distance(lastPoint[index], lastPoint[index]);
            velocity[index] = Vector2f.sub(lastPoint[index], previousPoint[index]);

            averagePosition[index] = new Vector2f(0, 0);
            for (int i = 0; i < actionTrack.get(index).size(); i++) {
                averagePosition[index].add(actionTrack.get(index).get(i));
            }
            averagePosition[index].scale((float) 1 / (float) actionTrack.get(index).size());
            averageDistanceTravelled[index] = Vector2f.distance(startPosition[index], averagePosition[index]);
            averageDirectionVector[index] = averagePosition[index].copy();
            averageDirectionVector[index].add(startPosition[index].negative());

            averageDirectionVectorFollow[index] = 0;
            for (int i = 0; i < actionTrack.get(index).size(); i++) {
                averageDirectionVectorFollow[index] += Vector2f.dot(Vector2f.normalize(actionTrack.get(index).get(i)), Vector2f.normalize(averageDirectionVector[index]));
            }
            averageDirectionVectorFollow[index] /= (float) actionTrack.get(index).size();

            averageDistanceFromAveragePosition[index] = 0;
            for (int i = 0; i < actionTrack.get(index).size(); i++) {
                averageDistanceFromAveragePosition[index] += Vector2f.distance(actionTrack.get(index).get(i), averagePosition[index]);
            }
            averageDistanceFromAveragePosition[index] /= (float) actionTrack.get(index).size();

            Vector2f Max = new Vector2f(0, 0);
            Vector2f Min = new Vector2f(100000, 100000);
            for (Vector2f i : actionTrack.get(index)) {
                if (i.x > Max.x) {
                    Max.x = i.x;
                }
                if (i.y > Max.y) {
                    Max.y = i.y;
                }
                if (i.x < Min.x) {
                    Min.x = i.x;
                }
                if (i.y < Min.y) {
                    Min.y = i.y;
                }
            }
            circleCenter[index] = Vector2f.scale(Vector2f.add(Max, Min), 0.5f);

            if (!hasDoneCircle[index]) {
                float degrees;
                float tDegrees = 0;
                float pDegrees = (float) Math.atan2(actionTrack.get(index).get(0).x - circleCenter[index].x, actionTrack.get(index).get(0).y - circleCenter[index].y);
                for (int i = 1; i < actionTrack.get(index).size(); i++) {
                    degrees = (float) Math.atan2(actionTrack.get(index).get(i).x - circleCenter[index].x, actionTrack.get(index).get(i).y - circleCenter[index].y);
                    float difference = Math.abs(degrees - pDegrees);
                    if (difference > Math.PI) {
                        difference = Math.abs(2.0f * (float) Math.PI - difference);
                    } else if (difference < -Math.PI) {
                        difference = Math.abs(2.0f * (float) Math.PI + difference);
                    }
                    tDegrees += difference;

                    pDegrees = degrees;

                    if (Math.abs((tDegrees - 2 * Math.PI)) < 1.5) {
                        hasDoneCircle[index] = true;
                        i = actionTrack.get(index).size();

                        averageCircleFollow[index] = 0;
                        circleMaxDistance[index] = 0;
                        circleMinDistance[index] = 10000;
                        circleDistanceDifference[index] = 0;
                        for (int j = 0; j < actionTrack.get(index).size(); j++) {
                            float distance = Vector2f.add(actionTrack.get(index).get(j), circleCenter[index].negative()).length();
                            averageCircleFollow[index] += distance - averageDistanceFromAveragePosition[index];

                            if (distance > circleMinDistance[index]) {
                                circleMaxDistance[index] = distance;
                            }
                            if (distance < circleMinDistance[index]) {
                                circleMinDistance[index] = distance;
                            }
                        }
                        averageCircleFollow[index] /= actionTrack.get(index).size();
                        circleDistanceDifference[index] = circleMaxDistance[index] - circleMinDistance[index];
                    }
                }
            }
        }

    }


    /** processAction Processes the data provided by readAction to determine which action it might be */
    public void processAction(int index){
        time[index] = System.currentTimeMillis()-timer[index];
        readAction(index);

        // per-pointer actions
        if(!isTouching[index] && time[index] < 450 && averageDistanceTravelled[index] < 20){
            actions.get(index).add(actionType.TOUCH);
        }
        if(!isTouching[index] && time[index] < 1000 && averageDistanceTravelled[index] > 20 && averageDirectionVectorFollow[index] > 0.0){
            actions.get(index).add(actionType.SWIPE);
        }
        if(isTouching[index] && distanceTravelled[index] > 20 && averageDirectionVectorFollow[index] > 0.0 && !isHasSetScroll[index] && time[index] > 1000){
            actions.get(index).add(actionType.SCROLL);
            isHasSetScroll[index] = true;
        }
        if(time[index] > 450 && !hasSetLongTouch[index] && averageDistanceTravelled[index] < 20){
            actions.get(index).add(actionType.LONG_TOUCH);
            hasSetLongTouch[index] = true;
        }
        if(!hasSetCircle[index] && hasDoneCircle[index] && averageDistanceFromAveragePosition[index] > 20.0 && circleDistanceDifference[index] < 50.0){
            actions.get(index).add(actionType.CIRCLE);
            Log.w("action","circle " + averageCircleFollow[index]);
            hasSetCircle[index] = true;
        }

    }

    /** returns the index action in the action queue and removes it */
    public actionType getAction(int index){
        if(actions.get(index).size() > 0) {
            return actions.get(index).remove(0);
        }else{
            return actionType.NO_ACTION;
        }

    }
}
