package chessmod.common.capability.elo;

import javax.annotation.Nullable;

import chessmod.ChessMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class EloCapability {

	
	@CapabilityInject(IElo.class)
	public final static Capability<IElo> ELO_CAPABILITY = null;
	public static final Direction DEFAULT_FACING = null;
	public static final ResourceLocation ID = new ResourceLocation(ChessMod.MODID, "elo");
	private static final String ELO_TAG = "elo";
	private static final int INITIAL_ELO = 1000;
	
	public static void register() {

		CapabilityManager.INSTANCE.register(IElo.class, new Capability.IStorage<IElo>() {
			@Nullable
			@Override
			public INBT writeNBT(Capability<IElo> capability, IElo instance, Direction side) {
				CompoundNBT tag = new CompoundNBT();
				tag.putInt(ELO_TAG, instance.getElo());
				return tag;
			}

			@Override
			public void readNBT(Capability<IElo> capability, IElo instance, Direction side, INBT nbt) {
				CompoundNBT tag = (CompoundNBT)nbt;
				int elo = tag.getInt(ELO_TAG);
				if(elo == 0) instance.setElo(1000); //It didn't have the tag for some reason, restart them at 1000 
				else instance.setElo(elo); 
			}
		}, () -> new Elo(INITIAL_ELO));
		System.out.println("Finished Registering EloCapability");
	}

	public static LazyOptional<IElo> getElo(final LivingEntity entity){
        return entity.getCapability(ELO_CAPABILITY, DEFAULT_FACING);
    }

    public static ICapabilityProvider createProvider(final IElo mana) {
        return new EloProvider(ELO_CAPABILITY, DEFAULT_FACING, mana);
    }

    /**
     * Event handler for the {@link IElo} capability.
     */
    @Mod.EventBusSubscriber(modid = ChessMod.MODID)
    private static class EventHandler {

        /**
         * Attach the {@link IElo} capability to all living entities.
         *
         * @param event The event
         */
        @SubscribeEvent
        public static void attachCapabilities(final AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof PlayerEntity) {
                final Elo elo = new Elo(INITIAL_ELO);
                event.addCapability(ID, createProvider(elo));
            }
        }

        /**
         * Copy the player's elo when they respawn after dying or returning from the end.
         *
         * @param event The event
         */
        @SubscribeEvent
        public static void playerClone(final PlayerEvent.Clone event) {
            getElo(event.getOriginal()).ifPresent(oldElo -> {
                getElo(event.getPlayer()).ifPresent(newElo -> {
                    newElo.setElo(oldElo.getElo());
                });
            });
        }
    }
	
}
