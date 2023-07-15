package chessmod.setup;

import chessmod.ChessMod;
import chessmod.common.capability.elo.EloEvents;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ChessMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModSetup {

    public static void setup() {
        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addGenericListener(Entity.class, EloEvents::onAttachCapabilitiesPlayer);
        bus.addListener(EloEvents::onPlayerCloned);
        bus.addListener(EloEvents::onRegisterCapabilities);
    }

}
