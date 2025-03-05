package chunk;

import org.joml.Vector3f;

import models.ChunkMesh;
import models.FaceModel;
import models.FaceModel.*;

public class ChunkData {
    private Vector3f position;

    public static final int CHUNK_SIZE = 32; //32
    private static final int CHUNK_WIDTH = CHUNK_SIZE;
    private static final int CHUNK_DEPTH = CHUNK_SIZE;
    public static final int CHUNK_HEIGHT = 384; //384

    private int[][] heightMap;

    private BlockType[][][] blockType;
    private ChunkMesh mesh;
    
    // Parametri per la generazione del terreno
    
    private static final float TERRAIN_SCALE = 50.0f; 
    private static final int OCTAVES = 4;             
    private static final float PERSISTENCE = 0.2f;    
    private static final float BASE_HEIGHT = 32.0f;   

    private static final int[] p = new int[512];
    private static final int[] permutation = {
        151,160,137,91,90,15,131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,
        8,99,37,240,21,10,23,190,6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,
        35,11,32,57,177,33,88,237,149,56,87,174,20,125,136,171,168,68,175,74,165,71,
        134,139,48,27,166,77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,
        55,46,245,40,244,102,143,54,65,25,63,161,1,216,80,73,209,76,132,187,208,89,
        18,169,200,196,135,130,116,188,159,86,164,100,109,198,173,186,3,64,52,217,226,
        250,124,123,5,202,38,147,118,126,255,82,85,212,207,206,59,227,47,16,58,17,182,
        189,28,42,223,183,170,213,119,248,152,2,44,154,163,70,221,153,101,155,167,43,
        172,9,129,22,39,253,19,98,108,110,79,113,224,232,178,185,112,104,218,246,97,
        228,251,34,242,193,238,210,144,12,191,179,162,241,81,51,145,235,249,14,239,
        107,49,192,214,31,181,199,106,157,184,84,204,176,115,121,50,45,127,4,150,254,
        138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180
    }; 

    public ChunkData(Vector3f position) {
        this.position = position;
        this.blockType = new BlockType[CHUNK_WIDTH + 2][CHUNK_HEIGHT][CHUNK_DEPTH + 2];
        this.mesh = new ChunkMesh();
        heightMap = new int[CHUNK_WIDTH + 2][CHUNK_DEPTH + 2];
        for(int i = 0; i < 256; i++) {
            p[256 + i] = p[i] = permutation[i];
        }
        generateHeightMap();
        generateChunk();
        initializeVisibleBlockList();
    }

    // Per una questione di semplicità, quando un blocco viene rimosso viene ricreato 
    // l'intero chunk mesh da zero. Questo non è molto buono per le prestazioni, cercherò di
    // modificarlo in futuro.

    public BlockType removeBlock(int localX, int localY, int localZ) {
        // Considero pure i blocchi nella frontiera (range [-1, chunkSize])
        if(localX < -1 || localX > ChunkData.CHUNK_WIDTH
        || localY < -1 || localY > ChunkData.CHUNK_HEIGHT
        || localZ < -1 || localZ > ChunkData.CHUNK_DEPTH) {
            String message = "Valori immessi non validi: ";
            message += "[ " + localX + ", " + localY + ", " + localZ + "]\n";
            
            throw new IllegalArgumentException(message);
        }

        int x = localX + 1;
        int y = localY;
        int z = localZ + 1;

        BlockType oldType = blockType[x][y][z];
        blockType[x][y][z] = BlockType.AIR;

        updateVisibleBlockList();

        return oldType;
    }

    public void addBlock(int localX, int localY, int localZ, BlockType type) {
        if(localX < -1 || localX > ChunkData.CHUNK_WIDTH
        || localY < -1 || localY > ChunkData.CHUNK_HEIGHT
        || localZ < -1 || localZ > ChunkData.CHUNK_DEPTH) {
            String message = "Valori immessi non validi: ";
            message += "[ " + localX + ", " + localY + ", " + localZ + "]\n";

            throw new IllegalArgumentException(message);
        }

        int x = localX + 1;
        int y = localY;
        int z = localZ + 1;

        blockType[x][y][z] = type;

        updateVisibleBlockList();
            
    }

