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
    private Map<Vector, Edge> parents = null;

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
        parents.put(getStart(), new Edge(null,null));
    }
    
    @Override
    protected void growMap(int K) {
        for (int i=0 ; i<K; i++) {
            Vector qNear = nearestNeighbor(parents.keySet(),getRobot().getRandomConfiguration(getEnvironment(), random));
            newConf(qNear, DEFAULT_DELTA);
        }
    }

    /**
     * Generate a new configuration from a configuration and insert it
     * @param qnear    the beginning configuration of the random motion
     * @param duration the duration of the random motion
     * @return true if one new configuration is inserted, and false otherwise
     */
    @SuppressWarnings("boxing")
    private boolean newConf(Vector qnear, double duration) {
        Vector newControl = getRobot().getRandomControl(random);
        Trajectory newTrajectory = new Trajectory(newControl, DEFAULT_DELTA*5);
        if(getEnvironment().isValidMotion(getRobot(), qnear, newTrajectory, RESOLUTION )){
            Vector child = getRobot().move(qnear, newControl, DEFAULT_DELTA*5);
            if(!parents.containsKey(child)) {
                parents.put(child, new Edge(qnear, newTrajectory));
                return true;
            }else{
                return false;
            }
        }
        return false;
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
    }

}
