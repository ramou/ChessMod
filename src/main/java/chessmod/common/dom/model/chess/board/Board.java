package chessmod.common.dom.model.chess.board;

import java.util.BitSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import chessmod.ChessMod;
import chessmod.common.dom.model.chess.InvalidPointException;
import chessmod.common.dom.model.chess.Move;
import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.Point.Directions;
import chessmod.common.dom.model.chess.Point.InvalidPoint;
import chessmod.common.dom.model.chess.Side;
import chessmod.common.dom.model.chess.piece.InvalidMoveException;
import chessmod.common.dom.model.chess.piece.King;
import chessmod.common.dom.model.chess.piece.Piece;


public class Board {

	private BoardLinking boardLinking;

	// Method to initialize board linking
	public void initializeLinking(Board otherBoard) {
		if(this.boardLinking != null) {
			this.boardLinking.linkBoards();
			return;
		}

		this.boardLinking = new BoardLinking(this, otherBoard);

		// Link the other board only if it hasn't been linked yet
		//if (otherBoard.boardLinking == null) {
		//	otherBoard.initializeLinking(this);
		//}

		this.boardLinking.linkBoards();
	}

	public Side getCurrentPlayer() {
		return (moves.size()%2==0)?Side.BLACK:Side.WHITE;
	}
	
	Piece[][] pieces = new Piece[8][8];
	List<Move> moves = new LinkedList<Move>();
	
	public List<Move> getMoves() {
		return moves;
	}


	public void setMoves(List<Move> moves) {
		this.moves = moves;
	}

	/**
	 * Gotta remember that it's FIFO
	 * 
	 * @param b
	 */
	@SuppressWarnings("unused")
	private String showBoardPieceMask(SerializedBoard b) {
		BitSet pieceMaskSet = BitSet.valueOf(new long[]{b.piece_mask});
		StringBuilder sb = new StringBuilder("Deserializing board with mask: \n");
		for(int y = 0; y < 8; y++) {
			long pieceBits = (pieceMaskSet.get(y*8, (y+1)*8).toLongArray().length==1)?pieceMaskSet.get(y*8, (y+1)*8).toLongArray()[0]:0;
			sb.insert(0, String.format("%8s", Long.toUnsignedString(pieceBits, 2)).replace(' ', '0')+"\n");
		}
		
		return sb.toString();

	}

	/**
	 * Pro-debugging :P
	 */
	public String showBoard() {
		StringBuilder sb = new StringBuilder();
		sb.append(System.lineSeparator());
		for(int y = 0; y < 8; y++) {
			for(int x = 0; x < 8; x++) {
				Piece p = pieceAt(Point.create(x, y));
				sb.append(p==null?'.':p.getCharacter());
			}
			sb.append(System.lineSeparator());
		}
		
		return sb.toString();
	}

	public Point getCheck() {
		if(isUnderThreatBy(getCurrentPlayer().other())) {
			return getKing(getCurrentPlayer()).getPosition();
		}
		return null;
	}

	public Point getCheckMate() {
		Side s = getCurrentPlayer();
		if(isUnderThreatBy(s.other())) {
			Set<Move> moves = new HashSet<Move>();
			for(int y = 0; y < 8; y++) {
				for(int x = 0; x < 8; x++) {
					Piece piece = pieces[y][x];
					if(piece != null && piece.getSide().equals(s)) {
						try {
							moves.addAll(piece.getAllowedMoves(this));
						} catch (InvalidMoveException e) {
							ChessMod.LOGGER.error("Invlaid move while attempting to get Checkmate status");
						}
					}
				}
			}
			if(moves.isEmpty()) return getKing(getCurrentPlayer()).getPosition();
		}
		return null;
	}
	
	public Set<Point> findTargettablePointRun(Piece source, Directions ... dirs) {
		int x = 0;
		int y = 0;
		for(Directions d: dirs) {
			x += d.x;
			y += d.y;
		}
		return findTargettablePointRun(source, x, y);
	}
	
	public Set<Point> findTargettablePointRun(Piece source, int offsetX, int offsetY) {
		Set<Point> valid = new HashSet<Point>();
		Point p = Point.create(source.getPosition(), offsetX, offsetY);
		while(!(p instanceof InvalidPoint) && (pieceAt(p) == null || pieceAt(p).getSide() != source.getSide())) {
			valid.add(p);
			if(pieceAt(p) != null) break; // We hit an attackable piece, stop after adding it.
			p = Point.create(p, offsetX, offsetY);
		}
		return valid;
	}

