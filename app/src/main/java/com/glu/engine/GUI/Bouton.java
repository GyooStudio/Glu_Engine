package com.glu.engine.GUI;

import com.glu.engine.Scene.Ressources;
import com.glu.engine.actionManager.ActionManager;
import com.glu.engine.vectors.Vector2f;

import java.util.ArrayList;

public final class Bouton {
    public GUIBase bouttonPressé;
    public GUIBase bouttonDéfaut;
    public GUIBase bouttonSurvol;

    public Vector2f taille;
    public float rotation;
    public Vector2f position;

    public String name = "name";

    public boolean[] pointeurTouchants;
    public boolean[] pointeurDébutDedant;
    public boolean[] pointeurFinDedant;

    private boolean aInterragit = false;

    private long clicMoment = 0;
    public int clicImmédiatLongueur = 50;

    /** L'état actuel ddu bouton**/
    public enum État{
        DÉFAUT,
        PRESSÉ,
        SURVOL
    }
    private État état = État.DÉFAUT;

    /** Ce qui ce passera lorsque le doigt glisse sur le bouton**/
    public enum EnSurvol{
        RIEN, /** Il ne se passera rien**/
        SURVOLE, /** Changer l'état à survol**/
        CLIC /** Cliquer le bouton**/
    }
    private EnSurvol enSurvol = EnSurvol.SURVOLE;

    /** Ce qui ce passera lorsque le doigt qui a passé sur le bouton glisse à coté du bouton*/
    public enum EnSurvolDébarque{
        RESTE, /** Il ne se passe rien. Le bouton reste dans l'état dans lequel il est**/
        RETOURNE_DÉFAUT, /** Le bouton retourne à son état par défaut**/
        CLIC /** Cliquer le bouton**/
    }
    private EnSurvolDébarque enSurvolDébarque = EnSurvolDébarque.RETOURNE_DÉFAUT;

    /** Ce qui ce passera lorsque le doigt qui a passé sur le bouton se lève de l'écran sur le bouton**/
    public enum EnSurvolRelâche{
        RESTE, /** Il ne se passe rien. Le bouton reste dans l'état dans lequel il est**/
        RETOURNE_DÉFAUT, /** Le bouton retourne à son état par défaut**/
        CLIC /** Cliquer le bouton**/
    }
    private EnSurvolRelâche enSurvolRelâche = EnSurvolRelâche.RETOURNE_DÉFAUT;

    /** Ce qui ce passera lorsque le doigt appuie sur le bouton**/
    public enum EnDoigtTouché{
        RIEN, /** Il ne se passe rien. Le bouton reste dans l'état dans lequel il est**/
        SURVOL, /** Le bouton se met dans l'état de survol**/
        CLIC /** Cliquer le bouton**/
    }
    private EnDoigtTouché enDoigtTouché = EnDoigtTouché.SURVOL;

    /** Ce qui ce passera lorsque le doigt appuie sur le bouton et glisse à coté du bouton**/
    public enum EnDoigtDébarque{
        RESTE, /** Il ne se passe rien. Le bouton reste dans l'état dans lequel il est**/
        RETOURNE_DÉFAUT, /** Le bouton retourne à son état par défaut**/
        CLIC /** Cliquer le bouton**/
    }
    private EnDoigtDébarque enDoigtDébarque = EnDoigtDébarque.RETOURNE_DÉFAUT;

    /** Ce qui ce passera lorsque le doigt qui a appuyé sur le bouton se lève de l'écran, sur le bouton**/
    public enum EnDoigtRelâche{
        RESTE, /** Il ne se passe rien. Le bouton reste dans l'état dans lequel il est**/
        RETOURNE_DÉFAUT, /** Le bouton retourne à son état par défaut**/
        SURVOL, /** Le bouton se met dans un état de survol**/
        CLIC /** Cliquer le bouton**/
    }
    private EnDoigtRelâche enDoigtRelâche = EnDoigtRelâche.CLIC;

