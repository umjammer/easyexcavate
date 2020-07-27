/*
 * Copyright (c) 2020 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.shnupbups.easyexcavate.model;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;


/**
 * BreakPacket.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2020/07/15 umjammer initial version <br>
 */
public class BreakPacket implements Packet {

    /** */
    public static final Identifier IDENTIFIER = new Identifier("easyexcavate", "break_block");

    @Override
    public Identifier getIdentifier() {
        return IDENTIFIER;
    }

    public BlockPos pos;

    /** */
    public BreakPacket(PacketByteBuf buf) {
        if (buf.readBoolean()) {
            pos = buf.readBlockPos();
        }
    }

    /** */
    public BreakPacket(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public PacketByteBuf toPacket() {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBoolean(pos != null);
        if (pos != null) {
            buf.writeBlockPos(pos);
        }
        return buf;
    }
}

/* */
