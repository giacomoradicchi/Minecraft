package toolbox;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import entities.Camera;

public class Maths {
    
    public static Matrix4f createTransformationMatrix(Vector3f translation, float rx, float ry, 
            float rz, float scale) {
        Matrix4f matrix = new Matrix4f();
        matrix.identity();
        matrix.translate(translation);

        matrix.rotate(
            (float) Math.toRadians(rx),
            new Vector3f(1, 0, 0),
            matrix
        );

        matrix.rotate(
            (float) Math.toRadians(ry),
            new Vector3f(0, 1, 0),
            matrix
        );

        matrix.rotate(
            (float) Math.toRadians(rz),
            new Vector3f(0, 0, 1),
            matrix
        );

        matrix.scale(scale);

        return matrix;
    }

    public static Matrix4f createViewMatrix(Camera camera) {
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.identity();
        viewMatrix.rotate((float) Math.toRadians(camera.getPitch()), new Vector3f(1, 0, 0));
        viewMatrix.rotate((float) Math.toRadians(camera.getYaw()), new Vector3f(0, 1, 0));
        viewMatrix.lookAt(
            camera.getPosition(),                                   // Posizione della fotocamera
            new Vector3f(0, 0, -1).add(camera.getPosition()),   // Punto guardato
            new Vector3f(0, 1, 0)                             // Verso in alto
        );

        return viewMatrix;
    }

    public static float distance(Vector2f vec1, Vector2f vec2) {
        return (float) Math.sqrt(Math.pow(vec1.x - vec2.x, 2) + Math.pow(vec1.y - vec2.y, 2));
    }

    public static float distance(float x1, float y1, float x2, float y2) {
        return distance(new Vector2f(x1, y1), new Vector2f(x2, y2));
    }
}
