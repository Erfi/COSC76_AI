import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by erfi on 2/27/16.
 */
public class Agent {
    ColorSensor sensor;
    Pair<Integer, Integer> location;
    Map<Pair<Integer, Integer>, Color> mazeWold;
    Pair<Integer, Integer> worldSize;
    List<Color> memory;
    List<Color> colorList;

    public Agent(Pair<Integer, Integer> loc, Map<Pair<Integer, Integer>,Color> world, Pair<Integer, Integer> size, List<Color> c){
        location = loc;
        mazeWold = world;
        worldSize = size;
        colorList = c;
        sensor = new ColorSensor(mazeWold, colorList);
        memory = new LinkedList<>();
    }

    
}
