package models;

import renderEngine.Loader;

public class WireFaceModel extends Model {

    public static final float RED = 0f;
    public static final float GREEN = 0.0f;
    public static final float BLUE = 0;
    
    public WireFaceModel(Loader loader, byte face) {
        super(getRawModel(loader, face));
    }

    private static RawModel getRawModel(Loader loader, byte face) {
        return loader.loadToVAO(getVertices(face), getIndices());
    }

    private static float[] getVertices(byte face) {
        return FaceModel.getVertices(face);
    }

    private static int[] getIndices() {
        int[] indices = new int[] {
            0, 1, 0, 3, 1, 2, 2, 3
            /*
            0, 1, 0, 2, 0, 4,
            1, 3, 1, 5,
            2, 3, 2, 6,
            3, 7,
             4, 5, 4, 6,
            5, 7,
            6, 7 */
        };

        return indices;
    }
}
