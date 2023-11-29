package chessmod.common.capability.elo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class Elo {

	private int elo;

	public Elo(int elo) {
		super();
		this.elo = elo;
	}

	public int getElo() {
		return elo;
	}

	public void setElo(int elo) {
		this.elo = elo;
	}

	//Development Coefficient
	public static final float K = 30;
	public static final float T = 400;
	public static void updateElo(Player p1, Player p2, boolean won) {
		float S1 = won?1f:0.5f;
		float S2 = won?1f:0.5f;
		Elo P1 = p1.getCapability(EloProvider.capability).resolve().get();
		Elo P2 = p2.getCapability(EloProvider.capability).resolve().get();
		double newP1 = P1.getElo() + K*(S1-(Math.pow(10, P1.getElo()/T)/(Math.pow(10, P1.getElo()/T) + Math.pow(10, P2.getElo()/T))));
		double newP2 = P2.getElo() + K*(S2-(Math.pow(10, P2.getElo()/T)/(Math.pow(10, P1.getElo()/T) + Math.pow(10, P2.getElo()/T))));
		P1.setElo((int)newP1);
		//TODO: Fix initial cludge that shows working by "playing with yourself"
		if(!p1.equals(p2)) P2.setElo((int)newP2);
	}
	
    public void copyFrom(Elo source) {
    	elo = source.elo;
    }
	
    public void saveNBTData(CompoundTag compound) {
        compound.putInt("elo", elo);
    }

    public void loadNBTData(CompoundTag compound) {
        elo = compound.getInt("elo");
    }
	
}
