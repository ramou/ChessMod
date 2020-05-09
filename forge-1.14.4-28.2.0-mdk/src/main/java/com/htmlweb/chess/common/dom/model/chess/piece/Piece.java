package com.htmlweb.chess.common.dom.model.chess.piece;

import java.util.HashSet;
import java.util.Set;

import com.htmlweb.chess.common.dom.model.chess.Move;
import com.htmlweb.chess.common.dom.model.chess.Point;
import com.htmlweb.chess.common.dom.model.chess.Side;
import com.htmlweb.chess.common.dom.model.chess.Move.PawnPromotion;
import com.htmlweb.chess.common.dom.model.chess.board.Board;
import com.htmlweb.chess.common.dom.model.chess.board.BoardFactory;

public abstract class Piece {
	public Piece(Point position, Side side) {
		super();
		this.position = position;
		this.side = side;
	}

	private Point position;
	private Side side;
	private boolean moved = false;

	public boolean hasMoved() {
		return moved;
	}

	public void setMoved(boolean moved) {
		this.moved = moved;
	}

	public Point getPosition() {
		return position;
	}

	public void setPosition(Point position) {
		this.position = position;
	}

	public Side getSide() {
		return side;
	}

	public Set<Move> getMovesFinalThreatNotwithstanding(Board b) throws InvalidMoveException {
		Set<Move> possible = new HashSet<Move>();
		if(!this.getSide().equals(b.getCurrentPlayer())) throw new InvalidMoveException(this + " trying to move when it is " + b.getCurrentPlayer() + "'s turn.");
		getPossibleThreat(b).forEach(p->possible.add(createMove(b, p)));
		return possible;
	}
	
	public Set<Move> getAllowedMoves(Board b) throws InvalidMoveException {
		Set<Move> allowed = new HashSet<Move>();
		
		Set<Move> possible = new HashSet<Move>();
		possible.addAll(getMovesFinalThreatNotwithstanding(b));
		allowed.addAll(possible);
		for(Move m: possible) {
			Board temp = BoardFactory.createBoard(b);
			if(m instanceof PawnPromotion) {
				temp.move(Move.create(m.getSource(), m.getTarget()));
			} else {
				temp.move(m);
			}
			if(temp.isUnderThreatBy(getSide().other())) allowed.remove(m);
		}

		//Eliminate moves that leave the king under threat.
		
		
		return allowed;
	}
	
	public abstract Set<Point> getPossibleThreat(Board b);

	public Move createMove(Board b, Point t) {
		return Move.create(getPosition(), t);
	}
	
	public abstract char getCharacter();

	public abstract int serialize();
	
	@Override
	public String toString() {
		return String.format("%-6s(%s) @%s", this.getClass().getSimpleName(), side, getPosition());
	}
	
}
