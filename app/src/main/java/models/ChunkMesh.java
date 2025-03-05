package models;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import renderEngine.Loader;
import models.FaceModel.BlockType;

public class ChunkMesh {
    private List<Float> vertices = new ArrayList<>();
    private List<Float> textureCoords = new ArrayList<>();
    private List<Float> normals = new ArrayList<>();
    private List<Integer> indices = new ArrayList<>();
    private List<Integer> aos = new ArrayList<>();
    private int vertexCount = 0;

    private static final int FACE_VERTECES = 2*2;
    
    public void addFace(Vector3f position, BlockType type, byte faceID, int[] faceAOs) {
        // Aggiungo i vertici della faccia
        float[] faceVertices = offsetVertices(FaceModel.getVertices(faceID), position);
        for (float vertex : faceVertices) {
            vertices.add(vertex);
        }
        
        // Aggiungo le coordinate texture
        float[] faceTexCoords = FaceModel.getTextureCoords(type, faceID);
        for (float texCoord : faceTexCoords) {
            textureCoords.add(texCoord);
        }
        
        // Aggiungo le normali
        float[] faceNormals = FaceModel.getNormals(faceID);
        for (float normal : faceNormals) {
            normals.add(normal);
        }
        
        // Aggiungo gli indici
        int[] faceIndices = FaceModel.getIndices(faceID);
        for (int index : faceIndices) {
            indices.add(index + vertexCount);
        }

        // Aggiungo il count di blocchi adiacenti ad ogni vertice per l'ambient occlusion
        for (int ao : faceAOs) {
            aos.add(ao);
        }
        
        vertexCount += FACE_VERTECES; // Ogni faccia ha 4 vertici
    }

    public float[] getVertices() {
        return listToFloatArray(vertices);
    }

    public float[] getNormals() {
        return listToFloatArray(normals);
    }

    public float[] getAOs() {
        return integerListToFloatArray(aos);
    }

    public int[] getIndices() {
        return listToIntArray(indices);
    }
    
    private float[] offsetVertices(float[] vertices, Vector3f position) {
        float[] offsetVertices = new float[vertices.length];
        for (int i = 0; i < vertices.length; i += 3) {
            offsetVertices[i] = vertices[i] + position.x;
            offsetVertices[i + 1] = vertices[i + 1] + position.y;
            offsetVertices[i + 2] = vertices[i + 2] + position.z;
        }
        return offsetVertices;
    }
    
    public RawModel build(Loader loader) {
        float[] verticesArray = listToFloatArray(vertices);
        float[] textureCoordsArray = listToFloatArray(textureCoords);
        float[] normalsArray = listToFloatArray(normals);
        float[] aOArray = integerListToFloatArray(aos);
        int[] indicesArray = listToIntArray(indices);
        
        return loader.loadToVAO(verticesArray, textureCoordsArray, normalsArray, indicesArray, aOArray);
    }
    
    private float[] listToFloatArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    private float[] integerListToFloatArray(List<Integer> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    private int[] listToIntArray(List<Integer> list) {
        int[] array = new int[list.size()];
        for(int i = 0; i < list.size(); i++) {
            array[i] = (int) list.get(i);
        }
        return array;
    }   
    
    public void clear() {
        vertices.clear();
        textureCoords.clear();
        normals.clear();
        indices.clear();
        aos.clear();
        vertexCount = 0;
    }
}
