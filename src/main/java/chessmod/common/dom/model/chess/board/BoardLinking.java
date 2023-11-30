package chessmod.common.dom.model.chess.board;

import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.piece.InvalidMoveException;
import chessmod.common.dom.model.chess.Move;
import chessmod.common.dom.model.chess.board.Board;

public class BoardLinking {

    private Board boardOne;
    private Board boardTwo;
    private boolean boardsAreLinked = false; // Flag to indicate if boards are linked


    public BoardLinking(Board boardOne, Board boardTwo) {
        this.boardOne = boardOne;
        this.boardTwo = boardTwo;
    }

    // Link the two boards together
    public void linkBoards() {
        // Logic to establish a link between boardOne and boardTwo
        // TODO: might involve setting some kind of 'partner' reference in each board if needed
        // or maybe keeping the knowledge of the link in this class
        // check if both boards are not null before linking
        if (boardOne != null && boardTwo != null) {
            // Set references to each other
            boardOne.setLinkedBoard(boardTwo);
            boardTwo.setLinkedBoard(boardOne);
//            boardOne.initializeLinking(boardTwo);
//            boardTwo.initializeLinking(boardOne);

            // flag to indicate that the boards are linked
            boolean boardsAreLinked = true;
        }
    }

    // This method is called to mirror a move from one board to the other
    public void mirrorMove(Move move, Board originBoard) throws InvalidMoveException {
        if (originBoard == boardOne) {
            // Translate and make the move on boardTwo
            Move translatedMove = translateMove(move, boardTwo);
            boardTwo.moveSafely(translatedMove);
        } else if (originBoard == boardTwo) {
            // Translate and make the move on boardOne
            Move translatedMove = translateMove(move, boardOne);
            boardOne.moveSafely(translatedMove);
        }
    }

    // Translate a move to the linked board's coordinate system
    // This method should contain the logic to adjust the move according to the linked board's perspective
    private Move translateMove(Move originalMove, Board targetBoard) {
        // Get the source and destination points of the original move
        Point source = originalMove.getSource();
        Point destination = originalMove.getTarget();

        // Translate the source and destination points to the target board's coordinate system
        Point translatedSource = translatePoint(source, targetBoard);
        Point translatedDestination = translatePoint(destination, targetBoard);

        // Create a new move with the translated coordinates
        Move translatedMove = Move.create(translatedSource, translatedDestination);

        // TODO:Validate the translated move on the target board


        return translatedMove;
    }

    // Helper method to translate a Point to the target board's coordinate system
    private Point translatePoint(Point originalPoint, Board targetBoard) {

        // just a simple translation where the coordinates are flipped
        // to match the target board's coordinate system
        int translatedX = Point.MAX - originalPoint.getX();
        int translatedY = Point.MAX - originalPoint.getY();

        return Point.create(translatedX, translatedY);
    }


    // Getters and setter
    public Board getBoardOne() {

        return boardOne;
    }
    public void setBoardOne(Board boardOne) {

        this.boardOne = boardOne;
    }
    public Board getBoardTwo() {

        return boardTwo;
    }
    public void setBoardTwo(Board boardTwo) {

        this.boardTwo = boardTwo;
    }


}
