package com.glu.engine.GUI;

import android.util.Log;

import com.glu.engine.Scene.Ressources;
import com.glu.engine.vectors.Vector2f;

import java.util.ArrayList;

public final class Button{
    public GUIBase stateDefault;
    public GUIBase statePressed;
    public GUIBase stateReleased;

    private Ressources ressources = Ressources.getRessources();

    public ArrayList<Boolean> isPressed = new ArrayList<>();
    public ArrayList<Boolean> hasBeenPressed = new ArrayList<>();
    public ArrayList<Boolean> hasReleased = new ArrayList<>();
    public ArrayList<Boolean> isHovering = new ArrayList<>();
    public ArrayList<Boolean> hasClickedOff = new ArrayList<>();
    public ArrayList<Integer> pointerClicked = new ArrayList<>();

    public ArrayList<Vector2f> position = new ArrayList<>();
    public ArrayList<Vector2f> size = new ArrayList<>();

    public ArrayList<String> name = new ArrayList<>();

    public PressNumber pressNumber;
    public DoOnClick doOnClick;
    public DoOnClickRelease doOnClickRelease;
    public DoOnPass doOnPass;
    public DoOnRelease doOnRelease;
    public DoOnSecondClick doOnSecondClick;

    public enum PressNumber{
        CANNOT_PRESS,        // can't press the button
        PRESS_ONCE,          // can only press the button once
        PRESS_MORE_THAN_ONCE // can press the button more than once
    }

    public enum DoOnClick{
        STAY_ON_CLICK,     // doesn't change on click
        CHANGE_ON_CLICK    // change on click
    }

    public enum DoOnClickRelease{ //when finger presses on the button and releases
        STAY_ON_RELEASE,    // stay on the pressed state at release
        RETURN_ON_RELEASE,  // return to the default state at release
        CHANGE_ON_RELEASE,  // change to the released state at release
    }

    public enum DoOnPass{
        NOTHING_ON_PASS,    // don't react when the finger goes over the button
        PREVIEW_ON_PASS,    // change to pressed icon when the finger goes over the button
        CHANGE_ON_PASS,     // change to released icon when the finger goes over the button
        CLICK_ON_PASS       // click the button when the finger goes over the button
    }

    public enum DoOnRelease{ //when the finger passes over the button and releases
        NOTHING_ON_RELEASE,    // don't react if the finger releases when going over the button
        CLICK_ON_RELEASE       // click the button if the finger releases when going over it
    }

    public enum DoOnSecondClick{
        CHANGE_TO_RELEASED, // if on pressed state, go to released state
        STAY_PRESSED,       // remain in pressed/released state
        RETURN_TO_DEFAULT   // return to default state and reset to first click
    } //if doOnClickRelease is RETURN_TO_RELEASE, it goes back to first click, so the case is handled


    public ArrayList<State> state = new ArrayList<>();

    public enum State{
        STATE_DEFAULT,
        STATE_PRESSED,
        STATE_RELEASED
    }

    public Button(GUIBase stateDefault, GUIBase statePressed){
        this.stateDefault = (stateDefault);
        this.statePressed = (statePressed);
        this.stateReleased = (null);

        position.add(new Vector2f(0,0));
        this.size.add(new Vector2f(1,1));

        this.stateDefault.position.set(0,this.position.get(0));
        this.stateDefault.scale.get(0).multiply(this.size.get(0));
        this.stateDefault.show.set(0,true);
        this.statePressed.position.set(0,this.position.get(0));
        this.statePressed.scale.get(0).multiply(this.size.get(0));
        this.statePressed.show.set(0,false);

        isPressed.add(false);
        hasBeenPressed.add(false);
        hasReleased.add(true);
        isHovering.add(false);
        hasClickedOff.add(false);

        pressNumber = PressNumber.PRESS_MORE_THAN_ONCE;
        doOnClick = DoOnClick.CHANGE_ON_CLICK;
        doOnClickRelease = DoOnClickRelease.RETURN_ON_RELEASE;
        doOnPass = DoOnPass.NOTHING_ON_PASS;
        doOnRelease = DoOnRelease.NOTHING_ON_RELEASE;
        state.add(State.STATE_DEFAULT);
        pointerClicked.add(0);

        name.add("button"+(name.size()));
    }

