package renderEngine;

import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import chunk.ChunkData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import shaders.StaticShader;
import toolbox.Maths;
import models.Model;
import entities.Camera;
import entities.Entity;
import entities.Light;

public class MasterRenderer {
    
    private StaticShader shader = new StaticShader();
    private Renderer renderer;
    private Frustum frustum;
    private Matrix4f projectionViewMatrix;

    private Map<Model, List<Entity>> entities = new HashMap<>();
    private Map<Model, List<Entity>> overlayEntities = new HashMap<>();
    private Map<Model, List<Entity>> visibleEntities = new HashMap<>();
    private Map<Model, List<Entity>> staticEntities = new HashMap<>();

    public MasterRenderer(Window window) {
        renderer = new Renderer(shader, window);
        frustum = new Frustum();
        projectionViewMatrix = new Matrix4f();
    }

    public void render(Light sun, Camera camera) {
        visibleEntities = new HashMap<>();
        prepareForFrustumCulling(camera);
        prepareVisibleEntities(); 

        renderer.prepare();
        shader.start();
        shader.loadLight(sun);
        shader.loadViewMatrix(camera);
        renderer.render(visibleEntities, overlayEntities, staticEntities);
        shader.stop();
        entities.clear();
        overlayEntities.clear();
    }

    private void prepareForFrustumCulling(Camera camera) {
        Matrix4f projectionMatrix = renderer.getProjectionMatrix();
        Matrix4f viewMatrix = Maths.createViewMatrix(camera);
        
        projectionViewMatrix.set(projectionMatrix)
                          .mul(viewMatrix);
        
        frustum.update(projectionViewMatrix);
    }

    private void prepareVisibleEntities() {
        Vector3f transformedCenter = new Vector3f();
        Matrix4f transformationMatrix = new Matrix4f();
        
        for(Map.Entry<Model, List<Entity>> entry : entities.entrySet()) {
            Model model = entry.getKey();
            List<Entity> batch = entry.getValue();
            List<Entity> visibleBatch = new ArrayList<>();
            
            for(Entity entity : batch) {
                transformationMatrix = Maths.createTransformationMatrix(
                    entity.getPosition(),
                    entity.getRotX(),
                    entity.getRotY(),
                    entity.getRotZ(),
                    entity.getScale()
                );
                
                transformedCenter.set(0, 0, 0); 
                transformationMatrix.transformPosition(transformedCenter);
                
                float scaleX = transformationMatrix.m00();
                float scaleY = transformationMatrix.m11();
                float scaleZ = transformationMatrix.m22();
                float maxScale = Math.max(Math.abs(scaleX), Math.max(Math.abs(scaleY), Math.abs(scaleZ)));
                float scaledRadius = 3 * ChunkData.CHUNK_SIZE * maxScale;
                
                if(frustum.isSphereInFrustum(transformedCenter, scaledRadius)) {
                    visibleBatch.add(entity);
                } 
                
            }
            
            if(!visibleBatch.isEmpty()) {
                visibleEntities.put(model, visibleBatch);
            }
        }
    }

    public void processEntity(Entity entity, boolean overlay) {
        putEntity(overlay ? overlayEntities : entities, entity);
    } 

    private void putEntity(Map<Model, List<Entity>> map, Entity newEntinty) {
        Model entityModel = newEntinty.getModel();
        List<Entity> batch = map.get(entityModel);
        if(batch != null) {
            batch.add(newEntinty);
        } else {
            List<Entity> newBatch = new ArrayList<>();
            newBatch.add(newEntinty);
            map.put(entityModel, newBatch);
        }
    }

    public void processStaticEntity(Entity staticEntity) {
        Model entityModel = staticEntity.getModel();
        List<Entity> batch = staticEntities.get(entityModel);
        if(batch != null) {
            batch.add(staticEntity);
        } else {
            List<Entity> newBatch = new ArrayList<>();
            newBatch.add(staticEntity);
            staticEntities.put(entityModel, newBatch);
        }
    }

    public void removeStaticEntity(Entity staticEntity) {
        staticEntities.remove(staticEntity.getModel(), staticEntity);
    }

    public void removeStaticModel(Model model) {
        staticEntities.remove(model);
    }

    public void removeEntity(Entity entity) {
        
        Model entityModel = entity.getModel();
        List<Entity> batch = entities.get(entityModel);

        if(batch != null) {
            batch.remove(entity);
        } 
    }

    public void cleanUp() {
        shader.cleanUp();
        staticEntities.clear();
    }
}
