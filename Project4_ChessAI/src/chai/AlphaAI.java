package chai;

import java.util.Random;

import chesspresso.move.IllegalMoveException;
import chesspresso.move.Move;
import chesspresso.position.Position;

public class AlphaAI implements ChessAI {
    private final static int MAXIMUMDEPTH = 1; //maximum depth for the search

    public short getMove(Position position) {
        short move = 0;
        try{
             move = minimax(position);
        } catch (IllegalMoveException e) {
            e.printStackTrace();
        }
        return move;
    }

    public boolean isCutOff(Position position, int depth){
        if(position.isTerminal()){
            return true;
        }else if(depth >= MAXIMUMDEPTH){
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
            return new Random().nextInt(Integer.MAX_VALUE/2 - Integer.MIN_VALUE/2) + Integer.MIN_VALUE/2;
        }
    }

    public short minimax(Position position) throws IllegalMoveException {
        int maxValue = Integer.MIN_VALUE/2;
        short bestMove = 0;
        for(short move : position.getAllMoves()){
            position.doMove(move);
            if (min_value(position) > maxValue ){
                bestMove = move;
            }else{
                position.undoMove();
            }
        }
        return bestMove;
    }

    public int max_value(Position position) throws IllegalMoveException {
        if(isCutOff(position, MAXIMUMDEPTH)){
            return utility(position);
        }
        int value = Integer.MIN_VALUE/2;
        for(short move : position.getAllMoves()){
            position.doMove(move);
            value = Math.max(value,min_value(position));
            position.undoMove();
        }
        return value;
    }

    public int min_value(Position position) throws IllegalMoveException {
        if(isCutOff(position, MAXIMUMDEPTH)){
            return utility(position);
        }
        int value = Integer.MAX_VALUE/2;
        for(short move : position.getAllMoves()){
            position.doMove(move);
            value = Math.min(value, max_value(position));
            position.undoMove();
        }
        return value;
    }

}
