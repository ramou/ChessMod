package chess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import chessmod.common.dom.model.chess.Move;
import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.Side;
import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.piece.InvalidMoveException;
import chessmod.common.dom.model.chess.piece.King;
import chessmod.common.dom.model.chess.piece.Pawn;
import chessmod.common.dom.model.chess.piece.Rook;

class TestCastlingSafety {
    // Black moves first in ChessMod. Using a minimal board keeps these tests
    // independent of move sequences and Minecraft-version-specific code.
    private Board castleBoard(boolean kingSide) {
        Board board = new Board();
        board.setPiece(new King(Point.create(4, 7), Side.BLACK), Point.create(4, 7));
        int rookX = kingSide ? 7 : 0;
        board.setPiece(new Rook(Point.create(rookX, 7), Side.BLACK), Point.create(rookX, 7));
        board.setPiece(new King(Point.create(0, 0), Side.WHITE), Point.create(0, 0));
        return board;
    }

    private Move castle(boolean kingSide) {
        return Move.createCastle(Point.create(4, 7), Point.create(kingSide ? 6 : 2, 7));
    }

    @Test
    void cannotCastleOutOfCheck() {
        Board board = castleBoard(true);
        board.setPiece(new Rook(Point.create(4, 4), Side.WHITE), Point.create(4, 4));
        assertThrows(InvalidMoveException.class, () -> board.moveSafely(castle(true)));
        assertEquals(4, board.getKing(Side.BLACK).getPosition().x);
    }

    @Test
    void cannotCastleKingSideAcrossPawnAttackOnEmptySquare() {
        Board board = castleBoard(true);
        // White's pawn on e7 attacks f8, the empty square crossed by the king.
        board.setPiece(new Pawn(Point.create(4, 6), Side.WHITE), Point.create(4, 6));
        assertThrows(InvalidMoveException.class, () -> board.moveSafely(castle(true)));
    }

    @Test
    void cannotCastleQueenSideAcrossPawnAttackOnEmptySquare() {
        Board board = castleBoard(false);
        // The same pawn attacks d8, crossed during queenside castling.
        board.setPiece(new Pawn(Point.create(4, 6), Side.WHITE), Point.create(4, 6));
        assertThrows(InvalidMoveException.class, () -> board.moveSafely(castle(false)));
    }

    @Test
    void cannotCastleIntoCheck() {
        Board board = castleBoard(true);
        board.setPiece(new Rook(Point.create(6, 4), Side.WHITE), Point.create(6, 4));
        assertThrows(InvalidMoveException.class, () -> board.moveSafely(castle(true)));
    }

    @Test
    void mayCastleWhileRookIsAttacked() throws InvalidMoveException {
        Board board = castleBoard(true);
        // Attacks h8, but not e8, f8 or g8. This castle is legal in chess.
        board.setPiece(new Rook(Point.create(7, 4), Side.WHITE), Point.create(7, 4));
        board.moveSafely(castle(true));
        assertEquals(Point.create(6, 7), board.getKing(Side.BLACK).getPosition());
        assertEquals(Rook.class, board.pieceAt(5, 7).getClass());
    }

    @Test
    void mayCastleAcrossUnattackedSquares() throws InvalidMoveException {
        Board board = castleBoard(false);
        board.moveSafely(castle(false));
        assertEquals(Point.create(2, 7), board.getKing(Side.BLACK).getPosition());
        assertEquals(Rook.class, board.pieceAt(3, 7).getClass());
    }
}
