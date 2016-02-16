package chai;

import java.util.*;

import chesspresso.move.IllegalMoveException;
import chesspresso.move.Move;
import chesspresso.position.Position;

public class AlphaAI implements ChessAI {
    //==========GLOBAL VARIABLES=========
    private int AIplayer; //The number of the AI player (useful in AI vs. AI)
    int MaxVal = Integer.MAX_VALUE;
    int MinVal = Integer.MIN_VALUE;
    int Alpha = MinVal;
    int Beta = MaxVal;
    public final static int MAXIMUMDEPTH = 2; //maximum depth for the search
    short[] bestMovesArray = new short[MAXIMUMDEPTH]; //For storing the best move of different depths
    int[] bestMovesUtilArray = new int[MAXIMUMDEPTH]; //For storing the corresponding utility for best moves
    int bestMoveIndex = -1; //Index of the best move in the "bestMoveArray". Initialized to -1 as a flag.
    public int numNodes = 0; //Used for statistics purposes
    public final static int INITIALHASHSIZE = 1000000; //Used to initialize the Transportation table size
    public final static int MAXHASHSIZE = 6000000; //Used to limit the size of the transposition table
    public LinkedHashMap<Integer, Integer> TT = new LinkedHashMap<Integer, Integer>(INITIALHASHSIZE, 0.75f, true){
        protected boolean removeEldestEntry(Map.Entry eldest){
            return size() > MAXHASHSIZE;
        }};//Transposition Table
    //=======END OF GLOBAL VARIABLES======

    public short getMove(Position position) {
        AIplayer = position.getToPlay();// Setting the player number for the AIplayer
        return ID_AlphaBeta(position, MAXIMUMDEPTH);
    }

    //Checks to see if the current position is terminal or the maximum depth has exceeded
    private boolean isCutOff(Position position, int currentDepth, int maxDepth){
        return (position.isTerminal() || (currentDepth > maxDepth));
    }

    //return an int representing the value of the given position
    private int utility(Position position){
        if(!position.isTerminal()){//if not a terminal position, estimate
            return eval(position);
        }else{
            if(position.isStaleMate()){ //the this position is a draw
                return 0;
            }else{// if it is a mate && its the AI's turn, bad for AI, good for the other
                return (position.getToPlay() == AIplayer) ? MinVal : MaxVal;
            }
        }
    }

    //Estimates a value for the given position
    private int eval(Position position){ // I wrote this outside of utility() in case I wanted to have different eval() functions
        int index = (int)(position.getHashCode())%MAXHASHSIZE;
        if(TT.containsKey(index)) { //if the value is already in the hashtable return it
            return TT.get(index);
        }else{
            int posValue = position.getMaterial() + (int)(position.getDomination()); //calculate the position value
            posValue = (position.getToPlay() == AIplayer) ? posValue : -posValue; //adjust for AI or other player
            TT.put(index, posValue); //add to the transposition table
            return posValue;
        }
    }
    //===============================Alpha_Beta Methods===========================
    public short ID_AlphaBeta(Position oldposition, int maxDepth){
        Position position = new Position(oldposition);//cloning the position to resolve concurrency issues
        int currentDepth = 0; //starting depth

        for (int i=0; i<maxDepth; i++){
            numNodes=0;//reset
            TT.clear();//reset the transposition table
            bestMovesArray[i] = AlphaBeta(position, currentDepth+i);
            try {
                position.doMove(bestMovesArray[i]);
                bestMovesUtilArray[i] = utility(position);
                position.undoMove();
                bestMoveIndex = i;
                System.out.println("best move at depth " + i + " is: " + bestMovesArray[i] + " with utility: " + bestMovesUtilArray[i] + " after exploring " + numNodes + " nodes.");
            }catch (IllegalMoveException e){System.out.println("IllegalMoveException in ID_AlphaBeta");}

        }
        return bestMovesArray[bestMoveIndex];
    }

