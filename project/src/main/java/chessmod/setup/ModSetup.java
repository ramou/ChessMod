package chessmod.setup;

import chessmod.ChessMod;
import chessmod.common.capability.elo.EloEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ChessMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModSetup {
    public static void init(final FMLClientSetupEvent event) {
    	
    }
    
    public static void setup() {
        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addGenericListener(Entity.class, EloEvents::onAttachCapabilitiesPlayer);
        bus.addListener(EloEvents::onPlayerCloned);
        bus.addListener(EloEvents::onRegisterCapabilities);
    }
    
    public static final String TAB_NAME = "ChessMod";

    public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab(TAB_NAME) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Registration.WOOD_CHESSBOARD.get());
        }
    };
    
}
