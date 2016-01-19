package assignment_mazeworld;

/**
 * Created by erfi on 1/18/16.
 * Decription: this class will be a general version of SimpleMazeProblem.java.
 * It will be able to create problems with multiple robots and their goals.
 */

import java.util.ArrayList;
import java.util.Arrays;

// Find a path for a multiple agent to get from a start location (xStart, yStart)
//  to a goal location (xGoal, yGoal)

public class GeneralMazeProblem extends InformedSearchProblem {

    private static int actions[][] = {Maze.NORTH, Maze.EAST, Maze.SOUTH, Maze.WEST, Maze.ZERO};

//    private int xStart, yStart, xGoal, yGoal;

    private int[][] starts_xy; //array of [[xStart_1, yStart_1],...,[xStart_i, yStart_i],...,[xStart_k, yStart_k]] for start of the ith agent
    private int[][] goals_xy; //array of [[xGoal_1, yGoal_1],...,[xGoal_i, yGoal_i],...,[xGoal_k, yGoal_k]] for goal of the ith agent

    private Maze maze;

    public GeneralMazeProblem(Maze m, int[][] starts, int[][] goals) {

        starts_xy = starts;
        goals_xy = goals;

        startNode = new GeneralMazeNode(starts,0,0);
        maze = m;
    }


    //================SimpleMazeNode class=================
    // node class used by searches.  Searches themselves are implemented
    //  in SearchProblem and InformedSearchProblem
    public class GeneralMazeNode implements SearchNode {

        // location of the agents in the maze and the turn number --> [[x1,y1],...,[xi,yi],...,[xk,yk]]
        protected int[][] state;

        //whose turn is it?
        protected int turn;

        // how far the current node is from the start.  Not strictly required
        //  for uninformed search, but useful information for debugging,
        //  and for comparing paths
        private double cost;

        public GeneralMazeNode(int[][] s, double c, int t) {
            state = s; //deep copy (java passes primitive types by value)
            turn = t;
            cost = c;
        }

        public int getXof(int i) {
            return state[i][0];
        }

        public int getYof(int i) {
            return state[i][1];
        }

        /**
        * returns the legal successor states but not for all agents!
         * The return states depends on whose turn it is!
        */
        public ArrayList<SearchNode> getSuccessors() {

            ArrayList<SearchNode> successors = new ArrayList<SearchNode>();

            for (int[] action: actions) {
//                System.out.println(state.length);
                int xNew = state[turn][0] + action[0];
                int yNew = state[turn][1] + action[1];

//                System.out.println("testing successor " + xNew + " " + yNew);

                if(maze.isLegal(xNew, yNew) && isFree(xNew, yNew)) {
//                    System.out.println("legal successor found " + " " + xNew + " " + yNew);

                    // copy the current state, change it with the new x and y locations
                    // update the turn and cost
                    // make a new GeneralMazeNode and add it to the successors List
                    int[][] newState = new int[state.length][];
                    for(int i=0; i<state.length; i++){
                        newState[i] = Arrays.copyOf(state[i], state[i].length);
                    }
                    newState[turn][0] = xNew;
                    newState[turn][1] = yNew;
                    int newTurn = turn + 1; //its next agents turn, unless...
                    if(turn == state.length-1){ // all agents have moved so back to the first one! (back to back!)
                        newTurn = 0;
                    }

                    SearchNode succ = new GeneralMazeNode(newState, getCost() + 1.0, newTurn);
                    successors.add(succ);
                }
            }
            return successors;
        }

        /**
         * Helper method to check if a new state crashes with other agents or not
         * @param x
         * @param y
         * @return true if the spot is free, false is the spot is taken by another agent
         */
         private boolean isFree(int x, int y){
             for(int[] location : state){ // for every [x_i,y_i] in the states check if x_i == x and y_i ==y
                 if(location[0] == x && location[1] == y ){ //if there is an agent already there
                     if((state[turn][0] != x) && (state[turn][1] != y)) {// if the agent isn't it self (used in case this one is just chilling there and passing its turn)
                         return false;
                     }
                 }
             }
             return true;
         }

        @Override
        public boolean goalTest(){
            boolean result = true;
            for (int i=0; i<state.length; i++){
                if(state[i][0] == goals_xy[i][0] && state[i][1] == goals_xy[i][1]){
                    continue;
                }else{
                    result = false;
                }
            }
            return result;
        }


        // an equality test is required so that visited sets in searches
        // can check for containment of states
        @Override
        public boolean equals(Object other) {
            for(int i=0; i<state.length; i++){
                if(!Arrays.equals(state[i], ((GeneralMazeNode)other).state[i])){
                    return false;
                }
            }
            return true;
        }

        /**
         * Make a hash from the k states plus the turn
         * @return a unique integer representation of the states for hash purposes
         */
        @Override
        public int hashCode() {
            StringBuilder hashStr = new StringBuilder(); //make a stringBuilder because it is faster than the '+'
            for (int i=0; i<state.length; i++){//for all the [x, y]  in state
                hashStr.append(Integer.toString(state[i][0]));
                hashStr.append(Integer.toString(state[i][1]));
            }
            hashStr.append(Integer.toString(turn)); //if state->{{1,2},{3,4}} and turn->1, hashStr.toString()->"12341"

            return Integer.parseInt(hashStr.toString()); // int --> 12341
        }

        @Override
        public String toString() {
            StringBuilder strBuff = new StringBuilder();
            strBuff.append("{turn:").append(Integer.toString(turn)).append(" | cost:").append(Double.toString(cost)).append(" | [ ");
            for(int[] s : state){
                strBuff.append("[").append(Integer.toString(s[0])).append(",").append(Integer.toString(s[1])).append("] ");
            }
            strBuff.append(" ]}");

            return strBuff.toString();
        }

        @Override
        public double getCost() {
            return cost;
        }


        @Override
        public double heuristic() {
            // sum of manhattan distances metric for general maze with multiple agent:
            double result = 0;
            for (int i=0; i<state.length; i++){
                result += getManhattan(state[i], goals_xy[i]);
            }
            return result;
        }

        /**
         * Helper Method to be used in heuristic()
         * @param start : the int[2] start location
         * @param end : he int[2] end location
         * @return the mahattan distance between int[2] start and int[2] end
         */
        private double getManhattan(int[] start, int[] end){
            double dx = end[0] - start[0];
            double dy = end[1] - start[1];
            return Math.abs(dx) + Math.abs(dy);

        }

        @Override
        public int compareTo(SearchNode o) {
            return (int) Math.signum(priority() - o.priority());
        }

        @Override
        public double priority() {
            return heuristic() + getCost();
        }

    }
    //===============End of SimpleMazeNode class==================

}


