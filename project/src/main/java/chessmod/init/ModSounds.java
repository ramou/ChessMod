package chessmod.init;

import chessmod.ChessMod;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class ModSounds {
	public static final SoundEvent slidePiece = createRegisteredSoundEvent("slide_piece");
	public static final SoundEvent slidePieceTake = createRegisteredSoundEvent("slide_piece_take");
	public static final SoundEvent placePiece = createRegisteredSoundEvent("place_piece");
	public static final SoundEvent placePieceTake = createRegisteredSoundEvent("place_piece_take");

	private static SoundEvent createRegisteredSoundEvent(String soundEvent) {
		ResourceLocation loc = new ResourceLocation(ChessMod.MODID, soundEvent);
		return new SoundEvent(loc).setRegistryName(loc);
	}
}
