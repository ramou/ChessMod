package chessmod.common.dom.model.chess;

import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.piece.King;
import chessmod.common.dom.model.chess.piece.Pawn;
import chessmod.common.dom.model.chess.piece.Piece;

/**
 * 
 * This is to track moves, and possibly to be used by something that serializes the communication of moves. This does not do or validate moves, that should be taken care of by pieces.
 * 
 * @author Stuart Thiel
 *
 */
public class Move {
	private Point source;
	private Point target;
	public Point getSource() {
		return source;
	}
	public Point getTarget() {
		return target;
	}
	protected Move(Point source, Point target) {
		super();
		this.source = source;
		this.target = target;
	}
	
	public static Move create(final int serialized, Board b) {
		Point source = Point.create((serialized>>>10)&0b111111);
		Point target = Point.create((serialized>>>4) &0b111111);
		//Check if it's a special case
		if((serialized & 0b1111) > 0) {
			//It's a special case
			if(source.y == target.y) { //Castle
				return createCastle(source, target);
			} else if (target.y == Point.MIN || target.y == Point.MAX) { //Promotion {
				Piece p = PieceType.create(target, serialized);
				return createPawnPromotion(source, target, p);
			} else if (Math.abs(source.y-target.y) == 2) { //Double Pawn Advance
				return createPawnDoubleAdvance(source, target);
			} else { //Break it down further later, for now, En Passant!
				return createEnPassant(source, target);
			} //Check if it's a super-special case (impossible source/target combo)
			//TODO: Implement super-special cases		
		} else {
			return create(source, target);
		}
	}
	
	public static Move create(Point source, Point target) {
		return new Move(source, target);
	}
	
	public static Move createCastle(King k, Point target) {
		Point source = k.getPosition();
		return createCastle(source, target);
	}
	public static Move createCastle(Point source, Point target) {
		return new Castle(source, target);
	}

	public static Move createPawnDoubleAdvance(Pawn p) {
		Point source = p.getPosition();
		Point target = Point.create(source, 0, p.getDoubleAdvance());
		return createPawnDoubleAdvance(source, target);
	}
	
	public static Move createPawnDoubleAdvance(Point source, Point target) {
		return new PawnDoubleAdvance(source, target);
	}
	
	public static Move createPawnPromotion(Pawn p, Point target, Piece promotion) {
		Point source = p.getPosition();
		return createPawnPromotion(source, target, promotion);
	}
	
	public static Move createPawnPromotion(Point source, Point target, Piece promotion) {
		return new PawnPromotion(source, target, promotion);
	}

	public static Move createEnPassant(Pawn p, Point target) {
		Point source = p.getPosition();
		return createEnPassant(source, target);
	}
	
	public static Move createEnPassant(Point source, Point target) {
		return new EnPassant(source, target);
	}
	
	public void doMove(Board b) {
		Piece p = b.pieceAt(getSource());
		p.setMoved(true);
		b.setPiece(null, getSource());
		b.setPiece(p, getTarget());
	}

	
	@Override
	public boolean equals(Object obj) {
		return obj != null &&
				this.getClass() == obj.getClass() &&
						this.source.equals(((Move)obj).source) &&
						this.target.equals(((Move)obj).target);
	}
	
	@Override
	public String toString() {
		return "Move from " + source + " to " + target;
	}
	
	@Override
	public int hashCode() {
		return serialize();
	}
	
	@SpecialCase
	public static class PawnDoubleAdvance extends Move {
		private PawnDoubleAdvance(Point source, Point target) {
			super(source, target);
		}
		
		@Override
		public int serialize() {
			return super.serialize() | 0b0001;
		}
		
		@Override
		public String toString() {
			return super.toString()+" performing Pawn Double Advance";
		}
	}
	
	@SpecialCase
	public static class PawnPromotion extends Move {
		Piece promotion;

		public Piece getPromotion() {
			return promotion;
		}

		private PawnPromotion(Point source, Point target, Piece promotion) {
			super(source, target);
			this.promotion = promotion;
		}
		
