package chessmod.common.capability.enderPearlEvent;

import chessmod.block.QuantumChessBoardBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class EnderPearlEventHandler {
    // To store the first position
    private static BlockPos firstPos = null;
    private static BlockPos secondPos = null;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        System.out.println("Event fired for hand: " + event.getHand().toString());// Debugging line

        // Check if the player is holding an enderpearl
        if (event.getHand() == InteractionHand.MAIN_HAND && event.getItemStack().getItem() == Items.ENDER_PEARL) {
            Block clickedBlock= event.getLevel().getBlockState(event.getPos()).getBlock();

            // Check if the block clicked is one of your chessboards
            if (isChessboard(clickedBlock)) {
                if (firstPos == null) {
                    firstPos = event.getPos();
                    event.getEntity().displayClientMessage(Component.literal("First chessboard selected at: " + firstPos), false);
                } else {
                    BlockPos secondPos = event.getPos();
                    event.getEntity().displayClientMessage(Component.literal("Second chessboard selected at: " + secondPos), false);

                    // Link the two chessboards
                    linkChessboards(firstPos, secondPos);

                    // Reset for the next pair
                    firstPos = null;
                }

                // Cancel the default behavior of the enderpearl
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }

    // Helper function to check if block is quantum chessboards
    private boolean isChessboard(Block block) {
        return block instanceof QuantumChessBoardBlock;
    }

    // Logic to link the chessboards
    private void linkChessboards(BlockPos first, BlockPos second) {
        // Your linking logic here
    }
}
