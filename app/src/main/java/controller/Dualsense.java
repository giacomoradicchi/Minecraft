package controller;

import java.nio.FloatBuffer;

import org.lwjgl.glfw.GLFW;

import java.nio.ByteBuffer;

public class Dualsense {
    
    private FloatBuffer axes;
    private ByteBuffer button;
    private int id;
    private static final float DEAD_ZONE = 0.1f;

    public static final int SQUARE_BUTTON = 0;
    public static final int X_BUTTON = 1;
    public static final int CIRCLE_BUTTON = 2;
    public static final int L1_BUTTON = 4;
    public static final int R1_BUTTON = 5;
    public static final int L2_BUTTON = 6;
    public static final int R2_BUTTON = 7;
    public static final int L3_BUTTON = 10;

    public static final int LEFT_STICK_X = 0;
    public static final int LEFT_STICK_Y = 1;
    public static final int RIGHT_STICK_X = 2;
    public static final int RIGHT_STICK_Y = 5;

    private static int count = 0;

    public Dualsense() {
        /* 
        GLFW.GLFW_JOYSTICK_1 = 0 
        GLFW.GLFW_JOYSTICK_2 = 1
        e così via, dunque basta salvare una variabile id che salva il
        numero del controller. 
        */
        this.id = count;
        update();
        Dualsense.count++;
    }

    public void update() {
        this.axes = GLFW.glfwGetJoystickAxes(this.id);
        this.button = GLFW.glfwGetJoystickButtons(this.id);
        //System.out.println("Controller trovato: " + GLFW.glfwGetJoystickName(id));
    }

    public boolean isButtonPressed(int buttonId) {
        if(button == null) {
            return false;
        }

        if(button.get(buttonId) == 1) {
            return true;
        }

        return false;
    }

    public float getAxesValue(int axesId) {
        if(axes == null) {
            return 0;
        }

        return applyDeadZone(axes.get(axesId));
    }

    private float applyDeadZone(float value) {
        if (Math.abs(value) < DEAD_ZONE) {
            return 0.0f; // ignora i piccoli spostamenti dell'analogico
        }
        // eliminazione effetto della zona morta
        return (value - Math.signum(value) * DEAD_ZONE) / (1.0f - DEAD_ZONE);
    }
}