package com.htmlweb.chess.common.dom.model.chess.piece;

import java.util.HashSet;
import java.util.Set;

import com.htmlweb.chess.common.dom.model.chess.PieceInitializer;
import com.htmlweb.chess.common.dom.model.chess.Point;
import com.htmlweb.chess.common.dom.model.chess.Side;
import com.htmlweb.chess.common.dom.model.chess.Point.Directions;
import com.htmlweb.chess.common.dom.model.chess.board.Board;

public class Rook extends Piece {
	public Rook(Point position, Side side) {
		super(position, side);
	}

	public static char WHITE_SYMBOL = 'R';
	public static char BLACK_SYMBOL = 'r';	
	
	@Override
	public Set<Point> getPossibleThreat(Board b) {
		Set<Point> possible = new HashSet<Point>();
		for(Directions d: Directions.PERPENDICULARS) {
			possible.addAll(b.findTargettablePointRun(this, d));
		}
		return possible;
	}
	
	@Override
	public char getCharacter() {
		return getSide().equals(Side.WHITE)?WHITE_SYMBOL:BLACK_SYMBOL;
	}

	@Override
	public int serialize() {
		int ser = hasMoved()?PieceInitializer.mR.ordinal():PieceInitializer.nmR.ordinal();
		ser <<=1;
		ser |= getSide().ordinal();
		return ser;
	}
	
}
