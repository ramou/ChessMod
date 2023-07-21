package chess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import chessmod.common.dom.model.chess.Move;
import chessmod.common.dom.model.chess.PieceInitializer;
import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.Side;
import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.board.BoardFactory;
import chessmod.common.dom.model.chess.piece.InvalidMoveException;
import chessmod.common.dom.model.chess.piece.Knight;
import chessmod.common.dom.model.chess.piece.Pawn;
import chessmod.common.dom.model.chess.piece.Piece;
import chessmod.common.dom.model.chess.piece.Queen;
import chessmod.common.dom.model.chess.piece.Rook;

class TestBoard {


	Board b = null;
	@BeforeEach
	void setUp() {
		b = BoardFactory.createBoard();
		System.out.println(b.showBoard());
	}
	
	@Test
	void testSerializeNewBoard() {
		testClonedBoard(b);
	}
	
	@Test
	void testSerializeBoardWithMoves() {
		try {
			doubleAdvancePawn(b, 7, 6);
		} catch (InvalidMoveException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
		testClonedBoard(b);
	}

	@Test
	void testBlackFailsToGoFirst() {
		try {
			doubleAdvancePawn(b, 1, 1);
			fail("Black should not be able to move first!");
		} catch (InvalidMoveException e) {
			
		}
	}
	
	@Test
	void testEnPassant() {
		try {
			doubleAdvancePawn(b, 7, 6);
			doubleAdvancePawn(b, 1, 1);
			regularMove(b, 7, 4, 7, 3);
			regularMove(b, 1, 3, 1, 4);
			doubleAdvancePawn(b, 2, 6);
			enPassant(b, 1, 4, 2, 5);
			assertNull(b.pieceAt(Point.create(2, 4)));
		} catch (InvalidMoveException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	
	@Test
	void testBadEnPassant() {
		try {
			doubleAdvancePawn(b, 7, 6);
			doubleAdvancePawn(b, 1, 1);
			regularMove(b, 7, 4, 7, 3);
			regularMove(b, 1, 3, 1, 4);
			doubleAdvancePawn(b, 2, 6);
			enPassant(b, 1, 4, 0, 5);
			fail("You cannot En Passant to an empty square!");
		} catch (InvalidMoveException e) {
			
		}
	}

	@Test
	void testBadPawnDoubleMove() {
		try {
			doubleAdvancePawn(b, 7, 6);
			doubleAdvancePawn(b, 1, 1);
			Piece p = b.pieceAt(7, 4);
			assertEquals(1,p.getAllowedMoves(b).size());
			for(Move m: p.getAllowedMoves(b)) {
				assertEquals(new Point(7, 3), m.getTarget());
			}
			doubleAdvancePawn(b, 7, 4);
			fail("You cannot double move a pawn that has already moved!");
		} catch (InvalidMoveException e) {

		}
	}

	@Test
	void testBadPawnDoubleMoveOnClonedBoard() {
		try {
			doubleAdvancePawn(b, 7, 6);
			doubleAdvancePawn(b, 1, 1);
			Board b2 = BoardFactory.createBoard(b);
			Piece p = b2.pieceAt(7, 4);
			assertEquals(1,p.getAllowedMoves(b2).size());
			for(Move m: p.getAllowedMoves(b2)) {
				assertEquals(new Point(7, 3), m.getTarget());
			}
			doubleAdvancePawn(b, 7, 4);
			fail("You cannot double move a pawn that has already moved!");
		} catch (InvalidMoveException e) {

		}
	}

	@Test
	void testFailMovingIntoCheck() {
		try {
			doubleAdvancePawn(b, 7, 6);
			doubleAdvancePawn(b, 1, 1);
			regularMove(b, 7, 4, 7, 3);
			regularMove(b, 1, 3, 1, 4);
			doubleAdvancePawn(b, 2, 6);
			enPassant(b, 1, 4, 2, 5);
			regularMove(b, 7, 3, 7, 2);
			regularMove(b, 2, 5, 3, 6);
			regularMove(b, 7, 2, 6, 1);
			testClonedBoard(b);
			fail("You cannot move leaving yourself in check!");
		} catch (InvalidMoveException e) {
			
		}
	}
	
	@Test
	void testSerializeBoardWithMovesRegression001() {
		try {
			doubleAdvancePawn(b, 7, 6);
			doubleAdvancePawn(b, 1, 1);
			regularMove(b, 7, 4, 7, 3);
			regularMove(b, 1, 3, 1, 4);
			doubleAdvancePawn(b, 2, 6);
			enPassant(b, 1, 4, 2, 5);
			testClonedBoard(b);
			regularMove(b, 7, 3, 7, 2);
			regularMove(b, 2, 5, 3, 6);
		} catch (InvalidMoveException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
		testClonedBoard(b);
	}
	
	@Test
	void testKnightMove() {
		try {
			regularMove(b, 1, 7, 0, 5);
		} catch (InvalidMoveException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
		
		testClonedBoard(b);
	}
	
	@Test
	void testBishopMove() {
		try {
			doubleAdvancePawn(b, 4, 6);
			doubleAdvancePawn(b, 1, 1);
			regularMove(b, 5, 7, 3, 5);
		} catch (InvalidMoveException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
		
		testClonedBoard(b);
	}
	
	@Test
	void testCastle() {
		try {
			doubleAdvancePawn(b, 3, 6);
			doubleAdvancePawn(b, 4, 1);
			regularMove(b, 2, 7, 5, 4);
			regularMove(b, 5, 0, 1, 4);
			regularMove(b, 1, 7, 2, 5);
			regularMove(b, 6, 0, 7, 2);
			regularMove(b, 3, 7, 3, 6);
			castle(b, 4, 0, 6, 0);//black castles
			Piece r1 = b.pieceAt(5, 0);
			assert(r1 instanceof Rook);
			assert(r1.getSide().equals(Side.WHITE));
			castle(b, 4, 7, 2, 7);//white queen castles
			Piece r2 = b.pieceAt(3, 7);
			b.showBoard();
			assert(r2 instanceof Rook);
			assert(r2.getSide().equals(Side.BLACK));
		} catch (InvalidMoveException e) {
			e.printStackTrace();
			fail();
		}

		testClonedBoard(b);
	}
	
	@Test
	void testCastleIntoCheckFails() {
		try {
			doubleAdvancePawn(b, 3, 6);
			doubleAdvancePawn(b, 4, 1);
			regularMove(b, 2, 7, 6, 3); //White Bishop moves badly
			regularMove(b, 3, 0, 6, 3); //Black Queen takes bishop
			regularMove(b, 1, 7, 2, 5); //White Knight moves out
			regularMove(b, 6, 0, 7, 2); //Black Knight moves out
			regularMove(b, 3, 7, 3, 6); //White Queen moves out
			castle(b, 4, 0, 6, 0);//black castles
			fail("That castle was illegal so we should have thrown an InvalidMoveException!");
		} catch (InvalidMoveException e) {
			Piece queen = b.pieceAt(3, 6);
			assert(queen instanceof Queen);
			assertEquals(queen.getSide(), Side.BLACK);
		}
	}
	
	@Test
	void testCastleThroughThreatFails() {
		try {
			doubleAdvancePawn(b, 3, 6); //White Queen's Pawn double advance
			doubleAdvancePawn(b, 4, 1); //Black King's Pawn double advance
			doubleAdvancePawn(b, 4, 6); //White Kings's Pawn double advance
			doubleAdvancePawn(b, 3, 1); //Black Queen's Pawn double advance
			regularMove(b, 3, 7, 6, 4); //White Queen moves badly
			regularMove(b, 2, 0, 6, 4); //Black Bishop takes Queen
			regularMove(b, 2, 7, 6, 3); //White Bishop moves badly
			regularMove(b, 3, 0, 6, 3); //Black Queen takes Bishop
			regularMove(b, 1, 7, 3, 6); //White Knight moves to block Black Queen
			regularMove(b, 6, 0, 7, 2); //Black Knight moves out
			castle(b, 4, 7, 2, 7);//White King Queen-side castles
			fail("That castle was illegal so we should have thrown an InvalidMoveException!");
		} catch (InvalidMoveException e) {
			Piece knight = b.pieceAt(7, 2);
			assert(knight instanceof Knight);
			assertEquals(knight.getSide(), Side.WHITE);
		}
	}
	
	@Test
	void testQueentMoveRegression002() {
		try {
			doubleAdvancePawn(b, 2, 6); //White Queen's Bishop Pawn double advance
			doubleAdvancePawn(b, 2, 1); //White Queen's Bishop Pawn double advance
			regularMove(b, 3, 6, 3, 5); //White Queen's Pawn advance
			regularMove(b, 3, 0, 0, 3);
		} catch (InvalidMoveException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
		
		testClonedBoard(b);
	}
	
	@Test
	void testPawnPromotion() {
		try {
			doubleAdvancePawn(b, 3, 6); //White Queen's Pawn double advance
			doubleAdvancePawn(b, 4, 1); //Black King's Pawn double advance
			doubleAdvancePawn(b, 4, 6); //White Kings's Pawn double advance
			doubleAdvancePawn(b, 3, 1); //Black Queen's Pawn double advance
			regularMove(b, 4, 4, 3, 3);
			regularMove(b, 4, 3, 3, 4);
			regularMove(b, 3, 3, 3, 2);
			regularMove(b, 3, 4, 3, 5);
			regularMove(b, 3, 2, 2, 1);
			regularMove(b, 3, 5, 2, 6);
			promotion(b, 2, 1, 1, 0, PieceInitializer.Q);
			
			Piece q = b.pieceAt(1, 0);
			assert(q instanceof Queen);
			assert(q.getSide().equals(Side.BLACK));
			
		} catch (InvalidMoveException e) {
			e.printStackTrace();
			fail("We should have been able to do a Pawn Promotion!");
		}

		testClonedBoard(b);
	}

	private void enPassant(Board b, int x1, int y1, int x2, int y2) throws InvalidMoveException {
		regularMove(b, x1, y1, x2, y2);
	}
	
	private void promotion(Board b, int x1, int y1, int x2, int y2, PieceInitializer pi) throws InvalidMoveException {
		Pawn pawn = (Pawn)b.pieceAt(x1, y1);
		Point target = Point.create(x2, y2);
		Move m = pawn.createPromotionMove(b, target, pi.create(target, pawn.getSide()));
		moveSafely(b, m);
	}

	private void moveSafely(Board b, Move m) throws InvalidMoveException {
		System.out.println("Doing " + m);
		b.moveSafely(m);
		System.out.println(b.showBoard());
	}

	private void regularMove(Board b, int x1, int y1, int x2, int y2) throws InvalidMoveException {
		Move m = b.pieceAt(x1, y1).createMove(b, Point.create(x2, y2));
		System.out.println("Doing  " + m);
		b.moveSafely(m);
		System.out.println(b.showBoard());
	}
	
	private void doubleAdvancePawn(Board b, int x, int y) throws InvalidMoveException {
		Pawn pawn = (Pawn)b.pieceAt(x, y);
		Move m = pawn.createMove(b, pawn.getPosition().toY(pawn.getDoubleAdvance()));
		moveSafely(b, m);
	}
	
	private void castle(Board b, int x1, int y1, int x2, int y2) throws InvalidMoveException {
		regularMove(b, x1, y1, x2, y2);
	}

	private void testClonedBoard(Board b) {
		Board b2 = BoardFactory.createBoard(b);

		for(int y = Point.MIN; y <= Point.MAX; y++) {
			for(int x = Point.MIN; x <= Point.MAX; x++) {
				Point p = Point.create(x, y);
				Piece piece1 = b.pieceAt(p);
				Piece piece2 = b2.pieceAt(p);
				if(piece1 == piece2 && piece1 == null) continue;
				assertEquals(piece1.getClass(), piece2.getClass());
				assertEquals(piece1.getPosition(), piece2.getPosition());
				assertEquals(piece1.getSide(), piece2.getSide());
				assertEquals(piece1.serialize(), piece2.serialize());
				assertIterableEquals(b.getMoves(), b2.getMoves());
			}
		}
	}
	
}
