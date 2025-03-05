package models;

import renderEngine.MasterRenderer;

import org.joml.Vector3f;

import entities.Entity;
import renderEngine.Loader;

public class CrossHair {
    
    public CrossHair(Loader loader, MasterRenderer renderer) {
        StaticImageModel crosshairModel = new StaticImageModel("res/img/crosshair.png", loader);
		Entity crosshairEntity = new Entity(crosshairModel, new Vector3f(0, 0, 0), 0, 0, 0, 0.1f);
		renderer.processStaticEntity(crosshairEntity);
    }
}