    private void generateHeightMap() {
        for(int x = 0; x < CHUNK_WIDTH + 2; x++) {
            for(int z = 0; z < CHUNK_DEPTH + 2; z++) {
                heightMap[x][z] = generateHeight(x - 1, z - 1);
            }
        }
    }

    private int generateHeight(int x, int z) {
        float height = 0;
        float amplitude = 14.0f;
        float frequency = 0.48f;
        float maxValue = 3;
        
        float globalX = position.x + x;
        float globalZ = position.z + z;
        
        for(int i = 0; i < OCTAVES; i++) {
            float sampleX = globalX * frequency / TERRAIN_SCALE;
            float sampleZ = globalZ * frequency / TERRAIN_SCALE;
            
            float perlinValue = noise(sampleX, sampleZ); 
            height += perlinValue * amplitude;

            maxValue += amplitude;
            amplitude *= PERSISTENCE;
            frequency *= 2;
        }
        
        height = (height / maxValue);
        height = height * height;
        height = BASE_HEIGHT + (height * CHUNK_HEIGHT / 2);
        
        return (int) Math.max(1, Math.min(CHUNK_HEIGHT - 1, height));
    }
    
    private float noise(float x, float z) {
        // Implementazione semplificata del rumore di Perlin
        int X = (int)Math.floor(x) & 255;
        int Z = (int)Math.floor(z) & 255;
        
        x -= Math.floor(x);
        z -= Math.floor(z);
        
        float u = fade(x);
        float w = fade(z);
        
        int A = p[X  ]+Z, AA = p[A], AB = p[A+1];
        int B = p[X+1]+Z, BA = p[B], BB = p[B+1];
        
        return lerp(w, lerp(u, grad(p[AA], x, z), 
        grad(p[BA], x - 1, z)),
        lerp(u, grad(p[AB], x, z - 1),
        grad(p[BB], x - 1, z - 1)));
    }
    
    private float fade(float t) { 
        return t * t * t * (t * (t * 6 - 15) + 10); 
    }
    
    private float lerp(float t, float a, float b) { 
        return a + t * (b - a); 
    }
    
