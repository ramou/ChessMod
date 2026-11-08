package chessmod.block;

import javax.annotation.Nullable;

import chessmod.blockentity.ChessboardBlockEntity;
import chessmod.blockentity.QuantumChessBoardBlockEntity;
import chessmod.blockentity.Utility;
import chessmod.client.gui.entity.GoldChessboardGui;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public class QuantumChessBoardBlock extends GoldChessboardBlock {
    public static final BooleanProperty IS_LINKED = BooleanProperty.create("is_linked");

    public QuantumChessBoardBlock() {
        super(BlockBehaviour.Properties.of().lightLevel((state) -> state.getValue(IS_LINKED) ? 15 : 0));
    }

    public void doRegisterDefaultState() {
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(IS_LINKED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(IS_LINKED, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(IS_LINKED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
        return new QuantumChessBoardBlockEntity(pos, state);
    }
    
    @OnlyIn(Dist.CLIENT)
    @Override
    protected void openGui(final Level levelIn, final BlockPos pos){
        final BlockEntity blockEntity = levelIn.getBlockEntity(pos);
        if (blockEntity instanceof ChessboardBlockEntity) {
            Minecraft.getInstance().setScreen(new GoldChessboardGui((ChessboardBlockEntity)blockEntity));
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult pHit) {

        if(!heldItem.is(Items.ENDER_PEARL)) {
            return super.useItemOn(heldItem, state, level, pos, player, hand, pHit);
        }

        if (!level.isClientSide && heldItem.is(Items.ENDER_PEARL)) {
            try {
                CompoundTag nbtData = heldItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

                if (nbtData.getInt("BlockPosX") == 0) {
                    // first board selection
                    QuantumChessBoardBlockEntity blockEntity = (QuantumChessBoardBlockEntity) level.getBlockEntity(pos);
                    if (blockEntity != null) {
                        nbtData.putInt("BlockPosX", pos.getX());
                        nbtData.putInt("BlockPosY", pos.getY());
                        nbtData.putInt("BlockPosZ", pos.getZ());
                        nbtData.putString("Dimension", level.dimension().location().toString());
                        CustomData.set(DataComponents.CUSTOM_DATA, heldItem, nbtData);
                        player.displayClientMessage(Component.translatable("chessmod.quantum.first_notification", pos.getX(), pos.getY(), pos.getZ(), level.dimension().location().toString()), false);
                    }
                } else {
                    // second boardselection
                    BlockPos firstPosition = new BlockPos(
                            nbtData.getInt("BlockPosX"),
                            nbtData.getInt("BlockPosY"),
                            nbtData.getInt("BlockPosZ"));
                    String firstDimension = nbtData.getString("Dimension");

                    QuantumChessBoardBlockEntity secondBoardEntity = (QuantumChessBoardBlockEntity) level.getBlockEntity(pos);
                    if (secondBoardEntity != null) {
                        // id match link the chessboards
                        linkChessboards(player, firstPosition, firstDimension, pos, level.dimension().location().toString());
                        player.getItemInHand(InteractionHand.MAIN_HAND).shrink(1);
                        // clear stored data for next selection
                        nbtData.remove("BlockPosX");
                        nbtData.remove("BlockPosY");
                        nbtData.remove("BlockPosZ");
                        nbtData.remove("Dimension");
                        CustomData.set(DataComponents.CUSTOM_DATA, heldItem, nbtData);
                    } else {
                        // Handle error if Ids don't match or not a chessboard block
                        player.displayClientMessage(Component.translatable("chessmod.quantum.unmatched_board_notification"), false);
                    }
                }
            } catch (QuantumChessBoardBlockEntity.FailureToLinkQuantumChessBoardEntityException e) {
                player.displayClientMessage(Component.translatable(e.getMessage()), false);
            }

            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.SUCCESS;
    }

    private void linkChessboards(Player player,
                                 BlockPos firstPosition, String firstDimension,
                                 BlockPos secondPosition, String secondDimension)
            throws QuantumChessBoardBlockEntity.FailureToLinkQuantumChessBoardEntityException {
        if(firstPosition.equals(secondPosition) && firstDimension.equals(secondDimension)) throw new QuantumChessBoardBlockEntity.FailureToLinkQuantumChessBoardEntityException("chessmod.quantum.circular_linkage_notification");

        if(Utility.getBlockEntityInDimension(firstDimension, firstPosition) instanceof QuantumChessBoardBlockEntity firstEntity){
            if(Utility.getBlockEntityInDimension(secondDimension, secondPosition) instanceof QuantumChessBoardBlockEntity secondEntity){
                firstEntity.linkChessBoards(secondEntity);
                player.displayClientMessage(Component.translatable("chessmod.quantum.second_notification",
                        firstPosition.getX(), firstPosition.getY(), firstPosition.getZ(), firstDimension,
                        secondPosition.getX(), secondPosition.getY(), secondPosition.getZ(), secondDimension), false);
                return;
            }
        }
        throw new QuantumChessBoardBlockEntity.FailureToLinkQuantumChessBoardEntityException("chessmod.quantum.unmatched_board_notification");
    }

}
