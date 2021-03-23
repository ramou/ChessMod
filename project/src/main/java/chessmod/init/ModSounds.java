package chessmod.init;

import chessmod.ChessMod;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModSounds {
	public static final SoundEvent slidePiece = createRegisteredSoundEvent("slide_piece");
	public static final SoundEvent slidePieceTake = createRegisteredSoundEvent("slide_piece_take");
	public static final SoundEvent placePiece = createRegisteredSoundEvent("place_piece");
	public static final SoundEvent placePieceTake = createRegisteredSoundEvent("place_piece_take");

	private static SoundEvent createRegisteredSoundEvent(String soundEvent) {
		Identifier loc = new Identifier(ChessMod.MODID, soundEvent);
		return Registry.register(Registry.SOUND_EVENT, loc, new SoundEvent(loc));
	}

	public static void init() {
	}
}
