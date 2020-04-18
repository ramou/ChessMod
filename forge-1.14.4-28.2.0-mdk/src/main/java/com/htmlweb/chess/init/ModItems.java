package com.htmlweb.chess.init;

import java.util.function.Supplier;

import com.htmlweb.chess.ChessMod;

import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
	public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, ChessMod.MODID);
	
	public static final RegistryObject<Item> EXAMPLE_SHINYEGG = ITEMS.register("shinyegg", 
			new Supplier<Item>() {
				private Item internal = null;
			
				@Override
				public Item get() {
					synchronized (ITEMS) {
						if(internal==null) {
							internal = new Item(new Item.Properties().group(ModItemGroups.MOD_ITEM_GROUP));
						}
					}
					
					return internal;
				};
		
			}
			);
}
