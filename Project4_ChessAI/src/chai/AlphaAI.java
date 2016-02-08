package chai;

import java.util.Random;

import chesspresso.move.IllegalMoveException;
import chesspresso.move.Move;
import chesspresso.position.Position;

public class AlphaAI implements ChessAI {
    private final static int MAXIMUMDEPTH = 6; //maximum depth for the search

    public short getMove(Position position) {
        short move = Move.NO_MOVE;
//        move = ID_minimax(position, MAXIMUMDEPTH);
        move = ID_AlphaBeta(position, MAXIMUMDEPTH);
//        move = AlphaBeta(position, MAXIMUMDEPTH);
        return move;
    }

    private boolean isCutOff(Position position, int currentDepth, int maxDepth){
        if(position.isTerminal()){
            return true;
        }else if(currentDepth > maxDepth){
            return true;
        }else{
            return false;
        }
    }

    private int utility(Position position){

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

    private int eval(Position position){ // I wrote this outside of utility() in case I wanted to have different eval() funcitions
        int result;
        if(position.getToPlay()==0){//white
            result = (int) (-position.getMaterial() + -(position.getDomination()+0.5));
            if(position.isCheck()){
                result -= 50; // rewarding the check moves (50 is arbitrary!)
            }
        }else{
            result = (int) (position.getMaterial() + (position.getDomination()+0.5));
            if(position.isCheck()) {
                result += 50;
            }
        }
//        result = (int) (position.getMaterial() + (position.getDomination()+0.5));
//        if(position.isCheck()) {
//            result += (position.getToPlay() == 0) ? 50 : -50;
//        }
        return result;//(position.getToPlay()==0) ? -position.getMaterial() : position.getMaterial();
    }
    //===============================Alpha_Beta Methods===========================
    public short ID_AlphaBeta(Position position, int maxDepth){
        int currentDepth = 0;
        short bestMove = Move.NO_MOVE;
        int bestMoveUtility = Integer.MIN_VALUE/2;
        short tempMove;
        int tempUtility;

        for (int i=0; i<maxDepth; i++){
            try {
                tempMove = AlphaBeta(position, currentDepth+i);
                position.doMove(tempMove);
                tempUtility = utility(position);
                System.out.println("bestmove at depth " + i + " is: " + tempMove + " with utility value of: " + tempUtility);
                if(tempUtility > bestMoveUtility){
                    bestMove = tempMove;
                    bestMoveUtility = tempUtility;
                }
                position.undoMove();
            }catch (IllegalMoveException e){
                // Don't do anything!
                // This means that at this depth the terminal state has already happened!
            }
        }
        return bestMove;
    }

    public short AlphaBeta(Position position, int maxDepth) {
        int maxValue = Integer.MIN_VALUE;
        int currentDepth = 0;
        short bestMove = Move.NO_MOVE;
        for(short move : position.getAllMoves()){
            try {
                position.doMove(move);
                int newVal = alpha_beta_max_value(position, Integer.MIN_VALUE / 2, Integer.MAX_VALUE / 2, currentDepth, maxDepth);
                if (newVal > maxValue) {
                    bestMove = move;
                    maxValue = newVal;
                }
                position.undoMove();
            }catch(IllegalMoveException e){
                System.out.println("IllegalMoveException in AlphaBeta");
            }
        }
        return bestMove;
    }

    private int alpha_beta_max_value(Position position, int alpha, int beta, int currentDepth, int maxDepth) {
        if(isCutOff(position, currentDepth, maxDepth)){
            return utility(position);
        }
        int value = Integer.MIN_VALUE/2;
//        int Alpha = alpha;
//        int Beta = beta;
        for (short move : position.getAllMoves()){
            try{
                position.doMove(move);
                value = Math.max(value, alpha_beta_min_value(position, alpha, beta, currentDepth+1, maxDepth));
                position.undoMove();
//                System.out.println("max_value: " + value);
                if(value >= beta){
                    return value;
                }
                alpha = Math.max(alpha, value);
            }catch(IllegalMoveException e){
                System.out.println("IllegalMoveException in alpha_beta_max_value");
            }
        }
        return value;
    }

    private int alpha_beta_min_value(Position position, int alpha, int beta, int currentDepth, int maxDepth) {
        if(isCutOff(position, currentDepth, maxDepth)){
            return utility(position);
        }
        int value = Integer.MAX_VALUE/2;
//        int Alpha = alpha;
//        int Beta = beta;
        for (short move : position.getAllMoves()){
            try{
                position.doMove(move);
                value = Math.min(value, alpha_beta_max_value(position, alpha, beta, currentDepth+1, maxDepth));
                position.undoMove();
//                System.out.println("min_value: " + value);
                if(value <= alpha){
                    return value;
                }
                beta = Math.min(beta, value);
            }catch (IllegalMoveException e){
                System.out.println("IllegalMoveException in alpha_beta_min_value");
            }
        }
        return value;
    }

    //============================================================================


    //================================MINIMAX METHODS=============================
    public short ID_minimax(Position position, int maxDepth) {
        int currentDepth = 0;
        short bestMove = Move.NO_MOVE;
        int bestMoveUtility = Integer.MIN_VALUE/2;
        short tempMove;
        int tempUtility;

        for (int i=0; i<maxDepth; i++){
            try {
                tempMove = minimax(position, currentDepth+i);
                position.doMove(tempMove);
                tempUtility = utility(position);
                System.out.println("bestmove at depth " + i + " is: " + tempMove + " with utility value of: " + tempUtility);
                if(tempUtility > bestMoveUtility){
                    bestMove = tempMove;
                    bestMoveUtility = tempUtility;
                }
                position.undoMove();
            }catch (IllegalMoveException e){
                // Don't do anything!
                // This means that at this depth the terminal state has already happened!
            }
        }
        return bestMove;
    }

    public short minimax(Position position, int maxDepth) throws IllegalMoveException {
        int maxValue = Integer.MIN_VALUE;
        int currentDepth = 0;
        short bestMove = Move.NO_MOVE;
        for(short possibleMove : position.getAllMoves()){
            position.doMove(possibleMove);
            int newVal = min_value(position, currentDepth, maxDepth);
            if (newVal > maxValue ){
                bestMove = possibleMove;
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
    //=========================================================================================
}
