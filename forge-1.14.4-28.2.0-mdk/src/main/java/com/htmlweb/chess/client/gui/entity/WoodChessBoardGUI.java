package com.htmlweb.chess.client.gui.entity;

import java.util.HashMap;

import javax.vecmath.Point2i;

import org.lwjgl.opengl.GL11;

import com.htmlweb.chess.ChessMod;
import com.htmlweb.chess.common.network.ChessPlay;
import com.htmlweb.chess.common.network.ChessPromotion;
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
	
	WoodChessboardTileEntity board;
	
	Tessellator tessellator = Tessellator.getInstance();
	BufferBuilder bufferbuilder = tessellator.getBuffer();
	
	private Point2i selected = null;
	private Piece sideBoardSelected = null;
	
	private static HashMap<Character, Piece> pieceMap = new HashMap<Character, Piece>();
	private static HashMap<Integer, Piece> blackSideboardMap = new HashMap<Integer, Piece>();
	private static HashMap<Integer, Piece> whiteSideboardMap = new HashMap<Integer, Piece>();
	
	private static enum Side {
		BLACK,
		WHITE;
	}
	
	private enum Piece {
		WHITE_KING(0, Side.WHITE, 'K', new ResourceLocation(ChessMod.MODID, "textures/gui/wk.png")),
		WHITE_QUEEN(1, Side.WHITE, 'Q', new ResourceLocation(ChessMod.MODID, "textures/gui/wq.png")),
		WHITE_ROOK(2, Side.WHITE, 'R', new ResourceLocation(ChessMod.MODID, "textures/gui/wr.png")),
		WHITE_KNIGHT(3, Side.WHITE, 'N', new ResourceLocation(ChessMod.MODID, "textures/gui/wn.png")),
		WHITE_BISHOP(4, Side.WHITE, 'B', new ResourceLocation(ChessMod.MODID, "textures/gui/wb.png")),
		WHITE_PAWN(5, Side.WHITE, 'P', new ResourceLocation(ChessMod.MODID, "textures/gui/wp.png")),
		BLACK_KING(0, Side.BLACK, 'k', new ResourceLocation(ChessMod.MODID, "textures/gui/bk.png")),
		BLACK_QUEEN(1, Side.BLACK, 'q', new ResourceLocation(ChessMod.MODID, "textures/gui/bq.png")),
		BLACK_ROOK(2, Side.BLACK, 'r', new ResourceLocation(ChessMod.MODID, "textures/gui/br.png")),
		BLACK_KNIGHT(3, Side.BLACK, 'n', new ResourceLocation(ChessMod.MODID, "textures/gui/bn.png")),
		BLACK_BISHOP(4, Side.BLACK, 'b', new ResourceLocation(ChessMod.MODID, "textures/gui/bb.png")),
		BLACK_PAWN(5, Side.BLACK, 'p', new ResourceLocation(ChessMod.MODID, "textures/gui/bp.png"));


		
		
		public void draw(WoodChessBoardGUI current, int x, int y) {
			current.drawPiece(x, y, tile);
		}
		
		public char getPiece() {
			return piece;
		}

		public int getIndex() {
			return index;
		}

		public Side getSide() {
			return side;
		}

		int index;
		Side side;
		char piece;
		ResourceLocation tile;
		
		public ResourceLocation getTile() {
			return tile;
		}

		Piece(int i, Side s, char c, ResourceLocation tile) {
			this.index=i;
			this.side=s;
			this.piece=c;
			this.tile=tile;
			pieceMap.put(c, this);
			if(s.equals(Side.BLACK)) blackSideboardMap.put(i, this);
			else whiteSideboardMap.put(i, this);
		}
		
	}
	
	public WoodChessBoardGUI(WoodChessboardTileEntity board) {
		super(null);
		background = new ResourceLocation(ChessMod.MODID, "textures/gui/chessboard.png");
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
		
		//Draw sideboard
		//Also, if we don't use p, it doesn't get loaded and things get weird fast!
		for(Piece p: Piece.values()) drawSideboardPiece(p);

		//Draw the existing pieces
		char[][] squares = board.getBoardState();	
        //"rnbqkbnrpppppppp................................PPPPPPPPRNBQKBNR"
        for(int by = 0; by < 8; by++) { 
			for(int bx = 0; bx < 8; bx++) {
				Piece p = pieceMap.get(squares[by][bx]);
				if(p != null) p.draw(this, bx, by);
			}
        }

        //Draw pieces on the side for WoodChessBoard users to add pieces at will.
        
        //Test highlighting squares
        if(selected!=null)highlightSelected();
        if(sideBoardSelected!=null)highlightSideBoardSelected();

		//Draw highlights of squares that are hovered over that are playable
		
		//Draw highlights of any selected pieces and their valid moves
		
		//Draw highlights of any King under threat and the pieces threatening it
		
		
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
		Point2i maybeSelected = new Point2i((int)(myX/24), (int)(myY/(24*myHeight/256f)));
		if(myX < 0 || myY < 0 || maybeSelected.x>7 || maybeSelected.y>7 || maybeSelected.equals(selected)) {
			
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
			ChessMod.LOGGER.debug("Chessboard Square Selected: " + selected);
			
			//Do sideboard selection check
			if(sideBoardSelected != null) {
				notifyServerOfPromotion(sideBoardSelected.piece, maybeSelected);
				sideBoardSelected = null;
			} else {//Do other Selection Check 
				if(selected != null && board.getBoardState()[selected.y][selected.x] != '.') {//Move
					notifyServerOfMove(maybeSelected);		
					selected = null;
				} else {//Select
					selected = maybeSelected;
					sideBoardSelected = null;
				} 
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
	
	private void notifyServerOfPromotion(char c, Point2i maybeSelected) {
		ChessMod.LOGGER.debug("Notifying Server of Promotion: " + selected);
		PacketHandler.sendToServer(
				new ChessPromotion(
						c, 
						maybeSelected, 
						board.getPos().getX(), 
						board.getPos().getY(), 
						board.getPos().getZ()));
	}

	private void drawBackground() {
		this.minecraft.getTextureManager().bindTexture(background);
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
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
		GlStateManager.enableBlend();
		GlStateManager.disableTexture();
		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.color4f(0.5f, 0.5f, 0.8f, 0.5f);
		  
		float myHeight=Math.min(height, 256);
		float x1 = width/2f - 128+32+selected.x*24;
		float x2 = x1+24;
		float y1 = height/2f - myHeight/2f+(32+selected.y*24)*myHeight/256f;
		float y2 = y1+24*myHeight/256f;
		  
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		bufferbuilder.pos(x1, y2, blitOffset).endVertex();
		bufferbuilder.pos(x2, y2, blitOffset).endVertex();
		bufferbuilder.pos(x2, y1, blitOffset).endVertex();
		bufferbuilder.pos(x1, y1, blitOffset).endVertex();
		tessellator.draw();
		GlStateManager.enableTexture();
		GlStateManager.disableBlend();
	}
	
	private void highlightSideBoardSelected() {
		GlStateManager.enableBlend();
		GlStateManager.disableTexture();
		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.color4f(0.5f, 0.8f, 0.5f, 0.5f);
		  
		float myHeight=Math.min(height, 256);
		float x1 = width/2f - 128 + (sideBoardSelected.getSide().equals(Side.BLACK)?0:16+24*9);
		float x2 = x1+24;
		float y1 = height/2f - myHeight/2f+(32+sideBoardSelected.index*24)*myHeight/256f;
		float y2 = y1+24*myHeight/256f;
		  
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
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
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
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
	
	private void drawSideboardPiece(Piece piece) {
		float myHeight=Math.min(height, 256);
		this.minecraft.getTextureManager().bindTexture(piece.tile);

		float x1 = width/2f - 128+(piece.side.equals(Side.BLACK)?0:16+9*24);
		float x2 = x1+24;
		float y1 = height/2f - myHeight/2f+(32+piece.index*24)*myHeight/256f;
		float y2 = y1+24*myHeight/256f;
		
		
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		bufferbuilder.pos(x1, y2, blitOffset).tex(0,1).endVertex();
		bufferbuilder.pos(x2, y2, blitOffset).tex(1,1).endVertex();
		bufferbuilder.pos(x2, y1, blitOffset).tex(1,0).endVertex();
		bufferbuilder.pos(x1, y1, blitOffset).tex(0,0).endVertex();
		tessellator.draw();
	}
	
}
