package chai;

import java.util.*;

import chesspresso.move.IllegalMoveException;
import chesspresso.move.Move;
import chesspresso.position.Position;

public class AlphaAI implements ChessAI {
    //============TTEntry Class============
    //This class is used to hold information in the Transposition table
    private class TTEntry{
        Long zobrist;
        int depth;
        int utility;

        public TTEntry(Long z, int d, int u){
            zobrist = z;
            depth = d;
            utility = u;
        }
    }
    //=====================================
    private final static int MAXIMUMDEPTH = 6; //maximum depth for the search
    private int numNodes = 0;
    private final static int INITIALHASHSIZE = 100; //10^2
    private final static int MAXHASHSIZE = 10000;//10^4
    private LinkedHashMap<Integer, TTEntry> TT = new LinkedHashMap<Integer, TTEntry>(INITIALHASHSIZE, 0.75f, true){
        protected boolean removeEldestEntry(Map.Entry eldest){
            return size() > MAXHASHSIZE;
        }
    };//Transposition Table
//    private LinkedHashMap<Integer, double[]> TT = new LinkedHashMap<Integer, double[]>(INITIALHASHSIZE, 0.75f, true){
//    protected boolean removeEldestEntry(Map.Entry eldest){
//        return size() > MAXHASHSIZE;
//        }
//    };//Transposition Table

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

    private int eval(Position position){ // I wrote this outside of utility() in case I wanted to have different eval() functions
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
                numNodes++; //for stats
                tempUtility = utility(position);
                System.out.println("bestmove at depth " + i + " is: " + tempMove + " with utility value of: " + tempUtility + " nodes explored: " + numNodes);
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

                //Check with the Transposition Table
                Integer tempVal = getUtilityFromTT(position, currentDepth);
                if(tempVal != null){
                    value = tempVal;
                }else {
                    value = Math.max(value, alpha_beta_min_value(position, alpha, beta, currentDepth + 1, maxDepth));
                    //Put this value in the Transposition Table
                    putUtilityinTT(position, currentDepth, value);
                }

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

                //Check with the Transposition Table
                Integer tempVal = getUtilityFromTT(position, currentDepth);
                if(tempVal != null){
                    value = tempVal;
                }else {
                    value = Math.min(value, alpha_beta_max_value(position, alpha, beta, currentDepth + 1, maxDepth));
                    //Put this value in the Transposition Table
                    putUtilityinTT(position, currentDepth, value);
                }

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

//    private Integer getUtilityFromTT(Position position, int currentDepth){
//        int hashIndex = (int)(position.getHashCode()%MAXHASHSIZE);
//        if(TT.containsKey(hashIndex)){// check if the position is in the TT
//            if((long)(TT.get(hashIndex)[0]) == position.getHashCode()){//check if the right position is stored in that index (ignoring the zobrist hash collisions!)
//                if((int)(TT.get(hashIndex)[1]) >= currentDepth){// if the position in the table is of higher quality (obtained from deeper plys))
//                    return (int)(TT.get(hashIndex)[2]);
//                }
//            }
//        }
//        return null;
//    }
//
//    private void putUtilityinTT(Position position, int currentDepth, int utilVal){
//        int hashIndex = (int)(position.getHashCode()%MAXHASHSIZE);
//        if(!TT.containsKey(hashIndex)){ //if the position is not in the table, add it
//            double[] e = new double[]{position.getHashCode(), currentDepth, utilVal};
////            TTEntry e = new TTEntry(position.getHashCode(), currentDepth, utilVal);
//            TT.put(hashIndex, e);
//        }else{ //if the position is in the table and is of less quality (lower depth), replace it
//            if ((int)(TT.get(hashIndex)[1]) < currentDepth) {//Not checking the zobrist hash (not checking if the position is the same)
////                TTEntry e = new TTEntry(position.getHashCode(), currentDepth, utilVal);
//                double[] e = new double[]{position.getHashCode(), currentDepth, utilVal};
//                TT.put(hashIndex, e);
//            }
//        }
//    }

    private Integer getUtilityFromTT(Position position, int currentDepth){
        int hashIndex = (int)(position.getHashCode()%MAXHASHSIZE);
        if(TT.containsKey(hashIndex)){// check if the position is in the TT
            if(TT.get(hashIndex).zobrist == position.getHashCode()){//check if the right position is stored in that index (ignoring the zobrist hash collisions!)
                if(TT.get(hashIndex).depth >= currentDepth){// if the position in the table is of higher quality (obtained from deeper plys))
                    return TT.get(hashIndex).utility;
                }
            }
        }
        return null;
    }

    private void putUtilityinTT(Position position, int currentDepth, int utilVal){
        int hashIndex = (int)(position.getHashCode()%MAXHASHSIZE);
        if(!TT.containsKey(hashIndex)){ //if the position is not in the table, add it
            TTEntry e = new TTEntry(position.getHashCode(), currentDepth, utilVal);
            TT.put(hashIndex, e);
        }else{ //if the position is in the table and is of less quality (lower depth), replace it
            if (TT.get(hashIndex).depth < currentDepth) {//Not checking the zobrist hash (not checking if the position is the same)
                TTEntry e = new TTEntry(position.getHashCode(), currentDepth, utilVal);
                TT.put(hashIndex, e);
            }
        }
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
