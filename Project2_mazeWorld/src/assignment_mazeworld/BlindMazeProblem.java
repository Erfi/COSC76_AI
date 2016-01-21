package assignment_mazeworld;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// Find a path for a single blind agent to get from a start location (xStart, yStart)
//  to a goal location (xGoal, yGoal)

public class BlindMazeProblem extends InformedSearchProblem {

    private static int actions[][] = {Maze.NORTH, Maze.EAST, Maze.SOUTH, Maze.WEST};

    private int xGoal, yGoal;
//    private HashSet<int[]> startSet;
    private HashSet<ArrayList<Integer>> startSet;

    private Maze maze;

    public BlindMazeProblem(Maze m, HashSet<ArrayList<Integer>> beliefState,int gx, int gy) {
        startNode = new BlindMazeNode(beliefState, 0);
        startSet = beliefState;
        xGoal = gx;
        yGoal = gy;
        maze = m;
    }


    //================BlindMazeNode class=================
    // node class used by searches.  Searches themselves are implemented
    //  in SearchProblem.
    public class BlindMazeNode implements SearchNode {

        // possible locations of the agent in the maze --> {[x1,y1], [x2,y2] ,...}
        protected HashSet<ArrayList<Integer>> beliefState = new HashSet<ArrayList<Integer>>();

        // how far the current node is from the start.  Not strictly required
        //  for uninformed search, but useful information for debugging,
        //  and for comparing paths
        private double cost;

        public BlindMazeNode(HashSet<ArrayList<Integer>> B_State , double c) {
            beliefState = B_State;
            cost = c;
        }

        public ArrayList<SearchNode> getSuccessors() {
            ArrayList<SearchNode> successors = new ArrayList<SearchNode>();
            for (int[] action: actions) { //for each direction
                HashSet<ArrayList<Integer>> tempSet = new HashSet<ArrayList<Integer>>();
                for(ArrayList<Integer> loc : beliefState) { //for each location in the beliefState
                    int xNew = loc.get(0) + action[0];
                    int yNew = loc.get(1) + action[1];

                    //System.out.println("testing successor " + xNew + " " + yNew);
                    if (maze.isLegal(xNew, yNew)) { //if the new location is legal tehn add it
//                        System.out.println("legal successor found " + " " + xNew + " " + yNew);
                        ArrayList<Integer> tempArray = new ArrayList<Integer>();
                        tempArray.add(0, xNew);
                        tempArray.add(1, yNew);
                        tempSet.add(tempArray); //add the possible location to this belief set for this direction
                    }else{//if not add the old location
                        tempSet.add(loc);
                    }
                }
                SearchNode succ = new BlindMazeNode(tempSet, getCost()+1.0); //Make a node with the new belief state for this direction and add as a successor
                successors.add(succ);
            }
            return successors;
        }

        @Override
        public boolean goalTest() { //the goal is reached if the belief state has shrank to 1, and that is the goal location
            if(beliefState.size() == 1){
                boolean isGoal = true;
                for (ArrayList<Integer> loc : beliefState){ //only way to get to the only [x,y] in the set
                    isGoal = ((loc.get(0) == xGoal) && (loc.get(1) == yGoal));
                }
                return isGoal;
            }else{
                return false;
            }
        }


        // an equality test is required so that visited sets in searches
        // can check for containment of states
        @Override
        public boolean equals(Object other) {
            return (beliefState.equals(((BlindMazeNode)other).beliefState));
//            boolean equal = true; //assume they are equal at first
//            for (ArrayList<Integer> i : beliefState) {
//                for (ArrayList<Integer> j : ((BlindMazeNode) other).beliefState) {
//                    if (Arrays.equals(i, j)) {
//                        equal = true;
//                        break;
//                    } else {
//                        equal = false;
//                    }
//                }
//                if (!equal) {
//                    return false;
//                }
//            }
//            return true;
        }

        @Override
        public int hashCode() {
            StringBuilder hashStr = new StringBuilder(); //make a stringBuilder because it is faster than the '+'
            //since set will not always iterate in the same order I convert to charArray and sort first
            for(ArrayList<Integer> loc : beliefState) {//for each location in the beliefState
                hashStr.append(Integer.toString(loc.get(0)));
                hashStr.append(Integer.toString(loc.get(1)));
            }
            //since set will not always iterate in the same order I convert to charArray and sort first
            char[] c = hashStr.toString().toCharArray();
            Arrays.sort(c);
            return new String(c).hashCode();
        }

        @Override
        public String toString() {
            String str = "{ Cost: " + getCost() + " | ";
            for(ArrayList<Integer> loc : beliefState){
                str = str + "[" + loc.get(0) + "," + loc.get(1) + "] ";
            }
            str = str + "}";
            return str;
        }

        @Override
        public double getCost() {
            return cost;
        }


        @Override
        public double heuristic() {
            // min of manhattan distances for all possible states: that is optimistic!
            double minDist = startSet.size(); //initialize minDist to the size of the maze
            for (ArrayList<Integer> loc : beliefState){
                double tempDist = getManhattan(new int[]{loc.get(0), loc.get(1)}, new int[]{xGoal,yGoal});
                if(tempDist < minDist){
                    minDist =  tempDist;
                }
            }
            return minDist;
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
    //===============End of BlindMazeNode class==================

}

