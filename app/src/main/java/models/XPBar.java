package models;

import org.joml.Vector3f;

import entities.Entity;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import textures.Texture;

public class XPBar {
    
    private static final float XP_SCALE = 0.0374f;
    private static final String XP_PATH = "res/img/icons/xp_bar/xp_bar_low.png";


    public XPBar(Loader loader, MasterRenderer renderer) {
        Texture XPTexture = new Texture(XP_PATH);
		StaticImageModel hungerModel = new StaticImageModel(XPTexture, loader);
		Entity hungerEntity = new Entity(hungerModel, new Vector3f(0, -0.73f, 0), 0, 0, 0, XP_SCALE);
		renderer.processStaticEntity(hungerEntity);
    }
}
