package csp_solver;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import javafx.util.Pair;

/**
 * Simple CSP solver
 *
 */
public class ConstraintSatisfactionProblem {
    private int nodesExplored;
    private int constraintsChecked;
    private Map<Integer, Set<Integer>> variablesMap;
    private Map<Integer, Constraint> constraintMap;
    private class Constraint{
        public Integer var1;
        public Integer var2;
        Set<Pair<Integer, Integer>> constraints;

        public Constraint(Integer v1, Integer v2, Set<Pair<Integer, Integer>> c){
            var1 = v1;
            var2 = v2;
            constraints = c;
        }
    }


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

    private void initMaps(){
        variablesMap = new HashMap<Integer, Set<Integer>>();
        constraintMap = new HashMap<Integer, Constraint>();
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
        variablesMap.put(id, domain);
    }
    
    /**
     * Add a binary constraint
     * @param id1         the identifier of the first variable
     * @param id2         the identifier of the second variable
     * @param constraint  the constraint
     */
    public void addConstraint(Integer id1, Integer id2, Set<Pair<Integer, Integer>> constraint) {

    }
    
    /**
     * Enforce consistency by AC-3, PC-3.
     */
    private boolean enforceConsistency() {
        return false;
    }
    
    private boolean revise(Integer id1, Integer id2) {
        return false;
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
