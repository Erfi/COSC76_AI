package assignment_mazeworld;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import assignment_mazeworld.BlindMazeProblem.BlindMazeNode;

public class BlindMazeDriver extends Application {

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
        HashSet<int[]> start = new HashSet<int[]>();
        //initialize the belief state to the maze locations except the walls
        for(int i=0; i<maze.getSize()[0]; i++) { //for the width of the maze
            for (int j = 0; j < maze.getSize()[1]; j++) {// for the height of the maze
                if(maze.isLegal(i,j)){
                    start.add(new int[]{i, j});
                }
            }
        }

        int gx = 2;
        int gy = 2;


        BlindMazeProblem mazeProblem = new BlindMazeProblem(maze,start, gx,
                gy);

//        List<SearchNode> bfsPath = mazeProblem.breadthFirstSearch();
//        animationPathList.add(new AnimationPath(mazeView, bfsPath));
//        System.out.println("BFS:  ");
//        mazeProblem.printStats();
//
//        List<SearchNode> dfsPath = mazeProblem
//                .depthFirstPathCheckingSearch(5000);
//        animationPathList.add(new AnimationPath(mazeView, dfsPath));
//        System.out.println("DFS:  ");
//        mazeProblem.printStats();
//
//        List<SearchNode> astarPath = mazeProblem.astarSearch();
//        animationPathList.add(new AnimationPath(mazeView, astarPath));
//        System.out.println("A*:  ");
//        mazeProblem.printStats();
//
//        List<SearchNode> uniformCostPath = mazeProblem.UniformCostSearch();
//        animationPathList.add(new AnimationPath(mazeView, uniformCostPath));
//        System.out.println("Uniform-Cost:  ");
//        mazeProblem.printStats();

        List<SearchNode> blindAstarPath = mazeProblem.astarSearch();
        animationPathList.add(new AnimationPath(mazeView, blindAstarPath));
        System.out.println("BlindAstar:  ");
        mazeProblem.printStats();
        System.out.println(blindAstarPath);
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
        Timeline timeline = new Timeline(2); //The higher the number the faster it goes [sec/number --> wait]
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(.05), new GameHandler()));
        timeline.playFromStart();

    }

//    // every frame, this method gets called and tries to do the next move
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
            BlindMazeNode firstNode = (BlindMazeNode) searchPath.get(0);
            int beliefSize = firstNode.beliefState.size(); //size of the belief state
            pieces = new Node[beliefSize];
            lastXs = new int[beliefSize];
            lastYs = new int[beliefSize];
            int index = 0;
            for(int[] loc : firstNode.beliefState){// for all possible locations of the agent
                pieces[index] = mazeView.addPiece(loc[0], loc[1]);
                lastXs[index] = loc[0];
                lastYs[index] = loc[1];
                index += 1;
            }
        }

        // try to do the next step of the animation. Do nothing if
        // the mazeView is not ready for another step.
        public void doNextMove() {

            // animationDone is an instance variable that is updated
            //  using a callback triggered when the current animation
            //  is complete
            if (currentMove < searchPath.size() && animationDone) {
                BlindMazeNode mazeNode = (BlindMazeNode) searchPath
                        .get(currentMove);
                int index = 0;
                for(int[] loc : mazeNode.beliefState){
                    int dx = loc[0] - lastXs[index];
                    int dy = loc[1] - lastYs[index];
                    animateMove(pieces[index], dx, dy);
                    lastXs[index] = loc[0];
                    lastYs[index] = loc[1];
                    index += 1;
                }
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
