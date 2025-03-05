package textures;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;


import static org.lwjgl.opengl.GL11.*;

public class Texture {

    private int width, height;
    private int texture;

    public Texture(String path) {
        texture = load(path);
    }

    public Texture(String[] path) {
        if(path.length == 1) {
            texture = load(path[0]);
        } else {
            texture = loadTextureWithManualMipmaps(path);
        }
        
    }

    private int load(String path) {
        int[] pixels = null;
        int result = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, result);

        try {
            BufferedImage image = ImageIO.read(new FileInputStream(path));
            width = image.getWidth();
            height = image.getHeight();
            pixels = new int[width * height];
            image.getRGB(0, 0, width, height, pixels, 0, width);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int[] data = new int[width * height];
        for (int i = 0; i < width * height; i++) {
            int a = (pixels[i] & 0xff000000) >> 24;
            int r = (pixels[i] & 0xff0000) >> 16;
            int g = (pixels[i] & 0xff00) >> 8;
            int b = (pixels[i] & 0xff);

            data[i] = a << 24 | b << 16 | g << 8 | r;
        }

        IntBuffer buffer = ByteBuffer.allocateDirect(data.length << 2)
            .order(ByteOrder.nativeOrder()).asIntBuffer();
        buffer.put(data).flip();

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA,
            GL_UNSIGNED_BYTE, buffer);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0);

        //GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D); 

        glBindTexture(GL_TEXTURE_2D, 0);

        

        return result;
    }

    
    /* private int loadTextureWithManualMipmaps(String[] mipmapPaths) {
        int[] pixels = null;
        int result = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, result);

        for(int level = 0; level < mipmapPaths.length; level++) {
            try {
                BufferedImage image = ImageIO.read(new FileInputStream(mipmapPaths[level]));
                width = image.getWidth();
                height = image.getHeight();
                pixels = new int[width * height];
                image.getRGB(0, 0, width, height, pixels, 0, width);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int[] data = new int[width * height];
            for (int i = 0; i < width * height; i++) {
                int a = (pixels[i] & 0xff000000) >> 24;
                int r = (pixels[i] & 0xff0000) >> 16;
                int g = (pixels[i] & 0xff00) >> 8;
                int b = (pixels[i] & 0xff);

                data[i] = a << 24 | b << 16 | g << 8 | r;
            }

            IntBuffer buffer = ByteBuffer.allocateDirect(data.length << 2)
                .order(ByteOrder.nativeOrder()).asIntBuffer();
            buffer.put(data).flip();

            glTexImage2D(GL_TEXTURE_2D, level, GL_RGBA, width, height, 0, GL_RGBA,
            GL_UNSIGNED_BYTE, buffer);
        }

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0);

        //GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

        glBindTexture(GL_TEXTURE_2D, 0);

        return result;
    } */
    private int loadTextureWithManualMipmaps(String[] mipmapPaths) {
        int[] pixels = null;
        int result = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, result);
        int level = 0;
    
        for (; level < mipmapPaths.length; level++) {
            try {
                BufferedImage image = ImageIO.read(new FileInputStream(mipmapPaths[level]));
                int width = image.getWidth();
                int height = image.getHeight();
                pixels = new int[width * height];
                image.getRGB(0, 0, width, height, pixels, 0, width);
    
                int[] data = new int[width * height];
                for (int i = 0; i < width * height; i++) {
                    int a = (pixels[i] & 0xff000000) >> 24;
                    int r = (pixels[i] & 0xff0000) >> 16;
                    int g = (pixels[i] & 0xff00) >> 8;
                    int b = (pixels[i] & 0xff);
                    data[i] = a << 24 | b << 16 | g << 8 | r;
                }
    
                IntBuffer buffer = ByteBuffer.allocateDirect(data.length << 2)
                    .order(ByteOrder.nativeOrder()).asIntBuffer();
                buffer.put(data).flip();
    
                glTexImage2D(GL_TEXTURE_2D, level, GL_RGBA, width, height, 0, GL_RGBA,
                            GL_UNSIGNED_BYTE, buffer);
    
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    
        // Usa mipmap bilineari per la minificazione
        glTexParameteri(GL_TEXTURE_2D, GL30.GL_TEXTURE_MAX_LEVEL, level - 1);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, -1f);

        glBindTexture(GL_TEXTURE_2D, 0);
    
        return result;
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, texture);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public int getTextureID() {
        return texture;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}