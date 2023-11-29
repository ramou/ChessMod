package chessmod.common.dom.model.chess;

public class Point {
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public static enum Directions {
		NORTH(0,1),
		NORTH_EAST(1,1),
		EAST(1,0),
		SOUTH_EAST(1,-1),
		SOUTH(0,-1),
		SOUTH_WEST(-1,-1),
		WEST(-1,0),
		NORTH_WEST(-1,1);
		
		public static Directions[] DIAGONALS = {NORTH_EAST, SOUTH_EAST, SOUTH_WEST, NORTH_WEST};
		public static Directions[] PERPENDICULARS = {NORTH, EAST, SOUTH, WEST};
		
		public int x;
		public int y;
		private Directions(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	public Point n() {
		return Point.create(this, Directions.NORTH.x, Directions.NORTH.y);
	}
	public Point ne() {
		return Point.create(this, Directions.NORTH_EAST.x, Directions.NORTH_EAST.y);
	}
	public Point e() {
		return Point.create(this, Directions.EAST.x, Directions.EAST.y);
	}
	public Point se() {
		return Point.create(this, Directions.SOUTH_EAST.x, Directions.SOUTH_EAST.y);
	}
	public Point s() {
		return Point.create(this, Directions.SOUTH.x, Directions.SOUTH.y);
	}
	public Point sw() {
		return Point.create(this, Directions.SOUTH_WEST.x, Directions.SOUTH_WEST.y);
	}
	public Point w() {
		return Point.create(this, Directions.WEST.x, Directions.WEST.y);
	}
	public Point nw() {
		return Point.create(this, Directions.NORTH_WEST.x, Directions.NORTH_WEST.y);
	}
	
	public int x;
	public int y;
	public static final int MAX = 7;
	public static final int MIN = 0;
	public static InvalidPoint INVALID = new InvalidPoint();

	private Point() {
		
	}
	
	public Point(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}
	
	public Point toX(int x) {
		return Point.create(this, x, 0);
	}
	
	public Point toY(int y) {
		return Point.create(this, 0, y);
	}
	
	public static Point create(int serialized) {
		int x = (serialized>>>3)&0b111;
		int y = serialized&0b111;
		if(x < MIN || x > MAX || y < MIN || y > MAX) return INVALID;
		return new Point(x, y);
	}
	
	public static Point create(int x, int y) {
		if(x < MIN || x > MAX || y < MIN || y > MAX) return INVALID;
		return new Point(x, y);
	}
	
	public static Point create(Point p, int offsetX, int offsetY) {
		if((p.x+offsetX) < MIN || (p.x+offsetX) > MAX || (p.y+offsetY) < MIN || (p.y+offsetY) > MAX) return INVALID;
		return new Point((p.x+offsetX), (p.y+offsetY));
	}
	
	@SpecialCase
	public static class InvalidPoint extends Point {
		@Override
		public String toString() {
			return "INVALID_POINT";
		}		
	}
	
	@Override
	public String toString() {
		return "("+x+", "+y+")";
	}
	
	@Override
	public boolean equals(Object obj) {
		return  !(obj == null) && 
				!(obj instanceof InvalidPoint) &&
				((obj.getClass() == this.getClass() &&
				((Point)obj).x==x &&
				((Point)obj).y==y));
	}

	@Override
	public int hashCode() {
		return serialize();
	}
	
	public int serialize() {
		int ser = 0;
		ser |= x;
		ser <<= 3;
		ser |= y;
		return ser;
	}

}
