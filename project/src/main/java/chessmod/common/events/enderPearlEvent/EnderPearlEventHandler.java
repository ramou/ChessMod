package chessmod.common.events.enderPearlEvent;

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

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class EnderPearlEventHandler {
    // To store the first position
    private static BlockPos firstPos = null;
    private static BlockPos secondPos = null;

    // Create a dedicated lock object for synchronization
    private final Object lock = new Object();
    private long currentNonce = 0;
    private long firstNonce = -1;


    private static final long COOLDOWN = 1000; // 1 second cooldown
    private long lastClickTime = -1;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        long currentTime = System.currentTimeMillis();

        // Cooldown check
        if (currentTime - lastClickTime < COOLDOWN) {
            return; // Exit if we are within cooldown period
        }

        // Update last click time
        lastClickTime = currentTime;

        System.out.println("Event fired for hand: " + event.getHand().toString());
        // Check if the player is holding an enderpearl
        if (event.getHand() == InteractionHand.MAIN_HAND && event.getItemStack().getItem() == Items.ENDER_PEARL) {
            Block clickedBlock = event.getLevel().getBlockState(event.getPos()).getBlock();

            // Check if the block clicked is one of your chessboards
            if (isChessboard(clickedBlock)) {
                synchronized (lock) {
                    long thisNonce = getNextNonce();
                    if (firstPos == null) {
                        // Select the first chessboard
                        firstPos = event.getPos();
                        firstNonce = thisNonce;
                        event.getEntity().displayClientMessage(Component.literal("First chessboard selected at: " + firstPos), false);
                    } else if (!firstPos.equals(event.getPos()) && thisNonce != firstNonce) {
                        // Select the second chessboard and link
                        BlockPos secondPos = event.getPos();
                        event.getEntity().displayClientMessage(Component.literal("Second chessboard selected at: " + secondPos), false);

                        // Link the two chessboards
                        linkChessboards(firstPos, secondPos);

                        // Reset for the next pair
                        firstPos = null;
                        firstNonce = -1;
                    }
                    // If the same block is selected twice or if there's an attempt to link a board to itself, reset the first selection
                    else if (firstPos.equals(event.getPos()) || thisNonce == firstNonce) {
                        event.getEntity().displayClientMessage(Component.literal("The same chessboard cannot be linked. Reselect the first chessboard."), false);
                        firstPos = null;
                        firstNonce = -1;
                    }
                }

                // Cancel the default behavior of the enderpearl
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }

    private long getNextNonce() {
        return currentNonce++;
    }


    // Helper function to check if block is quantum chessboards
    private boolean isChessboard(Block block) {
        return block instanceof QuantumChessBoardBlock;
    }

    private static final Map<BlockPos, BlockPos> linkedChessboards = new HashMap<>();

    // Logic to link the chessboards
    private void linkChessboards(BlockPos first, BlockPos second) {
        if (first != null && second != null) {
            linkedChessboards.put(first, second);
            linkedChessboards.put(second, first);
        }

        if (first == null || second == null) {
            return;
        }

        // Check if one of the chessboards is already linked to another position
        if (linkedChessboards.containsKey(first)) {
            unlinkChessboards(first); // Unlink the first chessboard from its current link
            //event.getEntity().displayClientMessage(Component.literal("First chessboard was already linked. Previous link removed."), false);
        }

        if (linkedChessboards.containsKey(second)) {
            unlinkChessboards(second); // Unlink the second chessboard from its current link
            // event.getEntity().displayClientMessage(Component.literal("Second chessboard was already linked. Previous link removed."), false);
        }

        // Link the chessboards to each other
        linkedChessboards.put(first, second);
        linkedChessboards.put(second, first);

        //event.getEntity().displayClientMessage(Component.literal("Successfully linked chessboards at positions: " + first + " and " + second), false);}
    }

    private void unlinkChessboards(BlockPos pos) {
        BlockPos linked = linkedChessboards.remove(pos);
        if (linked != null) {
            linkedChessboards.remove(linked);
            //event.getEntity().displayClientMessage(Component.literal("Unlinked chessboards at positions: " + pos + " and " + linked), false);
        }
    }
}