    public Button(GUIBase stateDefault, GUIBase statePressed, GUIBase stateReleased){
        this.stateDefault = (stateDefault);
        this.statePressed = (statePressed);
        this.stateReleased = (stateReleased);

        position.add(new Vector2f(0,0));
        this.size.add(new Vector2f(1,1));

        this.stateDefault.position.set(0,this.position.get(0));
        this.stateDefault.scale.get(0).multiply(this.size.get(0));
        this.stateDefault.show.set(0,true);
        this.statePressed.position.set(0,this.position.get(0));
        this.statePressed.scale.get(0).multiply(this.size.get(0));
        this.statePressed.show.set(0,false);
        this.stateReleased.position.set(0,this.position.get(0));
        this.stateReleased.scale.get(0).multiply(this.size.get(0));
        this.stateReleased.show.set(0,false);

        isPressed.add(false);
        hasBeenPressed.add(false);
        hasReleased.add(true);
        isHovering.add(false);
        hasClickedOff.add(false);

        pressNumber = PressNumber.PRESS_MORE_THAN_ONCE;
        doOnClick = DoOnClick.STAY_ON_CLICK;
        doOnClickRelease = DoOnClickRelease.RETURN_ON_RELEASE;
        doOnPass = DoOnPass.NOTHING_ON_PASS;
        doOnRelease = DoOnRelease.NOTHING_ON_RELEASE;
        state.add(State.STATE_DEFAULT);
        pointerClicked.add(0);

        name.add("button"+(name.size()));
    }

    public Button(GUIBase stateDefault, GUIBase statePressed, Vector2f position){
        this.stateDefault = (stateDefault);
        this.statePressed = (statePressed);
        this.stateReleased = (null);

        this.position.add(position);
        this.size.add(new Vector2f(1,1));

        this.stateDefault.position.set(0,this.position.get(0));
        this.stateDefault.scale.get(0).multiply(this.size.get(0));
        this.stateDefault.show.set(0,true);
        this.statePressed.position.set(0,this.position.get(0));
        this.statePressed.scale.get(0).multiply(this.size.get(0));
        this.statePressed.show.set(0,false);

        isPressed.add(false);
        hasBeenPressed.add(false);
        hasReleased.add(true);
        isHovering.add(false);
        hasClickedOff.add(false);

        pressNumber = PressNumber.PRESS_MORE_THAN_ONCE;
        doOnClick = DoOnClick.STAY_ON_CLICK;
        doOnClickRelease = DoOnClickRelease.RETURN_ON_RELEASE;
        doOnPass = DoOnPass.NOTHING_ON_PASS;
        doOnRelease = DoOnRelease.NOTHING_ON_RELEASE;
        state.add(State.STATE_DEFAULT);
        pointerClicked.add(0);

        name.add("button"+(name.size()));
    }

    public Button(GUIBase stateDefault, GUIBase statePressed, Vector2f position, Vector2f size){
        this.stateDefault = (stateDefault);
        this.statePressed = (statePressed);
        this.stateReleased = (null);

        this.position.add(position);
        this.size.add(size);

        this.stateDefault.position.set(0,this.position.get(0));
        this.stateDefault.scale.get(0).multiply(this.size.get(0));
        this.stateDefault.show.set(0,true);
        this.statePressed.position.set(0,this.position.get(0));
        this.statePressed.scale.get(0).multiply(this.size.get(0));
        this.statePressed.show.set(0,false);

        isPressed.add(false);
        hasBeenPressed.add(false);
        hasReleased.add(true);
        isHovering.add(false);
        hasClickedOff.add(false);

        pressNumber = PressNumber.PRESS_MORE_THAN_ONCE;
        doOnClick = DoOnClick.STAY_ON_CLICK;
        doOnClickRelease = DoOnClickRelease.RETURN_ON_RELEASE;
        doOnPass = DoOnPass.NOTHING_ON_PASS;
        doOnRelease = DoOnRelease.NOTHING_ON_RELEASE;
        state.add(State.STATE_DEFAULT);
        pointerClicked.add(0);

        name.add("button"+(name.size()));
    }

