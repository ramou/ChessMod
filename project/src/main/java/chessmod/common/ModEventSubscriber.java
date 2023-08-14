package chessmod.common;

import chessmod.ChessMod;
import chessmod.init.ModSounds;

import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = ChessMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {

	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> evt) {
		IForgeRegistry<SoundEvent> r = evt.getRegistry();
		r.register(ModSounds.slide_piece);
		r.register(ModSounds.slide_piece_take);
		r.register(ModSounds.place_piece);
		r.register(ModSounds.place_piece_take);
		
		ChessMod.LOGGER.debug("Registered Sosunds!");
	}
}
