package chess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import chessmod.common.dom.model.chess.Move;
import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.Side;
import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.piece.Bishop;
import chessmod.common.dom.model.chess.piece.InvalidMoveException;
import chessmod.common.dom.model.chess.piece.King;
import chessmod.common.dom.model.chess.piece.Knight;
import chessmod.common.dom.model.chess.piece.Pawn;
import chessmod.common.dom.model.chess.piece.Piece;
import chessmod.common.dom.model.chess.piece.Rook;

class TestKingEdgeEscape {
    private void put(Board board, Piece piece, int x, int y) {
        board.setPiece(piece, Point.create(x, y));
    }

    @Test
    void blackKingAtB1CanEscapeKnightCheckToA1() throws InvalidMoveException {
        // Screenshot reconstruction: White's knight at c3 attacks Black's
        // king on b1, but not a1. ChessMod's board rows run top to bottom.
        // The rook on d1 is BLACK, not white.
        Board board = new Board();
        put(board, new King(Point.create(1, 0), Side.WHITE), 1, 0); // b8
        put(board, new Rook(Point.create(3, 0), Side.WHITE), 3, 0); // d8
        put(board, new Rook(Point.create(4, 0), Side.WHITE), 4, 0); // e8
        put(board, new Pawn(Point.create(1, 1), Side.WHITE), 1, 1); // b7
        put(board, new Pawn(Point.create(6, 1), Side.WHITE), 6, 1); // g7
        put(board, new Pawn(Point.create(7, 1), Side.WHITE), 7, 1); // h7
        put(board, new Knight(Point.create(1, 3), Side.BLACK), 1, 3); // b5
        put(board, new Bishop(Point.create(2, 4), Side.BLACK), 2, 4); // c4
        put(board, new Pawn(Point.create(1, 5), Side.BLACK), 1, 5); // b3
        put(board, new Knight(Point.create(2, 5), Side.WHITE), 2, 5); // c3
        put(board, new Pawn(Point.create(0, 6), Side.BLACK), 0, 6); // a2
        put(board, new Pawn(Point.create(2, 6), Side.BLACK), 2, 6); // c2
        put(board, new Bishop(Point.create(3, 6), Side.WHITE), 3, 6); // d2
        put(board, new Pawn(Point.create(5, 6), Side.BLACK), 5, 6); // f2
        put(board, new Pawn(Point.create(6, 6), Side.BLACK), 6, 6); // g2
        put(board, new Pawn(Point.create(7, 6), Side.BLACK), 7, 6); // h2
        put(board, new King(Point.create(1, 7), Side.BLACK), 1, 7); // b1
        put(board, new Rook(Point.create(3, 7), Side.BLACK), 3, 7); // d1

        Point b1 = Point.create(1, 7);
        Point a1 = Point.create(0, 7);
        Move escape = Move.create(b1, a1);

        assertEquals(Side.BLACK, board.getCurrentPlayer());
        assertEquals(b1, board.getCheck());
        assertFalse(board.isUnderThreatBy(a1, Side.WHITE));
        // Specifically guard against interpreting a one-square edge move
        // as a castle because an out-of-bounds Point reports x == 0.
        assertEquals(Move.class, board.getKing(Side.BLACK).createMove(board, a1).getClass());
        assertTrue(board.getKing(Side.BLACK).getAllowedMoves(board).contains(escape));

        board.moveSafely(escape);
        assertEquals(a1, board.getKing(Side.BLACK).getPosition());
        assertFalse(board.isUnderThreatBy(Side.WHITE));
    }


    @Test
    void blackKingCanMoveVerticallyAlongAFile() throws InvalidMoveException {
        Board board = new Board();
        Point a1 = Point.create(0, 7);
        Point a2 = Point.create(0, 6);
        put(board, new King(a1, Side.BLACK), 0, 7);
        put(board, new King(Point.create(7, 0), Side.WHITE), 7, 0);
        Move move = Move.create(a1, a2);

        assertEquals(Move.class, board.getKing(Side.BLACK).createMove(board, a2).getClass());
        assertTrue(board.getKing(Side.BLACK).getAllowedMoves(board).contains(move));
        board.moveSafely(move);
        assertEquals(a2, board.getKing(Side.BLACK).getPosition());
    }

    @Test
    void whiteKingOnAFileMoveIsNotCastling() {
        Board board = new Board();
        Point a8 = Point.create(0, 0);
        Point a7 = Point.create(0, 1);
        put(board, new King(a8, Side.WHITE), 0, 0);
        put(board, new King(Point.create(7, 7), Side.BLACK), 7, 7);

        // Classification is independent of turn; ChessMod starts with Black.
        assertEquals(Move.class, board.getKing(Side.WHITE).createMove(board, a7).getClass());
    }

    @Test
    void blackKingCanMoveVerticallyAlongHFile() throws InvalidMoveException {
        Board board = new Board();
        Point h1 = Point.create(7, 7);
        Point h2 = Point.create(7, 6);
        put(board, new King(h1, Side.BLACK), 7, 7);
        put(board, new King(Point.create(0, 0), Side.WHITE), 0, 0);
        Move move = Move.create(h1, h2);

        // The speculative kingside rook probe must not dereference InvalidPoint.
        assertEquals(Move.class, board.getKing(Side.BLACK).createMove(board, h2).getClass());
        assertTrue(board.getKing(Side.BLACK).getAllowedMoves(board).contains(move));
        board.moveSafely(move);
        assertEquals(h2, board.getKing(Side.BLACK).getPosition());
    }

    @Test
    void twoFileSameRankMoveRemainsCastling() {
        Board board = new Board();
        Point e1 = Point.create(4, 7);
        Point g1 = Point.create(6, 7);
        put(board, new King(e1, Side.BLACK), 4, 7);
        put(board, new King(Point.create(0, 0), Side.WHITE), 0, 0);

        // Actual legality of castling is exercised by TestCastlingSafety.
        assertEquals(Move.Castle.class, board.getKing(Side.BLACK).createMove(board, g1).getClass());
    }
}
