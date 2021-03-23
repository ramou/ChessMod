package chessmod.client.gui.entity;

import java.util.HashMap;

import chessmod.ChessMod;
import chessmod.block.entity.ChessboardBlockEntity;
import chessmod.common.dom.model.chess.Move;
import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.Point.InvalidPoint;
import chessmod.common.dom.model.chess.Side;
import chessmod.common.dom.model.chess.piece.Piece;
import chessmod.common.network.ArbitraryPlacement;
import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public class WoodChessboardScreen extends ChessboardScreen {

	public WoodChessboardScreen(ChessboardBlockEntity board) {
		super(board);
		
		for(TilePiece p: TilePiece.values()) {
			if(p.getSide().equals(Side.BLACK)) blackSideboardMap.put(p.getIndex(), p);
			else whiteSideboardMap.put(p.getIndex(), p);
		}

	}
	
	protected TilePiece sideBoardSelected = null;
	protected static HashMap<Integer, TilePiece> blackSideboardMap = new HashMap<Integer, TilePiece>();
	protected static HashMap<Integer, TilePiece> whiteSideboardMap = new HashMap<Integer, TilePiece>();
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float tickDelta) {
	    //Draw the background
		drawBackground();

		//Draw sideboard
		//Also, if we don't use p, it doesn't get loaded and things get weird fast!
		for(TilePiece p: TilePiece.values()) drawSideboardPiece(p);
	
		//Draw the existing pieces
		drawPieces();
	    
	    //Test highlighting squares
	    if(selected!=null)highlightSelected();
	    if(sideBoardSelected!=null)highlightSideBoardSelected();
		renderSelected(mouseX, mouseY);

	}

	protected void highlightSideBoardSelected() {
		GlStateManager.enableBlend();
		GlStateManager.disableTexture();
		GlStateManager.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA.field_22545, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA.field_22528, GlStateManager.SrcFactor.ONE.field_22545, GlStateManager.DstFactor.ZERO.field_22528);
		GlStateManager.color4f(0.5f, 0.8f, 0.5f, 0.5f);
		  
		float myHeight=Math.min(height, 256);
		float x1 = width/2f - 128 + (sideBoardSelected.getSide().equals(Side.BLACK)?0:16+24*9);
		float x2 = x1+24;
		float y1 = height/2f - myHeight/2f+(32+sideBoardSelected.index*24)*myHeight/256f;
		float y2 = y1+24*myHeight/256f;
		  
		bufferbuilder.begin(GL11.GL_QUADS, VertexFormats.POSITION);
		bufferbuilder.vertex(x1, y2, getZOffset()).next();
		bufferbuilder.vertex(x2, y2, getZOffset()).next();
		bufferbuilder.vertex(x2, y1, getZOffset()).next();
		bufferbuilder.vertex(x1, y1, getZOffset()).next();
		tessellator.draw();
		GlStateManager.enableTexture();
		GlStateManager.disableBlend();
	}
	
	@Override
	public boolean mouseClicked(double x, double y, int maybeSomethingDragging) {
		boolean mouseClicked = super.mouseClicked(x, y, maybeSomethingDragging);
		float myHeight=Math.min(height, 256);
		final float boardOriginX = width/2f - 128;
		final float boardOriginY = height/2f - myHeight/2f;
		final float sideboardOriginY = boardOriginY+32*myHeight/256f;
		double myX = x-(boardOriginX + 32);
	
		double myY = y-sideboardOriginY;
		Point maybeSelected = Point.create((int)Math.floor(myX/24), (int)Math.floor(myY/(24*myHeight/256f)));
		
		if(maybeSelected instanceof InvalidPoint || maybeSelected.equals(selected)) {
			//Check for sideboard!
			if(y > sideboardOriginY && y < sideboardOriginY+6*24) {
				int index = (int)((y-sideboardOriginY)/24);
				//Check Left Sideboard
				if(x > boardOriginX && x < boardOriginX+24) {
					sideBoardSelected = blackSideboardMap.get(index); 
				} else if(x > (boardOriginX + 16 +24*9) && x < (boardOriginX + 40 +24*9)) { //Check Right Sideboard
					sideBoardSelected = whiteSideboardMap.get(index);
				}
			} else {
				selected = null;
				sideBoardSelected = null;
			}
		} else {
			//Do sideboard selection check
			if(sideBoardSelected != null) {
				notifyServerOfArbitraryPlacement(sideBoardSelected.getPieceInitializer().create(maybeSelected, sideBoardSelected.side));
				selected = null;
				sideBoardSelected = null;
			} else {//Do other Selection Check 
				if(pieceAt(selected) != null) {//Move
					notifyServerOfMove(Move.create(selected, maybeSelected));		
					selected = null;
				} else {//Select
					selected = maybeSelected;
					sideBoardSelected = null;
				}
			}
		}
		return mouseClicked;
	}
	
	protected void notifyServerOfArbitraryPlacement(Piece pi) {
		ClientPlayNetworking.send(new Identifier(ChessMod.MODID, "ap"), ArbitraryPlacement.encode(new ArbitraryPlacement(pi, board.getPos()), PacketByteBufs.create()));
	}
}
