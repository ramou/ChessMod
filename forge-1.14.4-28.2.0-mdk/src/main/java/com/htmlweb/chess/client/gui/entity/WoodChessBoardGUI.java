package com.htmlweb.chess.client.gui.entity;

import javax.vecmath.Point2i;

import com.htmlweb.chess.ChessMod;
import com.htmlweb.chess.common.network.ChessPlay;
import com.htmlweb.chess.common.network.PacketHandler;
import com.htmlweb.chess.tileentity.WoodChessboardTileEntity;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WoodChessBoardGUI extends Screen {

	private final ResourceLocation background;
	private final ResourceLocation bb;
	private final ResourceLocation wb;
	private final ResourceLocation bk;
	private final ResourceLocation wk;
	private final ResourceLocation bn;
	private final ResourceLocation wn;
	private final ResourceLocation bp;
	private final ResourceLocation wp;
	private final ResourceLocation bq;
	private final ResourceLocation wq;
	private final ResourceLocation br;
	private final ResourceLocation wr;
	
	WoodChessboardTileEntity board;
	
	Tessellator tessellator = Tessellator.getInstance();
	BufferBuilder bufferbuilder = tessellator.getBuffer();
	
	private Point2i selected = null;
	
	public WoodChessBoardGUI(WoodChessboardTileEntity board) {
		super(null);
		background = new ResourceLocation(ChessMod.MODID, "textures/gui/chessboard.png");
		bb = new ResourceLocation(ChessMod.MODID, "textures/gui/bb.png");
		wb = new ResourceLocation(ChessMod.MODID, "textures/gui/wb.png");
		bk = new ResourceLocation(ChessMod.MODID, "textures/gui/bk.png");
		wk = new ResourceLocation(ChessMod.MODID, "textures/gui/wk.png");
		bn = new ResourceLocation(ChessMod.MODID, "textures/gui/bn.png");
		wn = new ResourceLocation(ChessMod.MODID, "textures/gui/wn.png");
		bp = new ResourceLocation(ChessMod.MODID, "textures/gui/bp.png");
		wp = new ResourceLocation(ChessMod.MODID, "textures/gui/wp.png");
		bq = new ResourceLocation(ChessMod.MODID, "textures/gui/bq.png");
		wq = new ResourceLocation(ChessMod.MODID, "textures/gui/wq.png");
		br = new ResourceLocation(ChessMod.MODID, "textures/gui/br.png");
		wr = new ResourceLocation(ChessMod.MODID, "textures/gui/wr.png");
		this.board = board;

	}
	
	@Override
	public String getNarrationMessage() {
		return "";
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}
		
	@Override
	public boolean isPauseScreen() {
		return true;
	}
	
	@Override
	public void init() {
	    //TODO buttons, etc
	}
	
	@Override
	public void render(int par1, int par2, float par3) {
	    //Draw the background
		drawBackground();
		

		//Draw the existing pieces
		char[][] squares = board.getBoardState();	
        //"rnbqkbnrpppppppp................................PPPPPPPPRNBQKBNR"
        for(int by = 0; by < 8; by++) { 
       	 for(int bx = 0; bx < 8; bx++) {
       		 
	        	 switch(squares[by][bx]) {
	        	 	case 'r':
	        	 		drawPiece(bx, by, br);
	        	 		break;
	        	 	case 'n':
	        	 		drawPiece(bx, by, bn);
	        	 		break;
	        	 	case 'b':
	        	 		drawPiece(bx, by, bb);
	        	 		break;
	        	 	case 'q':
	        	 		drawPiece(bx, by, bq);
	        	 		break;
	        	 	case 'k':
	        	 		drawPiece(bx, by, bk);
	        	 		break;
	        	 	case 'p':
	        	 		drawPiece(bx, by, bp);
	        	 		break;
	        	 	case 'R':
	        	 		drawPiece(bx, by, wr);
	        	 		break;
	        	 	case 'N':
	        	 		drawPiece(bx, by, wn);
	        	 		break;
	        	 	case 'B':
	        	 		drawPiece(bx, by, wb);
	        	 		break;
	        	 	case 'Q':
	        	 		drawPiece(bx, by, wq);
	        	 		break;
	        	 	case 'K':
	        	 		drawPiece(bx, by, wk);
	        	 		break;
	        	 	case 'P':
	        	 		drawPiece(bx, by, wp);
	        	 		break;
	        	 }
       	 }
        }

        //Test highlighting squares
        if(selected!=null)highlightSelected();
		
		//Draw highlights of squares that are hovered over that are playable
		
		//Draw highlights of any selected pieces and their valid moves
		
		//Draw highlights of any King under threat and the pieces threatening it
		
		
	}

	
	@Override
	public boolean mouseClicked(double x, double y, int maybeSomethingDragging) {
		boolean mouseClicked = super.mouseClicked(x, y, maybeSomethingDragging);
		float myHeight=Math.min(height, 256);
		double myX = x-(width/2f - 128 + 32);
		double myY = y-(height/2f - myHeight/2f+32*myHeight/256f);
		Point2i maybeSelected = new Point2i((int)(myX/24), (int)(myY/(24*myHeight/256f)));
		if(myX < 0 || myY < 0 || maybeSelected.x>7 || maybeSelected.y>7 || maybeSelected.equals(selected)) {
			selected=null;
		} else {
			ChessMod.LOGGER.debug("Chessboard Square Selected: " + selected);
			if(selected != null && board.getBoardState()[selected.y][selected.x] != '.') {//Move
				
				/*
				char c = board.getBoardState()[selected.y][selected.x];
				board.getBoardState()[selected.y][selected.x]='.';
				board.getBoardState()[maybeSelected.y][maybeSelected.x]=c;
				*/
				notifyServerOfMove(maybeSelected);
				
				selected = null;
			} else {//Select
				selected = maybeSelected;
			}
		}
		return mouseClicked;
	}

	private void notifyServerOfMove(Point2i maybeSelected) {
		ChessMod.LOGGER.debug("Notifying Server of Move: " + selected);
		PacketHandler.sendToServer(
				new ChessPlay(
						selected, 
						maybeSelected, 
						ChessPlay.PieceType.PAWN, 
						board.getPos().getX(), 
						board.getPos().getY(), 
						board.getPos().getZ()));
	}

	private void drawBackground() {
		this.minecraft.getTextureManager().bindTexture(background);
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		float myHeight=Math.min(height, 256);
		float x1 = width/2f - 128;
		float x2 = x1+256;
		float y1 = height/2f - myHeight/2F;
		float y2 = y1+myHeight;
		bufferbuilder.pos(x1, y2, blitOffset).tex(0,1).endVertex();
		bufferbuilder.pos(x2, y2, blitOffset).tex(1,1).endVertex();
		bufferbuilder.pos(x2, y1, blitOffset).tex(1,0).endVertex();
		bufferbuilder.pos(x1, y1, blitOffset).tex(0,0).endVertex();
		tessellator.draw();
	}
	
	private void highlightSelected() {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture();
		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.color4f(0.5f, 0.5f, 0.8f, 0.5f);
		  
		float myHeight=Math.min(height, 256);
		float x1 = width/2f - 128+32+selected.x*24;
		float x2 = x1+24;
		float y1 = height/2f - myHeight/2f+(32+selected.y*24)*myHeight/256f;
		float y2 = y1+24*myHeight/256f;
		  
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
		bufferbuilder.pos(x1, y2, blitOffset).endVertex();
		bufferbuilder.pos(x2, y2, blitOffset).endVertex();
		bufferbuilder.pos(x2, y1, blitOffset).endVertex();
		bufferbuilder.pos(x1, y1, blitOffset).endVertex();
		tessellator.draw();
		GlStateManager.enableTexture();
		GlStateManager.disableBlend();
	}
	
	private void drawPiece(int bx, int by, ResourceLocation piece) {
		float myHeight=Math.min(height, 256);
		this.minecraft.getTextureManager().bindTexture(piece);
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		float x1 = width/2f - 128+32+bx*24;
		float x2 = x1+24;
		float y1 = height/2f - myHeight/2f+(32+by*24)*myHeight/256f;
		float y2 = y1+24*myHeight/256f;
		bufferbuilder.pos(x1, y2, blitOffset).tex(0,1).endVertex();
		bufferbuilder.pos(x2, y2, blitOffset).tex(1,1).endVertex();
		bufferbuilder.pos(x2, y1, blitOffset).tex(1,0).endVertex();
		bufferbuilder.pos(x1, y1, blitOffset).tex(0,0).endVertex();
		tessellator.draw();
	}
	
}
