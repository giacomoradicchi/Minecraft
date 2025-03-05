package models;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3f;

import renderEngine.Loader;
import textures.ModelTexture;

public class FaceModel extends Model {

    public enum BlockType {
        AIR(0, true),
        GRASS(1, false, "grass"),
        DIRT(2, false, "dirt"),
        STONE(3, false, "stone"),
        SAND(4, false, "sand"),
        WATER(5, false),
        TREE_WOOD(6, false, "tree_wood"),
        TREE_LEAVES(7, true, "tree_leaves"),
        DIAMOND(8, false),
        RAW_DIAMOND(9, false),
        GOLD(10, false);
    
        private final int id;
        private final boolean transparency;
        private final String fileName;
    
        BlockType(int id, boolean transparency, String fileName) {
            this.id = id;
            this.transparency = transparency;
            this.fileName = fileName;
        }

        BlockType(int id, boolean transparency) {
            this(id, transparency, null);
        }
    
        public int getId() {
            return id;
        }

        public boolean getTrasparency() {
            return transparency;
        }

        public String getIconPath() {
            return "res/img/icons/blocks/" + fileName + ".png";
        }

        public static int getLastId() {
            return BlockType.values().length - 1; 
        }
    
        public static BlockType fromId(int id) {
            for (BlockType block : BlockType.values()) {
                if (block.getId() == id) {
                    return block;
                }
            }
            throw new IllegalArgumentException("ID del blocco non valido: " + id);
        }
    }
    
    public static final byte BACK_FACE = 0;
    public static final byte FRONT_FACE = 1;
    public static final byte RIGHT_FACE = 2;
    public static final byte LEFT_FACE = 3;
    public static final byte UPPER_FACE = 4;
    public static final byte BOTTOM_FACE = 5;

    public static final byte[] FACES = {0, 1, 2, 3, 4, 5};

    private static Map<FaceModel.BlockType, Model[]> faceModels;
    private static ModelTexture textureAtlas;

    private static final String TEXTURE_PATH = "res/img/1.15 Pre-released levels/";
    private static final int ATLAS_SIZE = 64;
    private static final Map<BlockType, int[]> TEXTURE_ATLAS_COORDINATES = loadTextureAtlasCoords();

    public FaceModel(Loader loader, FaceModel.BlockType type, byte faceID) {
        super(getRawModel(loader, type, faceID), getModelTexture(loader));
    }

    public static void loadAllFaceModels(Loader loader) {
        faceModels = new HashMap<>();
        
        for(int blockID = 1; blockID <= FaceModel.BlockType.getLastId(); blockID++) {
            Model[] texturedModels = new Model[6]; 
            for(byte faceID = 0; faceID < 6; faceID++) {
                texturedModels[faceID] = new FaceModel(loader, FaceModel.BlockType.fromId(blockID), faceID);
            }
            faceModels.put(BlockType.fromId(blockID), texturedModels);
        }
    }

    public static Model[] getFaceModels(BlockType type) {
        return faceModels.get(type);
    }

    public static Model getFaceModel(BlockType type, byte faceID) {
        if(faceID < 0 || faceID >= 6) {
            throw new IllegalArgumentException("Invalid faceID: " + faceID);
        }
        return faceModels.get(type)[faceID];
    }

    public static ModelTexture getModelTexture(Loader loader) {
        if(textureAtlas == null) {
            textureAtlas = new ModelTexture(loader.loadTexture(getMipMapTextureAtlases()));
        }
        return textureAtlas;
    }

    private static final Map<BlockType, int[]> loadTextureAtlasCoords() {
        Map<BlockType, int[]> map = new HashMap<>();
        /* Fornisco prima l'indice di riga v e poi l'indice di colonna u del texture atlas */
        map.put(BlockType.GRASS, new int[] {
            6, 20, // Facce laterali
            3, 33, // Faccia superiore
            // 16, 25 faccia superiore più chiara (secondo me è più bella, però il minecraft originale ha la faccia più scura)
            10, 17 // Faccia inferiore
        }); 
        map.put(BlockType.DIRT, new int[] {
            10, 17,
            10, 17,
            10, 17,
        });
        map.put(BlockType.STONE, new int[] {
            18, 30,
            18, 30,
            18, 30,
        });
        map.put(BlockType.SAND, new int[] {
            17, 28,
            17, 28,
            17, 28 
        });
        map.put(BlockType.WATER, new int[] {
            8, 19,
            8, 19,
            8, 19
        });
        map.put(BlockType.TREE_WOOD, new int[] {
            18, 25,
            18, 26,
            18, 26
        });
        map.put(BlockType.TREE_LEAVES, new int[] {
            2, 33,
            2, 33,
            2, 33,
        });
        map.put(BlockType.DIAMOND, new int[] {
            7, 17,
            7, 17,
            7, 17
        });
        map.put(BlockType.RAW_DIAMOND, new int[] {
            8, 17,
            8, 17,
            8, 17
        });
        map.put(BlockType.GOLD, new int[] {
            2, 20,
            2, 20,
            2, 20
        });
        
        return map;
    }

