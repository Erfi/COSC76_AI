/**
 * This algorithm is from the following paper
 * Steven M. LaValle and James  Kuffner Jr.,
 * Randomized Kinodynamic Planning, 
 * The International Journal of Robotics Research 20 (5), pp. 378–400.
 * http://dx.doi.org/10.1177/02783640122067453
 */

package assignment_motion_planning;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javafx.geometry.Point2D;

public class RRTPlanner extends MotionPlanner {
    private static final double DEFAULT_DELTA = 0.1;  // Duration for the control
    
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
        // YOU WILL WRITE THIS METHOD
        return null;
    }
    
    @Override
    public int getSize() {
        // YOU WILL WRITE THIS METHOD
        return 0;
    }

    @Override
    protected void setup() {
        // YOU WILL WRITE THIS METHOD
    }
    
    @Override
    protected void growMap(int K) {
        // YOU WILL WRITE THIS METHOD
    }

    /**
     * Generate a new configuration from a configuration and insert it
     * @param qnear    the beginning configuration of the random motion
     * @param duration the duration of the random motion
     * @return true if one new configuration is inserted, and false otherwise
     */
    @SuppressWarnings("boxing")
    private boolean newConf(Vector qnear, double duration) {
        // YOU WILL WRITE THIS METHOD
        return false;
    }
    
    @SuppressWarnings("boxing")
    @Override
    protected Trajectory findPath() {
        // YOU WILL WRITE THIS METHOD
        return null;
    }

    @Override
    protected void reset() {
        // YOU WILL WRITE THIS METHOD
    }

}
