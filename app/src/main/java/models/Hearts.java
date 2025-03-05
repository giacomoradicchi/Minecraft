package models;

import org.joml.Vector3f;

import entities.Entity;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import textures.Texture;

public class Hearts {
    
    private static final int HEART_QUANTITY = 10;
    private static final float HEART_SCALE = 0.06f;
    private static final String HEART_PATH = "res/img/icons/heart/heart.png";

    private float heartWidth;

    public Hearts(Loader loader, MasterRenderer renderer, float normalizedXPosition) {
        Texture heartTexture = new Texture(HEART_PATH);
		StaticImageModel heartModel = new StaticImageModel(heartTexture, loader);
		Entity[] heartEntities = new Entity[HEART_QUANTITY];
		heartWidth = (float) heartTexture.getWidth() / (heartTexture.getHeight()) / StaticImageModel.getWindowRatio() * HEART_SCALE;
		for(int i = 0; i < HEART_QUANTITY; i++) {
			heartEntities[i] = new Entity(heartModel, new Vector3f(normalizedXPosition + i*heartWidth - heartWidth/2, -0.67f, 0), 0, 0, 0, HEART_SCALE);
			renderer.processStaticEntity(heartEntities[i]);
		}
    }
}
