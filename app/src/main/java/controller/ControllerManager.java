package controller;

import java.util.ArrayList;
import java.util.List;

public class ControllerManager {
    private static List<Dualsense> controllers = new ArrayList<>();

    public static void addController() {
        controllers.add(new Dualsense());
    }

    public static void removeController(int controllerId) {
        controllers.remove(controllers.get(controllerId));
    }

    public static void updateControllers() {
        for (Dualsense controller : controllers) {
            controller.update();
        }
    }

    public static Dualsense getController(int controllerId) {
        return controllers.get(controllerId);
    }
}
