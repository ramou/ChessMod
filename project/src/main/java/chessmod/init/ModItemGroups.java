package chessmod.init;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import chessmod.ChessMod;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

/*
 * 
 * As Per Cadiboo's tutorial: https://cadiboo.github.io/tutorials/1.14.4/forge/
 * 
 * 
 */
public class ModItemGroups {

	public static final ItemGroup MOD_ITEM_GROUP = new ModItemGroup(ChessMod.MODID, () -> new ItemStack(
		ModBlocks.wood_chessboard.asItem()
	));

	public static final ItemGroup ITEM_GROUP = new ItemGroup("chessmod") {
		@Override
		public ItemStack createIcon() {
			return new ItemStack(ModItems.chess_wrench.getItem());
		}
	};



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
