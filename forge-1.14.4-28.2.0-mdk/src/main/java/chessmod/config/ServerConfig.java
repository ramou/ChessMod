package chessmod.config;

import java.util.ArrayList;
import java.util.List;

import chessmod.ChessMod;

import net.minecraft.item.DyeColor;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * For configuration settings that change the behaviour of code on the LOGICAL SERVER.
 * This can be moved to an inner class of ChessModConfig, but is separate because of personal preference and to keep the code organised
 *
 * @author Cadiboo
 */
final class ServerConfig {

	final ForgeConfigSpec.BooleanValue serverBoolean;
	final ForgeConfigSpec.ConfigValue<List<String>> serverStringList;
	final ForgeConfigSpec.ConfigValue<DyeColor> serverEnumDyeColor;


	ServerConfig(final ForgeConfigSpec.Builder builder) {
		builder.push("general");
		serverBoolean = builder
				.comment("An example boolean in the server config")
				.translation(ChessMod.MODID + ".config.serverBoolean")
				.define("serverBoolean", true);
		serverStringList = builder
				.comment("An example list of Strings in the server config")
				.translation(ChessMod.MODID + ".config.serverStringList")
				.define("serverStringList", new ArrayList<>());
		serverEnumDyeColor = builder
				.comment("An example enum DyeColor in the server config")
				.translation(ChessMod.MODID + ".config.serverEnumDyeColor")
				.defineEnum("serverEnumDyeColor", DyeColor.WHITE);

		builder.pop();
	}

}
