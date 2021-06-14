package chessmod.client.gui.entity;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;

import chessmod.common.Point2f;
import chessmod.common.dom.model.chess.Move;
import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.Point.InvalidPoint;
import chessmod.common.dom.model.chess.Side;
import chessmod.common.dom.model.chess.piece.Piece;
import chessmod.common.network.ArbitraryPlacement;
import chessmod.common.network.PacketHandler;
import chessmod.tileentity.ChessboardTileEntity;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WoodChessboardGUI extends ChessboardGUI {

	public WoodChessboardGUI(ChessboardTileEntity board) {
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
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
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

	}

	protected void highlightSideBoardSelected() {
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		  
		float myHeight=Math.min(height, 256);
		float x1 = width/2f - 128 + (sideBoardSelected.getSide().equals(Side.BLACK)?0:16+24*9);
		float x2 = x1+24;
		float y1 = height/2f - myHeight/2f+(32+sideBoardSelected.index*24)*myHeight/256f;
		float y2 = y1+24*myHeight/256f;
		  
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		Color4f.POSSIBLE.draw2DRect(bufferbuilder, new Point2f(x1,y1), new Point2f(x2,y2));

		tessellator.draw();
		RenderSystem.enableTexture();		
		RenderSystem.disableBlend();
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
		PacketHandler.sendToServer(new ArbitraryPlacement(pi, board.getPos()));
	}
}