    //The AlphaBeta pruning algorithm with move reordering
    //If will check for the capturing moves before the non-capturing moves.
    public short AlphaBeta(Position oldposition, int maxDepth) {
        Position position = new Position(oldposition);// clone the position to resolve concurrency issues
        int maxValue = Integer.MIN_VALUE;
        short bestMove = Move.NO_MOVE;
        int currentDepth = 0;//initial depth for the search to start
        int newVal;

        if(bestMoveIndex != -1){//if there is a candidate for the best move from the previous depth check that first
           short candidateMove = bestMovesArray[bestMoveIndex];
            try {
                position.doMove(candidateMove);
                newVal = alpha_beta_min_value(position, Alpha, Beta, currentDepth, maxDepth);
                position.undoMove();
                if (newVal > maxValue) {
                    bestMove = candidateMove;
                    maxValue = newVal;
                }
            }catch (IllegalMoveException e){System.out.println("IllegalMoveException in AlphaBeta 1");}

        }

        //Now check the capturing moves
        for(short move : position.getAllCapturingMoves()){
            try {
                position.doMove(move);
                newVal = alpha_beta_min_value(position, Alpha, Beta, currentDepth, maxDepth);
                position.undoMove();
                if (newVal > maxValue) {
                    bestMove = move;
                    maxValue = newVal;
                }
            }catch (IllegalMoveException e){System.out.println("IllegalMoveException in AlphaBeta 2");}

        }

        //Now check the non capturing moves
        for(short move : position.getAllNonCapturingMoves()){
            try {
                position.doMove(move);
                newVal = alpha_beta_min_value(position, Alpha, Beta, currentDepth, maxDepth);
                position.undoMove();
                if (newVal > maxValue) {
                    bestMove = move;
                    maxValue = newVal;
                }
            }catch (IllegalMoveException e){System.out.println("IllegalMoveException in AlphaBeta 3");}

        }
        return bestMove;
    }


    private int alpha_beta_max_value(Position oldposition, int alpha, int beta, int currentDepth, int maxDepth) {
        Position position = new Position(oldposition);// clone the position to resolve concurrency issues
        if(isCutOff(position, currentDepth, maxDepth)){
            return utility(position);
        }
        int value = MinVal;
        for (short move : position.getAllMoves()){
            try {
                position.doMove(move);
                numNodes++; // for stats
                value = Math.max(value, alpha_beta_min_value(position, alpha, beta, currentDepth + 1, maxDepth));
                position.undoMove();
                if(value >= beta){
                    return value;
                }
                alpha = Math.max(alpha, value);
            }catch (IllegalMoveException e){System.out.println("IllegalMoveException in alpha_beta_max_value");}

        }
        return value;
    }

    private int alpha_beta_min_value(Position oldposition, int alpha, int beta, int currentDepth, int maxDepth) {
        Position position = new Position(oldposition);//clone the position to resolve concurrency issues
        if(isCutOff(position, currentDepth, maxDepth)){
            return utility(position);
        }
        int value = Integer.MAX_VALUE/2;
        for (short move : position.getAllMoves()){
            try {
                position.doMove(move);
                numNodes++; //for stats
                value = Math.min(value, alpha_beta_max_value(position, alpha, beta, currentDepth + 1, maxDepth));
                position.undoMove();
                if(value <= alpha){
                    return value;
                }
                beta = Math.min(beta, value);
            }catch(IllegalMoveException e){System.out.println("IllegalMoveException in alpha_beta_min_value");}
        }
        return value;
    }
}

//=============================================
//================UNUSED CODE==================
//=============================================
//============TTEntry Class============
//This class is used to hold information in the Transposition table
//    private class TTEntry{
//        Long zobrist;
//        int depth;
//        int utility;
//
//        public TTEntry(Long z, int d, int u){
//            zobrist = z;
//            depth = d;
//            utility = u;
//        }
//    }
//=====================================

