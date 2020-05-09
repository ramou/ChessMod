package com.htmlweb.chess.common.dom.model.chess.piece;

import java.util.HashSet;
import java.util.Set;

import com.htmlweb.chess.common.dom.model.chess.PieceInitializer;
import com.htmlweb.chess.common.dom.model.chess.Point;
import static com.htmlweb.chess.common.dom.model.chess.Point.Directions.*;
import com.htmlweb.chess.common.dom.model.chess.Side;
import com.htmlweb.chess.common.dom.model.chess.board.Board;
import com.htmlweb.chess.common.dom.model.chess.board.Board.MoveType;

public class Knight extends Piece {
	public Knight(Point position, Side side) {
		super(position, side);
	}

	public static char WHITE_SYMBOL = 'N';
	public static char BLACK_SYMBOL = 'n';

	
	@Override
	public Set<Point> getPossibleThreat(Board b) {
		Set<Point> possible = new HashSet<Point>();
		possible.addAll(b.findTargettablePoint(this, MoveType.MOVE_OR_ATTACK, NORTH, NORTH_WEST));
		possible.addAll(b.findTargettablePoint(this, MoveType.MOVE_OR_ATTACK, NORTH, NORTH_EAST));
		possible.addAll(b.findTargettablePoint(this, MoveType.MOVE_OR_ATTACK, EAST, NORTH_EAST));
		possible.addAll(b.findTargettablePoint(this, MoveType.MOVE_OR_ATTACK, EAST, SOUTH_EAST));
		possible.addAll(b.findTargettablePoint(this, MoveType.MOVE_OR_ATTACK, SOUTH, SOUTH_EAST));
		possible.addAll(b.findTargettablePoint(this, MoveType.MOVE_OR_ATTACK, SOUTH, SOUTH_WEST));
		possible.addAll(b.findTargettablePoint(this, MoveType.MOVE_OR_ATTACK, WEST, SOUTH_WEST));
		possible.addAll(b.findTargettablePoint(this, MoveType.MOVE_OR_ATTACK, WEST, NORTH_WEST));
		return possible;
	}
	
	@Override
	public char getCharacter() {
		return getSide().equals(Side.WHITE)?WHITE_SYMBOL:BLACK_SYMBOL;
	}

	@Override
	public int serialize() {
		int ser = PieceInitializer.N.ordinal();
		ser <<=1;
		ser |= getSide().ordinal();
		return ser;
	}

}
