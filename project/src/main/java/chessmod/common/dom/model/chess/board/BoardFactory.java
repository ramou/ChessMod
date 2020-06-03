package chessmod.common.dom.model.chess.board;

import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.Side;
import chessmod.common.dom.model.chess.piece.Bishop;
import chessmod.common.dom.model.chess.piece.King;
import chessmod.common.dom.model.chess.piece.Knight;
import chessmod.common.dom.model.chess.piece.Pawn;
import chessmod.common.dom.model.chess.piece.Queen;
import chessmod.common.dom.model.chess.piece.Rook;

public class BoardFactory {

	public static Board createBoard(Board b) {
		return SerializedBoard.serialize(b).deSerialize();
	}

	public static Board createBoard() {
		Board b = new Board();
		Point p;
		
		p = Point.create(0, 0);
		b.setPiece(new Rook(p, Side.WHITE), p);
		p = Point.create(1, 0);
		b.setPiece(new Knight(p, Side.WHITE), p);
		p = Point.create(2, 0);
		b.setPiece(new Bishop(p, Side.WHITE), p);
		p = Point.create(3, 0);
		b.setPiece(new Queen(p, Side.WHITE), p);
		p = Point.create(4, 0);
		b.setPiece(new King(p, Side.WHITE), p);
		p = Point.create(5, 0);
		b.setPiece(new Bishop(p, Side.WHITE), p);
		p = Point.create(6, 0);
		b.setPiece(new Knight(p, Side.WHITE), p);
		p = Point.create(7, 0);
		b.setPiece(new Rook(p, Side.WHITE), p);
		
		p = Point.create(0, 1);
		b.setPiece(new Pawn(p, Side.WHITE), p);
		p = Point.create(1, 1);
		b.setPiece(new Pawn(p, Side.WHITE), p);
		p = Point.create(2, 1);
		b.setPiece(new Pawn(p, Side.WHITE), p);
		p = Point.create(3, 1);
		b.setPiece(new Pawn(p, Side.WHITE), p);
		p = Point.create(4, 1);
		b.setPiece(new Pawn(p, Side.WHITE), p);
		p = Point.create(5, 1);
		b.setPiece(new Pawn(p, Side.WHITE), p);
		p = Point.create(6, 1);
		b.setPiece(new Pawn(p, Side.WHITE), p);
		p = Point.create(7, 1);
		b.setPiece(new Pawn(p, Side.WHITE), p);
	
		p = Point.create(0, 6);
		b.setPiece(new Pawn(p, Side.BLACK), p);
		p = Point.create(1, 6);
		b.setPiece(new Pawn(p, Side.BLACK), p);
		p = Point.create(2, 6);
		b.setPiece(new Pawn(p, Side.BLACK), p);
		p = Point.create(3, 6);
		b.setPiece(new Pawn(p, Side.BLACK), p);
		p = Point.create(4, 6);
		b.setPiece(new Pawn(p, Side.BLACK), p);
		p = Point.create(5, 6);
		b.setPiece(new Pawn(p, Side.BLACK), p);
		p = Point.create(6, 6);
		b.setPiece(new Pawn(p, Side.BLACK), p);
		p = Point.create(7, 6);
		b.setPiece(new Pawn(p, Side.BLACK), p);
		
		p = Point.create(0, 7);
		b.setPiece(new Rook(p, Side.BLACK), p);
		p = Point.create(1, 7);
		b.setPiece(new Knight(p, Side.BLACK), p);
		p = Point.create(2, 7);
		b.setPiece(new Bishop(p, Side.BLACK), p);
		p = Point.create(3, 7);
		b.setPiece(new Queen(p, Side.BLACK), p);
		p = Point.create(4, 7);
		b.setPiece(new King(p, Side.BLACK), p);
		p = Point.create(5, 7);
		b.setPiece(new Bishop(p, Side.BLACK), p);
		p = Point.create(6, 7);
		b.setPiece(new Knight(p, Side.BLACK), p);
		p = Point.create(7, 7);
		b.setPiece(new Rook(p, Side.BLACK), p);
		
		return b;
	}

}
