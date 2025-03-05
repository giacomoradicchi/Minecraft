package models;

import textures.ModelTexture;

public class Model {

    private RawModel rawModel;
    private ModelTexture texture;
    private boolean isWireframe;

    public Model(RawModel model, ModelTexture texture) {
        this.rawModel = model;
        this.texture = texture;
        this.isWireframe = false;
    }

    public Model(RawModel model) {
        this.rawModel = model;
        this.isWireframe = true;
    }

    public void setRawModel(RawModel model) {
        this.rawModel = model;
    }

    public RawModel getRawModel() {
        return this.rawModel;
    }

    public boolean isWireframe() {
        return this.isWireframe;
    }

    public ModelTexture getTexture() {
        return this.texture;
    }
    
}
