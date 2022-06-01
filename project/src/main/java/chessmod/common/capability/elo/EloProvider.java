package chessmod.common.capability.elo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class EloProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    protected static Capability<Elo> capability;
    protected Elo instance = null;
    protected final LazyOptional<Elo> lazyOptional = LazyOptional.of(this::createElo);

    @Nonnull
    private Elo createElo() {
        if (instance == null) {
        	instance = new Elo(1000);
        }
        return instance;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap == capability) {
            return lazyOptional.cast();
        }
        return LazyOptional.empty();
    }
	
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return getCapability(cap);
    }
	
	
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createElo().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
    	createElo().loadNBTData(nbt);
    }


    public final Capability<Elo> getCapability() {
        return capability;
    }


    @Nullable
    public final Elo getInstance() {
        return instance;
    }
    
}
