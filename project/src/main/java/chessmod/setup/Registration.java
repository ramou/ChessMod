package chessmod.setup;

import chessmod.ChessMod;
import chessmod.block.AIChessboardBlock;
import chessmod.block.ChessesChessboardBlock;
import chessmod.block.GoldChessboardBlock;
import chessmod.block.PuzzleChessboardBlock;
import chessmod.block.WoodChessboardBlock;
import chessmod.blockentity.AIChessboardBlockEntity;
import chessmod.blockentity.ChessesChessboardBlockEntity;
import chessmod.blockentity.GoldChessboardBlockEntity;
import chessmod.blockentity.PuzzleChessboardBlockEntity;
import chessmod.blockentity.WoodChessboardBlockEntity;
import chessmod.common.network.ArbitraryPlacement;
import chessmod.common.network.ChessPlay;
import chessmod.common.network.PacketHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/*
 * Stuart:: Used the tutorial of this as of May 30, 2022
 * https://wiki.mcjty.eu/modding/index.php?title=Tutorial_1.18_Episode_1
 * 
 * I made some changes in grouping, and may rename the class Registry instead of Registration. It is used as a Registry.
 * I'd actually rather a proper Registry that lets me ask for Blocks.WOOD_CHESSBOARD and Items.WOOD_CHESSBOARD instead of dicking around like this.
 * 
 */
public class Registration {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ChessMod.MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ChessMod.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, ChessMod.MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ChessMod.MODID);

    public static final Item.Properties ITEM_PROPERTIES = new Item.Properties().tab(ModSetup.ITEM_GROUP);
    
	public static final RegistryObject<Block> WOOD_CHESSBOARD = BLOCKS.register("wood_chessboard", WoodChessboardBlock::new);
	public static final RegistryObject<Item> WOOD_CHESSBOARD_ITEM = fromBlock(WOOD_CHESSBOARD);
	public static final RegistryObject<BlockEntityType<WoodChessboardBlockEntity>> WOOD_CHESSBOARD_BE = 
			BLOCK_ENTITIES.register("wood_chessboard", 
					() -> BlockEntityType.Builder.of(WoodChessboardBlockEntity::new, WOOD_CHESSBOARD.get()).build(null));
	
	public static final RegistryObject<Block> GOLD_CHESSBOARD = BLOCKS.register("gold_chessboard", GoldChessboardBlock::new);
	public static final RegistryObject<Item> GOLD_CHESSBOARD_ITEM = fromBlock(GOLD_CHESSBOARD);
	public static final RegistryObject<BlockEntityType<GoldChessboardBlockEntity>> GOLD_CHESSBOARD_BE = 
			BLOCK_ENTITIES.register("gold_chessboard", 
					() -> BlockEntityType.Builder.of(GoldChessboardBlockEntity::new, GOLD_CHESSBOARD.get()).build(null));
	
	public static final RegistryObject<Block> CHESSES_CHESSBOARD = BLOCKS.register("chesses_chessboard", ChessesChessboardBlock::new);
	public static final RegistryObject<Item> CHESSES_CHESSBOARD_ITEM = fromBlock(CHESSES_CHESSBOARD);
	public static final RegistryObject<BlockEntityType<ChessesChessboardBlockEntity>> CHESSES_CHESSBOARD_BE = 
			BLOCK_ENTITIES.register("chesses_chessboard", 
					() -> BlockEntityType.Builder.of(ChessesChessboardBlockEntity::new, CHESSES_CHESSBOARD.get()).build(null));
	
	public static final RegistryObject<Block> AI_CHESSBOARD = BLOCKS.register("ai_chessboard", AIChessboardBlock::new);
	public static final RegistryObject<Item> AI_CHESSBOARD_ITEM = fromBlock(AI_CHESSBOARD);
	public static final RegistryObject<BlockEntityType<AIChessboardBlockEntity>> AI_CHESSBOARD_BE = 
			BLOCK_ENTITIES.register("ai_chessboard",
					() -> BlockEntityType.Builder.of(AIChessboardBlockEntity::new, AI_CHESSBOARD.get()).build(null));
	
	public static final RegistryObject<Block> PUZZLE_CHESSBOARD = BLOCKS.register("puzzle_chessboard", PuzzleChessboardBlock::new);
	public static final RegistryObject<Item> PUZZLE_CHESSBOARD_ITEM = fromBlock(PUZZLE_CHESSBOARD);
	public static final RegistryObject<BlockEntityType<PuzzleChessboardBlockEntity>> PUZZLE_CHESSBOARD_BE = 
			BLOCK_ENTITIES.register("puzzle_chessboard", 
					() -> BlockEntityType.Builder.of(PuzzleChessboardBlockEntity::new, PUZZLE_CHESSBOARD.get()).build(null));
    
	public static final RegistryObject<SoundEvent> SLIDE_PIECE_SOUND=
            SOUNDS.register("slide_piece", 
<<<<<<< Updated upstream
            		() -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ChessMod.MODID, "slide_piece")));
	public static final RegistryObject<SoundEvent> SLIDE_PIECE_TAKE_SOUND=
            SOUNDS.register("slide_piece_take", 
            		() -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ChessMod.MODID, "slide_piece_take")));
	public static final RegistryObject<SoundEvent> PLACE_PIECE_SOUND=
            SOUNDS.register("place_piece", 
            		() -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ChessMod.MODID, "place_piece")));
	public static final RegistryObject<SoundEvent> PLACE_PIECE_TAKE_SOUND=
            SOUNDS.register("place_piece_take", 
            		() -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ChessMod.MODID, "place_piece_take")));
            		() ->  SoundEvent.createVariableRangeEvent(new ResourceLocation(ChessMod.MODID, "slide_piece")));
	public static final RegistryObject<SoundEvent> SLIDE_PIECE_TAKE_SOUND=
            SOUNDS.register("slide_piece_take", 
            		() ->  SoundEvent.createVariableRangeEvent(new ResourceLocation(ChessMod.MODID, "slide_piece_take")));
	public static final RegistryObject<SoundEvent> PLACE_PIECE_SOUND=
            SOUNDS.register("place_piece", 
            		() ->  SoundEvent.createVariableRangeEvent(new ResourceLocation(ChessMod.MODID, "place_piece")));
	public static final RegistryObject<SoundEvent> PLACE_PIECE_TAKE_SOUND=
            SOUNDS.register("place_piece_take", 
            		() ->  SoundEvent.createVariableRangeEvent(new ResourceLocation(ChessMod.MODID, "place_piece_take")));
