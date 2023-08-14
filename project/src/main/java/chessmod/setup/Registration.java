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
import chessmod.item.ChessWrench;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
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
	private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ChessMod.MODID);
	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ChessMod.MODID);

	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
			DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ChessMod.MODID);


	public static final RegistryObject<Block> WOOD_CHESSBOARD = BLOCKS.register("wood_chessboard", WoodChessboardBlock::new);

	public static final RegistryObject<BlockEntityType<WoodChessboardBlockEntity>> WOOD_CHESSBOARD_BE =
			BLOCK_ENTITY_TYPES.register("wood_chessboard",
					() -> BlockEntityType.Builder.of(WoodChessboardBlockEntity::new, WOOD_CHESSBOARD.get()).build(null));

	public static final RegistryObject<Block> GOLD_CHESSBOARD = BLOCKS.register("gold_chessboard", GoldChessboardBlock::new);
	public static final RegistryObject<BlockEntityType<GoldChessboardBlockEntity>> GOLD_CHESSBOARD_BE =
			BLOCK_ENTITY_TYPES.register("gold_chessboard",
					() -> BlockEntityType.Builder.of(GoldChessboardBlockEntity::new, GOLD_CHESSBOARD.get()).build(null));

	public static final RegistryObject<Block> CHESSES_CHESSBOARD = BLOCKS.register("chesses_chessboard", ChessesChessboardBlock::new);
	public static final RegistryObject<BlockEntityType<ChessesChessboardBlockEntity>> CHESSES_CHESSBOARD_BE =
			BLOCK_ENTITY_TYPES.register("chesses_chessboard",
					() -> BlockEntityType.Builder.of(ChessesChessboardBlockEntity::new, CHESSES_CHESSBOARD.get()).build(null));

	public static final RegistryObject<Block> AI_CHESSBOARD = BLOCKS.register("ai_chessboard", AIChessboardBlock::new);
	public static final RegistryObject<BlockEntityType<AIChessboardBlockEntity>> AI_CHESSBOARD_BE =
			BLOCK_ENTITY_TYPES.register("ai_chessboard",
					() -> BlockEntityType.Builder.of(AIChessboardBlockEntity::new, AI_CHESSBOARD.get()).build(null));

	public static final RegistryObject<Block> PUZZLE_CHESSBOARD = BLOCKS.register("puzzle_chessboard", PuzzleChessboardBlock::new);
	public static final RegistryObject<BlockEntityType<PuzzleChessboardBlockEntity>> PUZZLE_CHESSBOARD_BE =
			BLOCK_ENTITY_TYPES.register("puzzle_chessboard",
					() -> BlockEntityType.Builder.of(PuzzleChessboardBlockEntity::new, PUZZLE_CHESSBOARD.get()).build(null));

	public static final RegistryObject<Item> CHESS_WRENCH = ITEMS.register("chess_wrench", () -> new Item(new Item.Properties()));


	static {
		ITEMS.register(WOOD_CHESSBOARD.getId().getPath(), () -> new BlockItem(WOOD_CHESSBOARD.get(), new Item.Properties()));
		ITEMS.register(GOLD_CHESSBOARD.getId().getPath(), () -> new BlockItem(GOLD_CHESSBOARD.get(), new Item.Properties()));
		ITEMS.register(CHESSES_CHESSBOARD.getId().getPath(), () -> new BlockItem(CHESSES_CHESSBOARD.get(), new Item.Properties()));
		ITEMS.register(AI_CHESSBOARD.getId().getPath(), () -> new BlockItem(AI_CHESSBOARD.get(), new Item.Properties()));
		ITEMS.register(PUZZLE_CHESSBOARD.getId().getPath(), () -> new BlockItem(PUZZLE_CHESSBOARD.get(), new Item.Properties()));
	}

	public static final RegistryObject<SoundEvent> SLIDE_PIECE_SOUND =
			SOUNDS.register("slide_piece",
					() -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ChessMod.MODID, "slide_piece")));
	public static final RegistryObject<SoundEvent> SLIDE_PIECE_TAKE_SOUND =
			SOUNDS.register("slide_piece_take",
					() -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ChessMod.MODID, "slide_piece_take")));
	public static final RegistryObject<SoundEvent> PLACE_PIECE_SOUND =
			SOUNDS.register("place_piece",
					() -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ChessMod.MODID, "place_piece")));
	public static final RegistryObject<SoundEvent> PLACE_PIECE_TAKE_SOUND =
			SOUNDS.register("place_piece_take",
					() -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ChessMod.MODID, "place_piece_take")));


	public static void init() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		BLOCKS.register(bus);
		ITEMS.register(bus);
		BLOCK_ENTITY_TYPES.register(bus);
		SOUNDS.register(bus);
		CREATIVE_MODE_TABS.register(bus);
		int id = 0;
		PacketHandler.HANDLER.messageBuilder(ChessPlay.class, id++, NetworkDirection.PLAY_TO_SERVER)
				.decoder(ChessPlay::decode)
				.encoder(ChessPlay::encode)
				.consumerNetworkThread(ChessPlay.Handler::handle)
				.add();
		PacketHandler.HANDLER.messageBuilder(ArbitraryPlacement.class, id++, NetworkDirection.PLAY_TO_SERVER)
				.decoder(ArbitraryPlacement::decode)
				.encoder(ArbitraryPlacement::encode)
				.consumerNetworkThread(ArbitraryPlacement.Handler::handle)
				.add();
	}

	static {
		CREATIVE_MODE_TABS.register("chesstab",
				() -> CreativeModeTab.builder().icon(() -> new ItemStack(Registration.WOOD_CHESSBOARD.get()))
						.title(Component.translatable("itemGroup.chessmod"))
						.displayItems((pParameters, pOutput) -> {
							pOutput.accept(Registration.WOOD_CHESSBOARD.get());
							pOutput.accept(Registration.GOLD_CHESSBOARD.get());
							pOutput.accept(Registration.CHESSES_CHESSBOARD.get());
							pOutput.accept(Registration.AI_CHESSBOARD.get());
							pOutput.accept(Registration.PUZZLE_CHESSBOARD.get());
							pOutput.accept(Registration.CHESS_WRENCH.get());
						})
						.build());

}
}