    private float grad(int hash, float x, float z) {
        int h = hash & 15;
        float u = h < 8 ? x : z;
        float v = h < 4 ? z : h == 12 || h == 14 ? x : 0;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    private void generateChunk() {
        for(int x = 0; x < CHUNK_WIDTH + 2; x++) {
            for(int z = 0; z < CHUNK_DEPTH + 2; z++) {
                int height = heightMap[x][z];
                
                for(int y = 0; y < CHUNK_HEIGHT; y++) {
                    if(y > height) {
                        if(blockType[x][y][z] == null) {
                            blockType[x][y][z] = BlockType.AIR;
                        }
                    } else if(y == height) {
                        if(y > BASE_HEIGHT) {
                            blockType[x][y][z] = BlockType.GRASS;
                        } else {
                            blockType[x][y][z] = BlockType.SAND;
                        }
                    } else if(y > height - 3) {
                        blockType[x][y][z] = BlockType.DIRT;
                    } else {
                        blockType[x][y][z] = BlockType.STONE;
                    }
                }

                generateTree(x, z, height);
            }
        }
    }

    private void generateTree(int x, int z, int height) {
        if(x == CHUNK_WIDTH/2 
        && z == CHUNK_DEPTH/2
        && height != BASE_HEIGHT) {
            blockType[x][height][z] = BlockType.DIRT;

            // Wood 
            int treeHeight = 8;
            int treeBaseHeight = treeHeight/2;
            for(int i = 0; i < treeHeight - 1; i++) {
                blockType[x][height + 1 + i][z] = BlockType.TREE_WOOD;
            }

            // Leaf 
            for(int i = 0; i < treeHeight - treeBaseHeight; i++) {
                int width = 4;
                if(i > 1) {
                    width = 2;
                }
                for(int j = -width/2; j < width/2 + 1; j++) {
                    for(int k = -width/2; k < width/2 + 1; k++) {
                        if((j != 0 || k != 0 || i == treeHeight - treeBaseHeight - 1)) {
                            blockType[x + j][height + treeBaseHeight + i + 1][ z + k] = BlockType.TREE_LEAVES;
                        }
                    }
                }
            }
        }
    }

    private void initializeVisibleBlockList() {
        for(int x = 1; x <= CHUNK_WIDTH; x++) {
            for(int z = 1; z <= CHUNK_DEPTH; z++) {
                for(int y = 0; y < CHUNK_HEIGHT; y++) {
                    BlockType type = blockType[x][y][z];
                    if(!type.equals(BlockType.AIR)) {
                        boolean[] visibileFaces = getVisibleFaces(x, y, z);
                        for(byte faceID = 0; faceID < 6; faceID++) {
                            if(visibileFaces[faceID]) {
                                Vector3f blockPos = new Vector3f(x, y, z);
                                mesh.addFace(blockPos, type, faceID, getAOs(x, y, z, faceID));
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateVisibleBlockList() {
        mesh.clear();
        initializeVisibleBlockList();
    }

    private int[] getAOs(int x, int y, int z, int faceID) {
        int[] ao = new int[4];

        
        if(faceID == FaceModel.UPPER_FACE
        || faceID == FaceModel.BOTTOM_FACE) {
            if(!blockType[x - 1][y + 1][z - 1].equals(BlockType.AIR)) {
                ao[1]++;
            }

            if(!blockType[x - 1][y + 1][z].equals(BlockType.AIR)) {
                ao[0]++;
                ao[1]++;
            }

            if(!blockType[x - 1][y + 1][z + 1].equals(BlockType.AIR)) {
                ao[0]++;
            }

            if(!blockType[x][y + 1][z - 1].equals(BlockType.AIR)) {
                ao[1]++;
                ao[2]++;
            } 

            if(!blockType[x][y + 1][z + 1].equals(BlockType.AIR)) {
                ao[0]++;
                ao[3]++;
            }
            
            if(!blockType[x + 1][y + 1][z - 1].equals(BlockType.AIR)) {
                ao[2]++;
            }

            if(!blockType[x + 1][y + 1][z].equals(BlockType.AIR)) {
                ao[2]++;
                ao[3]++;
            }
            
            if(!blockType[x + 1][y + 1][z + 1].equals(BlockType.AIR)) {
                ao[3]++;
            }
        } else if(faceID == FaceModel.FRONT_FACE
        || faceID == FaceModel.BACK_FACE) {
            if(!blockType[x - 1][y][z].equals(BlockType.AIR)) {
                ao[0]++;
                ao[1]++;
            }
            if(!blockType[x - 1][y + 1][z].equals(BlockType.AIR)) {
                ao[0]++;
            }
            if(!blockType[x][y + 1][z].equals(BlockType.AIR)) {
                ao[0]++;
            }

            if(!blockType[x][y - 1][z].equals(BlockType.AIR)) {
                ao[1]++;
            }
            if(!blockType[x - 1][y - 1][z].equals(BlockType.AIR)) {
                ao[1]++;
            }

            if(!blockType[x + 1][y - 1][z].equals(BlockType.AIR)) {
                ao[2]++;
            }
            if(!blockType[x][y - 1][z].equals(BlockType.AIR)) {
                ao[2]++;
            }
            if(!blockType[x + 1][y][z].equals(BlockType.AIR)) {
                ao[2]++;
            }

            if(!blockType[x + 1][y + 1][z].equals(BlockType.AIR)) {
                ao[3]++;
            }
            if(!blockType[x + 1][y][z].equals(BlockType.AIR)) {
                ao[3]++;
            }
            if(!blockType[x][y + 1][z].equals(BlockType.AIR)) {
                ao[3]++;
            }
        } else if(faceID == FaceModel.RIGHT_FACE
        || faceID == FaceModel.LEFT_FACE) {
            if(!blockType[x][y][z - 1].equals(BlockType.AIR)) {
                ao[0]++;
            }
            if(!blockType[x][y + 1][z - 1].equals(BlockType.AIR)) {
                ao[0]++;
            }
            if(!blockType[x][y + 1][z].equals(BlockType.AIR)) {
                ao[0]++;
            }

            if(!blockType[x][y][z - 1].equals(BlockType.AIR)) {
                ao[1]++;
            }
            if(!blockType[x][y - 1][z - 1].equals(BlockType.AIR)) {
                ao[1]++;
            }
            if(!blockType[x][y - 1][z].equals(BlockType.AIR)) {
                ao[1]++;
            }

            if(!blockType[x][y - 1][z + 1].equals(BlockType.AIR)) {
                ao[2]++;
            }
            if(!blockType[x][y][z + 1].equals(BlockType.AIR)) {
                ao[2]++;
            }
            if(!blockType[x][y - 1][z].equals(BlockType.AIR)) {
                ao[2]++;
            }

            if(!blockType[x][y + 1][z + 1].equals(BlockType.AIR)) {
                ao[3]++;
            }
            if(!blockType[x][y][z + 1].equals(BlockType.AIR)) {
                ao[3]++;
            }
            if(!blockType[x][y + 1][z].equals(BlockType.AIR)) {
                ao[3]++;
            }
        }
        return ao;
    }

    private boolean[] getVisibleFaces(int x, int y, int z) {
        boolean[] visibileFaces = new boolean[6];

        visibileFaces[0] = (blockType[x][y][z - 1].equals(BlockType.AIR))
        || blockType[x][y][z - 1].getTrasparency();
        visibileFaces[1] = (blockType[x][y][z + 1].equals(BlockType.AIR))
        || blockType[x][y][z + 1].getTrasparency();
        
        visibileFaces[2] = (blockType[x + 1][y][z].equals(BlockType.AIR))
        || blockType[x + 1][y][z].getTrasparency();
        visibileFaces[3] = (blockType[x - 1][y][z].equals(BlockType.AIR))
        || blockType[x - 1][y][z].getTrasparency();

        visibileFaces[4] = (y == CHUNK_HEIGHT - 1) || (blockType[x][y + 1][z].equals(BlockType.AIR))
        || blockType[x][y + 1][z].getTrasparency();
        visibileFaces[5] = (y == 0) ||  (blockType[x][y - 1][z].equals(BlockType.AIR))
        || blockType[x][y - 1][z].getTrasparency(); 


        return visibileFaces;
    }

    public ChunkMesh getChunkMesh() {
        return this.mesh;
    }

    public void updateBlock(int x, int y, int z, BlockType newType) {
        blockType[x][y][z] = newType;

        initializeVisibleBlockList();
    }

    public int getHeight(int x, int z) {
        if(x < 0 || x >= CHUNK_WIDTH || z < 0 || z >= CHUNK_DEPTH) {
            String s = "Valori immessi non validi.\n";
            s += "x: " + x + ", z: " + z;
            
            throw new IllegalArgumentException(s);
        }

        return heightMap[x + 1][z + 1];
    } 
    
    public BlockType getBlockType(int x, int y, int z) {
        if(x < -1 || x >= CHUNK_WIDTH 
        || y < -1 || y >= CHUNK_HEIGHT
        || z < -1 || z >= CHUNK_DEPTH) {
            String s = "Valori immessi non validi.\n";
            s += "x: " + x + ", y: " + y + ", z: " + z;
            
            throw new IllegalArgumentException(s);
        }

        return blockType[x + 1][y][z + 1];
    }

    public Vector3f getPosition() {
        return this.position;
    }
}