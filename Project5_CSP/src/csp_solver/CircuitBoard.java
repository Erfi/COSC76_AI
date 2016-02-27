package csp_solver;

import javafx.util.Pair;

import java.util.*;

/**
 * Created by Erfan Azad on 2/20/16.
 *
 */
public class CircuitBoard {
    ConstraintSatisfactionProblem csp;
    int BOARD_HEIGHT;
    int BOARD_WIDTH ;
    HashMap<Integer, Pair<Integer, Integer>> coordinateIDMap = new HashMap<>(); //maps an integer ID to coordinate
    HashMap<Pair<Integer, Integer>, Integer> coordinateMap = new HashMap<>(); //maps a coordinate to coordinate ID
    HashMap<Integer, ComponentNode> componenetMap = new HashMap<>();
    HashMap<Integer, String> componentRep = new HashMap<>();//alphabetical representation of the componenets

    //====Component Class=====
    private class ComponentNode{
        int ID;
        int width;
        int height;
        Set<Integer> domain;

        public ComponentNode(int id, int w, int h, Set<Integer> s){
            ID = id;
            width = w;
            height = h;
            domain = s;
        }
    }
    //=======================
    //setting up the problem including board size and componenets
    public CircuitBoard() {
        csp = new ConstraintSatisfactionProblem();

        BOARD_HEIGHT = 3;
        BOARD_WIDTH = 10;

        //Define Components
        List<ComponentNode> compList = new ArrayList<>();

        ComponentNode c1 = new ComponentNode(1, 3, 2, new HashSet<>());//id=1 width=3 height=2
        componenetMap.put(1,c1);
        componentRep.put(1,"a");
        compList.add(c1);
        ComponentNode c2 = new ComponentNode(2, 5, 2, new HashSet<>());
        componenetMap.put(2,c2);
        componentRep.put(2,"b");
        compList.add(c2);
        ComponentNode c3 = new ComponentNode(3, 2, 3, new HashSet<>());
        componenetMap.put(3,c3);
        componentRep.put(3,"c");
        compList.add(c3);
        ComponentNode c4 = new ComponentNode(4, 7, 1, new HashSet<>());
        componenetMap.put(4,c4);
        componentRep.put(4,"d");
        compList.add(c4);

        //fill the coordinateIDMap and coordinateMap (assign a number to every coordinate)
        int counter = 1;
        for(int i=0; i<BOARD_WIDTH; i++){
            for(int j=0; j<BOARD_HEIGHT; j++){
                Pair<Integer, Integer> p = new Pair<>(i, j);//coordinate of lower-left corner of each component
                coordinateIDMap.put(counter, p);
                coordinateMap.put(p, counter);
                counter++;
            }
        }

        //create the domain for each component
        for(ComponentNode c : compList){
            for(int i=0; i<=BOARD_WIDTH-c.width; i++){ //cols
                for(int j=0; j<=BOARD_HEIGHT-c.height; j++){ //rows
                    Pair<Integer, Integer> p = new Pair<>(i, j);
                    c.domain.add(coordinateMap.get(p)); //add the coordinate id to the component domain
                }
            }
            csp.addVariable(c.ID, c.domain);
        }

        //add constraints
        for(ComponentNode comp1 : compList){
            for(ComponentNode comp2 : compList){
                if(comp1.ID != comp2.ID) { //not the same node
                    Set<Pair<Integer, Integer>> tempSet = getConstraints(comp1, comp2);
                    if (tempSet.size() != 0) {
                        csp.addConstraint(comp1.ID, comp2.ID, tempSet);
                    }
                }
            }
        }
    }

    /**
     * returns a white list of constraints between two componenets
     * @return a white list (Set) of constaints
     */
    public Set<Pair<Integer,Integer>> getConstraints(ComponentNode c1, ComponentNode c2){
        Set<Pair<Integer,Integer>> result = new HashSet<>();
        for(int d1 : c1.domain){
            for(int d2: c2.domain){
                Pair<Integer, Integer> coor1 = coordinateIDMap.get(d1);
                Pair<Integer, Integer> coor2 = coordinateIDMap.get(d2);
                if(((coor2.getKey() >= coor1.getKey() + c1.width) ||
                        (coor2.getKey() <= coor1.getKey() - c2.width))
                        ||
                        ((coor2.getValue() >= coor1.getValue() + c1.height)||
                        (coor2.getValue() <= coor1.getValue() - c2.height))){
                    Pair<Integer, Integer> constraint = new Pair<>(d1, d2);
                    result.add(constraint);
                }
            }
        }
        return result;
    }

    public void solve(){
        Map<Integer, Integer> solution = csp.solve();
        if (solution == null) {
            System.out.println("No solutions found for the CircuitBoard :(");
        }else{
            printSolution(solution);
//            for(Integer key: solution.keySet()) {
//                System.out.println("ID: " +key+ " coor: "+coordinateIDMap.get(solution.get(key)));
//            }
        }
    }

    public void printSolution(Map<Integer, Integer> solution){
        int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];

        for(Integer id : solution.keySet()){
            ComponentNode cnode = componenetMap.get(id);
            Pair<Integer, Integer> coor = coordinateIDMap.get(solution.get(id));
            for(int i=coor.getKey(); i<coor.getKey()+cnode.width; i++){
                for(int j=coor.getValue(); j<coor.getValue()+cnode.height; j++){
                    board[j][i] = id;
                }
            }
        }

        for(int k=BOARD_HEIGHT-1; k>=0; k--){
            System.out.println(Arrays.toString(board[k]));
        }
    }

    public static void main(String[] args){
        CircuitBoard cb = new CircuitBoard();
        cb.solve();
    }
}