		@Override
		public void doMove(Board b) {
			super.doMove(b);
			promotion.setPosition(getTarget());
			b.setPiece(promotion, getTarget());
		}
		
		@Override
		public int serialize() {
			return super.serialize() | ((promotion==null)?0:promotion.serialize());
		}
		
		@Override
		public int hashCode() {
			return super.serialize();
		}
		
		@Override
		public boolean equals(Object obj) {
			return super.equals(obj) && ((promotion==null) || ((PawnPromotion)obj).promotion == null ||
					promotion.getSide().equals(((PawnPromotion)obj).promotion.getSide()) && 
					promotion.getClass().equals(((PawnPromotion)obj).promotion.getClass()));
		}
		
		@Override
		public String toString() {
			return super.toString()+" promoting to " + (promotion==null?"unknown":promotion.getClass().getSimpleName());
		}
	}

	@SpecialCase
	public static class EnPassant extends Move {

		private EnPassant(Point source, Point target) {
			super(source, target);
		}
		
		@Override
		public void doMove(Board b) {
			super.doMove(b);
			Pawn p = (Pawn)b.pieceAt(getTarget());
			Point pointToClear = Point.create(getTarget(), 0, -p.getSingleAdvance());
			b.setPiece(null, pointToClear);
		}
		
		@Override
		public int serialize() {
			return super.serialize() | 0b0001;
		}
		
		@Override
		public String toString() {
			return super.toString()+" performing En Passant";
		}
	}
	
	
	@SpecialCase
	public static class Castle extends Move {
		private Castle(Point source, Point target) {
			super(source, target);
		}
		
		@Override
		public void doMove(Board b) {
			super.doMove(b);
			
			boolean kingCastle = getSource().x < getTarget().x;
			Point oldRookPos = Point.create(kingCastle?Point.MAX:Point.MIN, getTarget().y);
			Point newRookPos = getTarget().toX(kingCastle?-1:1);
			b.setPiece(b.pieceAt(oldRookPos), newRookPos);
			b.setPiece(null, oldRookPos);
			
		}
		
		@Override
		public String toString() {
			return super.toString()+" performing Castling ";
		}
		
		@Override
		public int serialize() {
			return super.serialize() | 0b0001;
		}
		
	}
	
/*
 * 
 * Moves
 * Three-bit sourceX
 * Three-bit sourceY
 * Three-bit targetX
 * Three-bit targetY
 * Four-bit special
 * 
 * The last four bits are context-dependent. Aside from the special case codes that cannot be moves
 * we can identify a promotion and the piece that was promoted to, en passant, double pawn advancement and castling.
 * The last bit will indicate one of these special cases, with the position relative from source and target distinguishing
 * between those three options
 * 
 * If the first 12 bits are regular moves, then:
 * If the last four bits are 0001 and the move starts and ends on the same row, then it's something castling (or a problem move)
 * If the last four bits are non-zero and the move targets a promotion row but isn't a castle, it's a pawn promotion
 * If the last four bits are 0001 and there is a difference in the y position of two, it's a pawn double advance
 * If the last four bits are 0001 and it wasn't any of the above cases, it's en passant
 * 
 * 
 * We currently define the following super special move states, to be implemented as needed.
 * 
 * Special Move State
 * 0000000000000000 termination, the previous move was the last move, or no moves yet if this is the first entry
 * 0000000000000001 white concedes
 * 0000000000000010 black concedes
 * 0000000000000011 players agreed to stalemate
 * 0000000000000100 the previous move resulted in checkmate and the game is won
 * 0000000000000101 the previous move resulted in no possible additional moves and the game is stalemated
 * 0000000000000110 The player applied the threefold repetition rule so the game is stalemated
 * 
 */
	public int serialize() {
		int ser = 0;
		ser |= source.serialize();
		ser <<= 6;
		ser |= target.serialize();
		ser <<= 4;
		return ser;
	}
	
}