    public Button(GUIBase stateDefault, GUIBase statePressed, GUIBase stateReleased, Vector2f position){
        this.stateDefault = (stateDefault);
        this.statePressed = (statePressed);
        this.stateReleased = (stateReleased);

        this.position.add(position);
        this.size.add(new Vector2f(1,1));

        this.stateDefault.position.set(0,this.position.get(0));
        this.stateDefault.scale.get(0).multiply(this.size.get(0));
        this.stateDefault.show.set(0,true);
        this.statePressed.position.set(0,this.position.get(0));
        this.statePressed.scale.get(0).multiply(this.size.get(0));
        this.statePressed.show.set(0,false);
        this.stateReleased.position.set(0,this.position.get(0));
        this.stateReleased.scale.get(0).multiply(this.size.get(0));
        this.stateReleased.show.set(0,false);

        isPressed.add(false);
        hasBeenPressed.add(false);
        hasReleased.add(true);
        isHovering.add(false);
        hasClickedOff.add(false);

        pressNumber = PressNumber.PRESS_MORE_THAN_ONCE;
        doOnClick = DoOnClick.STAY_ON_CLICK;
        doOnClickRelease = DoOnClickRelease.RETURN_ON_RELEASE;
        doOnPass = DoOnPass.NOTHING_ON_PASS;
        doOnRelease = DoOnRelease.NOTHING_ON_RELEASE;
        state.add(State.STATE_DEFAULT);
        pointerClicked.add(0);

        name.add("button"+(name.size()));
    }

    public Button(GUIBase stateDefault, GUIBase statePressed, GUIBase stateReleased, Vector2f position, Vector2f size){
        this.stateDefault = (stateDefault);
        this.statePressed = (statePressed);
        this.stateReleased = (stateReleased);

        this.position.add(position);
        this.size.add(size);

        this.stateDefault.position.set(0,this.position.get(0));
        this.stateDefault.scale.get(0).multiply(this.size.get(0));
        this.stateDefault.show.set(0,true);
        this.statePressed.position.set(0,this.position.get(0));
        this.statePressed.scale.get(0).multiply(this.size.get(0));
        this.statePressed.show.set(0,false);
        this.stateReleased.position.set(0,this.position.get(0));
        this.stateReleased.scale.get(0).multiply(this.size.get(0));
        this.stateReleased.show.set(0,false);

        isPressed.add(false);
        hasBeenPressed.add(false);
        hasReleased.add(true);
        isHovering.add(false);
        hasClickedOff.add(false);

        pressNumber = PressNumber.PRESS_MORE_THAN_ONCE;
        doOnClick = DoOnClick.STAY_ON_CLICK;
        doOnClickRelease = DoOnClickRelease.RETURN_ON_RELEASE;
        doOnPass = DoOnPass.NOTHING_ON_PASS;
        doOnRelease = DoOnRelease.NOTHING_ON_RELEASE;
        state.add(State.STATE_DEFAULT);
        pointerClicked.add(0);

        name.add("button"+(name.size()));
    }

    public Button(Button buttonToCopy){
        stateDefault = buttonToCopy.stateDefault;
        statePressed = buttonToCopy.statePressed;
        stateReleased = buttonToCopy.stateReleased;

        position = new ArrayList<>(buttonToCopy.position);
        size = new ArrayList<>(buttonToCopy.size);

        stateDefault = buttonToCopy.stateDefault;
        statePressed = buttonToCopy.statePressed;
        stateReleased = buttonToCopy.stateReleased;

        isPressed = new ArrayList<>(buttonToCopy.isPressed);
        hasBeenPressed = new ArrayList<>(buttonToCopy.hasBeenPressed);
        hasReleased = new ArrayList<>(buttonToCopy.hasReleased);
        isHovering = new ArrayList<>(buttonToCopy.isHovering);
        hasClickedOff = new ArrayList<>(buttonToCopy.hasClickedOff);

        pressNumber = PressNumber.PRESS_MORE_THAN_ONCE;
        doOnClick = DoOnClick.STAY_ON_CLICK;
        doOnClickRelease = DoOnClickRelease.RETURN_ON_RELEASE;
        doOnPass = DoOnPass.NOTHING_ON_PASS;
        doOnRelease = DoOnRelease.NOTHING_ON_RELEASE;
        state.add(State.STATE_DEFAULT);
        pointerClicked = new ArrayList<>(buttonToCopy.pointerClicked);

        name.add("button"+(name.size()));
    }

