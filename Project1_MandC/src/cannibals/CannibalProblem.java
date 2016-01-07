package cannibals;

import java.util.ArrayList;
import java.util.Arrays;


// for the first part of the assignment, you might not extend UUSearchProblem,
//  since UUSearchProblem is incomplete until you finish it.

public class CannibalProblem extends UUSearchProblem {

    // the following are the only instance variables you should need.
    //  (some others might be inherited from UUSearchProblem, but worry
    //  about that later.)

    private int goalm, goalc, goalb;
    private int totalMissionaries, totalCannibals;

    public CannibalProblem(int sm, int sc, int sb, int gm, int gc, int gb) {
        // I (djb) wrote the constructor; nothing for you to do here.

        startNode = new CannibalNode(sm, sc, 1, 0);
        goalm = gm;
        goalc = gc;
        goalb = gb;
        totalMissionaries = sm;
        totalCannibals = sc;

    }
    //====================Private CannibalNode Class========================
    // node class used by searches.  Searches themselves are implemented
    //  in UUSearchProblem.
    private class CannibalNode implements UUSearchNode {

        // do not change BOAT_SIZE without considering how it affect
        // getSuccessors.

        private final static int BOAT_SIZE = 2;

        // how many missionaries, cannibals, and boats
        // are on the starting shore
        private int[] state;

        // how far the current node is from the start.  Not strictly required
        //  for search, but useful information for debugging, and for comparing paths
        private int depth;

        public CannibalNode(int m, int c, int b, int d) {
            state = new int[3];
            this.state[0] = m;
            this.state[1] = c;
            this.state[2] = b;

            depth = d;

        }

        public ArrayList<UUSearchNode> getSuccessors() {
            // add actions (denoted by how many missionaries and cannibals to put
            // in the boat) to current state.

            // You write this method.  Factoring is usually worthwhile.  In my
            //  implementation, I wrote an additional private method 'isSafeState',
            //  that I made use of in getSuccessors.  You may write any method
            //  you like in support of getSuccessors.

            //The big idea here is that im gonna make make CannibalNodes containing all the possible
            //states (which is 5)
            //Then I check to see if they are 1)Feasible and 2)Legal each of them that meet those conditions
            //will be added to the final list that is returned....(dropping the mic!)



            ArrayList<UUSearchNode> successors = new ArrayList<UUSearchNode>();//this will be returned
            ArrayList<CannibalNode> helper = new ArrayList<CannibalNode>();

            if (this.state[2]==1){ //if the boat is on the starting shore
                helper.add(new CannibalNode(this.state[0] - 2, this.state[1], this.state[2] - 1, this.depth + 1)); //subtracting <201>
                helper.add(new CannibalNode(this.state[0], this.state[1] - 2, this.state[2] - 1, this.depth + 1)); //subtracting <021>
                helper.add(new CannibalNode(this.state[0] - 1, this.state[1] - 1, this.state[2] - 1, this.depth + 1)); //subtracting <111>
                helper.add(new CannibalNode(this.state[0] - 1, this.state[1], this.state[2] - 1, this.depth + 1)); //subtracting <101>
                helper.add(new CannibalNode(this.state[0], this.state[1] - 1, this.state[2] - 1, this.depth + 1)); //subtracting <011>
            }else{ //if the boat is on the opposite shore
                helper.add(new CannibalNode(this.state[0] + 2, this.state[1], this.state[2] + 1, this.depth + 1)); //adding <201>
                helper.add(new CannibalNode(this.state[0], this.state[1] + 2, this.state[2] + 1, this.depth + 1)); //adding <021>
                helper.add(new CannibalNode(this.state[0] + 1, this.state[1] + 1, this.state[2] + 1, this.depth + 1)); //adding <111>
                helper.add(new CannibalNode(this.state[0] + 1, this.state[1], this.state[2] + 1, this.depth + 1)); //adding <101>
                helper.add(new CannibalNode(this.state[0], this.state[1] + 1, this.state[2] + 1, this.depth + 1)); //adding <011>
            }

            //now we check for feasibility and legality
            for(CannibalNode n : helper){
                if(isFeasibleState(n.state) && isLegalState(n.state)){
                    successors.add(n);
                }
            }

            return successors;
        }


        /*
        A state is feasible if the number of Missionaries and Cannibals
        is within the 0 to totalMissionaries/totalCannibals.
         */
        private boolean isFeasibleState(int[] state){
            if((state[0] <= totalMissionaries && state[0] >= 0)&&(state[1] <= totalCannibals && state[1] >= 0)) {
                return True;
            }
            return False;
        }

        /*
        A state is legal if it does not turn the Missionaries in to meals
        on either side of the shore!
         */
        private boolean isLegalState(int[] state){
            int[] stateOtherSide = new int[3];
            stateOtherSide[0] = totalMissionaries - state[0];
            stateOtherSide[1] = totalCannibals - state[1];
            stateOtherSide[2] = state[2]==0 ? 1 : 0;

            if((state[0] < state[1]) || (stateOtherSide[0] < stateOtherSide[1])) {
                return False;
            }else{
                return True;
            }
        }

        @Override
        public boolean goalTest() {
            // you write this method.  (It should be only one line long.
            return (this.state[0]==goalm && this.state[1]==goalc && this.state[2]==goalb);
        }



        // an equality test is required so that visited lists in searches
        // can check for containment of states
        @Override
        public boolean equals(Object other) {
            return Arrays.equals(state, ((CannibalNode) other).state);
        }

        @Override
        public int hashCode() {
            return state[0] * 100 + state[1] * 10 + state[0];
        }

        @Override
        public String toString() {
            //  -----------
            // |  <M,C,B>  |
            // | Depth = d |
            //  -----------

//            System.out.println(" ----------- ");
//            System.out.println("|  <" + this.state[0] + "," + this.state[1] + "," + this.state[2] + ">  |");
//            System.out.println("| Depth = " + this.depth + " |");
//            System.out.println(" ----------- ");

            return " ----------- \n" + "|  <" + this.state[0] + "," + this.state[1] + "," + this.state[2] + ">  |\n" + "| Depth = " + this.depth + " |\n" + " ----------- \n";
        }


        // You might need this method when you start writing
        // (and debugging) UUSearchProblem.
        @Override
        public int getDepth() {
            return depth;
        }


    }
    //===============End of CannibalNode class===================

    public static void main(String[] args){
        System.out.println("CannibalProblem class");
    }


}
