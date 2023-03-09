package com.glu.engine.GUI;

import com.glu.engine.Scene.Ressources;
import com.glu.engine.actionManager.ActionManager;
import com.glu.engine.vectors.Vector2f;

public final class Glissoire {
    public String nom = "nom";
    public float valMax;
    public float valMin;
    public float valeur;
    public float échelonsTaille;

    public GUIBase barre;
    public GUIBase bouton;

    private Ressources ressources = Ressources.getRessources();
    private ActionManager actionManager = ActionManager.getActionManager();

    private Vector2f posMax;
    private Vector2f posMin;

    private float rotation;
    private Vector2f position;

    private Vector2f bornesRayon;

    public Glissoire(GUIBase barre, GUIBase bouton){
        this.barre = barre;
        this.bouton = bouton;

        valMax = 10f;
        valMin = -10f;
        valeur = 0f;
        échelonsTaille = 0f;

        posMax = new Vector2f(barre.scale.get(0).x, 0f);
        posMin = new Vector2f(-barre.scale.get(0).x, 0f);

        rotation = 0f;
        position = new Vector2f(0);

        bornesRayon = new Vector2f(barre.scale.get(0).x, bouton.scale.get(0).y);

        assignerValeur(0f);
    }
    public Glissoire(GUIBase barre, GUIBase bouton, float valMin, float valMax){
        this.barre = barre;
        this.bouton = bouton;

        this.valMax = valMax;
        this.valMin = valMin;
        valeur = (valMax + valMin)*0.5f;
        échelonsTaille = 0f;

        posMax = new Vector2f(barre.scale.get(0).x, 0f);
        posMin = new Vector2f(-barre.scale.get(0).x, 0f);

        rotation = 0f;
        position = new Vector2f(0);

        bornesRayon = new Vector2f(barre.scale.get(0).x, bouton.scale.get(0).y);

        assignerValeur(valeur);
    }
    public Glissoire(GUIBase barre, GUIBase bouton, float valMin, float valMax, float échelonsTaille){
        this.barre = barre;
        this.bouton = bouton;

        this.valMax = valMax;
        this.valMin = valMin;
        this.valeur = (valMax + valMin)*0.5f;
        this.échelonsTaille = échelonsTaille;

        posMax = new Vector2f(barre.scale.get(0).x, 0f);
        posMin = new Vector2f(-barre.scale.get(0).x, 0f);

        rotation = 0f;
        position = new Vector2f(0);

        bornesRayon = new Vector2f(barre.scale.get(0).x, bouton.scale.get(0).y);

        assignerValeur(valeur);
    }

    public boolean actualiser(int index){
        if(actionManager.isTouching[index]){
            float rotationRad = rotation * (3.1416f/180f);

            Vector2f pointeurPos = actionManager.lastPoint[index].copy();
            Vector2f pointeurDébutPos = actionManager.startPosition[index].copy();
            pointeurPos.sub( Vector2f.scale(ressources.viewport, 0.5f) );
            pointeurDébutPos.sub( Vector2f.scale(ressources.viewport, 0.5f) );
            pointeurPos.scale(2f);
            pointeurDébutPos.scale(2f);

            pointeurPos.sub(position);
            pointeurDébutPos.sub(position);

            float angleBébut = (float)Math.atan2(pointeurDébutPos.y, pointeurDébutPos.x) - rotationRad;
            float L = pointeurDébutPos.length();

            pointeurDébutPos.x = (float) Math.cos(angleBébut) * L;
            pointeurDébutPos.y = (float) Math.sin(angleBébut) * L;

            if(pointeurDébutPos.x <= bornesRayon.x && pointeurDébutPos.x >= -bornesRayon.x && pointeurDébutPos.y <= bornesRayon.y && pointeurDébutPos.y >= -bornesRayon.y){
                //bouton.position.set(0,pointeurPos.copy());

                Vector2f boutonPos = new Vector2f(0f);
                //y = Px*cos(a)*sin(a) + Py*sin(a)*sin(a)
                boutonPos.y = pointeurPos.x * (float)Math.cos(rotationRad) * (float)Math.sin(rotationRad) + pointeurPos.y * (float)Math.sin(rotationRad) * (float)Math.sin(rotationRad);
                //x = -y*( (cos(a) + sin(a) )/( cos(a)-sin(a) ) + ( Px*cos(a) + Py*sin(a) )/( cos(a)-sin(a) )
                boutonPos.x = -boutonPos.y * ( ( (float)Math.cos(rotationRad) + (float)Math.sin(rotationRad) )
                                                / ( (float)Math.cos(rotationRad) - (float)Math.sin(rotationRad) ) ) +
                        ( (pointeurPos.x * (float)Math.cos(rotationRad) + pointeurPos.y * (float)Math.sin(rotationRad))
                                / ((float)Math.cos(rotationRad) - (float)Math.sin(rotationRad)) );
                boutonPos.add(position);
                Vector2f bornesMax = new Vector2f( Math.max(posMax.x, posMin.x), Math.max(posMax.y, posMin.y) );
                Vector2f bornesMin = new Vector2f( Math.min(posMax.x, posMin.x), Math.min(posMax.y, posMin.y) );

                //bouton.position.set(0,boutonPos);

                if(boutonPos.x > bornesMax.x || boutonPos.y > bornesMax.y){
                    boutonPos = bornesMax.copy();
                }else if(boutonPos.x < bornesMin.x || boutonPos.y < bornesMin.y){
                    boutonPos = bornesMin.copy();
                }

                float valeur = Vector2f.sub(boutonPos,posMin).length() / Vector2f.sub(posMax,posMin).length();
                valeur = valMin * (1f - valeur) + valMax * valeur;
                assignerValeur(valeur);

                return true;
            }
        }
        return false;
    }

