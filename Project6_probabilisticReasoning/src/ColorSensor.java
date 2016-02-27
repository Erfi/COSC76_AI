import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.*;

/**
 * Created by erfi on 2/27/16.
 */
public class ColorSensor {
    Map<Pair<Integer, Integer>, Color> mazeWorld;
    List<Color> colors;
    Random rand;

    public ColorSensor(Map<Pair<Integer, Integer>, Color> world, List<Color> c){
        mazeWorld = world;
        colors = c;
        rand = new Random();
    }

    /**
     * Returns the color that the sensor sees at the given location
     * @param loc
     * @return color at loc
     */
    public Color getColorAt(Pair<Integer, Integer> loc){
        Color c = mazeWorld.get(loc);
        double r1 = rand.nextDouble();
        if(r1 < 0.88){//with 0.88 probability
            return c;
        }else{
            List<Color> wrongColors = new ArrayList<>(colors);
            wrongColors.remove(c);//list of colors except color c
            int r2 = rand.nextInt(wrongColors.size());
            return wrongColors.get(r2);
        }
    }
}
