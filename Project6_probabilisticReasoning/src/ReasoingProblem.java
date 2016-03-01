import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.*;

/**
 * Created by erfi on 2/29/16.
 */
public class ReasoingProblem {
    static Pair<Integer, Integer> width_height;//size of the maze
    static Pair<Integer, Integer> UP = new Pair<>(0,1);
    static Pair<Integer, Integer> DOWN = new Pair<>(0,-1);
    static Pair<Integer, Integer> LEFT = new Pair<>(-1,0);
    static Pair<Integer, Integer> RIGHT = new Pair<>(1,0);

    Agent agent;
    MarkovModel model;
    Map<Pair<Integer, Integer>, Color> mazeWorld = new HashMap<>();
    Map<Pair<Integer, Integer>, Double> probMap = new HashMap<>();
    List<Color> colorList;
    Random rand = new Random();

    public ReasoingProblem(){
        initColorList();
        initWorld();
        initProbMap();
        agent = new Agent(
                new Pair<>(rand.nextInt(width_height.getKey()), rand.nextInt(width_height.getValue())),
                mazeWorld,
                width_height,
                colorList);
        model = new MarkovModel(mazeWorld, probMap);
    }

    //============INITIALIZATION METHODS=============
    private void initColorList(){
        colorList = new ArrayList<>();
        colorList.add(Color.RED); //0
        colorList.add(Color.GREEN); //1
        colorList.add(Color.BLUE); //2
        colorList.add(Color.YELLOW); //3
    }

    private void initWorld(){
        Maze m = Maze.readFromFile("/Users/erfi/Documents/COSC76_AI/Project6_probabilisticReasoning/src/simple.maz");
        int[] size = m.getSize();
        width_height = new Pair<>(size[0], size[1]);
        for(int x=0; x<width_height.getKey(); x++){
            for(int y=0; y<width_height.getValue();y++){ //NOTE: (0,0) is at bottom left corner
                Pair<Integer, Integer> loc = new Pair<>(x, y);
                Color c = getColorAt(x, y, m);
                if(c != Color.BLACK) {//if not a wall
                    mazeWorld.put(loc, c);
                }
            }
        }
    }

    private Color getColorAt(int x, int y, Maze maze){
        char c = maze.getChar(x,y);
        Color color = null;
        switch (c){
            case 'r' : color = colorList.get(0);
                break;
            case 'g' : color = colorList.get(1);
                break;
            case 'b' : color = colorList.get(2);
                break;
            case 'y' : color = colorList.get(3);
                break;
            case '#' : color = Color.BLACK;
        }
        return color;
    }

    private void initProbMap(){
        double uniformProb = 1.0/mazeWorld.size();
        for(int i=0; i<width_height.getKey(); i++){
            for(int j=0; j<width_height.getValue(); j++){
                Pair<Integer, Integer> loc = new Pair<>(i, j);
                if(mazeWorld.containsKey(loc)){
                    probMap.put(loc, uniformProb);
                }
            }
        }
    }

    //============END OF INITIALIZATION METHODS=============

    private void runReasoningProblem(){
        Color currentColor = agent.getLocationColor();//see where you are
        agent.memorize(currentColor);//remember the color of current square
        updateProbMap();//update the probability map of the whole maze
        System.out.println("Starting...");
        System.out.println("Agent is at: " + agent.location);
        System.out.println("Agent sees: " + currentColor + " actual color is: " + mazeWorld.get(agent.location));
        //print the maze

        /*
         for number of iterations [or can be a infinite loop]
            move in a random direction
            memorize the color
            update the probability map
         */




    }

    private void updateProbMap(){
        for(int i=0; i<width_height.getKey(); i++){
            for(int j=0; j<width_height.getValue(); j++){
                Pair<Integer, Integer> loc = new Pair<>(i, j);
                if(mazeWorld.containsKey(loc)) {
                    double newprob = model.UpdateLocProb(loc, agent.remember());
                    probMap.put(loc, newprob);//updating the probability map
                }
            }
        }
        normalizeProbMap();
    }

    private void normalizeProbMap(){
        double sumProb = 0.0;
        for(int i=0; i<width_height.getKey(); i++){
            for(int j=0; j<width_height.getValue(); j++){
                Pair<Integer, Integer> loc = new Pair<>(i, j);
                if(probMap.containsKey(loc)) {
                    sumProb = probMap.get(loc);
                }
            }
        }

        for(int i=0; i<width_height.getKey(); i++){
            for(int j=0; j<width_height.getValue(); j++){
                Pair<Integer, Integer> loc = new Pair<>(i, j);
                if(probMap.containsKey(loc)) {
                    //divide the probability of each square by sumProb
                    probMap.put(loc, probMap.get(loc)/sumProb);
                }
            }
        }
    }


    public static void main(String[] args){
        ReasoingProblem rp = new ReasoingProblem();
        rp.runReasoningProblem();

    }
}
