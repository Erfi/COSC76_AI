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
import javafx.util.Pair;

public class RRTPlanner extends MotionPlanner {
    private static final double DEFAULT_DELTA = 0.1;  // Duration for the control
    private Map<Vector, Edge> parents = null; //tree holding the graph
    private Map<Vector, Edge> parents2 = null; //second tree for bidirectional search!
    private double goalBias = 0.1; //using goal biased with goalBias/1 probability
    private boolean useBidirectionalMap = true; //to change between using RRT and bidirectional RRT
    private boolean connected = false;
    private double epsilon = 0.1;
    private Pair<Vector, Edge> connection;//connection to tree2 in tree1

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
        for (Vector v : parents2.keySet()){
            if(parents2.get(v).parentVector != null) { //the goal node has no parents
                Point2D childPoint = new Point2D(v.get(0), v.get(1));
                Point2D parentPoint = new Point2D(parents2.get(v).parentVector.get(0), parents2.get(v).parentVector.get(1));
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
            Vector qn, qn_hat, qs, qs_hat;
            //extent the first tree
            qn = nearestNeighbor(parents.keySet(), getRobot().getRandomConfiguration(getEnvironment(), random));
            qs = newConf(qn,parents, DEFAULT_DELTA); //returns new vertex of the first tree that we just added to

            //extend the second tree toward the first tree using qs (if qs is not null!)
            if(qs!=null) {
                qn_hat = nearestNeighbor(parents2.keySet(), qs);
                qs_hat = newConf(qn_hat, parents2, DEFAULT_DELTA);
            }else{//if qs is null then grow the second tre in a random direction!
                qn_hat = nearestNeighbor(parents2.keySet(), getRobot().getRandomConfiguration(getEnvironment(), random));
                qs_hat = newConf(qn_hat, parents2, DEFAULT_DELTA);
            }
            if(qs!=null && qs_hat!=null) {//if the two new vertices are not null
                if (getRobot().getMetric(qs, qs_hat) < epsilon) {//if the two newly added nodes are less than an epsilon apart
                    if(connectTrees(qs, parents, qs_hat, parents2)) {//makes a connection between tree1 and tree2 if you can
                        //uncomment if you want to stop expansion as soon as a single path if found!
                        return; //if tree 1 and two are connected then there is a path from start to goal so we are done exploring
                    }
                }
            }
            if(parents.size() > parents2.size()){//balance the trees by swapping their references
                swapTrees(parents, parents2);
            }
        }
    }


    /**
     * Auxilary function. Takes two trees (HashMaps) and swaps their references
     * @param t1 tree1
     * @param t2 tree2
     */
    private void swapTrees(Map<Vector, Edge> t1, Map<Vector, Edge> t2){
        Map<Vector, Edge>  tempMap = t1;
        t1 = t2;
        t2 = tempMap;
    }

    /**
     *Returns the magnitude of vector connecting two vertices
     * @param v1 vertex1
     * @param v2 vertex2
     * @return magnitude of vector connecting two vertices
     */
    private double getMagnitude(Vector v1, Vector v2){
        double deltax = v2.get(0) - v1.get(0);
        double deltay = v2.get(1) - v1.get(1);
        double deltaz = v2.get(2) - v1.get(2);
        return Math.sqrt(deltax*deltax + deltay*deltay + deltaz*deltaz);
    }

    /**
     * This is an auxillary method used by the bidirectional search to connect two trees when
     * they grow too close to each other!
     * @param qs vertex from the first tree
     * @param tree first tree
     * @param qs_hat vertext from the second tree
     * @param tree_hat secong tree
     */
    private boolean connectTrees(Vector qs, Map<Vector, Edge> tree, Vector qs_hat, Map<Vector, Edge> tree_hat){
        //make trajectory between the two vertices(make one!)
        Vector control1 = getNormDirection(qs, qs_hat); //direction
        double len = getMagnitude(qs, qs_hat);
        Trajectory t1 = new Trajectory(control1,(len/DEFAULT_DELTA)*DEFAULT_DELTA);
        //check if that trajectory is valid (use isValidMotion)
        if(getEnvironment().isValidMotion(getRobot(),qs,t1,RESOLUTION)) {//if the trajectory is validMotion
            //make edges out of the vertices and trajectories
            Edge e1 = new Edge(qs, t1); //Edge form qs to qs_hat
            if(!parents.containsKey(qs_hat)){
                parents.put(qs_hat, e1);
            }else{
                return false;
            }
            connection = new Pair<>(qs_hat, e1);
            connected = true;
            System.out.println("Connected!");
            return true;
        }else{
            return false;
        }
    }

