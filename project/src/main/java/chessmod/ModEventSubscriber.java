package chessmod;

import javax.annotation.Nonnull;

import chessmod.block.*;
import chessmod.tileentity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
import chessmod.init.ModBlocks;
import chessmod.init.ModItemGroups;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

@EventBusSubscriber(modid = ChessMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {
	private static final Logger LOGGER = LogManager.getLogger(ChessMod.MODID + " Mod Event Subscriber");
	
	/**
	 * This method will be called by Forge when it is time for the mod to register its Blocks.
	 * This method will always be called before the Item registry method.
	 */
	@SubscribeEvent
	public static void onRegisterBlocks(final RegistryEvent.Register<Block> event) {
		// Register all your blocks inside this registerAll call		
		event.getRegistry().registerAll(
				setup(new WoodChessboardBlock(Block.Properties.create(Material.ROCK).hardnessAndResistance(3.5F).lightValue(0)), "wood_chessboard"),
				setup(new GoldChessboardBlock(Block.Properties.create(Material.ROCK).hardnessAndResistance(3.5F).lightValue(0)), "gold_chessboard"),
				setup(new ChessesChessboardBlock(Block.Properties.create(Material.ROCK).hardnessAndResistance(3.5F).lightValue(0)), "chesses_chessboard"),
				setup(new AIChessboardBlock(Block.Properties.create(Material.ROCK).hardnessAndResistance(3.5F).lightValue(0)), "ai_chessboard"),
				setup(new PuzzleChessboardBlock(Block.Properties.create(Material.ROCK).hardnessAndResistance(3.5F).lightValue(0)), "puzzle_chessboard")
		);
		LOGGER.debug("Registered Blocks");
	}
	
	/**
	 * This method will be called by Forge when it is time for the mod to register its Items.
	 * This method will always be called after the Block registry method.
	 */
	@SubscribeEvent
	public static void onRegisterItems(final RegistryEvent.Register<Item> event) {
		final IForgeRegistry<Item> registry = event.getRegistry();
		registry.registerAll(
				// This is a very simple Item. It has no special properties except for being on our creative tab.
				//setup(new Item(new Item.Properties().group(ModItemGroups.MOD_ITEM_GROUP)), "wood_chessboard")
				
				//We'd do the other ones here.
		);

		// We need to go over the entire registry so that we include any potential Registry Overrides
		for (final Block block : ForgeRegistries.BLOCKS.getValues()) {

			final ResourceLocation blockRegistryName = block.getRegistryName();
			// An extra safe-guard against badly registered blocks
			Preconditions.checkNotNull(blockRegistryName, "Registry Name of Block \"" + block + "\" of class \"" + block.getClass().getName() + "\"is null! This is not allowed!");

			// Check that the blocks is from our mod, if not, continue to the next block
			if (!blockRegistryName.getNamespace().equals(ChessMod.MODID)) {
				continue;
			}

			// Make the properties, and make it so that the item will be on our ItemGroup (CreativeTab)
			final Item.Properties properties = new Item.Properties().group(ModItemGroups.MOD_ITEM_GROUP);
			// Create the new BlockItem with the block and it's properties
			final BlockItem blockItem = new BlockItem(block, properties);
			// Setup the new BlockItem with the block's registry name and register it
			registry.register(setup(blockItem, blockRegistryName));
		}
		LOGGER.debug("Registered Items");
	}
	
	/**
	 * This method will be called by Forge when it is time for the mod to register its TileEntityType.
	 * This method will always be called after the Block and Item registry methods.
	 */
	@SubscribeEvent
	public static void onRegisterTileEntityTypes(@Nonnull final RegistryEvent.Register<TileEntityType<?>> event) {
		// Register your TileEntityTypes here if you have them
		event.getRegistry().registerAll(
				// We don't have a datafixer for our TileEntity, so we pass null into build
				setup(
						TileEntityType.Builder.create(
								WoodChessboardTileEntity::new, 
								ModBlocks.wood_chessboard
							).build(null), "wood_chessboard"),
				setup(
						TileEntityType.Builder.create(
								GoldChessBoardTileEntity::new,
								ModBlocks.gold_chessboard
						).build(null), "gold_chessboard"),
				setup(
						TileEntityType.Builder.create(
								ChessesChessboardTileEntity::new,
								ModBlocks.chesses_chessboard
						).build(null), "chesses_chessboard"),
				setup(
						TileEntityType.Builder.create(
								AIChessboardTileEntity::new,
								ModBlocks.ai_chessboard
						).build(null), "ai_chessboard"),
				setup(
						TileEntityType.Builder.create(
								PuzzleChessboardTileEntity::new,
								ModBlocks.puzzle_chessboard
						).build(null), "puzzle_chessboard")
		);
		LOGGER.debug("Registered TileEntityTypes");
	}
	
	/**
	 * Performs setup on a registry entry
	 *
	 * @param name The path of the entry's name. Used to make a name who's domain is our mod's modid
	 */
	@Nonnull
	private static <T extends IForgeRegistryEntry<T>> T setup(@Nonnull final T entry, @Nonnull final String name) {
		Preconditions.checkNotNull(name, "Name to assign to entry cannot be null!");
		return setup(entry, new ResourceLocation(ChessMod.MODID, name));
	}

	/**
	 * Performs setup on a registry entry
	 *
	 * @param registryName The full registry name of the entry
	 */
	@Nonnull
	private static <T extends IForgeRegistryEntry<T>> T setup(@Nonnull final T entry, @Nonnull final ResourceLocation registryName) {
		Preconditions.checkNotNull(entry, "Entry cannot be null!");
		Preconditions.checkNotNull(registryName, "Registry name to assign to entry cannot be null!");
		entry.setRegistryName(registryName);
		return entry;
	}
	
}
