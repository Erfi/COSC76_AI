/**
 * This algorithm is from the following paper
 * Steven M. LaValle and James  Kuffner Jr.,
 * Randomized Kinodynamic Planning, 
 * The International Journal of Robotics Research 20 (5), pp. 378–400.
 * http://dx.doi.org/10.1177/02783640122067453
 */

package assignment_motion_planning;

import java.util.*;

import javafx.geometry.Point2D;
import javafx.scene.Node;

public class RRTPlanner extends MotionPlanner {
    private static final double DEFAULT_DELTA = 0.1;  // Duration for the control
    private Map<Vector, Edge> parents = null;
    private List<Vector> finalCandidates = null;

    //=========Edge Class===========
    private class Edge{
        Vector parentVector = null; //by default
        public Edge(Vector P){
            parentVector = P;
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
    public Map<Point2D, Point2D> getEdges() {
        Map<Point2D, Point2D> edgeMap = new HashMap<Point2D, Point2D>();
        for (Vector v : parents.keySet()){
            if(parents.get(v).parentVector != null) { //the start node has no parents
                Point2D childPoint = new Point2D(v.get(0), v.get(1));
                Point2D parentPoint = new Point2D(parents.get(v).parentVector.get(0), parents.get(v).parentVector.get(1));
                edgeMap.put(childPoint, parentPoint);
            }
        }
        return edgeMap;
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
        if(finalCandidates == null){
            finalCandidates = new ArrayList<Vector>();
        }
        parents.put(getStart(), new Edge(null));
    }
    
    @Override
    protected void growMap(int K) {
//        System.out.println("entering: growMap");
        for (int i=0 ; i<K; i++) {
            Vector qNear = nearestNeighbor(parents.keySet(),getRobot().getRandomConfiguration(getEnvironment(), random));
            newConf(qNear, DEFAULT_DELTA);
//            System.out.println("done with one iteration of growMap. i : " + i);
        }
//        System.out.println("exiting: growMap");
    }

    /**
     * Generate a new configuration from a configuration and insert it
     * @param qnear    the beginning configuration of the random motion
     * @param duration the duration of the random motion
     * @return true if one new configuration is inserted, and false otherwise
     */
    @SuppressWarnings("boxing")
    private boolean newConf(Vector qnear, double duration) { //consider checking if the new configuration is valid
        Vector newControl = getRobot().getRandomControl(random);
        if(getEnvironment().isValidMotion(getRobot(), qnear, new Trajectory(newControl, DEFAULT_DELTA),RESOLUTION )){
            Vector child = getRobot().move(qnear, newControl, DEFAULT_DELTA);
            if(!parents.containsKey(child)) {
                parents.put(child, new Edge(qnear));
                if(child.equals(nearestNeighbor(parents.keySet(), getGoal()))){//if the child's nearest neighbor happened to be the goal vertex, then add the child as one of the final candidates for backchaining
                    finalCandidates.add(child);
                    System.out.println("found a candidate!");
                }
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
        double minDist = Double.POSITIVE_INFINITY;
        Vector closestVertex = null;
        for(Vector v : finalCandidates){
            double newMin = getRobot().getMetric(v, getGoal());
            if( newMin < minDist){
                minDist = newMin;
                closestVertex = v;
            }
        }
        return closestVertex != null ? convertToTrajectory(backChain(closestVertex)) : null;
    }

    /**
     * Convert a list of configurations to a corresponding trajectory based on the steering method
     * @param path a list of configurations
     * @return a trajectory
     */
    private Trajectory convertToTrajectory(List<Vector> path) {
        Trajectory result = new Trajectory();
        Vector previous = path.get(0);
        for (int i = 1; i < path.size(); ++i) {
            Vector next = path.get(i);
            result.append(getRobot().steer(previous, next));
            previous = next;
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
