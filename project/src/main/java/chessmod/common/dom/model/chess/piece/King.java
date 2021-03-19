package chessmod.common.dom.model.chess.piece;

import java.util.HashSet;
import java.util.Set;

import chessmod.common.dom.model.chess.Point.Directions;
import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.board.Board.MoveType;
import chessmod.common.dom.model.chess.Move;
import chessmod.common.dom.model.chess.PieceType;
import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.Side;

public class King extends Piece {
	public King(Point position, Side side) {
		super(position, side);
	}

	public static char WHITE_SYMBOL = 'K';
	public static char BLACK_SYMBOL = 'k';
	
	@Override
	public Set<Move> getMovesFinalThreatNotwithstanding(Board b) throws InvalidMoveException {
		Set<Move> possible = super.getMovesFinalThreatNotwithstanding(b);
		
		if(!hasMoved()) {
			
			//Check left castle
			final Piece leftRook = b.pieceAt(Point.create(getPosition(), -4, 0));
			if(leftRook != null && !leftRook.hasMoved()) {
				b.findTargettablePoint(this, MoveType.MOVE_ONLY, -1, 0).forEach(
						p1 -> b.findTargettablePoint(this, MoveType.MOVE_ONLY, -2, 0).forEach(
								p2 -> b.findTargettablePoint(this, MoveType.MOVE_ONLY, -3, 0).forEach(
										p3 -> {
											//Check for threat on the point we pass over... post-move threat is checked elsewhere
											if(!b.isUnderThreatBy(getPosition().toX(-1), getSide().other()))
											possible.add(Move.createCastle(this, p2));
										}
										)
								)
						);
			}
			
			//Check right castle
			final Piece rightRook = b.pieceAt(Point.create(getPosition(), 3, 0));
			if(rightRook != null && !rightRook.hasMoved()) {
				b.findTargettablePoint(this, MoveType.MOVE_ONLY, 1, 0).forEach(
						p1 -> b.findTargettablePoint(this, MoveType.MOVE_ONLY, 2, 0).forEach(
								p2 -> {
									//Check for threat on the point we pass over... post-move threat is checked elsewhere
									if(!b.isUnderThreatBy(getPosition().toX(1), getSide().other()))
									possible.add(Move.createCastle(this, p2));
								}
								)
						);
			}
		}

		return possible;
	}

	@Override
	public Set<Point> getPossibleThreat(Board b) {
		Set<Point> possible = new HashSet<Point>();
		for(Directions d: Directions.values()) {
			possible.addAll(b.findTargettablePoint(this, MoveType.MOVE_OR_ATTACK, d));
		}
		return possible;
	}
	
	@Override
	public char getCharacter() {
		return getSide().equals(Side.WHITE)?WHITE_SYMBOL:BLACK_SYMBOL;
	}
	
	@Override
	public int serialize() {
		int ser = hasMoved()? PieceType.mK.ordinal(): PieceType.nmK.ordinal();
		ser <<=1;
		ser |= getSide().ordinal();
		return ser;
	}
	
	@Override
	public Move createMove(Board b, Point t) {
		if(getPosition().toX(-2).x == t.x || getPosition().toX(2).x == t.x) { //Castle
			return Move.createCastle(this, t);
		} else { //Regular Move
			return super.createMove(b, t);
		}
	}

}
