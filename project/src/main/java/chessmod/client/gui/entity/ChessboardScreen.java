package chessmod.client.gui.entity;

import java.util.HashMap;

import chessmod.ChessMod;
import chessmod.common.dom.model.chess.Move;
import chessmod.common.dom.model.chess.PieceType;
import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.Side;
import chessmod.common.dom.model.chess.board.Board;
import chessmod.common.dom.model.chess.piece.Piece;
import chessmod.common.network.ChessPlay;
import chessmod.block.entity.ChessboardBlockEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public abstract class ChessboardScreen extends Screen {

	protected final Identifier background;
	protected ChessboardBlockEntity board;
	Tessellator tessellator = Tessellator.getInstance();
	BufferBuilder bufferbuilder = tessellator.getBuffer();
	protected Point selected = null;
	protected final WPlainPanel dummy = new WPlainPanel();

	protected static HashMap<Character, TilePiece> pieceMap = new HashMap<Character, TilePiece>();

	protected enum TilePiece {
		WHITE_KING(0, Side.WHITE, PieceType.nmK, 'K', new Identifier(ChessMod.MODID, "textures/gui/wk.png")),
		WHITE_QUEEN(1, Side.WHITE, PieceType.Q, 'Q', new Identifier(ChessMod.MODID, "textures/gui/wq.png")),
		WHITE_ROOK(2, Side.WHITE, PieceType.nmR, 'R', new Identifier(ChessMod.MODID, "textures/gui/wr.png")),
		WHITE_KNIGHT(3, Side.WHITE, PieceType.N, 'N', new Identifier(ChessMod.MODID, "textures/gui/wn.png")),
		WHITE_BISHOP(4, Side.WHITE, PieceType.B, 'B', new Identifier(ChessMod.MODID, "textures/gui/wb.png")),
		WHITE_PAWN(5, Side.WHITE, PieceType.P, 'P', new Identifier(ChessMod.MODID, "textures/gui/wp.png")),
		BLACK_KING(0, Side.BLACK, PieceType.nmK, 'k', new Identifier(ChessMod.MODID, "textures/gui/bk.png")),
		BLACK_QUEEN(1, Side.BLACK, PieceType.Q, 'q', new Identifier(ChessMod.MODID, "textures/gui/bq.png")),
		BLACK_ROOK(2, Side.BLACK, PieceType.nmR, 'r', new Identifier(ChessMod.MODID, "textures/gui/br.png")),
		BLACK_KNIGHT(3, Side.BLACK, PieceType.N, 'n', new Identifier(ChessMod.MODID, "textures/gui/bn.png")),
		BLACK_BISHOP(4, Side.BLACK, PieceType.B, 'b', new Identifier(ChessMod.MODID, "textures/gui/bb.png")),
		BLACK_PAWN(5, Side.BLACK, PieceType.P, 'p', new Identifier(ChessMod.MODID, "textures/gui/bp.png"));
	
		public void draw(ChessboardScreen current, int x, int y) {
			current.drawPiece(x, y, texture);
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
		
		public PieceType getPieceInitializer() {
			return pi;
		}
		
		public Piece create(Point p) {
			return pi.create(p, side);
		}
	
		int index;
		Side side;
		char piece;
		PieceType pi;
		Identifier texture;
		
		public Identifier getTexture() {
			return texture;
		}
	
		TilePiece(int i, Side s, PieceType pi, char c, Identifier texture) {
			this.index=i;
			this.side=s;
			this.pi=pi;
			this.piece=c;
			this.texture = texture;
			pieceMap.put(c, this);
		}
		
	}

	public ChessboardScreen(ChessboardBlockEntity board) {
		super(null);
		background = new Identifier(ChessMod.MODID, "textures/gui/chessboard.png");
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

	}

	protected Piece pieceAt(Point s) {
		if(s == null) return null;
		return board.getBoard().pieceAt(Point.create(s.x, s.y));
	}
	
	protected void notifyServerOfMove(Move m) {
		ClientPlayNetworking.send(new Identifier(ChessMod.MODID, "cp"), ChessPlay.encode(new ChessPlay(m.serialize(), board.getPos()), PacketByteBufs.create()));
	}

	protected void drawBackground() {
		float myHeight=Math.min(height, 256);
		float x1 = width/2f - 128;
		float x2 = x1+256;
		float y1 = height/2f - myHeight/2F;
		float y2 = y1+myHeight;
		dummy.setSize((int) (x2 - x1) + 100, (int) (y2 - y1) + 10);
		dummy.setLocation((int) (x1 - 50), (int) (y1 - 5));
		BackgroundPainter.VANILLA.paintBackground(dummy.getX(), dummy.getY(), dummy);

		this.client.getTextureManager().bindTexture(background);
		bufferbuilder.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
		bufferbuilder.vertex(x1, y2, getZOffset()).texture(0,1).next();
		bufferbuilder.vertex(x2, y2, getZOffset()).texture(1,1).next();
		bufferbuilder.vertex(x2, y1, getZOffset()).texture(1,0).next();
		bufferbuilder.vertex(x1, y1, getZOffset()).texture(0,0).next();
		bufferbuilder.end();
		BufferRenderer.draw(bufferbuilder);
	}

	protected static class Color4f {
		float r,g,b,a;

		public Color4f(float r, float g, float b, float a) {
			super();
			this.r = r;
			this.g = g;
			this.b = b;
			this.a = a;
		}

		public static Color4f SELECTED = new Color4f(0.5F, 0.5F, 0.8F, 0.5F);
		public static Color4f POSSIBLE = new Color4f(0.5F, 0.8F, 0.5F, 0.5F);
		public static Color4f CHECK = new Color4f(0.9F, 0.1F, 0.1F, 0.5F);
		public static Color4f CHECKMATE = new Color4f(0.9F, 0.1F, 0.1F, 0.9F);
		public static Color4f WHITE = new Color4f(1F, 1F, 1F, 1F);
		
		public void apply() {
			GlStateManager.color4f(r, g, b, a);
		}
	}

	public void renderSelected(int mouseX, int mouseY) {
		if (selected != null) {
			this.drawPiece(mouseX, mouseY, pieceMap.get(board.getBoard().pieceAt(selected).getCharacter()).texture);
		}
	}

	protected void highlightSelected() {
		highlightSquare(selected, Color4f.SELECTED);
	}

	public void highlightSquare(Point thisselected, Color4f chosenColor) {
		float myHeight=Math.min(height, 256);
		float x1 = width/2f - 128+32+thisselected.x*24;
		float x2 = x1+24;
		float y1 = height/2f - myHeight/2f+(32+thisselected.y*24)*myHeight/256f;
		float y2 = y1+24*myHeight/256f;
		
		Vec2f p1 = new Vec2f(x1, y1);
		Vec2f p2 = new Vec2f(x2, y2);
		
		
		highlightSquare(p1, p2, chosenColor);
	}

	protected void highlightSquare(Vec2f p1, Vec2f p2, Color4f c) {
		GlStateManager.enableBlend();
		GlStateManager.disableTexture();
		GlStateManager.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA.field_22545, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA.field_22528);

		c.apply();
		  
		bufferbuilder.begin(GL11.GL_QUADS, VertexFormats.POSITION);
		bufferbuilder.vertex(p1.x, p2.y, getZOffset()).next();
		bufferbuilder.vertex(p2.x, p2.y, getZOffset()).next();
		bufferbuilder.vertex(p2.x, p1.y, getZOffset()).next();
		bufferbuilder.vertex(p1.x, p1.y, getZOffset()).next();
		bufferbuilder.end();
		BufferRenderer.draw(bufferbuilder);
		GlStateManager.enableTexture();
		
		Color4f.WHITE.apply();
		GlStateManager.disableBlend();
	}



	protected void drawPiece(int bx, int by, Identifier piece) {
		float myHeight=Math.min(height, 256);
		this.client.getTextureManager().bindTexture(piece);
		bufferbuilder.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
		float x1 = width/2f - 128+32+bx*24;
		float x2 = x1+24;
		float y1 = height/2f - myHeight/2f+(32+by*24)*myHeight/256f;
		float y2 = y1+24*myHeight/256f;
		bufferbuilder.vertex(x1, y2, getZOffset()).texture(0,1).next();
		bufferbuilder.vertex(x2, y2, getZOffset()).texture(1,1).next();
		bufferbuilder.vertex(x2, y1, getZOffset()).texture(1,0).next();
		bufferbuilder.vertex(x1, y1, getZOffset()).texture(0,0).next();
		bufferbuilder.end();
		BufferRenderer.draw(bufferbuilder);
	}

	protected void drawSideboardPiece(TilePiece piece) {
		float myHeight=Math.min(height, 256);
		this.client.getTextureManager().bindTexture(piece.texture);
	
		float x1 = width/2f - 128+(piece.side.equals(Side.BLACK)?0:16+9*24);
		float x2 = x1+24;
		float y1 = height/2f - myHeight/2f+(32+piece.index*24)*myHeight/256f;
		float y2 = y1+24*myHeight/256f;


		bufferbuilder.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);

		bufferbuilder.vertex(x1, y2, getZOffset()).texture(0,1).next();
		bufferbuilder.vertex(x2, y2, getZOffset()).texture(1,1).next();
		bufferbuilder.vertex(x2, y1, getZOffset()).texture(1,0).next();
		bufferbuilder.vertex(x1, y1, getZOffset()).texture(0,0).next();
		bufferbuilder.end();
		BufferRenderer.draw(bufferbuilder);
	}

	protected void drawPieces() {
		Board b = board.getBoard();
	    for(int by = 0; by < 8; by++) { 
			for(int bx = 0; bx < 8; bx++) {
				Piece piece = b.pieceAt(Point.create(bx, by));
				if(piece != null) {
					pieceMap.get(piece.getCharacter()).draw(this, bx, by);
				}
			}
	    }
	}
}
