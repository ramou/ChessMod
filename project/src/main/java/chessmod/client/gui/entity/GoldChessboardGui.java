package chessmod.client.gui.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.gui.GuiGraphics;

import chessmod.ChessMod;
import chessmod.blockentity.ChessboardBlockEntity;
import chessmod.common.dom.model.chess.Move;
import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.Point.InvalidPoint;
import chessmod.common.dom.model.chess.Side;
import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.piece.InvalidMoveException;
import chessmod.common.dom.model.chess.piece.Pawn;
import chessmod.common.dom.model.chess.piece.Piece;
import net.minecraft.resources.ResourceLocation;

public class GoldChessboardGui extends ChessboardGUI {

	public GoldChessboardGui(ChessboardBlockEntity board) {
		super(board);

		//I find that if I don't do this, Pieces
		//don't get loaded and pieceMap doesn't get filled.
		//Might warrant me looking into... is it the non-static nature?
		for(TilePiece p: TilePiece.values()) {
			if(!p.equals(TilePiece.BLACK_PAWN) && !p.equals(TilePiece.WHITE_PAWN) && !p.equals(TilePiece.BLACK_KING) && !p.equals(TilePiece.WHITE_KING)) {
				if(p.getSide().equals(Side.BLACK)) blackSideboardMap.put(p.getIndex(), p);
				else whiteSideboardMap.put(p.getIndex(), p);
			}
		}
	}
	
	Set<Move> possibleMoves = new HashSet<Move>(); 
	
	protected static HashMap<Integer, TilePiece> blackSideboardMap = new HashMap<Integer, TilePiece>();
	protected static HashMap<Integer, TilePiece> whiteSideboardMap = new HashMap<Integer, TilePiece>();
	Point promotionPosition = null;
	
	
	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				
	    //Draw the background
		drawBackground(guiGraphics);
		showTurnColor(guiGraphics);

		//Draw the existing pieces
		drawPieces(guiGraphics);
	    
	    //Test highlighting squares
	    if(selected!=null)highlightSelected(guiGraphics);
	
	    
	    //Draw highlights of any selected pieces and their valid moves
		highlightPossibleMoves(guiGraphics);
	    
		
		//Show sideboard for current player if they're trying to promote
		if(promotionPosition != null) {
			Map<Integer, TilePiece> map = board.getBoard().getCurrentPlayer().equals(Side.WHITE)?whiteSideboardMap:blackSideboardMap;
			for(TilePiece p: map.values()) {
				drawSideboardPiece(guiGraphics, p);
			} 
		}

