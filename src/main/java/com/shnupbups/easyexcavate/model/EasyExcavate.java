/*
 * Copyright (c) 2020 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.shnupbups.easyexcavate.model;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shnupbups.easyexcavate.EasyExcavateMod;

import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;


/**
 * EasyExcavate.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2020/06/14 umjammer initial version <br>
 */
public class EasyExcavate {

    private EasyExcavateConfig config;

    public static final Identifier REQUEST_CONFIG = new Identifier("easyexcavate", "request_config");
    public static final Identifier START = new Identifier("easyexcavate", "start");
    public static final Identifier END = new Identifier("easyexcavate", "end");
    public static final Identifier BREAK_BLOCK = new Identifier("easyexcavate", "break_block");

    public static final String KEY_BIND_ID = "key.easyexcavate.excavate";
    public static final int KEY_BIND_CODE = 96;
    public static final String KEY_BIND_CATEGORY = "easyexcavate.category";

    /** */
    public EasyExcavate(File dir) {
        File configFile = new File(dir, "easyexcavate.json");
        try (FileReader reader = new FileReader(configFile)) {
            config = new Gson().fromJson(reader, EasyExcavateConfig.class);
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(config));
            } catch (IOException e2) {
                System.out.println("[EasyExcavate] Failed to update config file!");
            }
            System.out.println("[EasyExcavate] Config loaded!");
            debugOut("[EasyExcavate] Debug Output enabled! " + config.toString());
        } catch (IOException e) {
            System.out.println("[EasyExcavate] No config found, generating!");
            config = new EasyExcavateConfig();
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(config));
            } catch (IOException e2) {
                System.out.println("[EasyExcavate] Failed to generate config file!");
            }
        }
    }

    /** */
    public void start(PlayerEntity player, PacketByteBuf packetByteBuf) {
        BlockPos pos = null;
        Block block = null;
        float hardness = 0.0f;
        ItemStack tool = null;
        if (packetByteBuf.readBoolean()) {
            pos = packetByteBuf.readBlockPos();
            block = Registry.BLOCK.get(Identifier.splitOn(packetByteBuf.readString(packetByteBuf.readInt()), ':'));
            hardness = packetByteBuf.readFloat();
        }
        if (pos == null || block == null)
            return;
        if (packetByteBuf.readBoolean()) {
            tool = packetByteBuf.readItemStack();
        }
        EasyExcavateConfig serverConfig = EasyExcavateConfig.readConfig(packetByteBuf);
        EasyExcavateMod.model.debugOut("Start packet recieved! " + serverConfig.toString());
        World world = player.getEntityWorld();
        int blocksBroken = 1;
        List<BlockPos> brokenPos = new ArrayList<>();
        brokenPos.add(pos);
        BlockPos currentPos = pos;
        List<BlockPos> nextPos = new ArrayList<>();
        EasyExcavateMod.model.debugOut(pos + " be: " + world.getBlockEntity(pos));
        float exhaust = 0;
        while (blocksBroken < serverConfig.maxBlocks && player.isUsingEffectiveTool(block.getDefaultState()) &&
               player.getHungerManager().getFoodLevel() > exhaust / 2 &&
               (!(block instanceof BlockWithEntity) || serverConfig.enableBlockEntities)) {
            if ((Arrays.asList(serverConfig.blacklistBlocks).contains(Registry.BLOCK.getId(block).toString()) &&
                 !serverConfig.invertBlockBlacklist) ||
                (!Arrays.asList(serverConfig.blacklistBlocks).contains(Registry.BLOCK.getId(block).toString()) &&
                 serverConfig.invertBlockBlacklist) ||
                (tool != null &&
                 Arrays.asList(serverConfig.blacklistTools)
                         .contains(String.valueOf(Registry.ITEM.getId(tool.getItem()).toString())) &&
                 !serverConfig.invertToolBlacklist) ||
                (tool != null &&
                 !Arrays.asList(serverConfig.blacklistTools)
                         .contains(String.valueOf(Registry.ITEM.getId(tool.getItem()).toString())) &&
                 serverConfig.invertToolBlacklist) ||
                (tool == null && serverConfig.isToolRequired))
                break;
            List<BlockPos> neighbours = getSameNeighbours(world, currentPos, block);
            neighbours.removeAll(brokenPos);
            if (neighbours.size() >= 1) {
                for (BlockPos p : neighbours) {
                    if (blocksBroken >= serverConfig.maxBlocks || !player.isUsingEffectiveTool(block.getDefaultState()) ||
                        player.getHungerManager().getFoodLevel() <= exhaust / 2 ||
                        Arrays.asList(serverConfig.blacklistBlocks)
                                .contains(Registry.BLOCK.getId(block).toString()) && !serverConfig.invertBlockBlacklist ||
                        !Arrays.asList(serverConfig.blacklistBlocks)
                                .contains(Registry.BLOCK.getId(block).toString()) && serverConfig.invertBlockBlacklist ||
                        (tool != null &&
                         Arrays.asList(serverConfig.blacklistTools)
                                 .contains(String.valueOf(Registry.ITEM.getId(tool.getItem()).toString())) &&
                         !serverConfig.invertToolBlacklist) ||
                        (tool != null &&
                         !Arrays.asList(serverConfig.blacklistTools)
                                 .contains(String.valueOf(Registry.ITEM.getId(tool.getItem()).toString())) &&
                         serverConfig.invertToolBlacklist) ||
                        (block instanceof BlockWithEntity && !serverConfig.enableBlockEntities) ||
                        ((tool == null || !tool.getItem().isDamageable()) && serverConfig.isToolRequired) ||
                        (!serverConfig.dontTakeDurability && tool.getItem().isDamageable() &&
                         blocksBroken >= (tool.getMaxDamage() - tool.getDamage())))
                        break;
                    if (!brokenPos.contains(p) && player.isUsingEffectiveTool(world.getBlockState(p)) &&
                        (!serverConfig.checkHardness || world.getBlockState(p).getHardness(world, p) <= hardness)) {
                        if (Math.sqrt(p.getSquaredDistance(pos)) <= serverConfig.maxRange)
                            nextPos.add(p);
                        MinecraftClient.getInstance().getNetworkHandler().getConnection().send(EasyExcavate.createBreakPacket(p));
                        brokenPos.add(p);
                        blocksBroken++;
                        exhaust = (0.005F * blocksBroken) * ((blocksBroken * serverConfig.bonusExhaustionMultiplier) + 1);
                    }
                }
            }
            if (nextPos.size() >= 1) {
                currentPos = nextPos.get(0);
                nextPos.remove(currentPos);
            } else
                break;
        }
        if (!player.isCreative()) {
            MinecraftClient.getInstance().getNetworkHandler().getConnection().send(EasyExcavate.createEndPacket(blocksBroken));
            EasyExcavateMod.model.debugOut("End packet sent! blocks broken: " + blocksBroken);
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

    /** */
    public void resuestConfig(ServerPlayerEntity player, PacketByteBuf packetByteBuf) {
        BlockPos pos = null;
        Block block = null;
        float hardness = 0.0f;
        ItemStack tool = null;
        if (packetByteBuf.readBoolean()) {
            pos = packetByteBuf.readBlockPos();
            block = Registry.BLOCK.get(Identifier.splitOn(packetByteBuf.readString(packetByteBuf.readInt()), ':'));
            hardness = packetByteBuf.readFloat();
        }
        if (pos == null || block == null)
            return;
        if (packetByteBuf.readBoolean()) {
            tool = packetByteBuf.readItemStack();
        }
        debugOut("Config request packet recieved! pos: " + pos + " block: " + block + " hardness: " + hardness + " tool: " +
                 tool);
        player.networkHandler.sendPacket(createStartPacket(pos, block, hardness, tool, config));
        debugOut("Start packet sent! pos: " + pos + " block: " + block + " hardness: " + hardness + " tool: " + tool + " " +
                 config.toString());
    };

    /** */
    public void breakBock(PlayerEntity player, PacketByteBuf packetByteBuf) {
        BlockPos pos = null;
        if (packetByteBuf.readBoolean()) {
            pos = packetByteBuf.readBlockPos();
        }
        if (pos == null)
            return;
        World world = player.getEntityWorld();
        ItemStack stack = player.getMainHandStack();
        BlockState state = world.getBlockState(pos);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof Inventory) {
            debugOut(((Inventory) blockEntity).isEmpty());
            ItemScatterer.spawn(world, pos, (Inventory) blockEntity);
        }
        if (!config.dontTakeDurability)
            stack.postMine(world, state, pos, player);
        if (!player.isCreative()) {
            state.getBlock().afterBreak(world, player, pos, state, world.getBlockEntity(pos), stack.copy());
        }
        world.breakBlock(pos, false);
    };

    /** */
    public void end(PlayerEntity player, PacketByteBuf packetByteBuf) {
        int blocksBroken = packetByteBuf.readInt();
        float exhaust = (0.005F * blocksBroken) * (blocksBroken * config.bonusExhaustionMultiplier);
        player.addExhaustion(exhaust);
        debugOut("End packet recieved! blocks broken: " + blocksBroken + " exhaust: " + exhaust + " actual exhaust: " +
                 (exhaust + 0.005F * blocksBroken));
    };

    /** */
    public static CustomPayloadC2SPacket createRequestPacket(BlockPos pos, Block block, float hardness, ItemStack tool) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBoolean(pos != null && block != null);
        if (pos != null && block != null) {
            buf.writeBlockPos(pos);
            String s = Registry.BLOCK.getId(block).toString();
            buf.writeInt(s.length());
            buf.writeString(s);
            buf.writeFloat(hardness);
        }
        buf.writeBoolean(tool != null);
        if (tool != null) {
            buf.writeItemStack(tool);
        }
        return new CustomPayloadC2SPacket(REQUEST_CONFIG, buf);
    }

    /** */
    private static CustomPayloadS2CPacket createStartPacket(BlockPos pos,
                                                           Block block,
                                                           float hardness,
                                                           ItemStack tool,
                                                           EasyExcavateConfig serverConfig) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBoolean(pos != null && block != null);
        if (pos != null && block != null) {
            buf.writeBlockPos(pos);
            String s = Registry.BLOCK.getId(block).toString();
            buf.writeInt(s.length());
            buf.writeString(s);
            buf.writeFloat(hardness);
        }
        buf.writeBoolean(tool != null);
        if (tool != null) {
            buf.writeItemStack(tool);
        }
        serverConfig.writeConfig(buf);
        return new CustomPayloadS2CPacket(START, buf);
    }

    /** */
    private static CustomPayloadC2SPacket createEndPacket(int blocksBroken) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(blocksBroken);
        return new CustomPayloadC2SPacket(END, buf);
    }

    /** */
    private static CustomPayloadC2SPacket createBreakPacket(BlockPos pos) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBoolean(pos != null);
        if (pos != null) {
            buf.writeBlockPos(pos);
        }
        return new CustomPayloadC2SPacket(BREAK_BLOCK, buf);
    }

    /** */
    private String debugOut(String out) {
        if (config.debugOutput)
            System.out.println(out);
        return out;
    }

    /** */
    private Object debugOut(Object out) {
        debugOut(out.toString());
        return out;
    }

    /** */
    public boolean reverseBehavior() {
        return config.reverseBehavior;
    }

    public boolean debug() {
        return config.debugOutput;
    }
}

/* */
