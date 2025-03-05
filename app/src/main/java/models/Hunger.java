package models;

import org.joml.Vector3f;

import entities.Entity;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import textures.Texture;

public class Hunger {
    
    private static final int HUNGER_QUANTITY = 10;
    private static final float HUNGER_SCALE = 0.06f;
    private static final String HUNGER_PATH = "res/img/icons/hunger/hunger.png";

    private float hungerWidth;

    public Hunger(Loader loader, MasterRenderer renderer, float normalizedXPositionRight) {
        Texture hungerTexture = new Texture(HUNGER_PATH);
		StaticImageModel hungerModel = new StaticImageModel(hungerTexture, loader);
		Entity[] hungerEntities = new Entity[HUNGER_QUANTITY];
		hungerWidth = (float) hungerTexture.getWidth() / (hungerTexture.getHeight()) / StaticImageModel.getWindowRatio() * HUNGER_SCALE;
		for(int i = 0; i < HUNGER_QUANTITY; i++) {
			hungerEntities[i] = new Entity(hungerModel, new Vector3f(normalizedXPositionRight - (HUNGER_QUANTITY - i) * hungerWidth, -0.67f, 0), 0, 0, 0, HUNGER_SCALE);
			renderer.processStaticEntity(hungerEntities[i]);
		}
    }
}
