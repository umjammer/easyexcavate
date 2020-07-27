/*
 * Copyright (c) 2020 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.shnupbups.easyexcavate.model;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;


/**
 * Packet.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2020/07/15 umjammer initial version <br>
 */
public interface Packet {

    /** id */
    Identifier getIdentifier();

    /** serialize */
    PacketByteBuf toPacket();

    /** to server */
    default void send() {
        MinecraftClient.getInstance().getNetworkHandler().getConnection().send(new CustomPayloadC2SPacket(getIdentifier(), toPacket()));
    }

    /** to client */
    default void send(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(getIdentifier(), toPacket()));
    }
}

/* */