    /** Ce qui ce passera lorsque le système aurat déterminé qu'il faut cliquer le bouton**/
    public enum EnClic{
        RIEN, /** Il ne se passe rien**/
        CLIC_IMMÉDIAT, /** Le bouton se met dans un état cliqué et retourne à son état par défaut après un temp spécifié par clicImmédiatLongueur en millisecondes**/
        ALTERNE, /** Le bouton alterne entre l'état cliqué et l'état par défaut, comme un interupteur ou une checkbox**/
        CLIC_RESTE /** Le bouton reste cliqué jusqu'à ce qu'on le retourne à son état par défaut manuellementw**/
    }
    private EnClic enClic = EnClic.CLIC_IMMÉDIAT;

    /** Les différents évènements arrivant au bouton qui peuvent être enregistrés**/
    public enum Événement{
        CLIC, /** **/
        CLIC_RELÂCHE,
        CLIC_DÉBARQUE,
        SURVOL,
        SURVOL_RELÂCHE,
        SURVOL_CLIC,
        SURVOL_DÉBARQUE,
    }
    private ArrayList<Événement> événements = new ArrayList<>();

    /** Des préréglages permettant de changer rapidement le comportement du bouton**/
    public enum Préréglages{
        BOUTON,
        SÉLECTION,
        CONTRÔLES,
        CASE_À_COCHER
    }

    private ActionManager actionManager = ActionManager.getActionManager();
    private Ressources ressources = Ressources.getRessources();

    public Bouton(GUIBase bouttonDéfaut, GUIBase bouttonPressé){
        this.bouttonDéfaut = bouttonDéfaut;
        this.bouttonPressé = bouttonPressé;
        this.bouttonSurvol = null;

        taille = Vector2f.scale(bouttonDéfaut.scale.get(0),0.5f);
        position = new Vector2f(0f);
        rotation = 0f;

        pointeurTouchants = new boolean[actionManager.MAX_POINTERS];
        pointeurDébutDedant = new boolean[actionManager.MAX_POINTERS];
        pointeurFinDedant = new boolean[actionManager.MAX_POINTERS];

        changerÉtat(État.DÉFAUT);
    }

    public Bouton(GUIBase bouttonDéfaut, GUIBase bouttonPressé, GUIBase bouttonSurvol){
        this.bouttonDéfaut = bouttonDéfaut;
        this.bouttonPressé = bouttonPressé;
        this.bouttonSurvol = bouttonSurvol;

        taille = Vector2f.scale(bouttonDéfaut.scale.get(0),0.5f);
        position = new Vector2f(0f);
        rotation = 0f;

        pointeurTouchants = new boolean[actionManager.MAX_POINTERS];
        pointeurDébutDedant = new boolean[actionManager.MAX_POINTERS];
        pointeurFinDedant = new boolean[actionManager.MAX_POINTERS];

        changerÉtat(État.DÉFAUT);
    }

