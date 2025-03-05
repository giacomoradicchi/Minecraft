package renderEngine;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;


import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import models.RawModel;
import textures.Texture;


public class Loader {
	
	private List<Integer> vaos = new ArrayList<Integer>();
	private List<Integer> vbos = new ArrayList<Integer>();
	private List<Integer> textures = new ArrayList<Integer>();

	private RawModel currentModel;
	
	public RawModel loadToVAO(float[] positions, float[] textureCoords, float[] normals, 
	int[] indices, float[] aOCount){
		int vaoID = createVAO();
		currentModel = new RawModel(vaoID, indices.length);
		bindIndicesBuffer(indices);
		storeDataInAttributeList(0, 3, positions);
		storeDataInAttributeList(1, 2, textureCoords);
		storeDataInAttributeList(2, 3, normals);
		storeDataInAttributeList(3, 1, aOCount);
		unbindVAO();
		
		RawModel model = currentModel;
		currentModel = null;
		return model;
	}

	public RawModel loadToVAO(float[] positions, float[] textureCoords, float[] normals, int[] indices){
		int vaoID = createVAO();
		currentModel = new RawModel(vaoID, indices.length);
		bindIndicesBuffer(indices);
		storeDataInAttributeList(0, 3, positions);
		storeDataInAttributeList(1, 2, textureCoords);
		storeDataInAttributeList(2, 3, normals);
		storeDataInAttributeList(3, 1, new float[1 * indices.length]);
		unbindVAO();

		RawModel model = currentModel;
		currentModel = null;
		return model;
	}

	public RawModel loadToVAO(float[] positions, int[] indices) {
		int vaoID = createVAO();
		currentModel = new RawModel(vaoID, indices.length);
		bindIndicesBuffer(indices);
		storeDataInAttributeList(0, 3, positions);
		storeDataInAttributeList(1, 2, new float[2 * indices.length]);
		storeDataInAttributeList(2, 3, new float[3 * indices.length]);
		storeDataInAttributeList(3, 1, new float[1 * indices.length]);

		unbindVAO();

		RawModel model = currentModel;
		currentModel = null;
		return model;
	}

	public RawModel loadToVAO(float[] positions, float[] textureCoords, int[] indices) {
		int vaoID = createVAO();
		currentModel = new RawModel(vaoID, indices.length);
		bindIndicesBuffer(indices);
		storeDataInAttributeList(0, 3, positions);
		storeDataInAttributeList(1, 2, textureCoords);
		storeDataInAttributeList(2, 3, new float[3 * indices.length]);
		storeDataInAttributeList(3, 1, new float[1 * indices.length]);

		unbindVAO();

		RawModel model = currentModel;
		currentModel = null;
		return model;
	}

	public int loadTexture(String fileName) {
		Texture texture = new Texture(fileName);
		int textureID = texture.getTextureID();
		textures.add(textureID);
		return textureID;
	}

	public int loadTexture(Texture texture) {
		int textureID = texture.getTextureID();
		textures.add(textureID);
		return textureID;
	}

	public int loadTexture(String[] mipmapFileName) {
		Texture texture = new Texture(mipmapFileName);
		int textureID = texture.getTextureID();
		textures.add(textureID);
		return textureID;
	}

	public void cleanUpModel(RawModel model) {
        int vaoID = model.getVaoID();
        GL30.glDeleteVertexArrays(vaoID);
        vaos.remove(Integer.valueOf(vaoID));
		
        for(Integer vbo : model.getVbos()) {
            GL15.glDeleteBuffers(vbo);
            vbos.remove(vbo);
        }
    }

	
	public void cleanUp(){
		for(int vao:vaos){
			GL30.glDeleteVertexArrays(vao);
		}
		for(int vbo:vbos){
			GL15.glDeleteBuffers(vbo);
		}
		for(int texture:textures){
			GL11.glDeleteTextures(texture);
		}
	}
	
	private int createVAO(){
		int vaoID = GL30.glGenVertexArrays();
		vaos.add(vaoID);
		GL30.glBindVertexArray(vaoID);
		return vaoID;
	}
	
	private void storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data){
		int vboID = GL15.glGenBuffers();
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		FloatBuffer buffer = storeDataInFloatBuffer(data);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT ,false,0,0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		currentModel.addVbo(vboID);
	}
	
	private void unbindVAO(){
		GL30.glBindVertexArray(0);
	}
	
	private void bindIndicesBuffer(int[] indices){
		int vboID = GL15.glGenBuffers();
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
		IntBuffer buffer = storeDataInIntBuffer(indices);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
	}
	
	private IntBuffer storeDataInIntBuffer(int[] data){
		IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}
	
	private FloatBuffer storeDataInFloatBuffer(float[] data){
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}


}