    public void assignerValeur(float valeur){
        if(échelonsTaille != 0f) {
            valeur = Math.round(valeur / échelonsTaille) * échelonsTaille;
        }
        float mix = (valeur-valMin)/(valMax-valMin);
        Vector2f mixPos = Vector2f.add(Vector2f.scale(posMin,1f - mix), Vector2f.scale(posMax,mix));
        bouton.position.set(0,mixPos);
        this.valeur = valeur;
    }

    public float rotation() {
        return rotation;
    }

    public void assignerRotation(float rotation) {
        this.rotation = rotation;
        posMin.sub(position);
        posMax.sub(position);

        float MinL = posMin.length();
        float MaxL = posMax.length();

        posMin.x = (float)Math.cos((rotation + 180) * (3.1416f/180f)) * MinL;
        posMin.y = (float)Math.sin((rotation + 180) * (3.1416f/180f)) * MinL;
        posMax.x = (float)Math.cos(rotation * (3.1416f/180f)) * MaxL;
        posMax.y = (float)Math.sin(rotation * (3.1416f/180f)) * MaxL;

        posMin.add(position);
        posMax.add(position);

        barre.rotation.set(0,rotation);
        bouton.rotation.set(0,rotation);

        assignerValeur(valeur);
    }

    public void tourner(float angle){
        this.rotation += angle;
        posMin.sub(position);
        posMax.sub(position);

        float MinL = posMin.length();
        float MaxL = posMax.length();

        float minAngle = (float)Math.atan2(posMin.y,posMin.x) + (angle * (3.1416f/180f));
        float maxAngle = (float)Math.atan2(posMax.y,posMax.x) + (angle * (3.1416f/180f));

        posMin.x = (float)Math.cos(minAngle) * MinL;
        posMin.y = (float)Math.sin(minAngle) * MinL;
        posMax.x = (float)Math.cos(maxAngle) * MaxL;
        posMax.y = (float)Math.sin(maxAngle) * MaxL;

        posMin.add(position);
        posMax.add(position);

        barre.rotation.set(0, barre.rotation.get(0) + angle);
        bouton.rotation.set(0, bouton.rotation.get(0) + angle);

        assignerValeur(valeur);
    }

    public Vector2f position() {
        return position;
    }

    public void assignerPosition(Vector2f position) {
        posMin.sub(this.position);
        posMax.sub(this.position);
        bouton.position.get(0).sub(this.position);
        this.position = position.copy();
        barre.position.set(0,position.copy());
        bouton.position.get(0).add(this.position);
        posMin.add(this.position);
        posMax.add(this.position);
    }

    public void bouger(Vector2f translation){
        posMin.add(translation);
        posMax.add(translation);
        position.add(translation);
        bouton.position.add(translation);
        barre.position.add(translation);
    }
}
