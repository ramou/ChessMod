package chessmod.client.gui.entity;

import java.util.HashMap;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import chessmod.blockentity.ChessboardBlockEntity;
import chessmod.common.dom.model.chess.Move;
import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.Point.InvalidPoint;
import chessmod.common.dom.model.chess.Side;
import chessmod.common.dom.model.chess.piece.Piece;
import chessmod.common.network.ArbitraryPlacement;
import chessmod.common.network.PacketHandler;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WoodChessboardGUI extends ChessboardGUI {

	public WoodChessboardGUI(ChessboardBlockEntity board) {
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
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {

	    //Draw the background
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, background);
		drawBackground(poseStack);
		//Draw sideboard
		//Also, if we don't use p, it doesn't get loaded and things get weird fast!
		for(TilePiece p: TilePiece.values()) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			drawSideboardPiece(poseStack, p);
		}

		//Draw the existing pieces
		drawPieces(poseStack);

	    //Test highlighting squares
	    if(selected!=null) {
	    	highlightSelected(poseStack);
	    }
	    
	    if(sideBoardSelected!=null) {
	    	highlightSideBoardSelected(poseStack);
	    }
	    

	}

	protected void highlightSideBoardSelected(PoseStack poseStack) {
		int x = (int)(width/2f - 128 + (sideBoardSelected.getSide().equals(Side.BLACK)?0:16+24*9));
		int y = (int)(height/2f - 128 + (32+sideBoardSelected.index*24));
		highlightSquare(poseStack, x, y, POSSIBLE);
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
		PacketHandler.sendToServer(new ArbitraryPlacement(pi, board.getBlockPos()));
	}
}
