package entities;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import chunk.ChunkManager;
import controller.Dualsense;
import controller.ControllerManager;
import renderEngine.Window;

public class Player {

    private static final float PLAYER_HEIGHT = 1.8f;
    private static final float PLAYER_WIDTH = 0.6f;  
    private static final float GRAVITY = -1f;
    private static final float JUMP_FORCE = 0.25f;
    private static final float MAX_MOVING_VELOCITY = 10f;
    private static final float ACCELERATION = 0.01f;
    private static final float FRICTION = 0.2f;
    private static final float ROTATION_VELOCITY = 180f;

    private static Window window;
    private static ChunkManager chunkManager;

    private Camera camera;
    private Vector3f position, futurePosition;
    private Vector3f velocity;
    private boolean landed;
    private float pitch, yaw;
    private byte velocityFactor;
    private Box boundingBox;

    private boolean hasMovementInput;
    private boolean freeRoam;

    public Player(Camera camera) {
        this.camera = camera;
        this.position = camera.getPosition();
        this.futurePosition = new Vector3f();
        this.velocity = new Vector3f();
        initPosition();
        setFuturePosition();
        initBoundingBox();
        
        this.landed = true;
        this.freeRoam = true;
        this.velocityFactor = 1;
    }

    public static void setChunkManager(ChunkManager chunkManager) {
        Player.chunkManager = chunkManager;
    }

    public static void setWindow(Window window) {
        Player.window = window;
    }

    private float[] getBoundingBoxValues() {
        float[] values = new float[6];

        values[0] = futurePosition.x - PLAYER_WIDTH/2;
        values[1] = futurePosition.x + PLAYER_WIDTH/2;

        values[2] = futurePosition.y;
        values[3] = futurePosition.y + PLAYER_HEIGHT;

        values[4] = futurePosition.z - PLAYER_WIDTH/2;
        values[5] = futurePosition.z + PLAYER_WIDTH/2;

        return values;
    }

    private void initBoundingBox() {
        this.boundingBox = new Box(getBoundingBoxValues());
    }

    private void updateBoundingBox() {
        this.boundingBox.updateBox(getBoundingBoxValues());
    }

    public void move(float frameTime) {
        handleInputs(frameTime);
        enablePhysics(frameTime);
        updatePosition();
        setCameraPov();
    }

    private void setCameraPov() {
        camera.setPosition(position);
        camera.setPitch(pitch);
        camera.setYaw(yaw);
    }

    private void initPosition() {
        int x = (int) camera.getPosition().x;
        int z = (int) camera.getPosition().z;
        int terrainHeightPos = chunkManager.getHeight(x, z);  // Usiamo x e z direttamente
        this.position = new Vector3f(x, terrainHeightPos + 1.8f, z);
        landed = true;
    }

    // Parte della fisica.

    private void setInAir() {
        landed = false;
    }

    private void enableGravity(float deltaTime) {
        if(!landed) {
            velocity.y += GRAVITY * deltaTime;
        }
    }

    private void enablePhysics(float deltaTime) {
        if(!freeRoam) {
            enableGravity(deltaTime);
        }
        enableFriction();
        setFuturePosition();
        updateBoundingBox();
    }

    private void updatePosition() {
        position.x = futurePosition.x;
        position.y = futurePosition.y;
        position.z = futurePosition.z;
    }

    private void setFuturePosition() {
        futurePosition.x = position.x + velocity.x;
        futurePosition.y = position.y + velocity.y;
        futurePosition.z = position.z + velocity.z;
    }

    private void enableFriction() {
        if(!hasMovementInput) {
            velocity.x *= (1 - FRICTION);
            velocity.z *= (1 - FRICTION);

            if(Math.abs(velocity.x) < 0.001) {
                velocity.x = 0;
            }
            if(Math.abs(velocity.z) < 0.001) {
                velocity.z = 0;
            }
        } 
    }

    // Parte relativa alla gestione degli inputs da tastiera e da controller.

