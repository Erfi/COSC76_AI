package chai;

import java.util.Random;

import chesspresso.move.Move;
import chesspresso.position.Position;

public class AlphaAI implements ChessAI {
    private final static int MAXIMUMDEPTH = 5; //maximum depth for the search

    public short getMove(Position position) {
        short [] moves = position.getAllMoves();
        short move = moves[new Random().nextInt(moves.length)];

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

    public short minimax(Position position){
        int maxValue = Integer.MIN_VALUE/2;
        short bestMove = 0;
        for(short move : position.getAllMoves()){
//            position.doMove(move);
//            if (min_value(position) > )
        }
        return 0;
    }

    public int max_value(Position position){
        return 0;
    }

    public int min_value(Position position){
        return 0;
    }

}
