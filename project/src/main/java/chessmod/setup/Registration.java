package chessmod.setup;

import chessmod.ChessMod;
import chessmod.block.*;
import chessmod.tileentity.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/*
 * Stuart:: Used the tutorial of this as of May 30, 2022
 * https://wiki.mcjty.eu/modding/index.php?title=Tutorial_1.18_Episode_1
 * 
 * I made some changes in grouping, and may rename the class Registry instead of Registration. It is used as a Registry.
 * I'd actually rather a proper Registry that lets me ask for Blocks.WOOD_CHESSBOARD and Items.WOOD_CHESSBOARD instead of dicking around like this.
 * 
 */
public class Registration {
	public static final Item.Properties ITEM_PROPERTIES = new Item.Properties();

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ChessMod.MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ChessMod.MODID);
    private static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ChessMod.MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ChessMod.MODID);

	public static final RegistryObject<Block> WOOD_CHESSBOARD = BLOCKS.register("wood_chessboard", () -> new WoodChessboardBlock(AbstractBlock.Properties.of(Material.STONE)));
	public static final RegistryObject<Item> WOOD_CHESSBOARD_ITEM = fromBlock(WOOD_CHESSBOARD);
	public static final RegistryObject<TileEntityType<?>> WOOD_CHESSBOARD_TE =
			TILE_ENTITIES.register("wood_chessboard",
					() -> TileEntityType.Builder.of(WoodChessboardTileEntity::new, WOOD_CHESSBOARD.get()).build(null));
	
	public static final RegistryObject<Block> GOLD_CHESSBOARD = BLOCKS.register("gold_chessboard", () -> new GoldChessboardBlock(AbstractBlock.Properties.of(Material.STONE)));
	public static final RegistryObject<Item> GOLD_CHESSBOARD_ITEM = fromBlock(GOLD_CHESSBOARD);
	public static final RegistryObject<TileEntityType<GoldChessboardTileEntity>> GOLD_CHESSBOARD_BE =
			TILE_ENTITIES.register("gold_chessboard",
					() -> TileEntityType.Builder.of(GoldChessboardTileEntity::new, GOLD_CHESSBOARD.get()).build(null));
	
	public static final RegistryObject<Block> CHESSES_CHESSBOARD = BLOCKS.register("chesses_chessboard", () -> new ChessesChessboardBlock(AbstractBlock.Properties.of(Material.STONE)));
	public static final RegistryObject<Item> CHESSES_CHESSBOARD_ITEM = fromBlock(CHESSES_CHESSBOARD);
	public static final RegistryObject<TileEntityType<ChessesChessboardTileEntity>> CHESSES_CHESSBOARD_BE =
			TILE_ENTITIES.register("chesses_chessboard",
					() -> TileEntityType.Builder.of(ChessesChessboardTileEntity::new, CHESSES_CHESSBOARD.get()).build(null));
	
	public static final RegistryObject<Block> AI_CHESSBOARD = BLOCKS.register("ai_chessboard", () -> new AIChessboardBlock(AbstractBlock.Properties.of(Material.STONE)));
	public static final RegistryObject<Item> AI_CHESSBOARD_ITEM = fromBlock(AI_CHESSBOARD);
	public static final RegistryObject<TileEntityType<AIChessboardTileEntity>> AI_CHESSBOARD_BE =
			TILE_ENTITIES.register("ai_chessboard",
					() -> TileEntityType.Builder.of(AIChessboardTileEntity::new, AI_CHESSBOARD.get()).build(null));
	
	public static final RegistryObject<Block> PUZZLE_CHESSBOARD = BLOCKS.register("puzzle_chessboard", () -> new PuzzleChessboardBlock(AbstractBlock.Properties.of(Material.STONE)));
	public static final RegistryObject<Item> PUZZLE_CHESSBOARD_ITEM = fromBlock(PUZZLE_CHESSBOARD);
	public static final RegistryObject<TileEntityType<PuzzleChessboardTileEntity>> PUZZLE_CHESSBOARD_BE =
			TILE_ENTITIES.register("puzzle_chessboard",
					() -> TileEntityType.Builder.of(PuzzleChessboardTileEntity::new, PUZZLE_CHESSBOARD.get()).build(null));
    
	public static final RegistryObject<SoundEvent> SLIDE_PIECE_SOUND=
            SOUNDS.register("slide_piece", 
            		() -> new SoundEvent(new ResourceLocation(ChessMod.MODID, "slide_piece")));
	public static final RegistryObject<SoundEvent> SLIDE_PIECE_TAKE_SOUND=
            SOUNDS.register("slide_piece_take", 
            		() -> new SoundEvent(new ResourceLocation(ChessMod.MODID, "slide_piece_take")));
	public static final RegistryObject<SoundEvent> PLACE_PIECE_SOUND=
            SOUNDS.register("place_piece", 
            		() -> new SoundEvent(new ResourceLocation(ChessMod.MODID, "place_piece")));
	public static final RegistryObject<SoundEvent> PLACE_PIECE_TAKE_SOUND=
            SOUNDS.register("place_piece_take", 
            		() -> new SoundEvent(new ResourceLocation(ChessMod.MODID, "place_piece_take")));
	
	
	public static void register() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILE_ENTITIES.register(bus);
        SOUNDS.register(bus);
    }
	
    // Conveniance function: Take a RegistryObject<Block> and make a corresponding RegistryObject<Item> from it
    public static <B extends Block> RegistryObject<Item> fromBlock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), ITEM_PROPERTIES));
    }
}