//================================MINIMAX METHODS=============================
//public short ID_minimax(Position position, int maxDepth) {
//    int currentDepth = 0;
//    short bestMove = Move.NO_MOVE;
//    int bestMoveUtility = Integer.MIN_VALUE/2;
//    short tempMove;
//    int tempUtility;
//
//    for (int i=0; i<maxDepth; i++){
//        try {
//            tempMove = minimax(position, currentDepth+i);
//            position.doMove(tempMove);
//            tempUtility = utility(position);
//            System.out.println("bestmove at depth " + i + " is: " + tempMove + " with utility value of: " + tempUtility + " nodes explored: " + numNodes);
//            if(tempUtility > bestMoveUtility){
//                bestMove = tempMove;
//                bestMoveUtility = tempUtility;
//            }
//            position.undoMove();
//        }catch (IllegalMoveException e){
//            // Don't do anything!
//            // This means that at this depth the terminal state has already happened!
//        }
//    }
//    return bestMove;
//}
//
//    public short minimax(Position position, int maxDepth) throws IllegalMoveException {
//        int maxValue = Integer.MIN_VALUE;
//        int currentDepth = 0;
//        short bestMove = Move.NO_MOVE;
//        for(short possibleMove : position.getAllMoves()){
//            position.doMove(possibleMove);
//            numNodes++;
//            int newVal = min_value(position, currentDepth, maxDepth);
//            if (newVal > maxValue ){
//                bestMove = possibleMove;
//                maxValue = newVal;
//            }
//            position.undoMove();
//        }
////        System.out.println("Minimax: " + maxValue + " bestmove " + bestMove);
//        return bestMove;
//    }
//
//    public int max_value(Position position, int currentDepth, int maxDepth) throws IllegalMoveException {
////        System.out.println("max_value at depth: " + maxDepth);
//        if(isCutOff(position, currentDepth, maxDepth)){
////            System.out.println("Max_value returning: " + utility(position) + "Player: " + position.getToPlay());
//            return utility(position);
//        }
//        int value = Integer.MIN_VALUE/2;
//        for(short move : position.getAllMoves()){
//            position.doMove(move);
//            numNodes++;
//            value = Math.max(value,min_value(position, currentDepth+1, maxDepth));
//            position.undoMove();
//        }
//        return value;
//    }
//
//    public int min_value(Position position, int currentDepth, int maxDepth) throws IllegalMoveException {
////        System.out.println("min_value at depth: " + maxDepth);
//        if(isCutOff(position, currentDepth, maxDepth)){
////            System.out.println("Min_value returning: " + utility(position) + "Player: " + position.getToPlay());
//            return utility(position);
//        }
//        int value = Integer.MAX_VALUE/2;
//        for(short move : position.getAllMoves()){
//            position.doMove(move);
//            numNodes++;
//            value = Math.min(value, max_value(position, currentDepth+1, maxDepth));
//            position.undoMove();
//        }
//        return value;
//    }
//=========================================================================================

//    private Integer getUtilityFromTT(Position position, int currentDepth){
//        int hashIndex = (int)(position.getHashCode()%MAXHASHSIZE);
//        if(TT.containsKey(hashIndex)){// check if the position is in the TT
//            if(TT.get(hashIndex).zobrist == position.getHashCode()){//check if the right position is stored in that index (ignoring the zobrist hash collisions!)
//                if(TT.get(hashIndex).depth >= currentDepth){// if the position in the table is of higher quality (obtained from deeper plys))
//                    return TT.get(hashIndex).utility;
//                }
//            }
//        }
//        return null;
//    }
//
//    private void putUtilityinTT(Position position, int currentDepth, int utilVal){
//        int hashIndex = (int)(position.getHashCode()%MAXHASHSIZE);
//        if(!TT.containsKey(hashIndex)){ //if the position is not in the table, add it
//            TTEntry e = new TTEntry(position.getHashCode(), currentDepth, utilVal);
//            TT.put(hashIndex, e);
//        }else{ //if the position is in the table and is of less quality (lower depth), replace it
//            if (TT.get(hashIndex).depth < currentDepth) {//Not checking the zobrist hash (not checking if the position is the same)
//                TTEntry e = new TTEntry(position.getHashCode(), currentDepth, utilVal);
//                TT.put(hashIndex, e);
//            }
//        }
//    }

//============================================================================