    /**
     * Auxilary function tht return the direction vector AB = B-A
     * @param v1 vector 1
     * @param v2 vector 2
     * @return v2 - v1
     */
    private Vector getNormDirection(Vector v1, Vector v2){
        Vector dir;
        double deltax = v2.get(0) - v1.get(0);
        double deltay = v2.get(1) - v1.get(1);
        double deltaz = v2.get(2) - v1.get(2);
        double len = Math.sqrt(deltax*deltax + deltay*deltay + deltaz*deltaz);
        dir = new Vector(deltax/len, deltay/len, deltaz/len);
//        System.out.println("dir: " + dir + " len: " + len);
        return dir;
    }

    /**
     * Generate a new configuration from a configuration and insert it
     * @param qnear    the beginning configuration of the random motion
     * @param duration the duration of the random motion
     * @return true if one new configuration is inserted, and false otherwise
     */
    @SuppressWarnings("boxing")
    private Vector newConf(Vector qnear, Map<Vector, Edge> parentMap, double duration) {
        Vector newControl = getRobot().getRandomControl(random);//direction from qNear to child
        Trajectory newTrajectory = new Trajectory(newControl,duration);//trajectory from qNear to child
        if(getEnvironment().isValidMotion(getRobot(), qnear, newTrajectory, RESOLUTION )){
            Vector child = getRobot().move(qnear, newControl, duration);
            if(!parentMap.containsKey(child)) {
                parentMap.put(child, new Edge(qnear, newTrajectory));//edge from parent to the child
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
        if(useBidirectionalMap){
            Vector closestVertex = null;
            if (connected) {
//                closestVertex = nearestNeighbor(parents2.keySet(), getStart());//<<<
                closestVertex = connection.getKey();//qs_hat
            }
            return closestVertex != null ? convertToTrajectory(backChain(closestVertex)) : null;
        }else {
            Vector closestVertex = nearestNeighbor(parents.keySet(), getGoal());
            return closestVertex != null ? convertToTrajectory(backChain(closestVertex)) : null;
        }
    }

    /**
     * Convert a list of configurations to a corresponding trajectory based on the steering method
     * @param path a list of configurations
     * @return a trajectory
     */
    private Trajectory convertToTrajectory(List<Vector> path) {
        Trajectory result = new Trajectory();
        if(useBidirectionalMap){
            boolean firstHalf = true;
//            Map<Vector, Edge>  parentTree = parents; //start with the first half of the path in the parents
            for (Vector v : path) {
                if(firstHalf){//if using the trajectories form the first half of the path
                    if(parents.get(v).parentTrajectory != null){
                        result.append(parents.get(v).parentTrajectory);
                    }
                }else {//using trajectories from the second half of the path
                    if (parents2.get(v).parentTrajectory != null) {// start vertex will have null parents
                        result.append(reverseTrajectory(parents2.get(v).parentTrajectory));
                    }
                }
                if(v == connection.getKey()){//switch the parentTree after reaching the connection point!
                    firstHalf = false;
                }
            }
        }else {
            for (Vector v : path) {
                if (parents.get(v).parentTrajectory != null) {// start vertex will have null parents
                    result.append(parents.get(v).parentTrajectory);
                }
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
        if(useBidirectionalMap) {
            for(Vector v = currentConfig; v != null; v = parents2.get(v).parentVector) { //for the second half starting at the connection vertex
                result.addLast(v);
            }
            for(Vector v = currentConfig; v != null; v = parents.get(v).parentVector){// for the first half starting at the connection vertex
                result.addFirst(v);
            }
        }else{
            for (Vector v = currentConfig; v != null; v = parents.get(v).parentVector) {
                result.addFirst(v);
            }
        }
        return result;
    }

    /**
     *
     * @return reverse of the given trajectory in terms of direction
     */
    private Trajectory reverseTrajectory(Trajectory T){
        double revX = -T.getControl(0).get(0);
        double revY = -T.getControl(0).get(1);
        double revZ = -T.getControl(0).get(2);
        Trajectory newTrajectory = new Trajectory(new Vector(revX, revY, revZ), T.getDuration(0));
        return newTrajectory;
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
