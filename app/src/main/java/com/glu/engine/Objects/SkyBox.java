package com.glu.engine.Objects;

public class SkyBox {
    public RawModel model;
    public GTexture HDRI;
    public float strength;

    public SkyBox(RawModel model, GTexture HDRI) {
        this.model = model;
        this.HDRI = HDRI;
        strength = 1.0f;
    }
}
