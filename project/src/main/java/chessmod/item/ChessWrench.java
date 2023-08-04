package chessmod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import chessmod.block.ChessboardBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ChessWrench extends Item {

    public ChessWrench(Properties p_41383_) {
        super(p_41383_);
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack item = player.getItemInHand(hand);
        HitResult hitResult  = player.pick(5.0D,0.0F,false);

        if (hitResult.getType() == HitResult.Type.BLOCK){

            BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
            BlockState state = level.getBlockState(pos);

            if (state.getBlock() instanceof ChessboardBlock){
                if (!level.isClientSide()){
                    Direction currentPosition = state.getValue(ChessboardBlock.FACING);
                    switch (currentPosition){
                        case NORTH:
                            currentPosition = Direction.EAST;
                            break;
                        case EAST:
                            currentPosition = Direction.SOUTH;
                            break;
                        case SOUTH:
                            currentPosition = Direction.WEST;
                            break;
                        case WEST:
                            currentPosition = Direction.NORTH;
                            break;
                        default:
                            break;
                    }
                    level.getBlockEntity(pos).getBlockState().setValue(ChessboardBlock.FACING, currentPosition);
                }
                return InteractionResultHolder.success(item);

            }
        }

        return InteractionResultHolder.pass(item);
    }
}