    public void setBehavior( PressNumber pressNumber,DoOnClick doOnClick, DoOnClickRelease doOnClickRelease, DoOnPass doOnPass, DoOnRelease doOnRelease, DoOnSecondClick doOnSecondClick){
        this.pressNumber = pressNumber;
        this.doOnClick = doOnClick;
        this.doOnClickRelease = doOnClickRelease;
        this.doOnPass = doOnPass;
        this.doOnRelease = doOnRelease;
        this.doOnSecondClick = doOnSecondClick;
    }

    public void setName(int index,String name){
        this.name.set(index,name);
    }

    private Boolean isInside(Vector2f click, int index){
        Vector2f pos = Vector2f.scale(Vector2f.sub(click,Vector2f.scale(ressources.viewport,0.5f)),2f);
        Vector2f ipos = position.get(index);
        Vector2f isize = size.get(index);
        if( (( pos.x > ipos.x-isize.x && pos.x < ipos.x+isize.x )
                && ( pos.y > ipos.y-isize.y && pos.y < ipos.y+isize.y ))
                && !hasClickedOff.get(index) ) {
            return true;
        }else {
            return false;
        }
    }

    public void checkClickAt(Vector2f click,int index, int pointer){
        if(isInside(click,index)){
            click(index,pointer);
        }
    }

    public void checkClickAt(Vector2f click, int pointer){
        for (int i = 0; i < name.size(); i++) {
            checkClickAt(click,i,pointer);
        }
    }

    public void checkPassAt(Vector2f click,int index,int pointer){
        if(isInside(click,index) && pointer == pointerClicked.get(index)){
            passOn(index, pointer);
        }else if(isHovering.get(index) && pointer == pointerClicked.get(index)){
            passOff(index, pointer);
        }
    }

    public void checkPassAt(Vector2f click, int pointer){
        for (int i = 0; i < name.size(); i++) {
            checkPassAt(click,i,pointer);
        }
    }

    public void checkReleaseIn(Vector2f click,int index,int pointer){
        if(isInside(click,index)){
            release(index, pointer);
        }
    }

    public void checkReleaseIn(Vector2f click,int pointer){
        for (int i = 0; i < name.size(); i++) {
            checkReleaseIn(click,i,pointer);
        }
    }

    public void checkReleaseOut(Vector2f click,int index, int pointer){
        if(!isInside(click,index)){
            boolean wasPressed = false;
            if( isPressed.get(index) && pointer == pointerClicked.get(index)) {
                wasPressed = true;
                release(index, pointer);
            }
            if(wasPressed){
                hasClickedOff.set(index,true);
            }
        }
    }

    public void checkReleaseOut(Vector2f click, int pointer){
        for (int i = 0; i < name.size(); i++) {
            checkReleaseOut(click,i);
        }
    }

