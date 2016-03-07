/**
 * This algorithm is based on the following paper:
 * Lydia E. Kavraki, Petr Svestka, Jean-Claude Latombe, and Mark H. Overmars,
 * Probabilistic roadmaps for path planning in high-dimensional configuration spaces, 
 * IEEE Transactions on Robotics and Automation 12 (4): 566–580.
 * http://dx.doi.org/10.1109/70.508439
 */
package Maze_Solver;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.DoubleAccumulator;

public class PRMPlanner extends MotionPlanner {
    private int numberOfAttempts = 10;
    Map<Vector, Map<Vector, Double>> roadMap;
    
    /**
     * Constructor
     * @param environment  the workspace
     * @param robot        a robot with a steering method
     */
    public PRMPlanner(Environment environment, Robot robot) {
        super(environment, robot);
    }
    
    @Override
    /**
     * returns the size of the map
     * which is the sum of number of vertecies and the edges
     */
    public int getSize() {
        return ( roadMap.size()+ getEdges().size());
    }

    @Override
    /**
     * will declare the initialize the roadMap
     */
    protected void setup() {
        if(roadMap == null) {
            roadMap = new HashMap<Vector, Map<Vector, Double>>();
        }
        //add the start and goal node before starting to grow the graph!
        addVertex(getStart());
        addVertex((getGoal()));
    }

    @Override
    protected void growMap(int K) {
        assert(K>0);
        for(int i=0; i<K; i++) {
            addVertex(generateFreeConfiguration());
        }
    }
    
    /**
     * Add a free configuration to the roadmap
     * @param free  a free configuration
     */
    @SuppressWarnings("boxing")
    private void addVertex(Vector free) {
        if(free != null) {
            if (!roadMap.containsKey(free)) {
                roadMap.put(free, new HashMap<Vector, Double>());
            }
            List<Vector> neighbors = this.nearestKNeighbors(roadMap.keySet(), free, kValue());
            for (Vector v : neighbors) {
                makeNeighbors(free, v);
            }
        }
    }

    /**
     * Auxilary mathod to be use in the addVertex() method
     * in order to add a and b in each others adjacency list if the
     * path between them is steerable
     * @param a
     * @param b
     */
    private void makeNeighbors(Vector a, Vector b){
        if(getEnvironment().isSteerable(getRobot(),a, b, RESOLUTION)){
            roadMap.get(a).put(b, getRobot().getMetric(a,b));
        }
        if(getEnvironment().isSteerable(getRobot(),b,a, RESOLUTION)){
            roadMap.get(b).put(a,getRobot().getMetric(b,a));
        }
    }

    @Override
    protected void reset() {
        if(roadMap != null) {
            roadMap.clear();
        }
    }   
    
    /**
     * 
     */
    @Override
    protected Trajectory query() {
        addVertex(getStart());
        addVertex(getGoal());
        return findPath();
    }
    
    /**
     * Generate a free configuration
     * @return a free configuration if possible, and null otherwise
     */
    private Vector generateFreeConfiguration() {
        int count = 0;
        while(count < numberOfAttempts) {
            Vector v = getRobot().getRandomConfiguration(getEnvironment(), random);
            if (getEnvironment().isValidConfiguration(getRobot(), v)) {
                return v;
            }
            count++;
        }
        return null;
    }
    
    /**
     * The number of vertices for a new vertex attempts to connect.
     * The following paper describes a way to determine the value in order
     * to ensure asymptotic optimality.
     * Sertac Karaman and Emilio Frazzoli, 
     * Sampling-based Algorithms for Optimal Motion Planning, 
     * International Journal of Robotics Research, vol. 30, no.7, pp. 846-894, 2011.
     * http://dx.doi.org/10.1177/0278364911406761
     * @return number of neighbors for a new vertex attempts to connect.
     */
    private int kValue() {
        return 15; // Magic number suggested in Steven M. LaValle's "Planning Algorithms"
    }
    
    /**
     * Determine whether this edge connecting two configurations can be ignored.
     * The following paper describes a way to ignore some edges while maintaining
     * asymptotic optimality.
     * Weifu Wang, Devin J. Balkcom, and Amit Chakrabarti
     * A fast online spanner for roadmap construction
     * International Journal of Robotics Research, vol. 34, no.11, pp. 1418-1432, 2015.
     * http://dx.doi.org/10.1177/0278364915576491
     * @param u first configuration
     * @param v second configuration
     * @return true if this edge can be ignored, and false otherwise. 
     */
    private boolean safeToIgnore(Vector u, Vector v) {
        return false; // Extra credit!!
    }
    
    @Override
    protected Trajectory findPath() {
        List<Vector> path = aStar(getStart(), getGoal());
        return path != null ? convertToTrajectory(path) : null; 
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
     * Astar search
     * @return a path
     */
    @SuppressWarnings("boxing")
    private List<Vector> aStar(Vector start, Vector goal) {
        NavigableSet<Node> pq = new TreeSet<>();
        Map<Vector, Node> map = new HashMap<>();
        Node root = new Node(start, null, 0, getRobot().getMetric(start, goal));
        pq.add(root);
        map.put(start, root);
        while (!pq.isEmpty()) {
            Node node = pq.pollFirst();
            Vector configuration = node.getConfiguration();
            double cost = node.getCost();
            if (goal.equals(configuration))
                return backChain(node);
            Collection<Vector> configs = getSuccessors(configuration);
            for (Vector config : configs){
                double heuristic = getRobot().getMetric(config, goal);
                double newCost = cost+1; // YOU NEED TO MODIFY THIS LINE
                Node test = map.get(config);
                
                if (test != null) {
                    if (test.getPriority() > newCost + heuristic)
                        pq.remove(test);
                    else
                        continue;
                }
                Node newNode = new Node(config, node, newCost, heuristic);
                map.put(config, newNode);
                pq.add(newNode);
            }
        }
        return null;
    }
    
    /**
     * Get successors for a configuration
     * @param configuration
     * @return a collection of successors
     */
    private Collection<Vector> getSuccessors(Vector configuration) {
        return roadMap.get(configuration).keySet();
    }
    
    /**
     * Backchain to construct a path
     * @param node the end node
     * @return a path
     */
    private static List<Vector> backChain(Node node) {
        LinkedList<Vector> result = new LinkedList<>();
        for (Node current = node; current != null; current = current.getParent()) {
            result.addFirst(current.getConfiguration());
        }
        return result;
    }
    
    final class Node implements Comparable<Node> {
        private Vector configuration;
        private Node parent;
        private double heuristic;
        private double cost;
        
        public Node(Vector config, Node p, double cost, double heuristic) {
            this.configuration = config;
            this.parent = p;
            this.heuristic = heuristic;
            this.cost = cost;
        }
        
        @Override
        public int compareTo(Node o) {
            int comparison = Double.compare(getPriority(), o.getPriority());
            return comparison == 0 ? configuration.compareTo(o.getConfiguration()) : comparison;
        }
        
        /**
         * Get the parent
         * @return the parent
         */
        public Node getParent(){
            return parent;
        }
        
        /**
         * Get the configuration
         * @return the configuration
         */
        public Vector getConfiguration() {
            return configuration;
        }
        
        /**
         * Get the cost
         * @return the cost
         */
        public double getCost() {
            return cost;
        }
        
        /**
         * Get the heuristic
         * @return the heuristic
         */
        public double getHeuristic() {
            return heuristic;
        }
        
        /**
         * Get the priority 
         * @return the priority
         */
        public double getPriority(){
            return getCost() + getHeuristic();
        }
    }
}
