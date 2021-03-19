package chessmod.init;

import java.util.List;

import chessmod.ChessMod;
import chessmod.block.GoldChessboardBlock;
import chessmod.block.WoodChessboardBlock;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;

public class ModBlocks {
	public static final Block WOOD_CHESSBOARD = new WoodChessboardBlock(AbstractBlock.Settings.of(Material.STONE).strength(3.5F).luminance(b -> 0));
	public static final Block GOLD_CHESSBOARD = new GoldChessboardBlock(AbstractBlock.Settings.of(Material.STONE).strength(3.5F).luminance(b -> 0));
	public static final ItemGroup MOD_ITEM_GROUP = FabricItemGroupBuilder.create(new Identifier(ChessMod.MODID, "itemgroup")).icon(() -> new ItemStack(WOOD_CHESSBOARD.asItem())).build();

	public static void init() {
		Registry.register(Registry.BLOCK, new Identifier(ChessMod.MODID, "wood_chessboard"), WOOD_CHESSBOARD);
		Registry.register(Registry.BLOCK, new Identifier(ChessMod.MODID, "gold_chessboard"), GOLD_CHESSBOARD);
		Item wood = Registry.register(Registry.ITEM, new Identifier(ChessMod.MODID, "wood_chessboard"), new BlockItem(WOOD_CHESSBOARD, new Item.Settings().group(MOD_ITEM_GROUP)) {
			@Override
			public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
				tooltip.add(new LiteralText("Sandbox"));
			}
		});
		Item gold = Registry.register(Registry.ITEM, new Identifier(ChessMod.MODID, "gold_chessboard"), new BlockItem(GOLD_CHESSBOARD, new Item.Settings().group(MOD_ITEM_GROUP)) {
			@Override
			public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
				tooltip.add(new LiteralText("Not a Sandbox"));
			}
		});
		Item.BLOCK_ITEMS.put(WOOD_CHESSBOARD, wood);
		Item.BLOCK_ITEMS.put(GOLD_CHESSBOARD, gold);
	}
}