    public void click(int index, int pointer){
        Log.w("click",pointer + " has clicked");
        switch (pressNumber){
            case CANNOT_PRESS:
                Log.w("clickButton","You can't press this button!");
                break;
            case PRESS_ONCE:
                hasReleased.set(index,false);
                switch (doOnClick){
                    case STAY_ON_CLICK:
                        if(!hasBeenPressed.get(index)){
                            isPressed.set(index,true);
                            hasBeenPressed.set(index,true);
                            pointerClicked.set(index,pointer);
                        }
                        break;
                    case CHANGE_ON_CLICK:
                        if(!hasBeenPressed.get(index)){
                            isPressed.set(index,true);
                            hasBeenPressed.set(index,true);
                            state.set(index,State.STATE_PRESSED);
                            pointerClicked.set(index,pointer);
                        }
                        break;
                    default:
                        Log.e("clickButton","doOnClick can't be : " + doOnClick + ", it must either be STAY_ON_CLICK or CHANGE_ON_CLICK.");
                        break;
                }
                break;
            case PRESS_MORE_THAN_ONCE:
                hasReleased.set(index,false);
                Log.w("click", "you can press more than once");
                switch (doOnClick){
                    case STAY_ON_CLICK:
                        isPressed.set(index,true);
                        hasBeenPressed.set(index,true);
                        pointerClicked.set(index,pointer);
                        break;
                    case CHANGE_ON_CLICK:
                        Log.w("click", "it changes on click");
                        if(hasBeenPressed.get(index)){
                            Log.w("click", "it has been pressed");
                            switch (doOnSecondClick){
                                case STAY_PRESSED:
                                    isPressed.set(index, true);
                                    hasBeenPressed.set(index, true);
                                    if(state.get(index) == State.STATE_PRESSED) {
                                        state.set(index, State.STATE_PRESSED);
                                    }else if(state.get(index) == State.STATE_RELEASED){
                                        state.set(index, State.STATE_RELEASED);
                                    }else {
                                        Log.e("Button click", "Something went very wrong. state cannot be STATE_DEFAULT at this stage.");
                                    }
                                    pointerClicked.set(index, pointer);
                                    break;
                                case CHANGE_TO_RELEASED:
                                    isPressed.set(index, true);
                                    hasBeenPressed.set(index, true);
                                    state.set(index, State.STATE_RELEASED);
                                    pointerClicked.set(index, pointer);
                                    break;
                                case RETURN_TO_DEFAULT:
                                    Log.w("click", "return to default");
                                    isPressed.set(index, false);
                                    hasBeenPressed.set(index, false);
                                    state.set(index, State.STATE_DEFAULT);
                                    pointerClicked.set(index, pointer);
                                    break;
                            }
                        }else {
                            isPressed.set(index, true);
                            hasBeenPressed.set(index, true);
                            state.set(index, State.STATE_PRESSED);
                            pointerClicked.set(index, pointer);
                        }
                        break;
                    default:
                        Log.e("clickButton","invalid value for doOnClick : "+doOnClick);
                        break;
                }
                break;
            default:
                Log.e("clickButton","invalid value for pressNumber : "+pressNumber);
                break;
        }
        updateIcon(index);
    }

    public void release(int index, int pointer){
        Log.w("release",pointer + " has released");
        if(pointer == pointerClicked.get(index)){
            switch (doOnClickRelease){
                case STAY_ON_RELEASE:
                    break;
                case CHANGE_ON_RELEASE:
                    isPressed.set(index,false);
                    hasBeenPressed.set(index,true);
                    state.set(index,State.STATE_RELEASED);
                    break;
                case RETURN_ON_RELEASE:
                    isPressed.set(index,false);
                    hasBeenPressed.set(index,true);
                    state.set(index,State.STATE_DEFAULT);
                    break;
                default:
                    Log.e("releaseButton","invalid value for doOnClickRelease : "+doOnClickRelease);
                    break;
            }
            updateIcon(index);
        }else if(pointer == pointerClicked.get(index) && isHovering.get(index)) {
            switch (doOnRelease){
                case NOTHING_ON_RELEASE:
                    break;
                case CLICK_ON_RELEASE:
                    click(index,pointer);
                    release(index,pointer);
                    break;
                default:
                    Log.e("releaseButton","invalid value for doOnRelease : "+doOnRelease);
                    break;
            }
        }
        isHovering.set(index,true);
        hasClickedOff.set(index,false);
        hasReleased.set(index,true);
    }

    public void passOn(int index,int pointer){
        Log.w("passOn",pointer + " has passedOn");
        switch (doOnPass){
            case NOTHING_ON_PASS:
                isHovering.set(index,true);
                break;
            case PREVIEW_ON_PASS:
                state.set(index,State.STATE_PRESSED);
                isHovering.set(index,true);
                break;
            case CHANGE_ON_PASS:
                state.set(index,State.STATE_RELEASED);
                isHovering.set(index,true);
                break;
            case CLICK_ON_PASS:
                click(index,pointer);
                isHovering.set(index,true);
                break;
            default:
                Log.e("passOnButton","invalid value for doOnPass : "+doOnPass);
                break;
        }
        updateIcon(index);
    }

