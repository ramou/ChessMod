package chessmod.common.dom.model.chess.piece;

import java.util.HashSet;
import java.util.Set;

import chessmod.common.dom.model.chess.Move;
import chessmod.common.dom.model.chess.Move.PawnDoubleAdvance;
import chessmod.common.dom.model.chess.PieceInitializer;
import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.Point.InvalidPoint;
import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.board.Board.MoveType;
import chessmod.common.dom.model.chess.Side;

public class Pawn extends Piece {

	public static char WHITE_SYMBOL = 'P';
	public static char BLACK_SYMBOL = 'p';
	
	public Pawn(Point position, Side side) {
		super(position, side);
		
		this.singleAdvance = side.equals(Side.BLACK)?-1:1;
		this.doubleAdvance = side.equals(Side.BLACK)?-2:2;
	}

	int singleAdvance;
	public int getSingleAdvance() {
		return singleAdvance;
	}

	int doubleAdvance;
	
	public int getDoubleAdvance() {
		return doubleAdvance;
	}

	@Override
	public Set<Move> getMovesFinalThreatNotwithstanding(Board b) throws InvalidMoveException {
		Set<Move> possible = super.getMovesFinalThreatNotwithstanding(b);
		
		//Can they do a regular advance?
		b.findTargettablePoint(this, MoveType.MOVE_ONLY, 0, singleAdvance).forEach(
				p -> {
					if(p.y == (getSide().equals(Side.BLACK)?Point.MIN:Point.MAX)) {
						//Pawn Promotion
						possible.add(createPromotionMove(b, p, null));
					} else {
						possible.add(createMove(b, p));
					}
				}
				);
		
		//Can they do a double advance from their starting position?
		if(!hasMoved() && !possible.isEmpty()) b.findTargettablePoint(this, MoveType.MOVE_ONLY, 0, doubleAdvance).forEach(
				p -> possible.add(createMove(b, p))
				);

		//Check for En Passant
		if(!b.getMoves().isEmpty() && b.getMoves().get(b.getMoves().size()-1) instanceof PawnDoubleAdvance) {
			PawnDoubleAdvance p = (PawnDoubleAdvance)b.getMoves().get(b.getMoves().size()-1);
			Point behindPawn = p.getTarget().toY(singleAdvance);
			
			//If you can move, the last move *must* have been the other side.
			Point left = getPosition().toX(-1).toY(singleAdvance);
			if(!(left instanceof InvalidPoint) && left.equals(behindPawn))  {
				possible.add(createMove(b, left));
			} else {
				Point right = getPosition().toX(1).toY(singleAdvance);
				if(!(right instanceof InvalidPoint) && right.equals(behindPawn))  {
					possible.add(createMove(b, right));
				}
			}
		}
		
		return possible;
	}

	@Override
	public Set<Point> getPossibleThreat(Board b) {
		Set<Point> possible = new HashSet<Point>();
		//Can they attack?
		b.findTargettablePoint(this, MoveType.ATTACK_ONLY, -1, singleAdvance).forEach(
				p -> {
					if(b.pieceAt(p) != null) possible.add(p);
				});
		b.findTargettablePoint(this, MoveType.ATTACK_ONLY, +1, singleAdvance).forEach(
				p -> {
					if(b.pieceAt(p) != null) possible.add(p);
				});
		return possible;
	}

	@Override
	public char getCharacter() {
		return getSide().equals(Side.WHITE)?WHITE_SYMBOL:BLACK_SYMBOL;
	}

	@Override
	public int serialize() {
		int ser = PieceInitializer.P.ordinal();
		ser <<=1;
		ser |= getSide().ordinal();
		return ser;
	}

	@Override
	public Move createMove(Board b, Point t) {
		if(getPosition().x != t.x && b.isEmptySquare(t)) { //En Passant
			return Move.createEnPassant(this, t);
		} else if (!hasMoved() && getPosition().toY(getDoubleAdvance()).y == t.y) { //Double Advance 
			return Move.createPawnDoubleAdvance(this);
		} else if(t.y == (getSide().equals(Side.BLACK)?Point.MIN:Point.MAX)) { //Pawn Promotion
			return createPromotionMove(b, t, null);
		} else { //Regular Move
			return super.createMove(b, t);
		}
	}
	
	public Move createPromotionMove(Board b, Point t, Piece p) {
		return Move.createPawnPromotion(this, t, p);
	}
	
	@Override
	public boolean hasMoved() {
		return getSide().equals(Side.BLACK)?getPosition().y != 6:getPosition().y != 1;
	}
	
}