    public boolean actualiser(int indexPointeur){
        boolean aInterragitAvant = aInterragit;
        aInterragit = false;
        if(actionManager.isTouching[indexPointeur]) { // si le doigt presse encore.

            Vector2f début = actionManager.startPosition[indexPointeur].copy();
            Vector2f fin = actionManager.lastPoint[indexPointeur].copy();

            début.sub(Vector2f.scale(ressources.viewport,0.5f));
            fin.sub(Vector2f.scale(ressources.viewport,0.5f));

            float conv = 3.1416f/180f;
            Vector2f tmp = new Vector2f(0f);
            tmp.x = ( ( début.x - position.x ) * (float)Math.cos(-rotation*conv) ) - ( ( début.y - position.y ) * (float)Math.sin(-rotation*conv) );
            tmp.y = ( ( début.y - position.y ) * (float)Math.cos(-rotation*conv) ) + ( ( début.x - position.x ) * (float)Math.sin(-rotation*conv) );
            début = tmp.copy();
            tmp.x = ( ( fin.x - position.x ) * (float)Math.cos(-rotation*conv) ) - ( ( fin.y - position.y ) * (float)Math.sin(-rotation*conv) );
            tmp.y = ( ( fin.y - position.y ) * (float)Math.cos(-rotation*conv) ) + ( ( fin.x - position.x ) * (float)Math.sin(-rotation*conv) );
            fin = tmp.copy();

            boolean débutDedant = false;
            boolean finDedant = false;
            if (début.x < taille.x && début.x > -taille.x && début.y < taille.y && début.y > -taille.y) {
                débutDedant = true;
            }
            if (fin.x < taille.x && fin.x > -taille.x && fin.y < taille.y && fin.y > -taille.y) {
                finDedant = true;
            }

            if( pointeurDébutDedant[indexPointeur] != débutDedant || pointeurFinDedant[indexPointeur] != finDedant || !pointeurTouchants[indexPointeur]) {
                if (!débutDedant && !finDedant && état == État.SURVOL) {
                    enSurvolDébarque();
                } else if (!débutDedant && finDedant) {
                    enSurvol();
                } else if (débutDedant && !finDedant) {
                    enDoigtDébarque();
                } else if (débutDedant && finDedant) {
                    enDoigtTouche();
                }
            }else{
                aInterragit = aInterragitAvant;
            }

            pointeurTouchants[indexPointeur] = true;
            pointeurDébutDedant[indexPointeur] = débutDedant;
            pointeurFinDedant[indexPointeur] = finDedant;

        }
        else if(pointeurTouchants[indexPointeur])
        { // si le doigt a relâché

            pointeurTouchants[indexPointeur] = false;

            Vector2f début = actionManager.startPosition[indexPointeur].copy();
            Vector2f fin = actionManager.lastPoint[indexPointeur].copy();

            début.sub(Vector2f.scale(ressources.viewport,0.5f));
            fin.sub(Vector2f.scale(ressources.viewport,0.5f));

            float conv = 3.1416f/180f;
            Vector2f tmp = new Vector2f(0f);
            tmp.x = ( ( début.x - position.x ) * (float)Math.cos(-rotation*conv) ) - ( ( début.y - position.y ) * (float)Math.sin(-rotation*conv) );
            tmp.y = ( ( début.y - position.y ) * (float)Math.cos(-rotation*conv) ) + ( ( début.x - position.x ) * (float)Math.sin(-rotation*conv) );
            début = tmp.copy();
            tmp.x = ( ( fin.x - position.x ) * (float)Math.cos(-rotation*conv) ) - ( ( fin.y - position.y ) * (float)Math.sin(-rotation*conv) );
            tmp.y = ( ( fin.y - position.y ) * (float)Math.cos(-rotation*conv) ) + ( ( fin.x - position.x ) * (float)Math.sin(-rotation*conv) );
            fin = tmp.copy();

            boolean débutDedant = false;
            boolean finDedant = false;
            if (début.x < taille.x && début.x > -taille.x && début.y < taille.y && début.y > -taille.y) {
                débutDedant = true;
            }
            if (fin.x < taille.x && fin.x > -taille.x && fin.y < taille.y && fin.y > -taille.y) {
                finDedant = true;
            }

            if (!débutDedant && !finDedant) {
            } else if (!débutDedant && finDedant) {
                enSurvolRelâche();
            } else if (débutDedant && !finDedant) {
            } else if (débutDedant && finDedant) {
                enDoigtRelâche();
            }

        } else
        {
            boolean touché = false;
            for (int i = 0; i < pointeurTouchants.length; i++) {
                touché = touché || pointeurTouchants[i];
            }
            if(!touché && enClic == EnClic.CLIC_IMMÉDIAT && System.currentTimeMillis() - clicMoment > clicImmédiatLongueur){
                changerÉtat(État.DÉFAUT);
                événements.add(Événement.CLIC_RELÂCHE);
            }
        }

        return aInterragit;
    }

