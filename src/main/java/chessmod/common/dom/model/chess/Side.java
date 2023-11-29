package chessmod.common.dom.model.chess;

public enum Side {
	WHITE,
	BLACK;
	
	public Side other() {
		return this.equals(WHITE)?BLACK:WHITE;
	}
}
