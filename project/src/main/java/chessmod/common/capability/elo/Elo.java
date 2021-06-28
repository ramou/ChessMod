package chessmod.common.capability.elo;

import net.minecraft.entity.player.PlayerEntity;

public class Elo implements IElo {

	private int elo;

	public Elo(int elo) {
		super();
		this.elo = elo;
	}

	@Override
	public int getElo() {
		return elo;
	}

	@Override
	public void setElo(int elo) {
		this.elo = elo;
	}

	//Development Coefficient
	public static final float K = 30;
	public static final float T = 400;
	public static void updateElo(PlayerEntity p1, PlayerEntity p2, boolean won) {
		float S1 = won?1f:0.5f;
		float S2 = won?1f:0.5f;
		IElo P1 = p1.getCapability(EloCapability.ELO_CAPABILITY).resolve().get();
		IElo P2 = p2.getCapability(EloCapability.ELO_CAPABILITY).resolve().get();
		double newP1 = P1.getElo() + K*(S1-(Math.pow(10, P1.getElo()/T)/(Math.pow(10, P1.getElo()/T) + Math.pow(10, P2.getElo()/T))));
		double newP2 = P2.getElo() + K*(S2-(Math.pow(10, P2.getElo()/T)/(Math.pow(10, P1.getElo()/T) + Math.pow(10, P2.getElo()/T))));
		P1.setElo((int)newP1);
		//TODO: Fix initial cludge that shows working by "playing with yourself"
		if(!p1.equals(p2)) P2.setElo((int)newP2);
	}
	
}