    private static String[] getMipMapTextureAtlases() {
        String[] paths = new String[5];

        for(int i = 0; i < paths.length; i++) {
            paths[i] = TEXTURE_PATH + i + ".png";
        }
        return paths;
    }

    public static RawModel getRawModel(Loader loader, BlockType type, byte faceID) {
        return loader.loadToVAO(getVertices(faceID), getTextureCoords(type, faceID), getNormals(faceID), getIndices(faceID));
    }

    public static float[] getVertices(byte faceID) {
        
        switch (faceID) {
            case BACK_FACE:
                return new float[] {
                    -0.5f,0.5f,-0.5f,	
                    -0.5f,-0.5f,-0.5f,	
                    0.5f,-0.5f,-0.5f,	
                    0.5f,0.5f,-0.5f
                };
            
            case FRONT_FACE:
                return new float[] {
                    -0.5f,0.5f,0.5f,	
                    -0.5f,-0.5f,0.5f,	
                    0.5f,-0.5f,0.5f,	
                    0.5f,0.5f,0.5f
                };
            
            case RIGHT_FACE:
                return new float[] {
                    0.5f,0.5f,-0.5f,	
			        0.5f,-0.5f,-0.5f,	
			        0.5f,-0.5f,0.5f,	
			        0.5f,0.5f,0.5f
                };

            case LEFT_FACE:
                return new float[] {
                    -0.5f,0.5f,-0.5f,	
			        -0.5f,-0.5f,-0.5f,	
			        -0.5f,-0.5f,0.5f,	
			        -0.5f,0.5f,0.5f
                };

            case UPPER_FACE:
                return new float[] {
                    -0.5f,0.5f,0.5f,
			        -0.5f,0.5f,-0.5f,
			        0.5f,0.5f,-0.5f,
			        0.5f,0.5f,0.5f
                };

            case BOTTOM_FACE:
                return new float[] {
                    -0.5f,-0.5f,0.5f,
			        -0.5f,-0.5f,-0.5f,
			        0.5f,-0.5f,-0.5f,
			        0.5f,-0.5f,0.5f
                };
        
            default:
                throw new IllegalArgumentException("Invalid faceID: " + faceID);
        }
    }

    public static float[] getTextureCoords(BlockType type, byte faceID) {
        int[] textureAtlasCoords = TEXTURE_ATLAS_COORDINATES.get(type);
        if(textureAtlasCoords == null) {
            return null;
        }
        float[] textureCoords = new float[2*4]; 

        /* // Facce laterali:
        for(int i = 0; i < 4; i++) {
            initializeTextureFace(i, textureAtlasCoords[0], textureAtlasCoords[1], textureCoords);
        }

        // Faccia superiore:
        initializeTextureFace(4, textureAtlasCoords[2], textureAtlasCoords[3], textureCoords);

        // Faccia inferiore:
        initializeTextureFace(5, textureAtlasCoords[4], textureAtlasCoords[5], textureCoords); */

        int index = 0;
        if(faceID == 4) {
            index = 2;
        } else if(faceID == 5) {
            index = 4;
        }

        initializeTextureFace(faceID, textureAtlasCoords[index], textureAtlasCoords[index + 1], textureCoords);
        
        return textureCoords;
    }

