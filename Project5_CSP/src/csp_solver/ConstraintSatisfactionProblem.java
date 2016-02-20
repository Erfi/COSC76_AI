package csp_solver;

import java.util.*;

import javafx.util.Pair;

/**
 * Simple CSP solver
 *
 */
public class ConstraintSatisfactionProblem {
    private int nodesExplored;
    private int constraintsChecked;
    private Map<Integer, Set<Integer>> variablesMap = new HashMap<>();//map<ID, Set<values>>
    private Map<Integer, Set<Integer>> removedVariablesMap = new HashMap<>();//contains the removed IDs and their removed values in MAC-3 algorithm
    private Map<Pair<Integer, Integer>, Set<Pair<Integer, Integer>>> constraintMap = new HashMap<>();//map<Pair<ID1,ID2> , set<Pair<ID1val, ID2val>>>
    private Map<Integer, Set<Integer>> neighborMap = new HashMap<>();//map<ID, set<ID>>




    /**
     * Solve for the CSP problem
     * @return the mapping from variables to values
     */
    public Map<Integer, Integer> solve() {
        resetStats();
        long before = System.currentTimeMillis();
        if (!enforceConsistency())
            return null;
//        System.out.println("enforce consistency is good!");
        Map<Integer, Integer> solution = backtracking(new HashMap<>());
//        System.out.println(solution);
        double duration = (System.currentTimeMillis() - before) / 1000.0;
        printStats();
        System.out.println(String.format("Search time is %.2f second", duration));
        return solution;
    }
    
    private void resetStats() {
        nodesExplored = 0;
        constraintsChecked = 0;
    }

    
    private void incrementNodeCount() {
        ++nodesExplored;
    }
    
    private void incrementConstraintCheck() {
        ++constraintsChecked;
    }
    
    public int getNodeCount() {
        return nodesExplored;
    }
    
    public int getConstraintCheck() {
        return constraintsChecked;
    }
    
    protected void printStats() {
        System.out.println("Nodes explored during last search:  " + nodesExplored);
        System.out.println("Constraints checked during last search " + constraintsChecked);
    }

    /**
     * Add a variable with its domain
     * @param id      the identifier of the variable
     * @param domain  the domain of the variable
     */
    public void addVariable(Integer id, Set<Integer> domain) {
        Set<Integer> clonedDomain = new HashSet<>(domain);
        variablesMap.put(id, clonedDomain);
    }
    
    /**
     * Add a binary constraint
     * @param id1         the identifier of the first variable
     * @param id2         the identifier of the second variable
     * @param constraint  the constraint
     */
    public void addConstraint(Integer id1, Integer id2, Set<Pair<Integer, Integer>> constraint) {
        Pair<Integer, Integer> newPair = new Pair<>(id1, id2);
        constraintMap.put(newPair, constraint);
        addNeighbors(id1, id2);// add them as each others neighbors

        //add the reverse of the constraints as well
        Pair<Integer, Integer> newReversePair = new Pair<>(id2, id1);
        Set<Pair<Integer, Integer>> reverseConstraint = new HashSet<>();
        if(!constraintMap.containsKey(newReversePair)){//if already not added (just an extra check!)
            for(Pair<Integer, Integer>  c : constraint){
                reverseConstraint.add(new Pair<>(c.getValue(), c.getKey()));
            }
            constraintMap.put(newReversePair, reverseConstraint);
        }
    }

    private void addNeighbors(Integer id1, Integer id2){
        //for id1
        if(!neighborMap.containsKey(id1)){//no id1 in the map
            Set<Integer> newSet = new HashSet<>();//make a new set
            newSet.add(id2);//add id2 as a neighbor
            neighborMap.put(id1, newSet);
        }else{//id1 in the map
            neighborMap.get(id1).add(id2);//add id2 as id1's neighbor
        }

        //for id2
        if(!neighborMap.containsKey(id2)){//no id2 in the map
            Set<Integer> newSet = new HashSet<>();//make a new set
            newSet.add(id1);//add id1 as a neighbor
            neighborMap.put(id2, newSet);
        }else{//id2 in the map
            neighborMap.get(id2).add(id1);//add id1 as id2's neighbor
        }
    }
    
    /**
     * Enforce consistency by AC-3, PC-3.
     */
    private boolean enforceConsistency() {
        boolean b = AC_3();
//        System.out.println(b);
        return b;
    }

