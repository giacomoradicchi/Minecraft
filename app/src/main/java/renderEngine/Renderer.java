package renderEngine;

import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;

import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import entities.Entity;
import models.RawModel;
import models.WireBlockModel;
import models.Model;
import shaders.StaticShader;
import toolbox.Maths;

public class Renderer {

	private static final float FOV = 70;
	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 1000;

	private static final float SKY_COLOR_RED = 0.4f;
	private static final float SKY_COLOR_GREEN = 0.7f;
	private static final float SKY_COLOR_BLUE = 1f; 

	private Matrix4f projectionMatrix;
	private StaticShader shader;

	public Renderer(StaticShader shader, Window window) {
		this.shader = shader;
		GL11.glEnable(GL_CULL_FACE);
		GL11.glCullFace(GL_BACK); 
		createProjectionMatrix(window);
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
	}

	public void prepare() {
		GL11.glEnable(GL_DEPTH_TEST);
        GL11.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(SKY_COLOR_RED, SKY_COLOR_GREEN, SKY_COLOR_BLUE, 1);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		//GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
	}

	public void render(Map<Model, List<Entity>> entities) {
		boolean trovato = false;
		shader.loadSkyColor(SKY_COLOR_RED, SKY_COLOR_GREEN, SKY_COLOR_BLUE);
		for(Model model : entities.keySet()) {
			prepareModel(model);
			List<Entity> batch = entities.get(model);

			boolean isWireframe = model.isWireframe();
			shader.loadIsWireframe(isWireframe);
			
			// Se è wireframe, disegno solo le linee.
			int drawMode = isWireframe ? GL11.GL_LINES : GL11.GL_TRIANGLES;
			
			if(!isWireframe) {
				prepareTexture(model);
				GL11.glEnable(GL_DEPTH_TEST);
				if(trovato) {
					System.out.println("Ce n'è un altro dopo.");
				}
			} else {
				GL11.glDisable(GL_DEPTH_TEST);
				shader.loadWireframeColor(1, 0, 0);
				trovato = true;
			}

			
			
			for(Entity entity : batch) {
				prepareInstance(entity);
				GL11.glDrawElements(drawMode, model.getRawModel().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
			}
			unbindTexturedModel(isWireframe);
		}
	}

	private void drawModel(Model model, List<Entity> batch) {
		prepareModel(model);
		boolean isWireframe = model.isWireframe();
		shader.loadIsWireframe(isWireframe);
		shader.loadIsStatic(false);
		
		// Se è wireframe, disegno solo le linee.
		int drawMode = isWireframe ? GL11.GL_LINES : GL11.GL_TRIANGLES;
		
		if(!isWireframe) {
			prepareTexture(model);
		} else {
			shader.loadWireframeColor(WireBlockModel.RED, WireBlockModel.GREEN, WireBlockModel.BLUE);
		}

		for(Entity entity : batch) {
			prepareInstance(entity);
			GL11.glDrawElements(drawMode, model.getRawModel().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
		}
		unbindTexturedModel(isWireframe);
	}

	private void drawStaticModel(Model staticModel, List<Entity> batch) {
		prepareModel(staticModel);
		boolean isWireframe = staticModel.isWireframe();
		shader.loadIsWireframe(isWireframe);
		shader.loadIsStatic(true);

		// Se è wireframe, disegno solo le linee.
		int drawMode = isWireframe ? GL11.GL_LINES : GL11.GL_TRIANGLES;

		if(!isWireframe) {
			prepareTexture(staticModel);
		} else {
			shader.loadWireframeColor(WireBlockModel.RED, WireBlockModel.GREEN, WireBlockModel.BLUE);
		}

		for(Entity entity : batch) {
			prepareInstance(entity);
			GL11.glDrawElements(drawMode, staticModel.getRawModel().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
		}

		unbindTexturedModel(isWireframe);
	}

	public void render(Map<Model, List<Entity>> entities, 
	Map<Model, List<Entity>> overlayEntities, 
	Map<Model, List<Entity>> staticEntities) {
		shader.loadSkyColor(SKY_COLOR_RED, SKY_COLOR_GREEN, SKY_COLOR_BLUE);

		// Disegno prima le entities normali
		for(Model model : entities.keySet()) {
			drawModel(model, entities.get(model));
		}

		// Disegno le entities in primo piano (attualmente solo il wireframe del blocco evidenziato)
		// Tolgo il depth test per disegnare in primo piano overlayEntity
		GL11.glDisable(GL_DEPTH_TEST);
		for(Model model : overlayEntities.keySet()) {
			drawModel(model, overlayEntities.get(model));
		}
		
		GL11.glEnable(GL_DEPTH_TEST);
		for(Model staticModel : staticEntities.keySet()) {
			drawStaticModel(staticModel, staticEntities.get(staticModel));
		}
		
	}

	private void prepareModel(Model model) {
		RawModel rawModel = model.getRawModel();
		GL30.glBindVertexArray(rawModel.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL20.glEnableVertexAttribArray(3);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
	}

	private void prepareTexture(Model model) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getID());
	}

	private void unbindTexturedModel(boolean isWireframe) {
		GL20.glDisableVertexAttribArray(0);
		if(!isWireframe) {
			GL20.glDisableVertexAttribArray(1);
			GL20.glDisableVertexAttribArray(2);
			GL20.glDisableVertexAttribArray(3);
		}
		
		GL30.glBindVertexArray(0);
	}

	private void prepareInstance(Entity entity) {
		Matrix4f transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(),
				entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
		shader.loadTransformationMatrix(transformationMatrix);
	}

	private void createProjectionMatrix(Window window){
		projectionMatrix = new Matrix4f().perspective(
			(float) Math.toRadians(FOV),  // Campo visivo
			(float) (float) window.getWidth() / (float) window.getHeight(), // Aspect ratio
			NEAR_PLANE,                          // Near plane
			FAR_PLANE                         // Far plane
		);
	}

	public Matrix4f getProjectionMatrix() {
		return this.projectionMatrix;
	}

}
