package com.htmlweb.chess.client.gui.entity;

import com.htmlweb.chess.ChessMod;
import com.htmlweb.chess.tileentity.WoodChessboardTileEntity;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

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

		
		//Draw highlights of squares that are hovered over that are playable
		
		//Draw highlights of any selected pieces and their valid moves
		
		//Draw highlights of any King under threat and the pieces threatening it
		
		
	}

	private void drawBackground() {
		this.minecraft.getTextureManager().bindTexture(background);
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		float x1 = width/2f - 128;
		float x2 = x1+256;
		float y1 = height/2f - 128;
		float y2 = y1+256;
		bufferbuilder.pos(x1, y2, blitOffset).tex(0,1).endVertex();
		bufferbuilder.pos(x2, y2, blitOffset).tex(1,1).endVertex();
		bufferbuilder.pos(x2, y1, blitOffset).tex(1,0).endVertex();
		bufferbuilder.pos(x1, y1, blitOffset).tex(0,0).endVertex();
		tessellator.draw();
	}
	
	private void drawPiece(int bx, int by, ResourceLocation piece) {
		this.minecraft.getTextureManager().bindTexture(piece);
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		float x1 = width/2f - 128+32+bx*24;
		float x2 = x1+24;
		float y1 = height/2f - 128+32+by*24;
		float y2 = y1+24;
		bufferbuilder.pos(x1, y2, blitOffset).tex(0,1).endVertex();
		bufferbuilder.pos(x2, y2, blitOffset).tex(1,1).endVertex();
		bufferbuilder.pos(x2, y1, blitOffset).tex(1,0).endVertex();
		bufferbuilder.pos(x1, y1, blitOffset).tex(0,0).endVertex();
		tessellator.draw();
	}
	
}
