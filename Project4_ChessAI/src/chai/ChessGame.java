package chai;

import chesspresso.Chess;
import chesspresso.move.IllegalMoveException;
import chesspresso.move.Move;
import chesspresso.position.Position;

public class ChessGame {

	public Position position;

	public int rows = 8;
	public int columns = 8;

	public ChessGame() {
	position = new Position(
//			"1r3k2/p1rR1P1p/6p1/2b5/2q5/2B4P/PP3QP1/3R2K1 w KQkq - 0 1"); // a puzzle from chess.com
			"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"); // original configuration
//			"2r2k2/8/8/8/8/8/K6r/8 w KQkq - 0 1"); //


	}

	public int getStone(int col, int row) {
		return position.getStone(Chess.coorToSqi(col, row));
	}
	
	public boolean squareOccupied(int sqi) {
		return position.getStone(sqi) != 0;
		
	}

	public boolean legalMove(short move) {
		
		for(short m: position.getAllMoves()) {
			if(m == move) return true;
		}
		System.out.println(java.util.Arrays.toString(position.getAllMoves()));
		System.out.println(move);
		return false;
	
	}

	// find a move from the list of legal moves from fromSqi to toSqi
	// return 0 if none available
	public short findMove(int fromSqi, int toSqi) {
		
		for(short move: position.getAllMoves()) {
			if(Move.getFromSqi(move) == fromSqi && 
					Move.getToSqi(move) == toSqi) return move;
		}
		return 0;
	}
	
	public void doMove(short move) {
		try {
			System.out.println("making move " + move);
			position.doMove(move);
			System.out.println(position);
		} catch (IllegalMoveException e) {
			System.out.println("illegal move!");
		}
	}

	public static void main(String[] args) throws IllegalMoveException {
		System.out.println();

		// Create a starting position using "Forsyth–Edwards Notation". (See
		// Wikipedia.)
		Position position = new Position(
				"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		System.out.println("initial --> " + position);
		short move = position.getAllMoves()[1];
		position.doMove(move);
//		for (short move: moves) {
//			System.out.println(move);
//			int nextSqr = Move.getFromSqi(move);
//			System.out.println(nextSqr);
//		}

		System.out.println("after --> " + position);
//		System.out.println(position.getToPlay());

	}
	
	

}