    private void enSurvol(){
        switch (enSurvol){
            case RIEN:
                break;
            case SURVOLE:
                changerÉtat(État.SURVOL);
                événements.add(Événement.SURVOL);
                aInterragit = true;
                break;
            case CLIC:
                enClic();
                aInterragit = true;
                break;
        }
    }

    private void enSurvolDébarque(){
        switch (enDoigtDébarque){
            case RESTE:
                break;
            case RETOURNE_DÉFAUT:
                changerÉtat(État.DÉFAUT);
                événements.add(Événement.SURVOL_DÉBARQUE);
                aInterragit = true;
                break;
            case CLIC:
                enClic();
                événements.add(Événement.SURVOL_CLIC);
                aInterragit = true;
                break;
        }
    }

    private void enSurvolRelâche(){
        switch (enDoigtRelâche){
            case SURVOL:
                changerÉtat(État.SURVOL);
                aInterragit = true;
                break;
            case RESTE:
                break;
            case RETOURNE_DÉFAUT:
                changerÉtat(État.DÉFAUT);
                événements.add(Événement.SURVOL_RELÂCHE);
                aInterragit = true;
                break;
            case CLIC:
                enClic();
                événements.add(Événement.SURVOL_CLIC);
                aInterragit = true;
                break;
        }
    }

    private void enDoigtTouche(){
        switch (enDoigtTouché){
            case RIEN:
                break;
            case SURVOL:
                changerÉtat(État.SURVOL);
                événements.add(Événement.SURVOL);
                aInterragit = true;
                break;
            case CLIC:
                enClic();
                événements.add(Événement.CLIC);
                aInterragit = true;
                break;
        }
    }

    private void enDoigtDébarque(){
        switch (enDoigtDébarque){
            case RESTE:
                break;
            case RETOURNE_DÉFAUT:
                if(état == État.PRESSÉ){
                    événements.add(Événement.CLIC_DÉBARQUE);
                    aInterragit = true;
                }
                changerÉtat(État.DÉFAUT);
                break;
            case CLIC:
                enClic();
                événements.add(Événement.CLIC);
                aInterragit = true;
                break;
        }
    }

    private void enDoigtRelâche(){
        switch (enDoigtRelâche){
            case RESTE:
                break;
            case SURVOL:
                changerÉtat(État.SURVOL);
                événements.add(Événement.CLIC_RELÂCHE);
                aInterragit = true;
                break;
            case RETOURNE_DÉFAUT:
                changerÉtat(État.DÉFAUT);
                événements.add(Événement.CLIC_RELÂCHE);
                aInterragit = true;
                break;
            case CLIC:
                enClic();
                événements.add(Événement.CLIC);
                aInterragit = true;
                break;
        }
    }

    private void enClic(){
        switch (enClic){
            case RIEN:
                break;
            case CLIC_IMMÉDIAT:
            case CLIC_RESTE:
                changerÉtat(État.PRESSÉ);
                clicMoment = System.currentTimeMillis();
                break;
            case ALTERNE:
                if(état != État.PRESSÉ){
                    changerÉtat(État.PRESSÉ);
                }else{
                    changerÉtat(État.DÉFAUT);
                }
                break;
        }
    }

    public void changerÉtat(État état){
        this.état = état;
        switch (état){
            case DÉFAUT:
                bouttonDéfaut.show.set(0,true);
                bouttonPressé.show.set(0,false);
                if(bouttonSurvol != null) {
                    bouttonSurvol.show.set(0, false);
                }
                break;
            case SURVOL:
                bouttonDéfaut.show.set(0,false);
                bouttonPressé.show.set(0,false);
                if (bouttonSurvol != null) {
                    bouttonSurvol.show.set(0, true);
                }
                break;
            case PRESSÉ:
                bouttonDéfaut.show.set(0,false);
                bouttonPressé.show.set(0,true);
                if(bouttonSurvol != null) {
                    bouttonSurvol.show.set(0, false);
                }
                break;
        }
    }

