import com.sun.tools.corba.se.idl.InterfaceGen;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by erfi on 2/28/16.
 */
public class MarkovModel {
    Map<Pair<Integer, Integer>, Color> mazeWorld;
    Map<Pair<Integer, Integer>, Double> probabilityMap; //contains probability of the agent being is each of the squares


    public MarkovModel(Map<Pair<Integer, Integer>, Color> world, Map<Pair<Integer, Integer>, Double> probMap){
        mazeWorld = world;
        probabilityMap = probMap;
    }

    /**
     * Calculates the transition probability of the agent based on if there are any
     * adjacent squares (NESW) or not
     * @param loc current location
     * @return a double representing the transition probability of being in the current loc
     * NOTE: We are assuming uniform probability of moving from one square to the adjacent
     * squares ==> 1/4 = 0.25
     */
    private double transition_prob(Pair<Integer, Integer> loc){
        Pair<Integer, Integer> NORTH = new Pair<>(loc.getKey(), loc.getValue()+1);
        Pair<Integer, Integer> SOUTH = new Pair<>(loc.getKey(), loc.getValue()-1);
        Pair<Integer, Integer> EAST = new Pair<>(loc.getKey()+1, loc.getValue());
        Pair<Integer, Integer> WEST = new Pair<>(loc.getKey()-1, loc.getValue());
        double transProb = 0.0; //transition probability

        if(mazeWorld.containsKey(NORTH)){//if the square to the north of current location can be reached
            transProb += 0.25 * probabilityMap.get(NORTH);//increment according to the probability of north
        }else{
            transProb += 0.25 * probabilityMap.get(loc);//increment according to the probability of current loc
        }

        if(mazeWorld.containsKey(SOUTH)){//if the square to the south of current location can be reached
            transProb += 0.25 * probabilityMap.get(SOUTH);
        }else{
            transProb += 0.25 * probabilityMap.get(loc);//increment according to the probability of current loc
        }

        if(mazeWorld.containsKey(EAST)){//if the square to the east of current location can be reached
            transProb += 0.25 * probabilityMap.get(EAST);
        }else{
            transProb += 0.25 * probabilityMap.get(loc);//increment according to the probability of current loc
        }

        if(mazeWorld.containsKey(WEST)){//if the square to the west of current location can be reached
            transProb += 0.25 * probabilityMap.get(WEST);
        }else{
            transProb += 0.25 * probabilityMap.get(loc);//increment according to the probability of current loc
        }
        return transProb;
    }

    /**
     * returns the probability that the observation made at current loc correct
     * @param c color at location loc
     * @param loc location
     * @return a double representing the confidence of the sensor observation
     */
    private double observation_prob(Color c, Pair<Integer, Integer> loc){
        if(mazeWorld.get(loc) == c){ //if the color of square at loc is indeed c
            return 0.88; //will give the correct result with 0.88 confidence!
        }else{
            return 0.04;
        }
    }

    /**
     *returns the probability of being in location loc given
     * the memory list of recently observed colors based on transition_prob and
     * observation_prob
     * @return a double representing the probability of being in loc
     */
    protected double UpdateLocProb(Pair<Integer, Integer> loc, List<Color> memory){
        double update = transition_prob(loc) * observation_prob(memory.get(0), loc);//using only the last color atm
        return update;
    }
}