	public boolean isEmptySquare(int x, int y) {
		return isEmptySquare(Point.create(x,y));
	}
	
	public boolean isEmptySquare(Point p) {
		if(p instanceof InvalidPoint) throw new InvalidPointException();
		return pieces[p.y][p.x] == null;
	}


	public Piece pieceAt(int x, int y) {
		return pieceAt(Point.create(x, y));
	}
	
	public Piece pieceAt(Point p) {
		if(p instanceof InvalidPoint) throw new InvalidPointException();
		return pieces[p.y][p.x];
	}
	
	public void setPiece(Piece piece, Point p) {
		pieces[p.y][p.x] = piece;
		if(piece != null) piece.setPosition(p);
	}

	public static enum MoveType {
		MOVE_ONLY,
		ATTACK_ONLY,
		MOVE_OR_ATTACK
	}
	
	public Set<Point> findTargettablePoint(Piece source, MoveType type, Directions ... dirs) {
		int x = 0;
		int y = 0;
		for(Directions d: dirs) {
			x += d.x;
			y += d.y;
		}
		return findTargettablePoint(source, type, x, y);
	}
	
	public Set<Point> findTargettablePoint(Piece source, MoveType type, int offsetX, int offsetY) {
		Set<Point> valid = new HashSet<Point>();
		Point p = Point.create(source.getPosition(), offsetX, offsetY);
		if(!(p instanceof InvalidPoint)) {
			Piece pieceAt = pieceAt(p);
			if(
					((type.equals(MoveType.MOVE_ONLY) || type.equals(MoveType.MOVE_OR_ATTACK)) && pieceAt == null) ||
					((type.equals(MoveType.ATTACK_ONLY) || type.equals(MoveType.MOVE_OR_ATTACK)) && pieceAt != null && !source.getSide().equals(pieceAt.getSide()))
				) {
				valid.add(p);
			}
		}
		return valid;
	}
	
	public boolean isUnderThreatBy(Point p, Side s) {
		for(int y = 0; y < 8; y++) {
			for(int x = 0; x < 8; x++) {
				Piece piece = pieces[y][x];
				if(piece != null && piece.getSide().equals(s)) {
					if(piece.getPossibleThreat(this).contains(p)) return true;
				}
			}
		}
		return false;
	}
	
	public boolean isUnderThreatBy(Side s) {
		Point p = getKing(s.other()).getPosition();

		//p must be set because there must be a king.		
		for(int y = 0; y < 8; y++) {
			for(int x = 0; x < 8; x++) {
				Piece piece = pieces[y][x];
				if(piece != null && piece.getSide().equals(s)) {
					Set<Point> possibleThreats = piece.getPossibleThreat(this);
					if(possibleThreats.contains(p)) return true;
				}
			}
		}
		return false;
	}


	public Piece getKing(Side s) {
		for(int y = 0; y < 8; y++) {
			for(int x = 0; x < 8; x++) {		
				Piece piece = pieces[y][x];
				if(piece != null && piece.getSide().equals(s) && piece instanceof King) {
					return piece;
				}
			}
		}
		return null;
	}

	private boolean isMirroring = false; // Flag to indicate if the current move is a mirrored move

	public void moveSafely(Move m) throws InvalidMoveException{
		if (pieceAt(m.getSource()) == null) {
			throw new InvalidMoveException("Null source detected for move " + m);
		}
		if (pieceAt(m.getSource()).getAllowedMoves(this).contains(m)) {
			m.doMove(this);
			moves.add(m);
			if (boardLinking != null && !isMirroring) {
				isMirroring = true;
				boardLinking.mirrorMove(m, this);
				isMirroring = false;
			}
		} else {
			throw new InvalidMoveException(m + " was not in the list of possible moves");
		}

//		if(pieceAt(m.getSource()) == null) throw new InvalidMoveException("Null source detected for move " + m);
//		if(pieceAt(m.getSource()).getAllowedMoves(this).contains(m)) {
//			m.doMove(this);
//			moves.add(m);
//		} else {
//			throw new InvalidMoveException(m + " was not in the list of possible moves");
//		}
//		//modicfied for the linking logic of quantum board
//		if (boardLinking != null) {
//			boardLinking.mirrorMove(m, this);
//		}
	}
	
	public void move(Move m) throws InvalidMoveException{
			m.doMove(this);
			moves.add(m);
	}

}
