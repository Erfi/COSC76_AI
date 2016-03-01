import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.ArrayList;
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

    /**
     * Moves the robot using the given move
     * @param move A move in form of <1,0> (East)
     */
    protected void move(Pair<Integer,Integer> move){
        Pair<Integer, Integer> newLoc = new Pair<>((location.getKey()+move.getKey()) , (location.getValue()+move.getValue()));
        if(isValidPosition(newLoc)){//if we can move to the new location
            location = newLoc;
        }
    }

    /**
     * Auxlary function to check if a location is valid on the map
     * @param loc location
     * @return returns true if valid location and false otherwise
     */
    private boolean isValidPosition(Pair<Integer, Integer> loc){
        return mazeWold.containsKey(loc);
    }

    protected Color getLocationColor(){
        return sensor.getColorAt(location);
    }

    protected void memorize(Color c){
        memory.add(0, c); //add to the beginning of the memory
    }


    /**
     * Using the markov model, this method aloows the agent to
     * remember the last two colors that it has seen.
     * @return A list of the last two colors that is has observed
     * NOTE: may return a list of size 1 if there is only one memory
     */
    protected List<Color> remember(){
//        List<Color> recentMemories = new ArrayList<>();
//        if(memory.size() < 2){// only one memory
//            recentMemories.add(memory.get(0));
//        }else{//more than one memory
//            recentMemories.add(memory.get(0));
//            recentMemories.add(memory.get(1));
//        }
//        return recentMemories;
        return memory;
    }
}























