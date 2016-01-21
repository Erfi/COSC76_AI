package assignment_mazeworld;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import assignment_mazeworld.SearchProblem.SearchNode;
import assignment_mazeworld.GeneralMazeProblem.GeneralMazeNode;

/**
 * Created by erfi on 1/18/16.
 * Same as SimpleMazeDriver.java but for GeneralMazeProblems
 */
public class GeneralMazeDriver extends Application {

    Maze maze;

    // instance variables used for graphical display
    private static final int PIXELS_PER_SQUARE = 32;
    MazeView mazeView;
    List<AnimationPath> animationPathList;

    // some basic initialization of the graphics; needs to be done before
    //  runSearches, so that the mazeView is available
    private void initMazeView() {
        maze = Maze.readFromFile("simple.maz");

        animationPathList = new ArrayList<AnimationPath>();
        // build the board
        mazeView = new MazeView(maze, PIXELS_PER_SQUARE);

    }

    // assumes maze and mazeView instance variables are already available
    private void runSearches() {

        //to be used with GeneralMazeProblem
        int[][] starts = new int[][]{{5,6},{2,0},{3,1}};
        int[][] goals = new int[][]{{0,3},{5,6},{3,6}};


        GeneralMazeProblem genMazeProblem = new GeneralMazeProblem(maze, starts , goals);


        //==========Testing GeneralMazeProblem===========
        List<SearchNode> genAstarPath = genMazeProblem.astarSearch();
        System.out.println(genAstarPath);
        System.out.println("GeneralMaze A*: ");
        genMazeProblem.printStats();
//        int numBots = genMazeProblem.getNumAgents();
        animationPathList.add(new AnimationPath(mazeView, genAstarPath));

//        System.out.println(genMazeProblem.startNode); //start
//        System.out.println(genMazeProblem.startNode.goalTest());
//        System.out.println(genMazeProblem.startNode.getSuccessors());

    }


    public static void main(String[] args) {
        launch(args);
    }

    // javafx setup of main view window for mazeworld
    @Override
    public void start(Stage primaryStage) {

        initMazeView();

        primaryStage.setTitle("CS 76 Mazeworld");

        // add everything to a root stackpane, and then to the main window
        StackPane root = new StackPane();
        root.getChildren().add(mazeView);
        primaryStage.setScene(new Scene(root));

        primaryStage.show();

        // do the real work of the driver; run search tests
        runSearches();

        // sets mazeworld's game loop (a javafx Timeline)
        Timeline timeline = new Timeline(4); //The higher the number the faster it goes [sec/number --> wait]
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(.05), new GameHandler()));
        timeline.playFromStart();

    }

    // every frame, this method gets called and tries to do the next move
    //  for each animationPath.
    private class GameHandler implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent e) {
            // System.out.println("timer fired");
            for (AnimationPath animationPath : animationPathList) {
                // note:  animationPath.doNextMove() does nothing if the
                //  previous animation is not complete.  If previous is complete,
                //  then a new animation of a piece is started.
                animationPath.doNextMove();
            }
        }
    }

    // each animation path needs to keep track of some information:
    // the underlying search path, the "piece" object used for animation,
    // etc.
    private class AnimationPath {
        private Node[] pieces;
        private List<SearchProblem.SearchNode> searchPath;
        private int currentMove = 0;

        private int[] lastXs;
        private int[] lastYs;

        boolean animationDone = true;

        public AnimationPath(MazeView mazeView, List<SearchNode> path) {
            searchPath = path;
            GeneralMazeNode firstNode = (GeneralMazeNode) searchPath.get(0);
            int numBots = firstNode.getNumAgents(); //number of agents on the maze
            pieces = new Node[numBots];
            lastXs = new int[numBots];
            lastYs = new int[numBots];
            for(int i=0; i<numBots; i++){
                pieces[i] = mazeView.addPiece(firstNode.getXof(i), firstNode.getYof(i));
                lastXs[i] = firstNode.getXof(i);
                lastYs[i] = firstNode.getYof(i);
            }
//            piece = mazeView.addPiece(firstNode.getXof(0), firstNode.getYof(0));
//            lastX = firstNode.getXof(0);
//            lastY = firstNode.getYof(0);
        }

        // try to do the next step of the animation. Do nothing if
        // the mazeView is not ready for another step.
        public void doNextMove() {

            // animationDone is an instance variable that is updated
            //  using a callback triggered when the current animation
            //  is complete
            if (currentMove < searchPath.size() && animationDone) {
                GeneralMazeNode mazeNode = (GeneralMazeNode) searchPath
                        .get(currentMove);
                for(int i=0; i<mazeNode.getNumAgents(); i++){
                    int dx = mazeNode.getXof(i) - lastXs[i];
                    int dy = mazeNode.getYof(i) - lastYs[i];
                    animateMove(pieces[i], dx, dy);
                    lastXs[i] = mazeNode.getXof(i);
                    lastYs[i] = mazeNode.getYof(i);
                }
//                int dx = mazeNode.getXof(0) - lastX;
//                int dy = mazeNode.getYof(0) - lastY;
//                // System.out.println("animating " + dx + " " + dy);
//                animateMove(piece, dx, dy);
//                lastX = mazeNode.getXof(0);
//                lastY = mazeNode.getYof(0);

                currentMove++;
            }

        }

        // move the piece n by dx, dy cells
        public void animateMove(Node n, int dx, int dy) {
            animationDone = false;
            TranslateTransition tt = new TranslateTransition(
                    Duration.millis(300), n);
            tt.setByX(PIXELS_PER_SQUARE * dx);
            tt.setByY(-PIXELS_PER_SQUARE * dy);
            // set a callback to trigger when animation is finished
            tt.setOnFinished(new AnimationFinished());

            tt.play();

        }

        // when the animation is finished, set an instance variable flag
        //  that is used to see if the path is ready for the next step in the
        //  animation
        private class AnimationFinished implements EventHandler<ActionEvent> {
            @Override
            public void handle(ActionEvent event) {
                animationDone = true;
            }
        }
    }
}