    /**
     *AC-3 for arc consistancy
     * @return a boolean for if the arc consistancy has been achieved or not
     */
    private boolean AC_3(){
        Queue queue = new LinkedList<Pair<Integer, Integer>>();//queue of arcs
        queue.addAll(constraintMap.keySet());// fill the queue with all the arcs

        while(!queue.isEmpty()){
            Pair tempPair = (Pair)queue.poll();
            if(revise((Integer)tempPair.getKey(), (Integer)tempPair.getValue())){ //if the arc domain is revised (changed)
                if(variablesMap.get(tempPair.getKey()).size() == 0){ //if domain of idi is empty
                    return false;
                }
                for(Integer neighbourID : neighborMap.get(tempPair.getKey())){ //for all the neighbors
                    if(neighbourID != tempPair.getValue()){ //except the neighbor that we just met (revised)
                        Pair<Integer, Integer> newPair = new Pair<>(neighbourID, (Integer) tempPair.getKey());
                        queue.add(newPair); // add them in the queue for future revision
                    }
                }
            }
        }
        return true;
    }
    
    private boolean revise(Integer id1, Integer id2) {
        boolean revised = false;
        for(Iterator<Integer> iterator = variablesMap.get(id1).iterator();  iterator.hasNext();) { // for  each value in the domain of id1
            Integer x = iterator.next();
            boolean satisfied = false;
            for (Integer y : variablesMap.get(id2)) { //for each value on the domain of id2
                Pair<Integer, Integer> p = new Pair<>(x, y);//constraint
                Pair<Integer, Integer> tempPair = new Pair<>(id1, id2);
                if (constraintMap.get(tempPair).contains(p)) { // is there is a legal relation between them
                    satisfied = true; //we are satisfied to find one
                    break;
                }
            }
            if (!satisfied) {//if there is no y in id2 such that (x,y) satisfies their constraint
//                variablesMap.get(id1).remove(x); //remove x from id1's domain
                iterator.remove();//remove x from id1's domain
                revised = true;
            }
        }
        return revised;
    }

    /**
     * Backtracking algorithm
     * @param partialSolution  a partial solution
     * @return a solution if found, null otherwise.
     */
    private Map<Integer, Integer> backtracking(Map<Integer, Integer> partialSolution) {
        if(isComplete(partialSolution)){
            return partialSolution;
        }
        Integer varID = selectUnassignedVariable(partialSolution);
        for(Integer value : orderDomainValues(varID, partialSolution)){
            HashMap<Integer, Set<Integer>> removed = new HashMap<>();//as backup
            if(isConsistent(varID, value, partialSolution)){
                partialSolution.put(varID, value);
                boolean inferences = inference(varID, partialSolution, removed);
                if(inferences) { //if MAC-3 worked
                    //add inferences to the assignment
                    Map<Integer,Integer> result = backtracking(partialSolution);
                    if(result != null){
                        return result;
                    }
                }
            }
            //not consistent remove {var, value} and inferences from partialSolution
            revertChanges(varID, partialSolution, removed);
        }
        return null;
    }

    /**
     * Auxilary method to remove the {varID = value} from the partial solution
     * along with reverting the changes made by inference (changes are in the "removed" set)
     * @param varID variable to remove from partial solution
     * @param partialSolution partial solution
     * @param removed set of the variables that were removed from the variablesMap by inference along with their domain
     */
    private void revertChanges(Integer varID, Map<Integer, Integer> partialSolution, HashMap<Integer, Set<Integer>> removed){
        //remove the {var, value} from partialSolutions
        if(partialSolution.containsKey(varID)) {
            partialSolution.remove(varID);
        }
        //remove the changes from the inference
        for(Integer id : removed.keySet()){
            variablesMap.get(id).addAll(removed.get(id));
        }
    }


