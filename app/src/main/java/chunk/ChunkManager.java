package chunk;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.joml.Vector2i;
import org.joml.Vector3f;

import block.BlockData;
import models.FaceModel.BlockType;
import models.RawModel;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.Window;

public class ChunkManager {
    private static final int MAX_VIEW_DISTANCE = 8;
    private static int viewDistance = 2;

    private static Window window;

    private Map<String, Chunk> chunks;
    private Map<String, ChunkData> chunksData;
    private Set<String> processingChunks;
    private ExecutorService chunkLoader;
    private Vector3f cameraPosition; 
    private Loader loader;
    private Object chunkLock; 
    
    
   

    public ChunkManager(Vector3f initialCameraPosition, Loader loader, Window window) {
        this.chunks = new HashMap<>();
        this.chunksData = new HashMap<>();
        this.processingChunks = new HashSet<>();
        this.chunkLock = new Object();
        this.chunkLoader = Executors.newFixedThreadPool(4);
        this.cameraPosition = initialCameraPosition;
        this.loader = loader;
        ChunkManager.window = window;

        initStartChunk(initialCameraPosition);
    }

    private void initStartChunk(Vector3f initialCameraPosition) {
        int startChunkX = (int) Math.floor(initialCameraPosition.x / ChunkData.CHUNK_SIZE);
        int startChunkZ = (int) Math.floor(initialCameraPosition.z / ChunkData.CHUNK_SIZE);
        String startChunkKey = getChunkKey(startChunkX, startChunkZ);
        
        ChunkData startChunkData = generateChunkData(startChunkX, startChunkZ);
        Chunk startChunk = new Chunk(startChunkData, loader);
        chunks.put(startChunkKey, startChunk);
    }

    private static void updateViewDistance() {
        if(window.getFPS() > 240 && viewDistance < MAX_VIEW_DISTANCE) {
            viewDistance++;
        } /* else if(window.getFPS() < 30 && viewDistance > MIN_VIEW_DISTANCE) {
            viewDistance--;
        } */
    }

    public void updateChunks(Vector3f newCameraPosition) {
        updateViewDistance();
        
        this.cameraPosition = newCameraPosition;
        int[] currentChunkPos = calculateChunkPositionXZ(cameraPosition.x, cameraPosition.z);

        removeChunks(currentChunkPos[0], currentChunkPos[1]);
        createChunk(currentChunkPos[0], currentChunkPos[1]);
        
    }

