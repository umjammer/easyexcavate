/*
 * Copyright (c) 2020 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.shnupbups.easyexcavate.model;

import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;


/**
 * ConfigPacket.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2020/07/15 umjammer initial version <br>
 */
public class ConfigPacket implements Packet {

    /** */
    public static final Identifier IDENTIFIER = new Identifier("easyexcavate", "request_config");

    @Override
    public Identifier getIdentifier() {
        return IDENTIFIER;
    }

    public BlockPos pos = null;
    public Block block = null;
    public float hardness = 0.0f;
    public ItemStack tool = null;

    /** */
    public ConfigPacket(PacketByteBuf buf) {
        if (buf.readBoolean()) {
            pos = buf.readBlockPos();
            block = Registry.BLOCK.get(Identifier.splitOn(buf.readString(buf.readInt()), ':'));
            hardness = buf.readFloat();
        }
        if (pos == null || block == null)
            return;
        if (buf.readBoolean()) {
            tool = buf.readItemStack();
        }
    }

    /** */
    public ConfigPacket(BlockPos pos, Block block, float hardness, ItemStack tool) {
        this.pos = pos;
        this.block = block;
        this.hardness = hardness;
        this.tool = tool;
    }

    @Override
    public PacketByteBuf toPacket() {
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
        return buf;
    }
}

/* */
