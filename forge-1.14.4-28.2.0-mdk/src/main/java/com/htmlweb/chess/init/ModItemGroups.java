package com.htmlweb.chess.init;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.htmlweb.chess.ChessMod;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

/*
 * 
 * As Per Cadiboo's tutorial: https://cadiboo.github.io/tutorials/1.14.4/forge/
 * 
 * 
 */
public class ModItemGroups {
	public static final ItemGroup MOD_ITEM_GROUP = new ModItemGroup(ChessMod.MODID, 
		new Supplier<ItemStack>() {
			private ItemStack internal = null;
		
			@Override
			public ItemStack get() {
				synchronized (MOD_ITEM_GROUP) {
					if(internal==null) {
						internal = new ItemStack(
								ModBlocks.MOD_WOOD_CHESSBOARD.get()
						);
					}
				}
				
				return internal;
			};
	
		});

	public static final class ModItemGroup extends ItemGroup {

		@Nonnull
		private final Supplier<ItemStack> iconSupplier;

		public ModItemGroup(@Nonnull final String name, @Nonnull final Supplier<ItemStack> iconSupplier) {
			super(name);
			this.iconSupplier = iconSupplier;
		}

		@Override
		@Nonnull
		public ItemStack createIcon() {
			return iconSupplier.get();
		}

	}
}