    private void removeChunks(int currentChunkX, int currentChunkZ) {
        synchronized(chunkLock) {
            Iterator<Map.Entry<String, Chunk>> it = chunks.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<String, Chunk> entry = it.next();
                String key = entry.getKey();
                int[] coords = parseChunkKey(key);
                
                if (Math.abs(coords[0] - currentChunkX) > viewDistance || 
                    Math.abs(coords[1] - currentChunkZ) > viewDistance) {
                    
                    Chunk chunk = entry.getValue();
                    if(chunk != null && chunk.getChunkEntity() != null) {
                        RawModel model = chunk.getChunkEntity().getModel().getRawModel();
                        loader.cleanUpModel(model);
                    }
                    it.remove();
                }
            }
        }
    }

    private void createChunk(int currentChunkX, int currentChunkZ) {
        synchronized(chunkLock) {
            for (int x = -viewDistance; x <= viewDistance; x++) {
                for (int z = -viewDistance; z <= viewDistance; z++) {
                    int chunkX = currentChunkX + x;
                    int chunkZ = currentChunkZ + z;
                    String chunkKey = getChunkKey(chunkX, chunkZ);
    

                    if (!chunks.containsKey(chunkKey) &&
                        !chunksData.containsKey(chunkKey) &&
                        !processingChunks.contains(chunkKey)) {
                        
                        processingChunks.add(chunkKey);
                        
                        final int finalChunkX = chunkX;
                        final int finalChunkZ = chunkZ;
                        final String finalChunkKey = chunkKey;
    
                        chunkLoader.submit(() -> {
                            try {
                                ChunkData newChunkData = generateChunkData(finalChunkX, finalChunkZ);
                                
                                synchronized(chunkLock) {
                                    chunksData.put(finalChunkKey, newChunkData);
                                    processingChunks.remove(finalChunkKey);
                                }
                            } catch (Exception e) {
                                synchronized(chunkLock) {
                                    processingChunks.remove(finalChunkKey);
                                }
                                e.printStackTrace();
                            }
                        });
                    }
                }
            }
    
            Set<String> completedChunks = new HashSet<>(chunksData.keySet());
            for (String chunkKey : completedChunks) {
                ChunkData chunkData = chunksData.remove(chunkKey);
                if (chunkData != null) {
                    Chunk chunk = new Chunk(chunkData, loader);
                    chunks.put(chunkKey, chunk);
                }
            }
        }
    }

    private ChunkData generateChunkData(int chunkX, int chunkZ) {
        Vector3f position = new Vector3f(chunkX * ChunkData.CHUNK_SIZE, 0, chunkZ * ChunkData.CHUNK_SIZE);
        return new ChunkData(position);
    }

    public int getHeight(float x, float z) {
        int[] currentChunkPos = calculateChunkPositionXZ(x, z);

        
        Chunk chunk = chunks.get(getChunkKey(currentChunkPos[0], currentChunkPos[1]));

        int relativeX = getRelativeX(x, currentChunkPos[0]);
        int relativeZ = getRelativeZ(z, currentChunkPos[1]);

        return chunk.getChunkData().getHeight(relativeX, relativeZ);
    }

    public BlockData getBlockData(float x, float y, float z) {
        if(y < 0 || y >= ChunkData.CHUNK_HEIGHT) {
            return null;
        }
        int[] currentChunkPos = calculateChunkPositionXZ(x, z);

        Chunk chunk = chunks.get(getChunkKey(currentChunkPos[0], currentChunkPos[1]));

        int relativeX = getRelativeX(x, currentChunkPos[0]);
        int relativeZ = getRelativeZ(z, currentChunkPos[1]);
        
        if(chunk != null && chunk.getChunkData() != null) {
            Vector3f absolutePosition = new Vector3f(
                (float) Math.floor(x) + 1, 
                (float) Math.floor(y), 
                (float) Math.floor(z) + 1
            );
            BlockType type = chunk.getChunkData().getBlockType(relativeX, (int) absolutePosition.y, relativeZ);
            
            Vector2i localXZPosition = new Vector2i(relativeX, relativeZ);
            return new BlockData(absolutePosition, localXZPosition, type);
        }
        return null;
    }

    private int getRelativeX(float x, int chunkPosX) {
        int relativeX = (x >= 0) ? (int) x % ChunkData.CHUNK_SIZE
        : (int) (-chunkPosX*ChunkData.CHUNK_SIZE + x) % ChunkData.CHUNK_SIZE; 
        
        return relativeX;
    }

    private int getRelativeZ(float z, int chunkPosZ) {
        int relativeZ = (z >= 0) ? (int) z % ChunkData.CHUNK_SIZE
                        : (int) (-chunkPosZ*ChunkData.CHUNK_SIZE + z) % ChunkData.CHUNK_SIZE;
        
        return relativeZ;
    }

    public void removeBlock(BlockData block) {
        if(block.getBlockType() == BlockType.AIR) {
            return;
        }

        int[] chunkPos = calculateChunkPositionXZ(block.getAbsolutePosition().x - 1, block.getAbsolutePosition().z - 1);


        int x = block.getLocalXZPosition().x;
        int z = block.getLocalXZPosition().y; //n.b: getLocalXZPosition().y = z 

        Chunk chunk;
        RawModel model;

        // Devo rimuovere il blocco non solo dal chunk in cui si trova, ma anche dai chunk vicini
        // qualora si trovi nel limite tra un chunk e un altro. (per gestire il rendering delle ombre)

        int[] xBoundValues = new int[] {0, x, ChunkData.CHUNK_SIZE - 1};
        int[] zBoundValues = new int[] {0, z, ChunkData.CHUNK_SIZE - 1};

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                if(x == xBoundValues[i] && z == zBoundValues[j]) {
                    int dirX = i - 1;
                    int dirZ = j - 1;

                    chunk = chunks.get(getChunkKey(chunkPos[0] + dirX, chunkPos[1] + dirZ));
                    

                    if (chunk != null) { 
                        chunk.getChunkData().removeBlock(xBoundValues[2 - i] - dirX, (int) block.getAbsolutePosition().y, zBoundValues[2 - j] - dirZ);
                        
                        model = chunk.getChunkEntity().getModel().getRawModel();
                        loader.cleanUpModel(model);
                        chunk.loadChunkData(loader);
                    }
                }
            }
        }
        
    }

    public void addBlock(int x, int y, int z, BlockType type) {
        int[] chunkPos = calculateChunkPositionXZ(x, z);

        int relativeX = getRelativeX(x, chunkPos[0]);
        int relativeZ = getRelativeZ(z, chunkPos[1]);

        Chunk chunk;
        RawModel model;

        int[] xBoundValues = {0, relativeX, ChunkData.CHUNK_SIZE - 1};
        int[] zBoundValues = {0, relativeZ, ChunkData.CHUNK_SIZE - 1};

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                if(relativeX == xBoundValues[i] && relativeZ == zBoundValues[j]) {
                    int dirX = i - 1;
                    int dirZ = j - 1;

                    chunk = chunks.get(getChunkKey(chunkPos[0] + dirX, chunkPos[1] + dirZ));

                    if(chunk != null) {
                        chunk.getChunkData().addBlock(xBoundValues[2 - i] - dirX, y, zBoundValues[2 - j] - dirZ, type);

                        model = chunk.getChunkEntity().getModel().getRawModel();
                        loader.cleanUpModel(model);
                        chunk.loadChunkData(loader);
                    }
                }
            }
        }
    }


    private int[] calculateChunkPositionXZ(float x, float z) {
        return new int[] {
            (int) Math.floor(x / ChunkData.CHUNK_SIZE),
            (int) Math.floor(z / ChunkData.CHUNK_SIZE)
        };
    }

    public BlockData getBlockData(Vector3f position) {
        return getBlockData(position.x, position.y, position.z);
    }

    private String getChunkKey(int chunkX, int chunkZ) {
        return chunkX + "," + chunkZ;
    }

    private int[] parseChunkKey(String key) {
        String[] parts = key.split(",");
        return new int[] {Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }

    public Chunk getChunkAt(int x, int z) {
        String chunkKey = getChunkKey(x / ChunkData.CHUNK_SIZE, z / ChunkData.CHUNK_SIZE);
        return chunks.get(chunkKey);
    }

    public void processChunkEntities(MasterRenderer renderer) {
        for(Chunk chunk : chunks.values()) {
            renderer.processEntity(chunk.getChunkEntity(), false);
        }
    }

    public void cleanup() {
        chunkLoader.shutdown();
        try {
            if (!chunkLoader.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                chunkLoader.shutdownNow();
            }
        } catch (InterruptedException e) {
            chunkLoader.shutdownNow();
        }
    }
}