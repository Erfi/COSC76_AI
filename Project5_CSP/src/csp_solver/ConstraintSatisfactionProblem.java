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
        Map<Integer, Integer> solution = backtracking(new HashMap<>());
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
        System.out.println(b);
        return b;
    }

    /**
     *AC-3 for arc consistancy
     * @return a boolean for if the arc consistancy has been achieved or not
     */
    private boolean AC_3(){
        Queue queue = new LinkedList<Pair<Integer, Integer>>();//queue of arcs
        queue.addAll(constraintMap.keySet());

        while(!queue.isEmpty()){
            Pair tempPair = (Pair)queue.poll();
            if(revise((Integer)tempPair.getKey(), (Integer)tempPair.getValue())){
                if(variablesMap.get(tempPair.getKey()).size() == 0){ //if domain of idi is empty
                    return false;
                }
                for(Integer neighbourID : neighborMap.get(tempPair.getKey())){
                    if(neighbourID != tempPair.getValue()){
                        Pair<Integer, Integer> newPair = new Pair<>(neighbourID, (Integer) tempPair.getKey());
                        queue.add(newPair);
                    }
                }
            }
        }
        return true;
    }
    
    private boolean revise(Integer id1, Integer id2) {
        boolean revised = false;
        for(Iterator<Integer> iterator = variablesMap.get(id1).iterator();  iterator.hasNext();) {
            Integer x = iterator.next();
            boolean satisfied = false;
            for (Integer y : variablesMap.get(id2)) {
                Pair<Integer, Integer> p = new Pair<>(x, y);//constraint
                if (constraintMap.get(new Pair(id1, id2)).contains(p)) {
                    satisfied = true;
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
        return null;
    }
    
    /**
     * Inference for backtracking
     * Implement FC and MAC3
     * @param var              the new assigned variable
     * @param value            the new assigned value
     * @param partialSolution  the partialSolution
     * @param removed          the values removed from other variables' domains
     * @return true if the partial solution may lead to a solution, false otherwise.
     */
    private boolean inference(Integer var, Integer value, Map<Integer, Integer> partialSolution, Map<Integer, Set<Integer>> removed) {
        return true;
    }
 
    /**
     * Look-ahead value ordering
     * Pick the least constraining value (min-conflicts)
     * @param var              the variable to be assigned
     * @param partialSolution  the partial solution
     * @return an order of values in var's domain
     */
    private Iterable<Integer> orderDomainValues(Integer var, Map<Integer, Integer> partialSolution) {
        return null;
    }

    /**
     * Dynamic variable ordering
     * Pick the variable with the minimum remaining values or the variable with the max degree.
     * Or pick the variable with the minimum ratio of remaining values to degree.
     * @param partialSolution  the partial solution
     * @return one unassigned variable
     */
    private Integer selectUnassignedVariable(Map<Integer, Integer> partialSolution) {
        return -1;
    }
    
    /**
     * Backjumping
     * Conflict-directed-backjumping
     * @param partialSolution
     */
    private void jumpBack(Map<Integer, Integer> partialSolution) {
    }
}
