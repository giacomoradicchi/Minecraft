package entities;

import org.joml.Vector3f;

public class Box {

    private float minX;
    private float maxX;
    private float minY;
    private float maxY;
    private float minZ;
    private float maxZ;

    public static final int X_AXIS = 0;
    public static final int Y_AXIS = 1;
    public static final int Z_AXIS = 2;

    public Box(float minX, float maxX, 
        float minY, float maxY, 
        float minZ, float maxZ) {
        updateBox(minX, maxX, minY, maxY, minZ, maxZ);
    }

    public Box(Vector3f position, Vector3f size) {
        minX = position.x;
        maxX = position.x + size.x;

        minY = position.y;
        maxY = position.y + size.y;

        minZ = position.z;
        maxZ = position.z + size.z;
    }

    public Box(float[] values) {
        if(values.length != 6) {
            throw new IllegalArgumentException("Dimensione array non valida.");
        }

        minX = values[0];
        maxX = values[1];

        minY = values[2];
        maxY = values[3];

        minZ = values[4];
        maxZ = values[5];
    }

    public void updateBox(float minX, float maxX, 
        float minY, float maxY, 
        float minZ, float maxZ) {
            
        this.minX = minX;
        this.maxX = maxX;

        this.minY = minY;
        this.maxY = maxY;

        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    public void updateBox(float[] newValues) {
        if(newValues.length != 6) {
            throw new IllegalArgumentException("Dimensione array non valida.");
        }

        minX = newValues[0];
        maxX = newValues[1];

        minY = newValues[2];
        maxY = newValues[3];

        minZ = newValues[4];
        maxZ = newValues[5];
    }

    public boolean intersects(Box other) {
        boolean noOverlapX = this.maxX <= other.minX || this.minX >= other.maxX;
        boolean noOverlapY = this.maxY <= other.minY || this.minY >= other.maxY;
        boolean noOverlapZ = this.maxZ <= other.minZ || this.minZ >= other.maxZ;

        return !(noOverlapX || noOverlapY || noOverlapZ);
    }

    public float calculateOverlap(Box other, int axis) {
        float overlap;
        switch (axis) {
            case X_AXIS:
                if(maxX > other.maxX) {
                    overlap = other.maxX - minX;
                } else {
                    overlap = maxX - other.minX;
                }
                break;
            
            case Y_AXIS:
                if(maxY > other.maxY) {
                    overlap = other.maxY - minY;
                } else {
                    overlap = maxY - other.minY;
                }
            
            case Z_AXIS:
                if(maxZ > other.maxZ) {
                    overlap = other.maxZ - minZ;
                } else {
                    overlap = maxZ - other.minZ;
                }
            
            default:
                overlap = 0;
                break;
        }

        return overlap;
    }

    public float getMinX() {
        return this.minX;
    }

    public float getMaxX() {
        return this.maxX;
    }

    public float getMinY() {
        return this.minY;
    }

    public float getMaxY() {
        return this.maxY;
    }

    public float getMinZ() {
        return this.minZ;
    }

    public float getMaxZ() {
        return this.maxZ;
    }
}
