package chessmod.common.dom.model.chess.board;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import chessmod.common.dom.model.chess.Move;
import chessmod.common.dom.model.chess.PieceInitializer;
import chessmod.common.dom.model.chess.Point;
import chessmod.common.dom.model.chess.Side;
import chessmod.common.dom.model.chess.piece.Piece;

/*
 * Board State
 * Four-bit representation
 * Three bits for piece type and move state for king/rook
 *   P 000
 *   B 001
 *   N 010
 *   Q 011
 * nmR 100
 *  mR 101
 * nmK 110
 *  mK 111
 * One bit for color
 *   W 0
 *   B 1
 * 
 * Four bits times a maximum of 32 pieces is 2 longs. 
 * A third long gives a bitmask of positions from 0,0 
 * at the top-left, with black being along the top.
 * 
 * 
 * Moves
 * Three-bit sourceX
 * Three-bit sourceY
 * Three-bit targetX
 * Three-bit targetY
 * Four-bit promotion | 0000 | 0001 if it was a pawn performing en passant
 * we don't use the last bit
 * 
 * Special Move State
 * 0000000000000000 this is not a move, you're probably de-serializing an incomplete frame and can stop
 * 0000000000000001 white concedes
 * 0000000000000010 black concedes
 * 0000000000000011 players agreed to stalemate
 * 0000000000000100 the previous move resulted in checkmate and the game is won
 * 0000000000000101 the previous move resulted in no possible additional moves and the game is stalemated
 * 0000000000000110 The player applied the threefold repetition rule so the game is stalemated
 * 0000000000001000 termination, the previous move was the last move, or no moves yet if this is the first entry

 */
public class SerializedBoard {
	
	public SerializedBoard(Long piece_mask, long[] pieces, long[] moves) {
		super();
		this.piece_mask = piece_mask;
		this.pieces = pieces;
		this.moves = moves;
	}
	public Long piece_mask;
	public long[] pieces;
	public long[] moves;
	
	
	public static SerializedBoard serialize(Board b) {
		Long myPieceMask = 0L;
		long piece = 0;
		long moveBatch = 0;
		
		List<Long> boardState = new LinkedList<Long>();
		List<Long> movesState = new LinkedList<Long>();

		//Remember, the first long is actually the last long because of FIFO
		//That means our second long must be the full one and the first one
		//is partially full. If your head is in the right space, it makes sense...
		int pieceCount = 0;
		for(int y = 0; y < 8; y++) {
			for(int x = 0; x < 8; x++) {
				if(b.pieces[y][x] != null) pieceCount++;
			}
		}
		int i = -(pieceCount % 16);
		for(int y = 0; y < 8; y++) {
			for(int x = 0; x < 8; x++) {
				Piece p = b.pieces[y][x];
				if(p != null) {
					myPieceMask |= 1;
					piece |= p.serialize();
					if(++i % 16 == 0) {
						boardState.add(0,piece);
						piece=0;
					} else {
						piece <<= 4;
					}
					
				}
				if((x != 7) || (y != 7)) myPieceMask <<= 1;
			}
		}
		
		i = -(b.moves.size()%4);
		for(Move m: b.moves) {
			moveBatch |= m.serialize();
			if(++i % 4 == 0) {
				movesState.add(0,moveBatch);
				moveBatch=0;
			} else {
				moveBatch <<= 16;
			}
		}

		return new SerializedBoard(myPieceMask, ArrayUtils.toPrimitive(boardState.toArray(new Long[] {})), ArrayUtils.toPrimitive(movesState.toArray(new Long[] {})));
	}


	public Board deSerialize() {
		Board b = new Board();
		
		BitSet pieceMaskSet = BitSet.valueOf(new long[]{this.piece_mask});
		BitSet piecesSet = BitSet.valueOf(this.pieces);
		int count = pieceMaskSet.cardinality();
		int index = pieceMaskSet.nextSetBit(0);
	
		//Build that board
		for(int i=0; i < count; i++) {
			long pieceBits = (piecesSet.get(i*4, (i+1)*4).toLongArray().length==1)?piecesSet.get(i*4, (i+1)*4).toLongArray()[0]:0;
			Side s = Side.values()[(int)(pieceBits & 0b0001)];
			Point p = new Point((7-(index%8)), 7-(index>>>3));
			Piece piece = PieceInitializer.values()[(int)(pieceBits & 0b1110)>>>1].create(p, s);
			b.setPiece(piece, p);
			index = pieceMaskSet.nextSetBit(index+1);
		}
		
		BitSet serializedMoves = BitSet.valueOf(this.moves);
		
		int i = 0;
		while(serializedMoves.get(i*16, (i+1)*16).toLongArray().length>0) {
			final long moveBits = serializedMoves.get(i*16, (++i)*16).toLongArray()[0];
			final Move m = Move.create((int)moveBits, b);
			b.moves.add(0, m);
		}
		
		return b;
		
	}
	
}