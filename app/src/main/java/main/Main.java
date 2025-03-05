package main;

import java.util.HashMap;
import java.util.Map;


import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import block.BlockData;
import chunk.ChunkManager;
import controller.ControllerManager;
import controller.Dualsense;
import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import models.CrossHair;
import models.FaceModel;
import models.Hearts;
import models.HotBar;
import models.Hunger;
import models.FaceModel.BlockType;
import models.StaticImageModel;
import models.WireBlockModel;
import models.XPBar;
import renderEngine.FixedTimeStamp;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.Window;

public class Main {
	private static int WIDTH = 1920;
	private static int HEIGHT = 1080;
	public static void main(String[] args) {
		Window window = new Window(WIDTH, HEIGHT);
		window.init();

		Loader loader = new Loader();
		Map<Byte, Boolean> buttonState = new HashMap<>();
		final Byte REMOVE_BUTTONS = 0;
		final Byte ADD_BUTTONS = 1;
		final Byte SWITCH_LEFT_HOTBAR_BUTTONS = 2;
		final Byte SWITCH_RIGHT_HOTBAR_BUTTONS = 3;

		buttonState.put(REMOVE_BUTTONS, false);
		buttonState.put(ADD_BUTTONS, false);
		buttonState.put(SWITCH_LEFT_HOTBAR_BUTTONS, false);
		buttonState.put(SWITCH_RIGHT_HOTBAR_BUTTONS, false);


		
		/* float[] vertices = {
			-0.5f, 0.5f, 0f,
			-0.5f, -0.5f, 0f,
			0.5f, -0.5f, 0f,
			0.5f, 0.5f, 0f,
		}; 

		int[] indices = {
			0, 1, 3,
			3, 1, 2
		};

		float[] textureCoords = {
			0, 0,
			0, 1,
			1, 1,
			1, 0
		}; */
		
		/* CubeModel cubeModel = new CubeModel(loader, CubeModel.BlockType.GRASS);
		CubeModel cubeModel2 = new CubeModel(loader, CubeModel.BlockType.DIRT);
		CubeModel cubeModel3 = new CubeModel(loader, CubeModel.BlockType.STONE);

		int number = 8;
		Entity[] entities = new Entity[number*number*number*number];
		int i = 0; 
		for(int x = 0; x < number; x++) {
			for(int y = 0; y < number*number; y++) {
				for(int z = 0; z < number; z++) {
					if(y == 0) {
						entities[i] = new Entity(cubeModel, new Vector3f(x, -1 - y, -3 - z), 0, 0, 0, 1);
					} else if (y > 0 && y < 4){
						entities[i] = new Entity(cubeModel2, new Vector3f(x, -1 - y, -3 - z), 0, 0, 0, 1);
					} else {
						entities[i] = new Entity(cubeModel3, new Vector3f(x, -1 - y, -3 - z), 0, 0, 0, 1);
					}
					
					i++;
				}
			}
		} */

		/* FaceModel.loadAllFaceModels(loader);
		Entity[] faces = new Entity[6];
		for(int i = 0; i < 6; i++) {
			faces[i] = new Entity(FaceModel.getFaceModels(FaceModel.BlockType.GRASS)[i], new Vector3f(0, 5, -3), 0, 0, 0, 1);
		}  */



		Light light = new Light(new Vector3f(2000, 3000, 2000), new Vector3f(1f,1f,1f));

		Camera camera = new Camera(window);
		//camera.setPosition(new Vector3f(90, 40, -67));
		//camera.setPosition(new Vector3f(75, 40, -70));
		camera.setPosition(new Vector3f(0, 30, 3));
		MasterRenderer renderer = new MasterRenderer(window);
		ChunkManager chunkManager = new ChunkManager(camera.getPosition(), loader, window);
		Camera.setChunkManager(chunkManager);

		Player.setChunkManager(chunkManager);
		Player.setWindow(window);
		Player player = new Player(camera);
		
		FixedTimeStamp timeStamp = new FixedTimeStamp(120);
		ControllerManager.addController();

		WireBlockModel wireBlock = new WireBlockModel(loader);

		StaticImageModel.setWindowRatio((float) WIDTH / HEIGHT);

		// Elementi statici
		HotBar hotbar = new HotBar(loader, renderer);
		new CrossHair(loader, renderer);
		new Hearts(loader, renderer, hotbar.getNormalizedXPosition());
		new Hunger(loader, renderer, hotbar.getNormalizedXPositionRight());
		new XPBar(loader, renderer);

		FaceModel.loadAllFaceModels(loader);
		Entity[] cube = new Entity[FaceModel.FACES.length];
		for(Byte face : FaceModel.FACES) {
			cube[face] = new Entity(FaceModel.getFaceModel(BlockType.GRASS, FaceModel.FACES[face]), new Vector3f(0, 33, -2), 0, 0, 0, 1);
		}
		


		
		while (!window.shouldClose()) {
			chunkManager.updateChunks(camera.getPosition());
			ControllerManager.updateControllers();
			Dualsense controller = ControllerManager.getController(0);
			timeStamp.accumulateTime(window.getDeltaTime());

			while(timeStamp.shouldUpdate()) {
				player.move(timeStamp.getFixedTimeStep());
			}

			for(int i = 0; i < HotBar.HOTBAR_SIZE; i++) {
				if(GLFW.glfwGetKey(window.getWindow(), GLFW.GLFW_KEY_1 + i) == GLFW.GLFW_PRESS) {
					hotbar.setIndex(i);
				}
			}

			if(!buttonState.get(SWITCH_LEFT_HOTBAR_BUTTONS) 
			&& controller.isButtonPressed(Dualsense.L1_BUTTON)) {
				hotbar.switchToLeft();
				buttonState.put(SWITCH_LEFT_HOTBAR_BUTTONS, true);
			} else if(!controller.isButtonPressed(Dualsense.L1_BUTTON)) {
				buttonState.put(SWITCH_LEFT_HOTBAR_BUTTONS, false);
			}

			if(!buttonState.get(SWITCH_RIGHT_HOTBAR_BUTTONS) 
			&& controller.isButtonPressed(Dualsense.R1_BUTTON)) {
				hotbar.switchToRight();
				buttonState.put(SWITCH_RIGHT_HOTBAR_BUTTONS, true);
			} else if(!controller.isButtonPressed(Dualsense.R1_BUTTON)) {
				buttonState.put(SWITCH_RIGHT_HOTBAR_BUTTONS, false);
			}
			

			BlockData block = camera.getBlockDataInDirection();
		 
			if(block != null) {
				byte blockFace = camera.getBlockFace(block.getAbsolutePosition());
				

				if(!buttonState.get(REMOVE_BUTTONS).booleanValue() 
				&& (controller.isButtonPressed(Dualsense.SQUARE_BUTTON)
				|| GLFW.glfwGetKey(window.getWindow(), GLFW.GLFW_KEY_E) == GLFW.GLFW_PRESS)) {
					chunkManager.removeBlock(block);
					buttonState.put(REMOVE_BUTTONS, true);
					hotbar.addBlockType(block.getBlockType());
				} else if(!controller.isButtonPressed(Dualsense.SQUARE_BUTTON)
				&& !(GLFW.glfwGetKey(window.getWindow(), GLFW.GLFW_KEY_E) == GLFW.GLFW_PRESS)) {
					buttonState.put(REMOVE_BUTTONS, false);
				}

				if(!buttonState.get(ADD_BUTTONS).booleanValue()
				&& (GLFW.glfwGetKey(window.getWindow(), GLFW.GLFW_KEY_R) == GLFW.GLFW_PRESS
				|| controller.isButtonPressed(Dualsense.CIRCLE_BUTTON))) {
					buttonState.put(ADD_BUTTONS, true);
					int x = (int) Math.floor(block.getAbsolutePosition().x) - 1;
					int y = (int) Math.floor(block.getAbsolutePosition().y);
					int z = (int) Math.floor(block.getAbsolutePosition().z) - 1;
					boolean foundFace = true;

					switch (blockFace) {
						case FaceModel.BACK_FACE:
							z--;
							break;
						case FaceModel.FRONT_FACE:
							z++;
							break;
						case FaceModel.RIGHT_FACE:
							x++;
							break;
						case FaceModel.LEFT_FACE:
							x--;
							break;
						case FaceModel.UPPER_FACE:
							y++;
							break;
						case FaceModel.BOTTOM_FACE:
							y--;
							break;
					
						default:
							foundFace = false;
							break;
					}
					
					BlockType currentBlockType = hotbar.getCurrentBlockType();
					boolean canAdd = hotbar.removeCurrentBlockType();
					if (canAdd && foundFace) {
						chunkManager.addBlock(x, y, z, currentBlockType);
					}
				} else if(!(GLFW.glfwGetKey(window.getWindow(), GLFW.GLFW_KEY_R) == GLFW.GLFW_PRESS)
				&& !controller.isButtonPressed(Dualsense.CIRCLE_BUTTON)) {
					buttonState.put(ADD_BUTTONS, false);
				}
			}  
			
			chunkManager.processChunkEntities(renderer);
			wireBlock.processWireFaceEntities(renderer, block);
			

			renderer.render(light, camera);
			window.updateWindow();
		}

		chunkManager.cleanup();
		renderer.cleanUp();
		loader.cleanUp();
		window.close();
	}

}
