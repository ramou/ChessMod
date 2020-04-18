package com.htmlweb.chess.init;

import java.util.function.Supplier;

import com.htmlweb.chess.ChessMod;
import com.htmlweb.chess.block.WoodChessBoardBlock;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {
	
	public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, ChessMod.MODID);
	
	public static final RegistryObject<Block> MOD_WOOD_CHESSBOARD = 
			BLOCKS.register(
					"wood_chessboard",
					new Supplier<Block>() {
						private Block internal = null;
					
						@Override
						public Block get() {
							synchronized (BLOCKS) {
								if(internal==null) {
									internal = new WoodChessBoardBlock(
										Block.Properties.create(Material.WOOD).hardnessAndResistance(3.0F, 3.0F)
									);	
								}
							}
							
							return internal;
						};

					}
			);
}
