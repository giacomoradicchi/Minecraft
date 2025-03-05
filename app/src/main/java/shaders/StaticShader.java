package shaders;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import entities.Camera;
import entities.Light;
import toolbox.Maths;

public class StaticShader extends ShaderProgram{

    private static final String VERTEX_FILE = "src/main/java/shaders/vertexShader.txt";
    private static final String FRAGMENT_FILE = "src/main/java/shaders/fragmentShader.txt";

    private int location_transformationMatrix;
    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int location_lightPosition;
    private int location_lightColor;
    private int location_skyColor;
    private int location_isWireframe;
    private int location_wireframeColor; 
    private int location_isStatic;

    public StaticShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void bindAttributes() {
       super.bindAttribute(0, "position");
       super.bindAttribute(1, "textureCoords");
       super.bindAttribute(2, "normal");
       super.bindAttribute(3, "aOCount");
    }

    @Override
    protected void getAllUniformLocations() {
        location_transformationMatrix = super.getUniformLocation("transformationMatrix");
        location_projectionMatrix = super.getUniformLocation("projectionMatrix");
        location_viewMatrix = super.getUniformLocation("viewMatrix");
        location_lightPosition = super.getUniformLocation("lightPosition");
        location_lightColor = super.getUniformLocation("lightColor");
        location_skyColor = super.getUniformLocation("skyColor");
        location_isWireframe = super.getUniformLocation("isWireframe");
        location_wireframeColor = super.getUniformLocation("wireframeColor");
        location_isStatic = super.getUniformLocation("isStatic");
    }

    public void loadSkyColor(float r, float g, float b) {
        super.loadVector(location_skyColor, new Vector3f(r, g, b));
    }

    public void loadTransformationMatrix(Matrix4f matrix) {
        super.loadMatrix(location_transformationMatrix, matrix);
    }

    public void loadLight(Light light) {
        super.loadVector(location_lightPosition, light.getPosition());
        super.loadVector(location_lightColor, light.getColor());
    }

    public void loadViewMatrix(Camera camera) {
        Matrix4f viewMatrix = Maths.createViewMatrix(camera);
        super.loadMatrix(location_viewMatrix, viewMatrix);
    }

    public void loadProjectionMatrix(Matrix4f projection) {
        super.loadMatrix(location_projectionMatrix, projection);
    }

    public void loadIsWireframe(boolean isWireframe) {
        super.loadBoolean(location_isWireframe, isWireframe);
    }

    public void loadWireframeColor(float r, float g, float b) {
        super.loadVector(location_wireframeColor, new Vector3f(r, g, b));
    }

    public void loadIsStatic(boolean isStatic) {
        super.loadBoolean(location_isStatic, isStatic);
    }
    
}
