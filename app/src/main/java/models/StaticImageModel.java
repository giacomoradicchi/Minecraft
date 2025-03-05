package models;

import renderEngine.Loader;
import textures.ModelTexture;
import textures.Texture;

public class StaticImageModel extends Model {
    private static float windowRatio;
    
    public StaticImageModel(float[] positions, float[] textureCoords, String texturePath, Loader loader) {
        super(getRawModel(loader, positions, textureCoords), getModelTexture(loader, texturePath));
    }

    public StaticImageModel(float[] positions, String texturePath, Loader loader) {
        this(positions, getBasicTextureCoords(), texturePath, loader);
    }

    public StaticImageModel(Texture texture, float[] textureCoords, Loader loader) {
        super(getRawModel(loader, 
        texture.getWidth() * (textureCoords[4] - textureCoords[0]), 
        texture.getHeight() * (textureCoords[3] - textureCoords[1]), textureCoords), 
        getModelTexture(loader, texture));

        /* this.width = -getProportionedPositions(texture.getWidth() * (textureCoords[4] - textureCoords[0]), 
        texture.getHeight() * (textureCoords[3] - textureCoords[1])) [0] * 2;
        this.height = getProportionedPositions(texture.getWidth() * (textureCoords[4] - textureCoords[0]), 
        texture.getHeight() * (textureCoords[3] - textureCoords[1])) [1] * 2; */
    }

    public StaticImageModel(Texture texture, Loader loader) {
        super(getRawModel(loader, texture.getWidth(), texture.getHeight(), getBasicTextureCoords()), 
        getModelTexture(loader, texture));
    }

    public StaticImageModel(String texturePath, Loader loader) {
        this(new Texture(texturePath), loader);
    }

    public StaticImageModel(String texturePath, float[] textureCoords, Loader loader) {
        this(new Texture(texturePath), textureCoords, loader);
    }

    public static void setWindowRatio(float windowRatio) {
        StaticImageModel.windowRatio = windowRatio;
    }

    public static float getWindowRatio() {
        return StaticImageModel.windowRatio;
    }

    private static RawModel getRawModel(Loader loader, float[] positions, float[] textureCoords) {
        return loader.loadToVAO(positions, textureCoords, getIndices());
    }

    private static RawModel getRawModel(Loader loader, float width, float height, float[] textureCoords) {
        return loader.loadToVAO(getProportionedPositions(width, height), textureCoords, getIndices());
    }

    private static ModelTexture getModelTexture(Loader loader, String texturePath) {
        return getModelTexture(loader, new Texture(texturePath));
    }

    private static ModelTexture getModelTexture(Loader loader, Texture texture) {
        return new ModelTexture(loader.loadTexture(texture));
    }

    private static int[] getIndices() {
        return new int[] {0,1,3, 3,1,2};
    }

    private static float[] getProportionedPositions(float width, float height) {
        float h = 1f;
        float w = h * width / height;

        if(windowRatio != 0) {
            w /= windowRatio;
        }

        return new float[] {
			-w/2, +h/2, 0,
			-w/2, -h/2, 0,
			+w/2, -h/2, 0,
			+w/2, +h/2, 0,
		};
    }

    private static float[] getBasicTextureCoords() {
        return new float[] {
            0, 0,
            0, 1,
            1, 1,
            1, 0
        };
    }
}
