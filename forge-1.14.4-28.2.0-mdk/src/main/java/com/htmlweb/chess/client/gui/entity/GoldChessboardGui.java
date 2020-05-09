package com.htmlweb.chess.client.gui.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Point2f;

import org.lwjgl.opengl.GL11;

import com.htmlweb.chess.ChessMod;
import com.htmlweb.chess.common.dom.model.chess.Move;
import com.htmlweb.chess.common.dom.model.chess.Point;
import com.htmlweb.chess.common.dom.model.chess.Point.InvalidPoint;
import com.htmlweb.chess.common.dom.model.chess.board.Board;
import com.htmlweb.chess.common.dom.model.chess.Side;
import com.htmlweb.chess.common.dom.model.chess.piece.InvalidMoveException;
import com.htmlweb.chess.common.dom.model.chess.piece.Pawn;
import com.htmlweb.chess.common.dom.model.chess.piece.Piece;
import com.htmlweb.chess.tileentity.ChessboardTileEntity;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class GoldChessboardGui extends ChessboardGUI {

	public GoldChessboardGui(ChessboardTileEntity board) {
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
	public void render(int par1, int par2, float par3) {
	    //Draw the background
		drawBackground();
		showTurnColor();

		//Draw the existing pieces
		drawPieces();
	    
	    //Test highlighting squares
	    if(selected!=null)highlightSelected();
	
	    
	    //Draw highlights of any selected pieces and their valid moves
		highlightPossibleMoves();
	    
		
		//Show sideboard for current player if they're trying to promote
		if(promotionPosition != null) {
			Map<Integer, TilePiece> map = board.getBoard().getCurrentPlayer().equals(Side.WHITE)?whiteSideboardMap:blackSideboardMap;
			for(TilePiece p: map.values()) {
				drawSideboardPiece(p);
			} 
		}

		//Draw highlights of any King under threat and the pieces threatening it
		
		
	}

	private void highlightPossibleMoves() {
		float myHeight=Math.min(height, 256);

	    
	    for(Move m: possibleMoves) {
			float x1 = width/2f - 128+32+m.getTarget().x*24;
			float x2 = x1+24;
			float y1 = height/2f - myHeight/2f+(32+m.getTarget().y*24)*myHeight/256f;
			float y2 = y1+24*myHeight/256f;
			Point2f p1 = new Point2f(x1, y1);
			Point2f p2 = new Point2f(x2, y2);
	    	
	    	highlightSquare(p1, p2, Color4f.POSSIBLE);
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
					int progressionRow = b.getCurrentPlayer().equals(Side.WHITE)?0:7;
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

	protected void showTurnColor() {
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableTexture();
		if(board.getBoard().getCurrentPlayer().equals(Side.WHITE)) GlStateManager.color4f(1f, 1f, 1f, 1f);
		else GlStateManager.color4f(0f, 0f, 0f, 1f);
		
		float myHeight=Math.min(height, 256);
		final float boardOriginX = width/2f - 128;
		final float boardOriginY = height/2f - myHeight/2f;
		  
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		//top
		bufferbuilder.pos(boardOriginX+26, boardOriginY+26F*myHeight/256F, blitOffset).endVertex();
		bufferbuilder.pos(boardOriginX+26, boardOriginY+30F*myHeight/256F, blitOffset).endVertex();
		bufferbuilder.pos(boardOriginX+256-26, boardOriginY+30F*myHeight/256F, blitOffset).endVertex();
		bufferbuilder.pos(boardOriginX+256-26, boardOriginY+26F*myHeight/256F, blitOffset).endVertex();

		//left
		bufferbuilder.pos(boardOriginX+26, boardOriginY+26F*myHeight/256F, blitOffset).endVertex();
		bufferbuilder.pos(boardOriginX+26, boardOriginY+myHeight-26F*myHeight/256F, blitOffset).endVertex();
		bufferbuilder.pos(boardOriginX+30, boardOriginY+myHeight-26F*myHeight/256F, blitOffset).endVertex();
		bufferbuilder.pos(boardOriginX+30, boardOriginY+26F*myHeight/256F, blitOffset).endVertex();

		//right
		bufferbuilder.pos(boardOriginX+256-30, boardOriginY+26F*myHeight/256F, blitOffset).endVertex();
		bufferbuilder.pos(boardOriginX+256-30, boardOriginY+myHeight-26F*myHeight/256F, blitOffset).endVertex();
		bufferbuilder.pos(boardOriginX+256-26, boardOriginY+myHeight-26F*myHeight/256F, blitOffset).endVertex();
		bufferbuilder.pos(boardOriginX+256-26, boardOriginY+26F*myHeight/256F, blitOffset).endVertex();		
		
		//bottom
		bufferbuilder.pos(boardOriginX+26, boardOriginY+myHeight-30F*myHeight/256F, blitOffset).endVertex();
		bufferbuilder.pos(boardOriginX+26, boardOriginY+myHeight-26F*myHeight/256F, blitOffset).endVertex();
		bufferbuilder.pos(boardOriginX+256-26, boardOriginY+myHeight-26F*myHeight/256F, blitOffset).endVertex();
		bufferbuilder.pos(boardOriginX+256-26, boardOriginY+myHeight-30F*myHeight/256F, blitOffset).endVertex();		
		tessellator.draw();
		
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		GlStateManager.enableTexture();
		GlStateManager.disableBlend();
	}
	
}
