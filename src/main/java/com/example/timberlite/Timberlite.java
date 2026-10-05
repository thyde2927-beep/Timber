package com.example.timberlite;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class Timberlite implements ModInitializer {
    private static final int MAX_LOGS = 128;

    @Override
    public void onInitialize() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) return;
            if (player.isSneaking() || player.isCreative()) return;

            ItemStack tool = player.getMainHandStack();
            if (!(tool.getItem() instanceof AxeItem)) return;
            if (!state.isIn(BlockTags.LOGS)) return;

            fellTree(world, serverPlayer, pos, tool);
        });
    }

    private void fellTree(World world, ServerPlayerEntity player, BlockPos start, ItemStack tool) {
        Set<BlockPos> seen = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        seen.add(start);
        int broken = 0;

        while (!queue.isEmpty() && broken < MAX_LOGS) {
            BlockPos current = queue.poll();

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos next = current.add(dx, dy, dz);
                        if (!seen.add(next)) continue;

                        BlockState nextState = world.getBlockState(next);
                        if (!nextState.isIn(BlockTags.LOGS)) continue;
                        if (broken >= MAX_LOGS || tool.isEmpty()) return;

                        world.breakBlock(next, true, player);
                        tool.damage(1, player, net.minecraft.entity.EquipmentSlot.MAINHAND);
                        broken++;
                        queue.add(next);
                    }
                }
            }
        }
    }
}