    public Événement obtenirÉvénement(){
        if(événements.size() > 0){
            return événements.remove(0);
        }else{
            return null;
        }
    }

    public void changerComportement(EnDoigtTouché enDoigtTouché, EnDoigtRelâche enDoigtRelâche, EnDoigtDébarque enDoigtDébarque, EnSurvol enSurvol, EnSurvolDébarque enSurvolDébarque, EnSurvolRelâche enSurvolRelâche, EnClic enClic){
        if(enDoigtTouché != null)    { this.enDoigtTouché = enDoigtTouché; }
        if(enDoigtRelâche != null)   { this.enDoigtRelâche = enDoigtRelâche; }
        if(enDoigtDébarque != null)  { this.enDoigtDébarque = enDoigtDébarque; }
        if(enSurvol != null)         { this.enSurvol = enSurvol; }
        if(enSurvolRelâche != null)  { this.enSurvolRelâche = enSurvolRelâche; }
        if(enSurvolDébarque != null) { this.enSurvolDébarque = enSurvolDébarque; }
        if(enClic != null)           { this.enClic = enClic; }
    }

    public void changerComportement(Préréglages préréglage){
        switch (préréglage){
            case BOUTON:
                changerComportement(EnDoigtTouché.SURVOL, EnDoigtRelâche.CLIC, EnDoigtDébarque.RETOURNE_DÉFAUT,EnSurvol.SURVOLE, EnSurvolDébarque.RETOURNE_DÉFAUT, EnSurvolRelâche.RETOURNE_DÉFAUT, EnClic.CLIC_IMMÉDIAT);
                break;
            case CONTRÔLES:
                changerComportement(EnDoigtTouché.CLIC, EnDoigtRelâche.RETOURNE_DÉFAUT, EnDoigtDébarque.RETOURNE_DÉFAUT, EnSurvol.RIEN, EnSurvolDébarque.RESTE, EnSurvolRelâche.RESTE, EnClic.CLIC_RESTE);
                break;
            case SÉLECTION:
                changerComportement(EnDoigtTouché.CLIC, EnDoigtRelâche.RESTE, EnDoigtDébarque.RETOURNE_DÉFAUT, EnSurvol.RIEN, EnSurvolDébarque.RESTE, EnSurvolRelâche.RESTE, EnClic.CLIC_RESTE);
                break;
            case CASE_À_COCHER:
                changerComportement(EnDoigtTouché.CLIC, EnDoigtRelâche.RESTE, EnDoigtDébarque.RESTE, EnSurvol.RIEN, EnSurvolDébarque.RESTE, EnSurvolRelâche.RESTE, EnClic.ALTERNE);
                break;
        }
    }

    public void changerTaille(Vector2f taille){
        this.taille = taille.copy();
        bouttonDéfaut.scale.set( 0, Vector2f.scale(taille.copy(),2f) );
        bouttonPressé.scale.set( 0, Vector2f.scale(taille.copy(),2f) );
        if(bouttonSurvol != null){
            bouttonSurvol.scale.set( 0, Vector2f.scale(taille.copy(),2f) );
        }
    }

    public void changerPosition(Vector2f position){
        this.position = position.copy();
        bouttonDéfaut.position.set( 0, Vector2f.scale(position.copy(),4f) );
        bouttonPressé.position.set( 0, Vector2f.scale(position.copy(),4f) );
        if(bouttonSurvol != null){
            bouttonSurvol.position.set( 0, Vector2f.scale(position.copy(),4f) );
        }
    }

    public void changerRotation(float rotation){
        this.rotation = rotation;
        bouttonDéfaut.rotation.set( 0, rotation );
        bouttonPressé.rotation.set( 0, rotation );
        if(bouttonSurvol != null){
            bouttonSurvol.rotation.set( 0, rotation );
        }
    }
}
