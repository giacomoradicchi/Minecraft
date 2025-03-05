package chunk;

import entities.Entity;
import models.FaceModel;
import models.RawModel;
import models.Model;
import renderEngine.Loader;

public class Chunk {
    
    private ChunkData chunkData;
    private Entity meshEntity;

    public Chunk(ChunkData chunkData, Loader loader) {
        this.chunkData = chunkData;
        loadChunkData(loader);
    }

    public void loadChunkData(Loader loader) {
        RawModel chunkModel = chunkData.getChunkMesh().build(loader);
        Model texturedModel = new Model(chunkModel, FaceModel.getModelTexture(loader));
        meshEntity = new Entity(texturedModel, chunkData.getPosition(), 0, 0, 0, 1);
    }

    public ChunkData getChunkData() {
        return this.chunkData;
    }

    public Entity getChunkEntity() {
        return meshEntity;
    }
}
