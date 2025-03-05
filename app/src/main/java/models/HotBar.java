package models;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3f;

import entities.Entity;
import models.FaceModel.BlockType;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import textures.Texture;

public class HotBar {

    private int currentIndex;

    private class BlockQuantity {
        private BlockType blockType;
        private int quantity;

        private static final int MAX_BLOCK_QUANTITY = 64;

        private BlockQuantity(BlockType blockType, int quantity) {
            this.blockType = blockType;
            this.quantity = quantity;
        }

        private boolean addBlock() {
            if(quantity < MAX_BLOCK_QUANTITY) {
                quantity++;
                return true;
            }
            return false;
        }

        private boolean removeBlock() {
            if(quantity > 0) {
                quantity--;
                return true;
            }
            return false;
        }

        private BlockType getBlockType() {
            return blockType;
        }

        private int getQuantity() {
            return quantity;
        }
    }

    private BlockQuantity[] hotbar;

    private Entity[] hotbarEntities;
    private Entity hotbarSelectedEntity;

    private Map<BlockType, StaticImageModel> blockIcons = new HashMap<>();

    private float hotbarWidth;
    private float normalizedOffset;

    private Loader loader;
    private MasterRenderer renderer;

    private static final float ICON_SCALE = 0.1f;
    private static final float HOTBAR_SCALE = 0.15f;
    private static final float HOTBAR_Y_CENTRE = -0.85f;
    private static final String HOTBAR_PATH = "res/img/hotbar.png";

    public static final int HOTBAR_SIZE = 9;

    public HotBar(Loader loader, MasterRenderer renderer) {
        this.hotbar = new BlockQuantity[HOTBAR_SIZE];
        this.loader = loader;
        this.renderer = renderer;

        loadHotBarModels();
        processHotBarEntity();
    }

    private void loadHotBarModels() {
		Texture hotbarTexture = new Texture(HOTBAR_PATH);
		StaticImageModel hotbarModel = new StaticImageModel(hotbarTexture, 
		new float[] {0, 0, 0f, 1f, 0.5f, 1, 0.5f, 0}, loader);
		StaticImageModel hotbarSelectedModel = new StaticImageModel(hotbarTexture, 
		new float[] {0.5f, 0, 0.5f, 1f, 1f, 1, 1f, 0}, loader);

		hotbarEntities = new Entity[HOTBAR_SIZE];
		hotbarWidth = (float) hotbarTexture.getWidth() / (2*hotbarTexture.getHeight()) / StaticImageModel.getWindowRatio() * HOTBAR_SCALE;
        normalizedOffset = -HOTBAR_SIZE/2*hotbarWidth;
		for(int i = 0; i < HOTBAR_SIZE; i++) {
			hotbarEntities[i] = new Entity(hotbarModel, new Vector3f(normalizedOffset + i*hotbarWidth, HOTBAR_Y_CENTRE, 0), 0, 0, 0, HOTBAR_SCALE);
		}

		hotbarSelectedEntity = new Entity(hotbarSelectedModel, new Vector3f(normalizedOffset, HOTBAR_Y_CENTRE, -1), 0, 0, 0, HOTBAR_SCALE * 1.2f);
    }

    /*  Per aggiungere un'icona da inserire dentro una casella dell'hotbar, 
     * devo evitare di creare più di una volta il modello di icona (sprecherei memoria).
     * Al tempo stesso, non decido di caricare tutto il modello di icone in una sola volta.
     * Decido dunque di tenere traccia dei modelli creati attraverso una mappa.
     * Il modello viene creato solamente una volta e solo quando viene richiesto.
     */
    private void addBlockIcon(BlockType blockType, int index) {
        StaticImageModel blockIconModel = blockIcons.get(blockType);

        if(blockIconModel == null) {
            blockIconModel = new StaticImageModel(blockType.getIconPath(), loader);
            blockIcons.put(blockType, blockIconModel);
        }

        float xCentre = (index -HOTBAR_SIZE/2) * hotbarWidth;
        
        Entity blockIconEntity = new Entity(blockIconModel, new Vector3f(xCentre, HOTBAR_Y_CENTRE, -1), 0, 0, 0, ICON_SCALE);
        renderer.processStaticEntity(blockIconEntity);
    }

