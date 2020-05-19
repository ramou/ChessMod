package chessmod.common.dom.model.chess;

import chessmod.common.dom.model.chess.piece.Bishop;
import chessmod.common.dom.model.chess.piece.King;
import chessmod.common.dom.model.chess.piece.Knight;
import chessmod.common.dom.model.chess.piece.Pawn;
import chessmod.common.dom.model.chess.piece.Piece;
import chessmod.common.dom.model.chess.piece.Queen;
import chessmod.common.dom.model.chess.piece.Rook;

public enum PieceInitializer {
	P((p, s) -> {
		return new Pawn(p, s);
	}),
	B((p, s) -> {
		return new Bishop(p, s);
	}),
	N((p, s) -> {
		return new Knight(p, s);
	}),
	Q((p, s) -> {
		return new Queen(p, s);
	}),
	nmR((p, s) -> {
		return new Rook(p, s);
	}),
	mR((p, s) -> {
		Rook r = new Rook(p, s);
		r.setMoved(true);
		return r;
	}),
	nmK((p, s) -> {
		return new King(p, s);
	}),
	mK((p, s) -> {
		King k = new King(p, s);
		k.setMoved(true);
		return k;
	});
	
	interface PieceCreatorInterface {
		Piece create(Point p, Side s);
	}
	
	PieceInitializer.PieceCreatorInterface creator;
	private PieceInitializer(PieceInitializer.PieceCreatorInterface creator) {
		this.creator = creator;
	}
	
	public Piece create(Point p, Side s) {
		return creator.create(p, s);
	}
	
	public static Piece create(Point target, int serialized) {
		return PieceInitializer.values()[(serialized>>>1)&0b111].create(target, Side.values()[serialized & 1]);
	}
}