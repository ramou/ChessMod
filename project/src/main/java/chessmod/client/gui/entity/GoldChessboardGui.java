package chessmod.client.gui.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;

import chessmod.ChessMod;
import chessmod.common.Point2f;
import chessmod.common.dom.model.chess.Move;
import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.Point.InvalidPoint;
import chessmod.common.dom.model.chess.Side;
import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.piece.InvalidMoveException;
import chessmod.common.dom.model.chess.piece.Pawn;
import chessmod.common.dom.model.chess.piece.Piece;
import chessmod.tileentity.ChessboardTileEntity;
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
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
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
		Point kingPoint = board.getBoard().getCheckMate();
		if(kingPoint == null) {
			kingPoint = board.getBoard().getCheck();
			if(kingPoint != null) highlightSquare(kingPoint, Color4f.CHECK);
		} else highlightSquare(kingPoint, Color4f.CHECKMATE);
		
	}

	private void highlightPossibleMoves() {
	    for(Move m: possibleMoves) {
	    	highlightSquare(m.getTarget(), Color4f.POSSIBLE);
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
						System.out.println(targetPiece + " has moves: " + possibleMoves);
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
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		
		Color4f c = Color4f.BLACK;
		
		if(board.getBoard().getCurrentPlayer().equals(Side.WHITE)) c = Color4f.WHITE;

		
		float myHeight = Math.min(height, 256);
		final float boardOriginX = width / 2f - 128;
		final float boardOriginY = height / 2f - myHeight / 2f;
		float x1Outter = boardOriginX + 26;
		float x1Inner = x1Outter + 4;
		float x2Inner = boardOriginX + 256 - 30;
		float x2Outter = x2Inner + 4;

		float y1Outter = boardOriginY + 26F * myHeight / 256F;
		float y1Inner = y1Outter + 4 * myHeight / 256F;
		float y2Inner = boardOriginY + myHeight - 30F * myHeight / 256F;
		float y2Outter = y2Inner + 4 * myHeight / 256F;

		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		//top
		
		Point2f p1 = new Point2f(x1Outter, y1Outter);
		Point2f p2 = new Point2f(x2Outter, y1Inner);
		c.draw2DRect(bufferbuilder, p1, p2);

		//left
		p2 = new Point2f(x1Inner, y2Outter);
		c.draw2DRect(bufferbuilder, p1, p2);

		//right
		p1 = new Point2f(x2Inner, y1Outter);
		p2 = new Point2f(x2Outter, y2Outter);
		c.draw2DRect(bufferbuilder, p1, p2);

		//bottom
		p1 = new Point2f(x1Outter, y2Inner);
		c.draw2DRect(bufferbuilder, p1, p2);
		tessellator.end();
		
		RenderSystem.enableTexture();		
		RenderSystem.disableBlend();
	}
	
}