    private static void initializeTextureFace(int faceID, int v, int u, float[] textureCoords) {
        /*  Il codice dovrebbe fare ciò che ho scritto qua sotto per determinare la coordinata
            della texture da passare al loader. Per rendere il codice più pulito, ho usato le 
            classi di resto.

            face = numero di una delle sei facce (0, 1, 2, 3, 4, 5).

            textureCoords[face*8] = (float) u/ATLAS_SIZE; 
            textureCoords[face*8 + 1] = (float) v/ATLAS_SIZE; 

            textureCoords[face*8 + 2] = (float) u/ATLAS_SIZE; 
            textureCoords[face*8 + 3] = (float) (v + 1)/ATLAS_SIZE; 

            textureCoords[face*8 + 4] = (float) (u + 1)/ATLAS_SIZE; 
            textureCoords[face*8 + 5] = (float) (v + 1)/ATLAS_SIZE; 

            textureCoords[face*8 + 6] = (float) (u + 1)/ATLAS_SIZE; 
            textureCoords[face*8 + 7] = (float) v/ATLAS_SIZE;  
            
            come si può notare, le coordinate u e v vengono aumentate di 1 o rimangono invariate 
            a seconda del vertice che stiamo definendo. In particolare, l'aumento di u e v,
            rispettivamente, segue questo pattern:
            {   0, 0
                0, 1,
                1, 1,
                1, 0
            }.

            Per riottenerlo nel ciclo for, a u devo sommare (i / 4) % 2, mentre a v aggiungo
            (i / 2 + i / 4) % 2).

            Per i = 0: 
                (0 / 4) % 2 = 0 % 2 = 0
                (0 / 2 + 0 / 4) % 2) = (0 + 0) % 2 = 0
            Per i = 2: 
                (2 / 4) % 2 = 2 % 2 = 0
                (2 / 2 + 2 / 4) % 2) = (1 + 0) % 2 = 1
            Per i = 4: 
                (4 / 4) % 2 = 1 % 2 = 1
                (4 / 2 + 4 / 4) % 2) = (2 + 1) % 2 = 1
            Per i = 6: 
                (6 / 4) % 2 = 1 % 2 = 1
                (6 / 2 + 6 / 4) % 2) = (3 + 1) % 2 = 0
            
            Riotteniamo dunque il pattern desiderato.
            */

        for(int i = 0; i < 4; i++) {
            textureCoords[2*i] = (float) (u + (i / 2) % 2)/ATLAS_SIZE;
            textureCoords[2*i + 1] = (float) (v + (i + i / 2) % 2)/ATLAS_SIZE;
        }
    }

    public static float[] getNormals(byte faceID) {
        switch(faceID) {
            case BACK_FACE:
                return new float[] {
                    0f, 0f, -1f,
                    0f, 0f, -1f,
                    0f, 0f, -1f,
                    0f, 0f, -1f
                };
            case FRONT_FACE:
                return new float[] {
                    0f, 0f, 1f,
                    0f, 0f, 1f,
                    0f, 0f, 1f,
                    0f, 0f, 1f
                };
            case RIGHT_FACE:
                return new float[] {
                    1f, 0f, 0f,
                    1f, 0f, 0f,
                    1f, 0f, 0f,
                    1f, 0f, 0f
                };
            case LEFT_FACE:
                return new float[] {
                    -1f, 0f, 0f,
                    -1f, 0f, 0f,
                    -1f, 0f, 0f,
                    -1f, 0f, 0f
                };
            case UPPER_FACE:
                return new float[] {
                    0f, 1f, 0f,
                    0f, 1f, 0f,
                    0f, 1f, 0f,
                    0f, 1f, 0f
                };
            case BOTTOM_FACE:
                return new float[] {
                    0f, -1f, 0f,
                    0f, -1f, 0f,
                    0f, -1f, 0f,
                    0f, -1f, 0f
                };
            default:
                throw new IllegalArgumentException("Invalid faceID: " + faceID);
        }
        
    }

    public static Vector3f getNormalsVector(byte face) {
        float[] normalsArray = getNormals(face);
        return new Vector3f(normalsArray[0], normalsArray[1], normalsArray[2]);
    }

    public static byte getFaceByNormal(Vector3f faceNormal) {
        for(byte face : FACES) {
            if(getNormalsVector(face).equals(faceNormal, 0)) {
                return face;
            }
        }

        return -1;
    }

    public static int[] getIndices(byte faceID) {
        if(faceID < 0 || faceID >= 6) {
            throw new IllegalArgumentException("Invalid faceID: " + faceID);
        }

        if(faceID % 2 == 1) {
            return new int[] {0,1,3, 3,1,2};
        }
        return new int[] {0,3,1, 3,2,1};
    }
}