    /**
     * Checks to see if the var-value pair is consistent with the given assignments map
     * @param varID1 variable in question
     * @param value1 value of the variable in quesiton
     * @param assignment the partial assignment so far
     * @return true if consistent and false otherwise
     */
    private boolean isConsistent(Integer varID1, Integer value1, Map<Integer, Integer> assignment){
        boolean result = true;
        for(Integer varID2 : assignment.keySet()){
            Pair<Integer, Integer> IDpair = new Pair<>(varID1, varID2);
            Pair<Integer, Integer> valuePair = new Pair<>(value1, assignment.get(varID2));
            if(constraintMap.containsKey(IDpair)){ //if there exist an arc between IDpair
                if(!constraintMap.get(IDpair).contains(valuePair)){ //if there is no relation such as valuePair as their constraint
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Checks to see if the given assignment is complete (to be used in the backtracking())
     * @param assignment
     * @return a boolean, true if complete, false otherwise
     */
    private boolean isComplete(Map<Integer, Integer> assignment){
        if(variablesMap.size() == assignment.size()){
            return true;
        }
        return false;
    }

    /**
     * Inference for backtracking
     * Implement FC and MAC3
     * @param varID              the new assigned variable
     * @param partialSolution  the partialSolution
     * @param removed          the values removed from other variables' domains
     * @return true if the partial solution may lead to a solution, false otherwise.
     */
    private boolean inference(Integer varID, Map<Integer, Integer> partialSolution, Map<Integer, Set<Integer>> removed) {
        //find the neighbors of varID that are still unassigned
        ArrayList<Integer> unassignedNeighbors = new ArrayList<Integer>();
        for(Integer neighbor : neighborMap.get(varID)){
//            System.out.println("Hi");
            if(!partialSolution.containsKey(neighbor)){ // if not part of the partial solution (means unassigned)
                unassignedNeighbors.add(neighbor);
            }
        }
        //pass unassigned neighbors list to the MAC-3 algorithm
        return MAC_3(varID, unassignedNeighbors, removed);
    }

    /**
     *
     * @param varID variable to do the MAC-3 algorithm for
     * @param unassigned list of unassigned neighbor IDs
     * @param removed map of removed cariables with their removed domein
     * @return true if arc consistency can complete and false otherwise
     */
    private boolean MAC_3(Integer varID, List<Integer> unassigned, Map<Integer,Set<Integer>> removed){
        Queue queue = new LinkedList<Pair<Integer, Integer>>(); //queue of arcs
        //fill the queue withe unassigned neighbors of varID
        for (Integer neighborId : unassigned){
            Pair<Integer, Integer> newPair = new Pair<>(varID, neighborId ); //or (neighborId, varID)?
            queue.add(newPair);
        }

        //do AC-3
        while(!queue.isEmpty()){
            Pair tempPair = (Pair)queue.poll();
            if(revise_MAC_3((Integer)tempPair.getKey(), (Integer)tempPair.getValue(), removed)){ //if the arc domain is revised (changed)
                if(variablesMap.get(tempPair.getKey()).size() == 0){ //if domain of idi is empty
                    return false;
                }
                for(Integer neighbourID : neighborMap.get(tempPair.getKey())){ //for all the neighbors
                    if(neighbourID != tempPair.getValue()){ //except the neighbor that we just met (revised)
                        Pair<Integer, Integer> newPair = new Pair<>(neighbourID, (Integer) tempPair.getKey());
                        queue.add(newPair); // add them in the queue for future revision
                    }
                }
            }
        }
        return true;
    }

    private boolean revise_MAC_3(Integer id1, Integer id2, Map<Integer,Set<Integer>> removed){
        boolean revised = false;
        for(Iterator<Integer> iterator = variablesMap.get(id1).iterator();  iterator.hasNext();) { // for  each value in the domain of id1
            Integer x = iterator.next();
            boolean satisfied = false;
            for (Integer y : variablesMap.get(id2)) { //for each value on the domain of id2
                Pair<Integer, Integer> p = new Pair<>(x, y);//constraint
                Pair<Integer, Integer> tempPair = new Pair<>(id1, id2);
                if (constraintMap.get(tempPair).contains(p)) { // is there is a legal relation between them
                    satisfied = true; //we are satisfied to find one
                    break;
                }
            }
            if (!satisfied) {//if there is no y in id2 such that (x,y) satisfies their constraint
//                variablesMap.get(id1).remove(x); //remove x from id1's domain
                iterator.remove();//remove x from id1's domain
                //add id1 to the removed map along withs removed value
                addToRemovedMap(id1, x, removed);
                revised = true;
            }
        }
        return revised;
    }

    private void addToRemovedMap(Integer id, Integer value, Map<Integer,Set<Integer>> removed){
        if(removed.containsKey(id)){
            removed.get(id).add(value);
        }else{
            HashSet<Integer>  valueSet = new HashSet<>();
            valueSet.add(value);
            removed.put(id, valueSet);
        }
    }

    //================================
    //============OrderNode===========
    //================================
    /**
     * This is an auxilary class for Look_ahead value ordering
     * function --> orderDomainValues()
     */
    private class OrderNode implements Comparable<OrderNode>{
        Integer value;
        Integer score;

        public OrderNode(Integer v, Integer s){
            value = v;
            score = s;
        }

        private void incrementScore(){
            score++;
        }

        @Override
        public int compareTo(OrderNode other){
            return score - other.score;
        }

        @Override
        public String toString(){
            return "{"+value+":"+score+"}";
        }
    }
    //================================
    //=====END OF OrderNode class=====
    //================================


    /**
     * Look-ahead value ordering
     * Pick the least constraining value (min-conflicts)
     * @param var              the variable to be assigned
     * @param partialSolution  the partial solution
     * @return an order of values in var's domain
     */
    private Iterable<Integer> orderDomainValues(Integer var, Map<Integer, Integer> partialSolution){
        //create nodeList
        List<OrderNode> nodes = new ArrayList<>();

        List<Integer> values = new ArrayList<>(variablesMap.get(var));//make a list from the var's domain (for consistent order)
        for(Integer val1: values){//for each value in the domain of the given variable --> var
            OrderNode n = new OrderNode(val1,0);//make a OrderNode for the val1 with score of 0
            for(Integer neighborID : neighborMap.get(var)){ //for each neighbor of the given variable
                if(!partialSolution.containsKey(neighborID)){ //if the neighbor is not yet assigned
                    for(Integer val2 : variablesMap.get(neighborID)){// for all the values in the neighbor's domain
                        //make the (val1, val2) constraint and check to see if it is satisfied
                        if(constraintMap.get(new Pair<>(var, neighborID)).contains(new Pair<>(val1, val2))){
                            //increase the score for val1
                            n.incrementScore();
                        }
                    }
                }
            }
            //add the OrderNode n to the nodes list
            nodes.add(n);
        }

        Collections.sort(nodes);//sort the OrderNode list based on their scores

        List<Integer> result = new ArrayList<>();
        for(OrderNode n: nodes){
            result.add(n.value);
        }

        return result;
    }

    /**
     * Dumb version of orderDomainValues()
     * @param var
     * @param partialSolution
     * @return a shuffeled list of values for the given var
     */
    private Iterable<Integer> FakeOrderDomainValues(Integer var, Map<Integer, Integer> partialSolution){
        ArrayList<Integer> values = new ArrayList<>(variablesMap.get(var));
        Collections.shuffle(values);
        return values;
    }
    /**
     * Dynamic variable ordering
     * Pick the variable with the minimum remaining values or the variable with the max degree.
     * Or pick the variable with the minimum ratio of remaining values to degree.
     * @param partialSolution  the partial solution
     * @return one unassigned variable
     */
    private Integer selectUnassignedVariable(Map<Integer, Integer> partialSolution) {
        Integer var = -1;
        int tempSize = Integer.MAX_VALUE;
        for(Integer v : variablesMap.keySet()){//for variable v in all the variables
            if(!partialSolution.containsKey(v)){//if v is not in the partial solutions
                int s = variablesMap.get(v).size();
                if(s < tempSize){
                    var = v;
                    tempSize = s;
                }
            }
        }

        return var;
    }

    /**
     * Dumb version of selectUnassignedVariable
     * @param partialSolution tge partial solution
     * @return one unassigned variable
     */
    private Integer FakeSelectUnasignedVariable(Map<Integer, Integer> partialSolution){
        Integer var = -1;
        for(Integer ID : variablesMap.keySet()){ // go through the variablesMap
            if(!partialSolution.containsKey(ID)){ //choose the first ID that does not yet exist in the partial solutions
                var = ID;
                break;
            }
        }
        return var;
    }
    
    /**
     * Backjumping
     * Conflict-directed-backjumping
     * @param partialSolution
     */
    private void jumpBack(Map<Integer, Integer> partialSolution) {
    }

    public static void main(String[] args){
        //==============================
        //Australia map coloring problem (Used for debugging)
        //==============================
        ConstraintSatisfactionProblem csp = new ConstraintSatisfactionProblem();
        Set<Integer> domain = new HashSet<>();
        domain.add(1); //red
        domain.add(2); //green
        domain.add(3); //blue


        csp.addVariable(11, domain); //WA
        csp.addVariable(12, domain); //NT
        csp.addVariable(13, domain); //Q
        csp.addVariable(14, domain); //NSW
        csp.addVariable(15, domain); //V
        csp.addVariable(16, domain); //SA


        Set<Pair<Integer,Integer>> constraints = new HashSet<>();
        constraints.add(new Pair<Integer, Integer>(1,2)); //red-green
        constraints.add(new Pair<Integer, Integer>(1,3)); //red-blue
        constraints.add(new Pair<Integer, Integer>(2,1)); //green-red
        constraints.add(new Pair<Integer, Integer>(2,3)); //green-blue
        constraints.add(new Pair<Integer, Integer>(3,1)); //blue-red
        constraints.add(new Pair<Integer, Integer>(3,2)); //blue-green

        csp.addConstraint(11,12,constraints);
        csp.addConstraint(12,11, constraints);

        csp.addConstraint(12,13,constraints);
        csp.addConstraint(13,12, constraints);

        csp.addConstraint(13,14,constraints);
        csp.addConstraint(14,13, constraints);

        csp.addConstraint(14,15,constraints);
        csp.addConstraint(15,14, constraints);

        csp.addConstraint(11,12,constraints);
        csp.addConstraint(12,11, constraints);

        csp.addConstraint(15,16,constraints);
        csp.addConstraint(16,15, constraints);

        csp.addConstraint(11,16,constraints);
        csp.addConstraint(16,11, constraints);

        csp.addConstraint(12,16,constraints);
        csp.addConstraint(16,12, constraints);

        csp.addConstraint(13,16,constraints);
        csp.addConstraint(16,13, constraints);

        csp.addConstraint(14,16,constraints);
        csp.addConstraint(16,14, constraints);


        csp.solve();
    }
}
