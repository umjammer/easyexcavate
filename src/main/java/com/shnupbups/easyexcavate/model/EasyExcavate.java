/*
 * Copyright (c) 2020 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.shnupbups.easyexcavate.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


/**
 * EasyExcavate.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2020/06/14 umjammer initial version <br>
 */
public class EasyExcavate {

    private static Log logger = LogFactory.getLog(EasyExcavate.class);

    private EasyExcavateConfig config;

    private static final String KEY_BIND_ID = "key.easyexcavate.excavate";
    private static final int KEY_BIND_CODE = 96;
    private static final String KEY_BIND_CATEGORY = "easyexcavate.category";

    /** */
    public KeyBinding keyBinding = new KeyBinding(KEY_BIND_ID, InputUtil.Type.KEYSYM, KEY_BIND_CODE, KEY_BIND_CATEGORY);

    /** */
    public EasyExcavate(File dir) {
        config = EasyExcavateConfig.fromFile(dir);
    }

    /** client */
    public void start(PlayerEntity player,
                      BlockPos pos,
                      Block block,
                      float hardness,
                      ItemStack tool,
                      EasyExcavateConfig serverConfig) {
        if (pos == null || block == null) {
            return;
        }
logger.debug("Start packet recieved! " + serverConfig);
        World world = player.getEntityWorld();
        int blocksBroken = 1;
        List<BlockPos> brokenPos = new ArrayList<>();
        brokenPos.add(pos);
        BlockPos currentPos = pos;
        List<BlockPos> nextPos = new ArrayList<>();
logger.debug(pos + " be: " + world.getBlockEntity(pos));
        float exhaust = 0;
        while (blocksBroken < serverConfig.maxBlocks && player.isUsingEffectiveTool(block.getDefaultState()) &&
               player.getHungerManager().getFoodLevel() > exhaust / 2 &&
               (!(block instanceof BlockWithEntity) || serverConfig.enableBlockEntities)) {
            if (serverConfig.isDeniedBlock(block) || serverConfig.isDeniedItem(tool) ||
                (tool == null && serverConfig.isToolRequired))
                break;
            List<BlockPos> neighbours = getSameNeighbours(world, currentPos, block);
            neighbours.removeAll(brokenPos);
            if (neighbours.size() >= 1) {
                for (BlockPos p : neighbours) {
                    if (blocksBroken >= serverConfig.maxBlocks || !player.isUsingEffectiveTool(block.getDefaultState()) ||
                        player.getHungerManager().getFoodLevel() <= exhaust / 2 ||
                        serverConfig.isDeniedBlock(block) || serverConfig.isDeniedItem(tool) ||
                        (block instanceof BlockWithEntity && !serverConfig.enableBlockEntities) ||
                        ((tool == null || !tool.getItem().isDamageable()) && serverConfig.isToolRequired) ||
                        (!serverConfig.dontTakeDurability && tool.getItem().isDamageable() &&
                         blocksBroken >= (tool.getMaxDamage() - tool.getDamage())))
                        break;
                    if (!brokenPos.contains(p) && player.isUsingEffectiveTool(world.getBlockState(p)) &&
                        (!serverConfig.checkHardness || world.getBlockState(p).getHardness(world, p) <= hardness)) {
                        if (Math.sqrt(p.getSquaredDistance(pos)) <= serverConfig.maxRange)
                            nextPos.add(p);
                        new BreakPacket(p).send();
                        brokenPos.add(p);
                        blocksBroken++;
                        exhaust = (0.005F * blocksBroken) * ((blocksBroken * serverConfig.bonusExhaustionMultiplier) + 1);
                    }
                }
            }
            if (nextPos.size() >= 1) {
                currentPos = nextPos.get(0);
                nextPos.remove(currentPos);
            } else {
                break;
            }
        }
        if (!player.isCreative()) {
            new EndPacket(blocksBroken).send();
logger.debug("End packet sent! blocks broken: " + blocksBroken);
        }
    }

    /** */
    private List<BlockPos> getSameNeighbours(World world, BlockPos pos, Block block) {
        List<BlockPos> list = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (!(x == 0 && y == 0 && z == 0) && world.getBlockState(pos.add(x, y, z)).getBlock().equals(block)) {
                        list.add(pos.add(x, y, z));
                    }
                }
            }
        }
        return list;
    }

    /** server */
    public void resuestConfig(ServerPlayerEntity player, BlockPos pos, Block block, float hardness, ItemStack tool) {
logger.debug("Config request packet recieved! pos: " + pos + " block: " + block + " hardness: " + hardness + " tool: " + tool);
        new StartPacket(pos, block, hardness, tool, config).send(player);
logger.debug("Start packet sent! pos: " + pos + " block: " + block + " hardness: " + hardness + " tool: " + tool + " " + config);
    }

    /** server */
    public void breakBlock(PlayerEntity player, BlockPos pos) {
        if (pos == null) {
            return;
        }
        World world = player.getEntityWorld();
        ItemStack stack = player.getMainHandStack();
        BlockState state = world.getBlockState(pos);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof Inventory) {
logger.debug(((Inventory) blockEntity).isEmpty());
            ItemScatterer.spawn(world, pos, (Inventory) blockEntity);
        }
        if (!config.dontTakeDurability) {
            stack.postMine(world, state, pos, player);
        }
        if (!player.isCreative()) {
            state.getBlock().afterBreak(world, player, pos, state, world.getBlockEntity(pos), stack.copy());
        }
        world.breakBlock(pos, false);
    }

    /** server */
    public void end(PlayerEntity player, int blocksBroken) {
        float exhaust = (0.005F * blocksBroken) * (blocksBroken * config.bonusExhaustionMultiplier);
        player.addExhaustion(exhaust);
logger.debug("End packet recieved! blocks broken: " + blocksBroken + " exhaust: " + exhaust + " actual exhaust: " + (exhaust + 0.005F * blocksBroken));
    }

    /** mixin */
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (world.isClient() &&
            (keyBinding.isPressed() && !config.reverseBehavior || !keyBinding.isPressed() && config.reverseBehavior) &&
            player.isUsingEffectiveTool(state) && player.getHungerManager().getFoodLevel() > 0) {

            new ConfigPacket(pos, state.getBlock(), state.getHardness(world, pos), player.getMainHandStack()).send();
        }
    }
}

/* */
