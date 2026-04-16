import java.awt.*;
import java.util.ArrayList;

public class Map {
    public ArrayList<Rectangle> staticPlatforms;
    public ArrayList<MovingPlatform> movingPlatforms;
    public String name;

    public Map(String name) {
        this.name = name;
        staticPlatforms = new ArrayList<>();
        movingPlatforms = new ArrayList<>();
    }
}