    private void handleInputs(float frameTime) {
        resetInput();
        float maxMovingVelocity = MAX_MOVING_VELOCITY * frameTime ;
        float rotationVelocity = ROTATION_VELOCITY * frameTime;
        velocityFactor = 1;
        // Comandi da tastiera:
        handleInputKeyboard(rotationVelocity);
        // Comandi da controller:
        Dualsense controller = ControllerManager.getController(0);
        if(controller != null) {
            handleInputController(rotationVelocity, controller);
        }

        limitVelocity(maxMovingVelocity * velocityFactor);
    }

    private void limitVelocity(float maxVelocity) {
        float module = (float) (Math.sqrt(Math.pow(this.velocity.x, 2) + Math.pow(this.velocity.z, 2)));
        if(module > maxVelocity) {
            this.velocity.x *= maxVelocity/module;
            this.velocity.z *= maxVelocity/module;
        }
    }

    private void handleInputKeyboard(float rotationVelocity) {
        if(GLFW.glfwGetKey(window.getWindow(), GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
            moveDepth(ACCELERATION, -1);
            updateMovementInput(true);
        }
        if(GLFW.glfwGetKey(window.getWindow(), GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
            moveDepth(ACCELERATION, 1);
            updateMovementInput(true);
        }
        if(GLFW.glfwGetKey(window.getWindow(), GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
            strafe(ACCELERATION, -1);
            updateMovementInput(true);
        }
        if(GLFW.glfwGetKey(window.getWindow(), GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
            strafe(ACCELERATION, 1);
            updateMovementInput(true);
        }
        
        if(GLFW.glfwGetKey(window.getWindow(), GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) {
            if(freeRoam) {
                position.y += MAX_MOVING_VELOCITY/100;
            } else {
                jump();
            }
        } 
        if(freeRoam && GLFW.glfwGetKey(window.getWindow(), GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS) {
            position.y -= MAX_MOVING_VELOCITY/100;
        } 
        if(GLFW.glfwGetKey(window.getWindow(), GLFW.GLFW_KEY_F) == GLFW.GLFW_PRESS) {
            switchMode();
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

    private void handleInputController(float rotationVelocity, Dualsense controller) {
        if(controller.isButtonPressed(Dualsense.L3_BUTTON)) {
            velocityFactor = 2;
        }

        if(!freeRoam && controller.isButtonPressed(Dualsense.X_BUTTON)) {
            jump();
        } else if(freeRoam && controller.isButtonPressed(Dualsense.R2_BUTTON)) {
            position.y += MAX_MOVING_VELOCITY/100;
        }

        if(freeRoam && controller.isButtonPressed(Dualsense.L2_BUTTON)) {
            position.y -= MAX_MOVING_VELOCITY/100;
        }

        // Spostamento laterale:
        float leftStickX = controller.getAxesValue(Dualsense.LEFT_STICK_X);
        float leftStickY = controller.getAxesValue(Dualsense.LEFT_STICK_Y);
        float rightStickX = controller.getAxesValue(Dualsense.RIGHT_STICK_X);
        float rightStickY = controller.getAxesValue(Dualsense.RIGHT_STICK_Y);

        strafe(ACCELERATION, leftStickX);

        // Spostamento in profondità:
        moveDepth(ACCELERATION, leftStickY);

        // Rotazione:
        rotateX(rotationVelocity, rightStickY);
        rotateY(rotationVelocity, rightStickX);

        updateMovementInput(leftStickX != 0);
        updateMovementInput(leftStickY != 0);

        adjustPitch();
    }

    private void jump() {
        if(landed) {
            velocity.y = JUMP_FORCE;
            setInAir();
        }
    }

    private void switchMode() {
        freeRoam = !freeRoam;
    }

    private void strafe(float maxVelocity, float direction) {
        this.velocity.z += maxVelocity*direction*Math.sin(Math.toRadians(yaw));
        this.velocity.x += maxVelocity*direction*Math.cos(Math.toRadians(yaw));
    }

    private void moveDepth(float maxVelocity, float direction) {
        this.velocity.z += maxVelocity*direction*Math.cos(Math.toRadians(yaw));
        this.velocity.x -= maxVelocity*direction*Math.sin(Math.toRadians(yaw));
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

    private void updateMovementInput(boolean isPressed) {
        hasMovementInput = hasMovementInput || isPressed;
    }

    private void resetInput() {
        hasMovementInput = false;
    }
}

