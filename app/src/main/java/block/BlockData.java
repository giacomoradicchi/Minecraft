package block;

import java.util.List;

import org.joml.Vector2i;
import org.joml.Vector3f;

import models.FaceModel.BlockType;

public class BlockData {

    /* public class FaceData {
        public static final byte BACK_FACE = 0;
        public static final byte FRONT_FACE = 1;
        public static final byte RIGHT_FACE = 2;
        public static final byte LEFT_FACE = 3;
        public static final byte UPPER_FACE = 4;
        public static final byte BOTTOM_FACE = 5;


    } */

    private BlockType type;
    private Vector3f absolutePosition;
    private Vector2i localXZPosition;
    private byte pointedFace;
    private List<Byte> facesVisibleByCamera;

    public BlockData(Vector3f absolutePosition, Vector2i localXZPosition, BlockType blockType) {
        this.absolutePosition = absolutePosition;
        this.localXZPosition = localXZPosition;
        this.type = blockType;
    }

    public BlockType getBlockType() {
        return this.type;
    }

    public Vector3f getAbsolutePosition() {
        return this.absolutePosition;
    }

    public Vector2i getLocalXZPosition() {
        return this.localXZPosition;
    }

    public void setPointedFace(byte face) {
        if(pointedFace != face) {
            pointedFace = face;
        }
    }

    public byte getPointedFace() {
        return pointedFace;
    }

    public void setFacesVisibleByCameraList(List<Byte> facesList) {
        facesVisibleByCamera = facesList;
    }

    public List<Byte> getFacesVisibleByCameraList() {
        return facesVisibleByCamera;
    }
}