>>>>>>> Stashed changes
	
	
	public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        SOUNDS.register(bus);
        int id = 0;
        PacketHandler.HANDLER.messageBuilder(ChessPlay.class, id++, NetworkDirection.PLAY_TO_SERVER)
        .decoder(buf->ChessPlay.decode(buf))
        .encoder(ChessPlay::encode)
        .consumer(ChessPlay.Handler::handle)
        .add();
        PacketHandler.HANDLER.messageBuilder(ArbitraryPlacement.class, id++, NetworkDirection.PLAY_TO_SERVER)
        .decoder(buf->ArbitraryPlacement.decode(buf))
        .encoder(ArbitraryPlacement::encode)
        .consumer(ArbitraryPlacement.Handler::handle)
        .add();
		//PacketHandler.HANDLER.registerMessage(id++, ChessPlay.class, ChessPlay::encode, ChessPlay::decode, ChessPlay.Handler::handle);
		//PacketHandler.HANDLER.registerMessage(id++, ArbitraryPlacement.class, ArbitraryPlacement::encode, ArbitraryPlacement::decode, ArbitraryPlacement.Handler::handle);
        
        
    }
	
    // Conveniance function: Take a RegistryObject<Block> and make a corresponding RegistryObject<Item> from it
    public static <B extends Block> RegistryObject<Item> fromBlock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), ITEM_PROPERTIES));
    }
}
