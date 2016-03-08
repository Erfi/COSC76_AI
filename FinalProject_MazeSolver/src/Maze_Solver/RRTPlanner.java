/**
 * This algorithm is from the following paper
 * Steven M. LaValle and James  Kuffner Jr.,
 * Randomized Kinodynamic Planning, 
 * The International Journal of Robotics Research 20 (5), pp. 378–400.
 * http://dx.doi.org/10.1177/02783640122067453
 */

package Maze_Solver;

import java.util.*;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.util.Pair;

public class RRTPlanner extends MotionPlanner {
    private static final double DEFAULT_DELTA = 0.1;  // Duration for the control
    private Map<Vector, Edge> parents = null; //tree holding the graph
    private Map<Vector, Edge> parents2 = null; //second tree for bidirectional search!
    private double goalBias = 0.1; //using goal biased with goalBias/1 probability
    private boolean useBidirectionalMap = false;
    private double epsilon = 0.001;

    //=========Edge Class===========
    private class Edge{
        Vector parentVector = null; //by default
        Trajectory parentTrajectory = null;
        public Edge(Vector P, Trajectory T){
            parentVector = P;
            parentTrajectory = T;
        }
    }
    
    /**
     * Constructor 
     * @param environment the workspace
     * @param robot       the robot
     */
    public RRTPlanner(Environment environment, Robot robot) {
        super(environment, robot);
    }
    
    @Override
    public List<Pair<Point2D, Point2D>> getEdges() {
        List<Pair<Point2D, Point2D>> edgeList = new ArrayList<Pair<Point2D, Point2D>>();
        for (Vector v : parents.keySet()){
            if(parents.get(v).parentVector != null) { //the start node has no parents
                Point2D childPoint = new Point2D(v.get(0), v.get(1));
                Point2D parentPoint = new Point2D(parents.get(v).parentVector.get(0), parents.get(v).parentVector.get(1));
                Pair<Point2D,Point2D> p = new Pair<Point2D, Point2D>(childPoint, parentPoint);
                edgeList.add(p);
            }
        }
        return edgeList;
    }
    
    @Override
    public int getSize() {
        return (parents.size() + getEdges().size());
    }

    @Override
    protected void setup() {
        if(parents == null){
            parents = new HashMap<Vector, Edge>();
        }
        if(parents2 == null){//second tree for bidirectional search
            parents2 = new HashMap<>();
        }
        parents.put(getStart(), new Edge(null,null));
        parents2.put(getGoal(),new Edge(null, null));
    }
    
    @Override
    protected void growMap(int K) {
        if(useBidirectionalMap){
            growBidirectionalMap(K);
        }else{
            growSingledirectionalMap(K);
        }
    }

    private void growSingledirectionalMap(int K){
        for (int i=0 ; i<K; i++) {
            Vector qNear;
            if(random.nextDouble() > goalBias) {
                qNear = nearestNeighbor(parents.keySet(), getRobot().getRandomConfiguration(getEnvironment(), random));
            }else{
                qNear = nearestNeighbor(parents.keySet(), getGoal());
            }
            newConf(qNear,parents, DEFAULT_DELTA);
        }
    }
    private void growBidirectionalMap(int K){
        for (int i=0; i<K; i++){
            //extent the first tree
            Vector qn = nearestNeighbor(parents.keySet(), getRobot().getRandomConfiguration(getEnvironment(), random));
            Vector qs = newConf(qn,parents, DEFAULT_DELTA); //returns new vertex of the first tree that we just added to

            //extend the second tree toward the first tree using qs
            Vector qn_hat = nearestNeighbor(parents2.keySet(), qs);
            Vector qs_hat = newConf(qn_hat, parents2, DEFAULT_DELTA);

            if(getRobot().getMetric(qs, qs_hat) < epsilon){//if the two newly added nodes are less than an epsilon apart
                connectTrees(qs,parents, qs_hat, parents2);//makes a connection between tree1 and tree2
            }
        }
    }

    /**
     * This is an auxillary method used by the bidirectiona search to connect two graphs when
     * the grow too close to each other!
     * @param qs vertex from the first tree
     * @param tree first tree
     * @param qs_hat vertext from the second tree
     * @param tree_hat secong tree
     */
    private void connectTrees(Vector qs, Map<Vector, Edge> tree, Vector qs_hat, Map<Vector, Edge> tree_hat){
        //make trajectory between the two vertices(make one!)

        //check if that trajectory is valid (use isValidMotion)

        //if the first trajectory is ok then make the second one too

        //make edges out of the vertices and trajectories

        //add them in both tree one and tree two correspondingly

        //you can even call the findPath here if you don't want to grow the graph anymore
    }

    /**
     * Generate a new configuration from a configuration and insert it
     * @param qnear    the beginning configuration of the random motion
     * @param duration the duration of the random motion
     * @return true if one new configuration is inserted, and false otherwise
     */
    @SuppressWarnings("boxing")
    private Vector newConf(Vector qnear, Map<Vector, Edge> parentMap, double duration) {
        Vector newControl = getRobot().getRandomControl(random);
        Trajectory newTrajectory = new Trajectory(newControl,duration);
        if(getEnvironment().isValidMotion(getRobot(), qnear, newTrajectory, RESOLUTION )){
            Vector child = getRobot().move(qnear, newControl, duration);
            if(!parentMap.containsKey(child)) {
                parentMap.put(child, new Edge(qnear, newTrajectory));
                return child;
            }else{
                return null;
            }
        }
        return null;
    }
    
    @SuppressWarnings("boxing")
    @Override
    protected Trajectory findPath() {
        Vector closestVertex = nearestNeighbor(parents.keySet(), getGoal());
        return closestVertex != null ? convertToTrajectory(backChain(closestVertex)) : null;
    }

    /**
     * Convert a list of configurations to a corresponding trajectory based on the steering method
     * @param path a list of configurations
     * @return a trajectory
     */
    private Trajectory convertToTrajectory(List<Vector> path) {
        Trajectory result = new Trajectory();
        for(Vector v : path){
            if(parents.get(v).parentTrajectory != null) {// start vertex will have null parents
                result.append(parents.get(v).parentTrajectory);
            }
        }
        return result;
    }

    /**
     * Backchain to construct a path
     * @param currentConfig of the end node
     * @return a path
     */
    private  List<Vector> backChain(Vector currentConfig) {
        LinkedList<Vector> result = new LinkedList<Vector>();
        for (Vector v = currentConfig; v != null; v = parents.get(v).parentVector) {
            result.addFirst(v);
        }
        return result;
    }


    @Override
    protected void reset() {
        if(parents != null){
            parents.clear();
        }
        if(parents2 != null){
            parents2.clear();
        }
    }

}
