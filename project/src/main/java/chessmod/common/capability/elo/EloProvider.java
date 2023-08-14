package chessmod.common.capability.elo;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class EloProvider implements ICapabilityProvider, INBTSerializable<INBT> {

    protected final Capability<IElo> capability;
    protected final Direction facing;
    protected final IElo instance;
    protected final LazyOptional<IElo> lazyOptional;

	protected EloProvider(final Capability<IElo> capability, @Nullable final Direction facing, @Nullable final IElo instance) {
		super();
		this.capability = capability;
		this.facing = facing;
		this.instance = instance;
		
        if (this.instance != null) {
            lazyOptional = LazyOptional.of(() -> this.instance);
        } else {
            lazyOptional = LazyOptional.empty();
        }
	}

	@Override
	public INBT serializeNBT() {
		final IElo instance = getInstance();

        if (instance == null) {
            return null;
        }
        if(getCapability() == null)
            return new CompoundNBT();
        return getCapability().writeNBT(instance, getFacing());
	}

	@Override
	public void deserializeNBT(INBT nbt) {
        final IElo instance = getInstance();

        if (instance == null) {
            return;
        }

        getCapability().readNBT(instance, getFacing(), nbt);
	}

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> capability, @Nullable final Direction facing) {
        if(getCapability() == null)
            return LazyOptional.empty();
        return getCapability().orEmpty(capability, lazyOptional);
    }


    public final Capability<IElo> getCapability() {
        return capability;
    }


    @Nullable
    public Direction getFacing() {
        return facing;
    }

    @Nullable
    public final IElo getInstance() {
        return instance;
    }
    
}
