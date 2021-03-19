package chessmod.common.network;

import chessmod.ChessMod;

import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class PacketHandler {
	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(new Identifier(ChessMod.MODID, "ap"), ArbitraryPlacement.Handler.INSTANCE);
		ServerPlayNetworking.registerGlobalReceiver(new Identifier(ChessMod.MODID, "cp"), ChessPlay.Handler.INSTANCE);
	}
}
