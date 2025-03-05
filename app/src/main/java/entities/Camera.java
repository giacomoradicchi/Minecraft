package entities;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import block.BlockData;
import chunk.ChunkManager;
import controller.Dualsense;
import models.FaceModel;
import models.FaceModel.BlockType;
import controller.ControllerManager;

import renderEngine.Window;

public class Camera {

    private static Window window;

    private static final float MOVING_VELOCITY = 10f;
    private static final float ROTATION_VELOCITY = 180f;
    private static final int MAX_CAMERA_DISTANCE = 6;

    private static ChunkManager chunkManager;
    
    private Vector3f position = new Vector3f(0, 0, 0);
    private float pitch;
    private float yaw;

    public Camera(Window window) {
        Camera.window = window;
    }

    public static void setChunkManager(ChunkManager chunkManager) {
        Camera.chunkManager = chunkManager;
    }

    public void move(float deltaTime) {
        float velocity = MOVING_VELOCITY * deltaTime ;
        float rotationVelocity = ROTATION_VELOCITY * deltaTime;

        // Comandi da tastiera:
        handleInputKeyboard(velocity, rotationVelocity);

        // Comandi da controller:
        handleInputController(velocity, rotationVelocity);
    }

    private void handleInputKeyboard(float velocity, float rotationVelocity) {
        if(GLFW.glfwGetKey(window.getWindow(), GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
            moveDepth(velocity, -1);
        }
        if(GLFW.glfwGetKey(window.getWindow(), GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
            moveDepth(velocity, 1);
        }
        if(GLFW.glfwGetKey(window.getWindow(), GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
            strafe(velocity, 1);
        }
        if(GLFW.glfwGetKey(window.getWindow(), GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
            strafe(velocity, -1);
        }
        if(GLFW.glfwGetKey(window.getWindow(), GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) {
            position.y += velocity;
        }
        if(GLFW.glfwGetKey(window.getWindow(), GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS) {
            position.y -= velocity;
        }
        if(GLFW.glfwGetKey(window.getWindow(), GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS) {
            rotateX(rotationVelocity, -1);
        }
        if(GLFW.glfwGetKey(window.getWindow(), GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS) {
            rotateX(rotationVelocity, 1);
        }
        if(GLFW.glfwGetKey(window.getWindow(), GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) {
            rotateY(rotationVelocity, 1);
        }
        if(GLFW.glfwGetKey(window.getWindow(), GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS) {
            rotateY(rotationVelocity, -1);
        }
    }

    private void handleInputController(float velocity, float rotationVelocity) {
        Dualsense controller = ControllerManager.getController(0);

        if(controller.isButtonPressed(Dualsense.L3_BUTTON)) {
            velocity *= 2;
        }

        if(controller.isButtonPressed(Dualsense.L2_BUTTON)) {
            position.y -= velocity;
        }

        if(controller.isButtonPressed(Dualsense.R2_BUTTON)) {
            position.y += velocity;
        }

        // Spostamento laterale:
        strafe(velocity, controller.getAxesValue(Dualsense.LEFT_STICK_X));

        // Spostamento in profondità:
        moveDepth(velocity, controller.getAxesValue(Dualsense.LEFT_STICK_Y));

        // Rotazione:
        rotateX(rotationVelocity, controller.getAxesValue(Dualsense.RIGHT_STICK_Y));
        rotateY(rotationVelocity,controller.getAxesValue(Dualsense.RIGHT_STICK_X));

        adjustPitch();
    }

    private void strafe(float velocity, float direction) {
        position.x += velocity*direction*Math.cos(Math.toRadians(yaw));
        position.z += velocity*direction*Math.sin(Math.toRadians(yaw));
    }

    private void moveDepth(float velocity, float direction) {
        position.z += velocity*direction*Math.cos(Math.toRadians(yaw));
        position.x -= velocity*direction*Math.sin(Math.toRadians(yaw));
    }

    // Rotazione lungo l'asse X (abbassa o alza la camera):
    private void rotateX(float rotationVelocity, float direction) {
        pitch += rotationVelocity*direction;
    }

    // Rotazione lungo l'asse Y (si gira a destra o a sinistra):
    private void rotateY(float rotationVelocity, float direction) {
        yaw += rotationVelocity*direction;
    }

    /* La camera non può superare né i 90 né i -90 gradi di angolazione rispetto all'asse X,
    altrimenti potrebbe fare un giro su se stessa o in avanti o all'indietro.
    */
    private void adjustPitch() {
        if(pitch > 90) {
            pitch = 90;
        } else if(pitch < -90) {
            pitch = -90;
        } 
    }

    /* Ottengo la direzione della camera nello spazio per determinare 
     * il blocco a cui punta essa (getCameraDirection restituisce un vettore già
     * normalizzato).
     * È necessario ora determinare l'equazione della retta nello spazio
     * passante per la posizione della telecamera e con direzione pari a quella
     * della camera.
     * 
     * in generale:
     * { x = x0 + vX * t
     * { y = y0 + vY * t
     * { z = z0 + vZ * t
     * 
     * Dove (x0, y0, z0) sarà la posizione della camera e (vX, vY, vZ) la direzione 
     * della camera.
    */

    private Vector3f getCameraDirection() {
        float vX = (float)(Math.cos(Math.toRadians(pitch)) * Math.sin(Math.toRadians(yaw)));
        float vY = - (float) (Math.sin(Math.toRadians(pitch)));
        float vZ = - (float)(Math.cos(Math.toRadians(pitch)) * Math.cos(Math.toRadians(yaw)));

        return new Vector3f(vX, vY, vZ);
    }

    public BlockData getBlockDataInDirection() {
        return getBlockDataInDirection(getCameraDirection(), null);
    }

    private BlockData getBlockDataInDirection(Vector3f cameraDirection, BlockData foundBlock) {
        for(float t = 0; t < MAX_CAMERA_DISTANCE; t += 0.1f) {
            Vector3f intersectPosition = new Vector3f(
                    position.x + cameraDirection.x * t - 0.5f, 
                    position.y + cameraDirection.y * t + 0.5f, 
                    position.z + cameraDirection.z * t - 0.5f
                );
            BlockData block = chunkManager.getBlockData(intersectPosition);
            BlockType type = block != null ? block.getBlockType() : BlockType.AIR;
            if(type != BlockType.AIR) {
                // Prima ricavo le possibili facce libere
                block.setFacesVisibleByCameraList(getPossibleFaces(cameraDirection, block.getAbsolutePosition()));

                // Sulla base delle facce trovate, ricavo quella a cui punta la camera
                byte foundFace = getPointedFace(position, cameraDirection, block);
                block.setPointedFace(foundFace);

                return block;
            }
        }
    
        return null;
    }

    private List<Byte> getPossibleFaces(Vector3f cameraDirection, Vector3f blockPosition) {
        List<Byte> possibleFaceList = new ArrayList<>();

        for(byte face : FaceModel.FACES) {
            Vector3f faceNormal = FaceModel.getNormalsVector(face);

            int x = (int) (blockPosition.x + faceNormal.x);
            int y = (int) (blockPosition.y + faceNormal.y);
            int z = (int) (blockPosition.z + faceNormal.z);

            BlockData block = chunkManager.getBlockData(x - 1, y, z - 1);
 
            /* Una volta preso il blocco nelle vicinanze della faccia,
             * qualora il blocco sia di tipo aria (dunque la faccia è libera)
             * e la normale alla faccia sia rivolta verso la telecamera 
             * (dunque il prodotto scalare è < 0), allora la faccia è una candidata.
             */
            if(block != null && block.getBlockType() == BlockType.AIR 
            && new Vector3f(cameraDirection).dot(faceNormal) < -0.1f) { 
                possibleFaceList.add(face);

                /* Le facce il cui prodotto scalare è negativo 
                 * (visibili dalla camera) sono al massimo 3. */
                if(possibleFaceList.size() == 3) {
                    break;
                }
            }
        }

        return possibleFaceList;
    }

    private byte getPointedFace(Vector3f rayOrigin, Vector3f rayDirection, BlockData block) {
        Vector3f blockPos = new Vector3f(block.getAbsolutePosition()).sub(1, 0, 1);
        
       
        float closestT = Float.MAX_VALUE;
        byte pointedFace = -1;
    
        for (Byte face : block.getFacesVisibleByCameraList()) {
            Vector3f normal = FaceModel.getNormalsVector(face);
            float d = -normal.dot(new Vector3f(blockPos));  // Equazione del piano
    
            // Calcolo dell'intersezione tra il raggio e il piano della faccia
            float t = -(normal.dot(rayOrigin) + d) / normal.dot(rayDirection);
            if (t > 0 && t < closestT) {  // Deve essere il più vicino
                closestT = t;
                pointedFace = face;
            }
        }
    
        return pointedFace;
    }

    public byte getBlockFace(Vector3f blockPosition) {
        Vector3f cameraDirection = getCameraDirection();

        byte closestFace = -1;
        float minDot = Float.MAX_VALUE;

        for(byte face: FaceModel.FACES) {

            int x = (int) Math.floor(blockPosition.x) - 1;
            int y = (int) Math.floor(blockPosition.y);
            int z = (int) Math.floor(blockPosition.z) - 1;

            Vector3f normal = FaceModel.getNormalsVector(face);
            x += normal.x;
            y += normal.y;
            z += normal.z;

            BlockData block = chunkManager.getBlockData(new Vector3f(x, y, z));
            if(block != null && block.getBlockType() == BlockType.AIR) {
                Vector3f faceNormal = new Vector3f(FaceModel.getNormals(face));
                float dot = cameraDirection.dot(faceNormal);

                if(dot < minDot) {
                    closestFace = face;
                    minDot = dot;
                }
            } 
        }

        return closestFace;
    }

    /* public byte getBlockFace(Vector3f blockPosition) {
        Vector3f cameraDirection = getCameraDirection();
        Vector3f cameraPosition = new Vector3f(position.x, position.y, position.z);
        Vector3f centerPosition = new Vector3f(
            (int) Math.floor(blockPosition.x) + 0.5f,
            (int) Math.floor(blockPosition.y) + 0.5f,
            (int) Math.floor(blockPosition.z) + 0.5f
        );

        byte closestFace = FaceModel.BACK_FACE;
        float minT = Float.MAX_VALUE;

        for (byte face : FaceModel.FACES) {
            Vector3f faceNormal = new Vector3f(FaceModel.getNormals(face));
            float dot = cameraDirection.dot(faceNormal);
            if(dot < 0) {
                
                Vector3f faceCentre = new Vector3f(
                    centerPosition.x + 0.5f * faceNormal.x,
                    centerPosition.y + 0.5f * faceNormal.y,
                    centerPosition.z + 0.5f * faceNormal.z
                );

                Vector3f diff = new Vector3f(faceCentre).sub(cameraPosition);
                float t = diff.dot(faceNormal) / dot;
                //float t = faceCentre.add(cameraPosition.mul(-1)).dot(faceNormal) / cameraDirection.dot(faceNormal);

                Vector3f intersectPosition = new Vector3f(
                    cameraPosition.x + cameraDirection.x * t, 
                    cameraPosition.y + cameraDirection.y * t, 
                    cameraPosition.z + cameraDirection.z * t
                );
                System.out.println("Intersezione: " + (double) intersectPosition.x);
                System.out.println("faceCentre: " + (double) faceCentre.x); 

                if(t > 0 && isInsideFace(intersectPosition, faceCentre, faceNormal) && t < minT) {
                    closestFace = face;
                    minT = t;
                } 
            }
        }

        
        return closestFace;
    } */

    /* private boolean isInsideFace(Vector3f intersectPosition, Vector3f faceCentre, Vector3f faceNormal) {
        if (faceNormal.x != 0) { // Lati LEFT/RIGHT
            return intersectPosition.y >= faceCentre.y - 0.5f && intersectPosition.y <= faceCentre.y + 0.5f &&
                   intersectPosition.z >= faceCentre.z - 0.5f && intersectPosition.z <= faceCentre.z + 0.5f;
        } 
        if (faceNormal.y != 0) { // Lati TOP/BOTTOM
            return intersectPosition.x >= faceCentre.x - 0.5f && intersectPosition.x <= faceCentre.x + 0.5f &&
                   intersectPosition.z >= faceCentre.z - 0.5f && intersectPosition.z <= faceCentre.z + 0.5f;
        } 
        if (faceNormal.z != 0) { // Lati FRONT/BACK
            return intersectPosition.x >= faceCentre.x - 0.5f && intersectPosition.x <= faceCentre.x + 0.5f &&
                   intersectPosition.y >= faceCentre.y - 0.5f && intersectPosition.y <= faceCentre.y + 0.5f;
        }
        return false;
    } */

    public Vector3f getPosition() {
        return this.position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void increasePosition(float dx, float dy, float dz) {
        this.position.x += dx;
        this.position.y += dy;
        this.position.z += dz;
    }

    public void increaseRotation(float dx, float dy) {
        this.pitch += dx;
        this.yaw += dy;
    }
}
