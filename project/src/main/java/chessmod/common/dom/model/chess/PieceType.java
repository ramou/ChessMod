package chessmod.common.dom.model.chess;

import chessmod.common.dom.model.chess.piece.Bishop;
import chessmod.common.dom.model.chess.piece.King;
import chessmod.common.dom.model.chess.piece.Knight;
import chessmod.common.dom.model.chess.piece.Pawn;
import chessmod.common.dom.model.chess.piece.Piece;
import chessmod.common.dom.model.chess.piece.Queen;
import chessmod.common.dom.model.chess.piece.Rook;

public enum PieceType {
	P(Pawn::new),
	B(Bishop::new),
	N(Knight::new),
	Q(Queen::new),
	nmR(Rook::new),
	mR((p, s) -> {
		Rook r = new Rook(p, s);
		r.setMoved(true);
		return r;
	}),
	nmK(King::new),
	mK((p, s) -> {
		King k = new King(p, s);
		k.setMoved(true);
		return k;
	});

	interface PieceFactory {
		Piece create(Point p, Side s);
	}
	
	PieceFactory creator;
	PieceType(PieceFactory creator) {
		this.creator = creator;
	}
	
	public Piece create(Point p, Side s) {
		return creator.create(p, s);
	}
	
	public static Piece create(Point target, int serialized) {
		return PieceType.values()[(serialized>>>1)&0b111].create(target, Side.values()[serialized & 1]);
	}
}