		//Draw highlights of any King under threat and the pieces threatening it
		Point kingPoint = board.getBoard().getCheckMate();
		if(kingPoint == null) {
			kingPoint = board.getBoard().getCheck();
			if(kingPoint != null) highlightSquare(guiGraphics, kingPoint, CHECK);
		} else highlightSquare(guiGraphics, kingPoint, CHECKMATE);
		
	}

	private void highlightPossibleMoves(GuiGraphics guiGraphics) {
	    for(Move m: possibleMoves) {
	    	highlightSquare(guiGraphics, m.getTarget(), POSSIBLE);
	    }
	}
	
	@Override
	public boolean mouseClicked(double x, double y, int maybeSomethingDragging) {
		boolean mouseClicked = super.mouseClicked(x, y, maybeSomethingDragging);
		float myHeight=Math.min(height, 256);
		final float boardOriginX = width/2f - 128;
		final float boardOriginY = height/2f - myHeight/2f;
		double myX = x-(boardOriginX + 32);	
		double myY = y-(boardOriginY + 32);
		Point maybeSelected = Point.create((int)Math.floor(myX/24), (int)Math.floor(myY/(24*myHeight/256f)));

		
		Piece piece = pieceAt(selected);
		//If we're promoting, we only allow promotion, otherwise return
		if(promotionPosition != null) {
			//TODO: Check if we've clicked on the sideboard to pick a piece
			TilePiece chosen = getFromSideboard(x, y);
			if(chosen != null){
				//TODO: If we've clicked on the sideboard to pick a piece, hide sideboard it and send the play to server
				Point target = Point.create(promotionPosition.x, promotionPosition.y);
				if(!(target instanceof InvalidPoint)) {
					notifyServerOfMove(Move.createPawnPromotion((Pawn)piece, target, chosen.create(target)));
					possibleMoves.clear();
					selected = null;
					promotionPosition = null;
				}
			}
		}
		
		// If we click outside of board area
		Board b = board.getBoard();

		if(maybeSelected instanceof InvalidPoint || maybeSelected.equals(selected)) {
			//Check whether we're picking for promotion:
			possibleMoves.clear();
			selected = null;
			promotionPosition = null;
		} else {
			//Do other Selection Check 
			if(piece != null) {//Move
				//Validate Move
				Move m = getMoveSelectedMove(selected, maybeSelected);
				if(m != null) {
					//Check if there is pawn promotion required, otherwise just do the move
					int progressionRow = b.getCurrentPlayer().equals(Side.BLACK)?0:7;
					if(piece instanceof Pawn && maybeSelected.y == progressionRow) {
						promotionPosition = maybeSelected;
					} else {
						notifyServerOfMove(m);
						possibleMoves.clear();
						selected = null;
					}
				}
				
			} else {//Select
				  //Check if piece belongs to current player
				Piece targetPiece = pieceAt(maybeSelected);
				if(targetPiece != null && targetPiece.getSide().equals(b.getCurrentPlayer())) {
					selected = maybeSelected;
					//Load possible move options
					possibleMoves.clear();
					
					try {
						possibleMoves.addAll(targetPiece.getAllowedMoves(b));
					} catch (InvalidMoveException e) {
						ChessMod.LOGGER.debug(e.getMessage());
						possibleMoves.clear();
					}
				}
				
			} 

		}
		return mouseClicked;
	}

	private Move getMoveSelectedMove(Point source, Point target) {
		for(Move m: possibleMoves) {
			if(m.getSource().equals(source) && m.getTarget().equals(target)) return m;
		}
		return null;
	}

	private TilePiece getFromSideboard(double x, double y) {
		float myHeight=Math.min(height, 256);
		final float boardOriginX = width/2f - 128;
		final float boardOriginY = height/2f - myHeight/2f;
		final float sideboardOriginY = boardOriginY+32*myHeight/256f;

		if(y > sideboardOriginY && y < sideboardOriginY+6*24) {
			int index = (int)((y-sideboardOriginY)/24);
			//Check Left Sideboard
			if (board.getBoard().getCurrentPlayer().equals(Side.WHITE)) {
				if(x > (boardOriginX + 16 +24*9) && x < (boardOriginX + 40 +24*9)) { //Check Right Sideboard
					return whiteSideboardMap.get(index);
				}
			} else {
				if(x > boardOriginX && x < boardOriginX+24) {
					return blackSideboardMap.get(index); 
				} 
			}
		} 
		
		return null;
	}

	protected void showTurnColor(GuiGraphics guiGraphics) {
		ResourceLocation c = BLACK;		
		if(board.getBoard().getCurrentPlayer().equals(Side.WHITE)) c = WHITE;

		final int boardOriginX = (int)(width / 2f - 128);
		final int boardOriginY = (int)(height / 2f - 128);
		int x1Outter = boardOriginX + 26;
		int x2Inner = boardOriginX + 256 - 30;
		int y1Outter = boardOriginY + 26;
		int y2Inner = boardOriginY + 256 - 30;

		//top
		highlightSquare(guiGraphics, x1Outter, y1Outter, 204, 4, c);

		//left
		highlightSquare(guiGraphics, x1Outter, y1Outter, 4, 204, c);

		//right
		highlightSquare(guiGraphics, x2Inner, y1Outter, 4, 204, c);

		//bottom
		highlightSquare(guiGraphics, x1Outter, y2Inner, 204, 4, c);
	}
	
}
