package com.htmlweb.chess.common;

import com.htmlweb.chess.ChessMod;
import com.htmlweb.chess.init.ModSounds;

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
		r.register(ModSounds.slidePiece);
		r.register(ModSounds.slidePieceTake);
		r.register(ModSounds.placePiece);
		r.register(ModSounds.placePieceTake);
		
		ChessMod.LOGGER.debug("Registered Sosunds!");
	}
}
