package Maze_Solver;

import java.util.List;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MotionPlannerDriver extends Application {
    private static final String TITLE = "CS 76 Motion Planner";
    private static final String PLANAR_ROBOT_ENVIRONMENT = "planar_robot_environment_hard";
    private static final String ROBOT_ARM_ENVIRONMENT = "robot_arm_environment_hard";
    private static final String EMPTY = "empty";
    private static final String HANOVER = "hanover";
    private static final boolean IS_PLANAR_ROBOT = true;  // Change this if you want to switch robot
    private static boolean DRAW_EDGES = true;
    private static final Environment ENVIRONMENT;
    private static final Vector START;
    private static final Vector GOAL;
    private static final Robot ROBOT;
    private static final double RESOLUTION = 0.05;  // resolution for interpolation
    private MotionPlannerView mpView;
    
    // Modify these in order to change environment, start, goal, and robot
    static {
        if (IS_PLANAR_ROBOT) {
            ENVIRONMENT = new Environment(PLANAR_ROBOT_ENVIRONMENT);
            START = new Vector(-4, -4, 0);
            GOAL = new Vector(4, 4, 0);
//            ROBOT = PlanarRobot.getDubinsCar();
//            ROBOT = PlanarRobot.getReedsSheppCar();
//            ROBOT = PlanarRobot.getDifferentialDrive();
            ROBOT = PlanarRobot.getOmnidirectionalRobot();
        } else {
            ENVIRONMENT = new Environment(ROBOT_ARM_ENVIRONMENT);
            START = new Vector(0.0, 0.0, 0.0, 0.0, 0.0);
            GOAL = new Vector(0.0,-Math.PI/2, 0.0, 0.0, Math.PI/2);
//            START = new Vector(Math.PI * 1.5, 0.0, -Math.PI / 2.0, 0.0,0.0);
//            GOAL = new Vector(0.0, 0.0, Math.PI / 2.0, 0.0,0.0);
            ROBOT = RobotArm.getRobotArm(5);
            DRAW_EDGES = false;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();        
        initMPView(primaryStage, root, ENVIRONMENT, ROBOT);
        primaryStage.setTitle(TITLE);
        root.getChildren().add(mpView);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        
        // do the real work of the driver; run search tests
        List<Vector> path = runSearches(ENVIRONMENT, ROBOT, START, GOAL);
        if (path != null)
            mpView.animate(path);
    }
    
    /**
     * Initialize the motion planning view
     * @param primaryStage the stage
     * @param robot        the robot
     */
    private void initMPView(Stage primaryStage, StackPane root, Environment environment, Robot robot) {
        mpView = new MotionPlannerView(primaryStage, root, environment, robot);
    }
    
    /**
     * Run the search
     * @return the path
     */
    private List<Vector> runSearches(Environment environment, Robot robot, Vector start, Vector goal) {
        MotionPlanner mp;
        if (IS_PLANAR_ROBOT) {
            mp = new RRTPlanner(environment, robot);
        } else {
            mp = new PRMPlanner(environment, robot);
        }
        // UNCOMMENT THIS LINE TO RUN SEARCH
        Trajectory path = mp.solve(start, goal);
//        Trajectory path = robot.steer(start, goal);
        if (DRAW_EDGES) {
            mpView.drawEdges(mp.getEdges());
        }
        if (path != null) {
            List<Vector> interpolatedPath = robot.interpolate(start, path, RESOLUTION);
            Vector finalConfiguration = interpolatedPath.get(interpolatedPath.size() - 1);
            System.out.println(String.format("Path is found with error %f", robot.getMetric(goal, finalConfiguration)));
            return interpolatedPath; 
        }
        System.out.print("Path not found");
        return null;
    }

    /**
     * Main function
     * @param args
     */
    public static void main(String[] args) {
       Application.launch(args);
    }

 }