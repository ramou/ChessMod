package chessmod.client.gui.entity;

import java.util.HashMap;

import com.mojang.blaze3d.systems.RenderSystem;

import chessmod.ChessMod;
import chessmod.blockentity.ChessboardBlockEntity;
import chessmod.common.dom.model.chess.Move;
import chessmod.common.dom.model.chess.PieceInitializer;
import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.Side;
import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.piece.Piece;
import chessmod.common.network.ChessPlay;
import chessmod.common.network.PacketHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;


public abstract class ChessboardGUI extends Screen {
	
	protected final ResourceLocation background;
	protected ChessboardBlockEntity board;
	protected Point selected = null;

	public static ResourceLocation SELECTED = new ResourceLocation(ChessMod.MODID, "textures/gui/shadeselected.png");
	public static ResourceLocation POSSIBLE = new ResourceLocation(ChessMod.MODID, "textures/gui/shadepossible.png");
	public static ResourceLocation CHECK = new ResourceLocation(ChessMod.MODID, "textures/gui/shadecheck.png");
	public static ResourceLocation CHECKMATE = new ResourceLocation(ChessMod.MODID, "textures/gui/shadecheckmate.png");
	public static ResourceLocation WHITE = new ResourceLocation(ChessMod.MODID, "textures/gui/shadewhite.png");
	public static ResourceLocation BLACK = new ResourceLocation(ChessMod.MODID, "textures/gui/shadeblack.png");
	
	protected static HashMap<Character, TilePiece> pieceMap = new HashMap<>();

	protected enum TilePiece {
		WHITE_KING(0, Side.WHITE, PieceInitializer.nmK, 'K', new ResourceLocation(ChessMod.MODID, "textures/gui/wk.png")),
		WHITE_QUEEN(1, Side.WHITE, PieceInitializer.Q, 'Q', new ResourceLocation(ChessMod.MODID, "textures/gui/wq.png")),
		WHITE_ROOK(2, Side.WHITE, PieceInitializer.nmR, 'R', new ResourceLocation(ChessMod.MODID, "textures/gui/wr.png")),
		WHITE_KNIGHT(3, Side.WHITE, PieceInitializer.N, 'N', new ResourceLocation(ChessMod.MODID, "textures/gui/wn.png")),
		WHITE_BISHOP(4, Side.WHITE, PieceInitializer.B, 'B', new ResourceLocation(ChessMod.MODID, "textures/gui/wb.png")),
		WHITE_PAWN(5, Side.WHITE, PieceInitializer.P, 'P', new ResourceLocation(ChessMod.MODID, "textures/gui/wp.png")),
		BLACK_KING(0, Side.BLACK, PieceInitializer.nmK, 'k', new ResourceLocation(ChessMod.MODID, "textures/gui/bk.png")),
		BLACK_QUEEN(1, Side.BLACK, PieceInitializer.Q, 'q', new ResourceLocation(ChessMod.MODID, "textures/gui/bq.png")),
		BLACK_ROOK(2, Side.BLACK, PieceInitializer.nmR, 'r', new ResourceLocation(ChessMod.MODID, "textures/gui/br.png")),
		BLACK_KNIGHT(3, Side.BLACK, PieceInitializer.N, 'n', new ResourceLocation(ChessMod.MODID, "textures/gui/bn.png")),
		BLACK_BISHOP(4, Side.BLACK, PieceInitializer.B, 'b', new ResourceLocation(ChessMod.MODID, "textures/gui/bb.png")),
		BLACK_PAWN(5, Side.BLACK, PieceInitializer.P, 'p', new ResourceLocation(ChessMod.MODID, "textures/gui/bp.png"));
	
		public void draw(GuiGraphics guiGraphics, ChessboardGUI current, int x, int y) {
			current.drawPiece(guiGraphics, x, y, tile);
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
		
		public PieceInitializer getPieceInitializer() {
			return pi;
		}
		
		public Piece create(Point p) {
			return pi.create(p, side);
		}
	
		final int index;
		final Side side;
		final char piece;
		final PieceInitializer pi;
		final ResourceLocation tile;

		TilePiece(int i, Side s, PieceInitializer pi, char c, ResourceLocation tile) {
			this.index=i;
			this.side=s;
			this.pi=pi;
			this.piece=c;
			this.tile=tile;
			pieceMap.put(c, this);
		}
		
	}

	public ChessboardGUI(ChessboardBlockEntity board) {
		super(Component.empty());
		background = new ResourceLocation(ChessMod.MODID, "textures/gui/chessboard.png");
		this.board = board;
	}

	
	@Override
	public @NotNull Component getNarrationMessage() {
		return Component.literal("");
	}


	@Override
	public boolean isPauseScreen(){
		return false;
	}

	@Override
	public void init() {

	}

	protected Piece pieceAt(Point s) {
		if(s == null) return null;
		return board.getBoard().pieceAt(Point.create(s.x, s.y));
	}
	
	protected void notifyServerOfMove(Move m) {
		//When running on a local server the client update doesn't happen till
		//you "unpause" by closing the gui, so this fudges things.
		if(getMinecraft().isLocalServer()) {
			m.doMove(board.getBoard());
		}
		
		PacketHandler.sendToServer(new ChessPlay(m.serialize(), board.getBlockPos()));
	}


	protected void drawBackground(GuiGraphics guiGraphics) {
		int x1 = (int)(width/2f - 128);
		int y1 = (int)(height/2f - 128);
		guiGraphics.blit(background, x1, y1, 0, 0, 256, 256);
	}

	
	protected void highlightSelected(GuiGraphics guiGraphics) {
		highlightSquare(guiGraphics, selected, SELECTED);
	}

	public void highlightSquare(GuiGraphics guiGraphics, Point target, ResourceLocation shade) {
		int x = (int)(width/2f - 128+32+target.x*24);
		int y = (int)(height/2f - 128 + (32+target.y*24));

		highlightSquare(guiGraphics, x, y, shade);
	}

	protected void highlightSquare(GuiGraphics guiGraphics, int x, int y, ResourceLocation shade) {
		highlightSquare(guiGraphics, x, y, 24, 24, shade);
	}

	protected void highlightSquare(GuiGraphics guiGraphics, int x, int y, int width, int height, ResourceLocation shade) {
		guiGraphics.blit(shade, x, y, width, height, width, height);
	}


	protected void drawPiece(GuiGraphics guiGraphics, int bx, int by, ResourceLocation piece) {
		int x1 = (int)(width/2f - 128 + 32 + bx * 24);
		int y1 = (int)(height/2f - 128 + 32 + by * 24);
		guiGraphics.blit(piece, x1, y1, 0, 0, 24, 24, 24, 24);
	}

	protected void drawSideboardPiece(GuiGraphics guiGraphics, TilePiece piece) {
		int x1 = (int)(width/2f - 128+(piece.side.equals(Side.BLACK)?0:16+9*24));
		int y1 = (int)(height/2f - 128 + 32 + piece.index * 24);
		guiGraphics.blit(piece.tile, x1, y1, 0, 0, 24, 24, 24, 24);
	}

	protected void drawPieces(GuiGraphics guiGraphics) {
		Board b = board.getBoard();
	    for(int by = 0; by < 8; by++) { 
			for(int bx = 0; bx < 8; bx++) {
				Piece piece = b.pieceAt(Point.create(bx, by));
				if(piece != null) {
					RenderSystem.setShader(GameRenderer::getPositionTexShader);
					pieceMap.get(piece.getCharacter()).draw(guiGraphics, this, bx, by);
				}
			}
	    }
	}

}