    // Rimuovo il modello dal renderer, ma non dalla lista blockIcon qualora mi dovesse riservire.
    private void removeBlockIcon(BlockType blockType) {
        renderer.removeStaticModel(blockIcons.get(blockType));
    }

    private void processHotBarEntity() {
        renderer.processStaticEntity(hotbarSelectedEntity);
		for(Entity hotbar : hotbarEntities) {
			renderer.processStaticEntity(hotbar);
		}
    }

    public void setIndex(int index) {
        if(index < 0 || index >= HotBar.HOTBAR_SIZE) {
            throw new IllegalArgumentException("Indice non valido.\n");
        }

        currentIndex = index;
        Vector3f previousPosition = hotbarSelectedEntity.getPosition();
	    hotbarSelectedEntity.setPosition(new Vector3f(
			-hotbarEntities.length/2*hotbarWidth + index*hotbarWidth, previousPosition.y, -1));
    }

    public float getNormalizedXPosition() {
        return normalizedOffset;
    }

    public float getNormalizedXPositionRight() {
        return -normalizedOffset + hotbarWidth/2;
    }

    public void switchToLeft() {
        setIndex((currentIndex - 1 + HotBar.HOTBAR_SIZE) % HotBar.HOTBAR_SIZE);
    }

    public void switchToRight() {
        setIndex((currentIndex + 1) % HotBar.HOTBAR_SIZE);
    }

    public boolean addBlockType(BlockType blockType) {
        boolean added = true;
        int index = -1;


        for(int i = 0; i < HOTBAR_SIZE; i++) {
            if(hotbar[i] != null && hotbar[i].getBlockType() == blockType) {
                index = i;
                break;
            }
        }

        if(index != -1) {
            addBlockIcon(blockType, index);
            return hotbar[index].addBlock();
        } else {
            for(int i = 0; i < HOTBAR_SIZE; i++) {
                if(hotbar[i] == null) {
                    index = i;
                    break;
                }
            }

            if(index != -1) {
                addBlockIcon(blockType, index);
                hotbar[index] = new BlockQuantity(blockType, 1);
            } else {
                added = false;
            }
            
        }

        return added;
    }

    public boolean removeCurrentBlockType() {
        return removeBlockType(currentIndex);
    }

    public boolean removeBlockType(int index) {
        if(index < 0 || index >= HotBar.HOTBAR_SIZE) {
            throw new IllegalArgumentException("Indice non valido.\n");
        }

        BlockQuantity blockQuantity = hotbar[index];

        if(blockQuantity == null) {
            return false;
        }

        // Se è l'ultimo elemento, resetto la cella a null ed elimino l'icona.
        if(blockQuantity.getQuantity() == 1) {
            removeBlockIcon(blockQuantity.getBlockType());
            hotbar[index] = null;
            return true;
        }

        return blockQuantity.removeBlock();

        /* if(blockQuantity != null) {
            if(blockQuantity.getQuantity() == 1) {
                // Elimino l'icona se sto per rimuovere l'ultimo elemento
                removeBlockIcon(blockQuantity.getBlockType());
            }
            
            return blockQuantity.removeBlock();
        } */
    }

    public int getQuantity(BlockType blockType) {
        for(BlockQuantity blockQuantity : hotbar) {
            if(blockQuantity.getBlockType() == blockType) {
                return blockQuantity.getQuantity();
            }
        }

        return 0;
    }

    public int getQuantity(int index) {
        if(index < 0 || index >= HotBar.HOTBAR_SIZE) {
            throw new IllegalArgumentException("Indice non valido.\n");
        }

        BlockQuantity blockQuantity = hotbar[index];

        return blockQuantity != null ? blockQuantity.getQuantity() : 0;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public BlockType getCurrentBlockType() {
        BlockQuantity blockQuantity = hotbar[currentIndex];
        return blockQuantity != null ? blockQuantity.getBlockType() : null;
    }
}
