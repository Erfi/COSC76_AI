package chai;

import java.util.Random;

import chesspresso.move.IllegalMoveException;
import chesspresso.move.Move;
import chesspresso.position.Position;

public class AlphaAI implements ChessAI {
    private final static int MAXIMUMDEPTH = 4; //maximum depth for the search

    public short getMove(Position position) {
        short move = Move.NO_MOVE;
        try{
             move = ID_minimax(position, MAXIMUMDEPTH);
        } catch (IllegalMoveException e) {
            e.printStackTrace();
        }
        return move;
    }

    public boolean isCutOff(Position position, int currentDepth, int maxDepth){
        if(position.isTerminal()){
            return true;
        }else if(currentDepth > maxDepth){
            return true;
        }else{
            return false;
        }
    }

    public int utility(Position position){
        if(position.isStaleMate()){ // draw
            return 0;
        }else if(position.isTerminal()){ // terminal state
            if(position.getToPlay() == 0){
                return Integer.MAX_VALUE/2;
            }else{
                return  Integer.MIN_VALUE/2;
            }
        }else{//not a terminal state or draw --> return a value between max(ish) and min(ish)
//            int val = new Random().nextInt(Integer.MAX_VALUE/2 - Integer.MIN_VALUE/2) + Integer.MIN_VALUE/2;
            return eval(position);
        }
    }

    public int eval(Position position){ // I wrote this outside of utility() in case I wanted to have different eval() funcitions
        return position.getMaterial();
    }


    public short ID_minimax(Position position, int maxDepth) throws IllegalMoveException {
        int currentDepth = 0;
        short bestMove = Move.NO_MOVE;
        int bestMoveUtility = Integer.MIN_VALUE/2;
        short tempMove;
        int tempUtility;

        for (int i=0; i<maxDepth; i++){
            tempMove = minimax(position, currentDepth+i);

            position.doMove(tempMove);
            tempUtility = utility(position);
            System.out.println("bestmove at depth " + i + " is: " + tempMove + " with utility value of: " + tempUtility);
            if(tempUtility > bestMoveUtility){
                bestMove = tempMove;
            }
            position.undoMove();
        }
        return bestMove;
    }

    public short minimax(Position position, int maxDepth) throws IllegalMoveException {
        int maxValue = Integer.MIN_VALUE/2;
        int currentDepth = 0;
        short bestMove = Move.NO_MOVE;
        for(short move : position.getAllMoves()){
            position.doMove(move);
            int newVal = min_value(position, currentDepth, maxDepth);
            if (newVal > maxValue ){
                bestMove = move;
                maxValue = newVal;
            }
            position.undoMove();
        }
//        System.out.println("Minimax: " + maxValue + " bestmove " + bestMove);
        return bestMove;
    }

    public int max_value(Position position, int currentDepth, int maxDepth) throws IllegalMoveException {
//        System.out.println("max_value at depth: " + maxDepth);
        if(isCutOff(position, currentDepth, maxDepth)){
//            System.out.println("Max_value returning: " + utility(position) + "Player: " + position.getToPlay());
            return utility(position);
        }
        int value = Integer.MIN_VALUE/2;
        for(short move : position.getAllMoves()){
            position.doMove(move);
            value = Math.max(value,min_value(position, currentDepth+1, maxDepth));
            position.undoMove();
        }
        return value;
    }

    public int min_value(Position position, int currentDepth, int maxDepth) throws IllegalMoveException {
//        System.out.println("min_value at depth: " + maxDepth);
        if(isCutOff(position, currentDepth, maxDepth)){
//            System.out.println("Min_value returning: " + utility(position) + "Player: " + position.getToPlay());
            return utility(position);
        }
        int value = Integer.MAX_VALUE/2;
        for(short move : position.getAllMoves()){
            position.doMove(move);
            value = Math.min(value, max_value(position, currentDepth+1, maxDepth));
            position.undoMove();
        }
        return value;
    }
}
