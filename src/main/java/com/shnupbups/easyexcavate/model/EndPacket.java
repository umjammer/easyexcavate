/*
 * Copyright (c) 2020 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.shnupbups.easyexcavate.model;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;


/**
 * EndPacket.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2020/07/15 umjammer initial version <br>
 */
public class EndPacket implements Packet {

    /** */
    public static final Identifier IDENTIFIER = new Identifier("easyexcavate", "end");

    @Override
    public Identifier getIdentifier() {
        return IDENTIFIER;
    }

    public int blocksBroken;

    /** */
    public EndPacket(PacketByteBuf buf) {
        blocksBroken = buf.readInt();
    }

    /** */
    public EndPacket(int blocksBroken) {
        this.blocksBroken = blocksBroken;
    }

    @Override
    public PacketByteBuf toPacket() {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(blocksBroken);
        return buf;
    }
}

/* */
