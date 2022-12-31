package com.glu.engine.GUI;

import com.glu.engine.Objects.GTexture;
import com.glu.engine.shader.TexQuadShader;
import com.glu.engine.vectors.Vector2f;

public class TexQuad extends GUIBase {

    public GTexture texture;

    public TexQuad(Vector2f screenDimensions, GTexture texture){
        super(screenDimensions);

        this.texture = texture;
    }

    public TexQuad(Vector2f screenDimensions){
        super(screenDimensions);

        this.texture = new GTexture(1,1,false,false,false,false,false);

        super.makeModel();
    }

    public TexQuad copy(){
        TexQuad a = new TexQuad(screenDimensions,texture);
        a.copy(this);
        return a;
    }

    public void scaleToTexture(int index){
        float x = (float) texture.bmp.getWidth();
        float y = (float) texture.bmp.getHeight();
        if(x<y){
            scale.set(index, new Vector2f(scale.get(index).x * (x/y),scale.get(index).y));
        }else {
            scale.set(index, new Vector2f(scale.get(index).x,scale.get(index).y * (y/x)));
        }
    }

}
