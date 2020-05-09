package com.htmlweb.chess.common.dom.model.chess.piece;

import java.util.HashSet;
import java.util.Set;

import com.htmlweb.chess.common.dom.model.chess.PieceInitializer;
import com.htmlweb.chess.common.dom.model.chess.Point;
import com.htmlweb.chess.common.dom.model.chess.Point.Directions;
import com.htmlweb.chess.common.dom.model.chess.board.Board;
import com.htmlweb.chess.common.dom.model.chess.Side;


public class Bishop extends Piece {

	public Bishop(Point position, Side side) {
		super(position, side);
	}

	public static char WHITE_SYMBOL = 'B';
	public static char BLACK_SYMBOL = 'b';
	
	@Override
	public char getCharacter() {
		return getSide().equals(Side.WHITE)?WHITE_SYMBOL:BLACK_SYMBOL;
	}

	@Override
	public Set<Point> getPossibleThreat(Board b) {
		Set<Point> possible = new HashSet<Point>();
		for(Directions d: Directions.DIAGONALS) {
			possible.addAll(b.findTargettablePointRun(this, d));
		}
		return possible;
	}

	@Override
	public int serialize() {
		int ser = PieceInitializer.B.ordinal();
		ser <<=1;
		ser |= getSide().ordinal();
		return ser;
	}

	
}