    public void passOff(int index, int pointer){
        Log.w("passOff",pointer + " has passedOff");
        if(doOnPass != DoOnPass.CLICK_ON_PASS){
            state.set(index,State.STATE_DEFAULT);
            isHovering.set(index,false);
        }
        updateIcon(index);
    }

    public void updateIcon(int index){
        //Log.w("updateButtonIcon","state : "+state.get(index));
        switch (state.get(index)){
            case STATE_DEFAULT:
                stateDefault.show.set(index,true);
                statePressed.show.set(index,false);
                if(stateReleased != null){stateReleased.show.set(index,false);}
                break;
            case STATE_PRESSED:
                stateDefault.show.set(index,false);
                statePressed.show.set(index,true);
                if(stateReleased != null){stateReleased.show.set(index,false);}
                break;
            case STATE_RELEASED:
                stateDefault.show.set(index,false);
                statePressed.show.set(index,false);
                if (stateReleased!= null){stateReleased.show.set(index,true);}else
                    {Log.e("updateButtonIcon",name.get(index)+" has no stateReleased icon!");}
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + state.get(index));
        }

        stateDefault.position.set(index,position.get(index));
        statePressed.position.set(index,position.get(index));
        if(stateReleased != null) {
            stateReleased.position.set(index, position.get(index));
        }
    }

    public Button copy(){
        return new Button(this);
    }

    public void addInstance(){
        isPressed.add(false);
        hasBeenPressed.add(false);
        position.add(new Vector2f(0,0));
        size.add(new Vector2f(1,1));
        state.add(State.STATE_DEFAULT);
        stateDefault.instance(0);
        stateDefault.position.set(stateDefault.position.size()-1,new Vector2f(0,0));
        statePressed.instance(0);
        statePressed.position.set(statePressed.position.size()-1,new Vector2f(0,0));
        if(stateReleased != null) {
            stateReleased.instance(0);
            stateReleased.position.set(stateReleased.position.size() - 1, new Vector2f(0, 0));
        }
        Log.w("addInstance","added instance");
    }

    public void addInstance(Vector2f position,Vector2f size){
        isPressed.add(false);
        hasBeenPressed.add(false);
        this.position.add(position);
        size.add(size);
        state.add(State.STATE_DEFAULT);
        stateDefault.addInstance(position,0,size);
        statePressed.addInstance(position,0,size);
        if(stateReleased != null) {
            stateReleased.addInstance(position, 0, size);
        }
        Log.w("addInstance","added instance");
    }

    public void instance(int index){
        isPressed.add(isPressed.get(index));
        hasBeenPressed.add(hasBeenPressed.get(index));
        this.position.add(position.get(index));
        size.add(size.get(index));
        state.add(state.get(index));
        stateDefault.instance(index);
        statePressed.instance(index);
        if(stateReleased != null) {
            stateReleased.instance(index);
        }
        Log.w("Instance","instanced "+index);
    }

    public void removeInstance(int index){
        isPressed.remove(index);
        hasBeenPressed.remove(index);
        this.position.remove(index);
        size.remove(index);
        state.remove(index);
        stateDefault.removeInstance(index);
        statePressed.removeInstance(index);
        if(stateReleased != null) {
            stateReleased.removeInstance(index);
        }
        Log.w("removedInstance","instance removed "+index);
    }

    public void reset(int index){
        isPressed.set(index,false);
        hasBeenPressed.set(index,false);
        state.set(index,State.STATE_DEFAULT);
    }

    public void setPosition(int index, Vector2f pos){
        position.set(index,pos);
        stateDefault.position.set(index,pos);
        statePressed.position.set(index,pos);
        if(stateReleased != null) {
            stateReleased.position.set(index, pos);
        }
    }

    public void setSize(int index, Vector2f s){
        size.set(index,s);
        stateDefault.scale.set(index,s);
        statePressed.scale.set(index,s);
        if(stateReleased != null) {
            stateReleased.scale.set(index, s);
        }
    }
}
