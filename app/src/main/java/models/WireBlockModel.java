package models;

import org.joml.Vector3f;

import block.BlockData;
import entities.Entity;

import renderEngine.Loader;
import renderEngine.MasterRenderer;

public class WireBlockModel {

    public static final float RED = 0f;
    public static final float GREEN = 0.0f;
    public static final float BLUE = 0;

    private Model[] wireFaceModels;
    
    public WireBlockModel(Loader loader) {
        wireFaceModels = new Model[6];

        for(byte i = 0; i < 6; i++) {
            wireFaceModels[i] = new WireFaceModel(loader, i);
        }
    }

    public void processWireFaceEntities(MasterRenderer renderer, BlockData block) {
        if(block != null) {
            for(byte face : block.getFacesVisibleByCameraList()) {
                Entity wireFaceEntity1 = new Entity(wireFaceModels[face], new Vector3f(block.getAbsolutePosition()), 0, 0, 0, 1);
                Entity wireFaceEntity2 = new Entity(wireFaceModels[face], new Vector3f(block.getAbsolutePosition()), 0, 0, 0, 1.005f);
                Entity wireFaceEntity3 = new Entity(wireFaceModels[face], new Vector3f(block.getAbsolutePosition()), 0, 0, 0, 1.01f);

                renderer.processEntity(wireFaceEntity1, true);
                renderer.processEntity(wireFaceEntity2, true);
                renderer.processEntity(wireFaceEntity3, true);
            
            }
        }
    }
}
