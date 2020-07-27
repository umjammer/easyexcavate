
package com.shnupbups.easyexcavate;

import com.shnupbups.easyexcavate.model.BreakPacket;
import com.shnupbups.easyexcavate.model.ConfigPacket;
import com.shnupbups.easyexcavate.model.EasyExcavate;
import com.shnupbups.easyexcavate.model.EndPacket;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;


public class EasyExcavateMod implements ModInitializer {

    public static EasyExcavate model;

    @Override
    public void onInitialize() {

        model = new EasyExcavate(FabricLoader.getInstance().getConfigDirectory());

        ServerSidePacketRegistry.INSTANCE.register(ConfigPacket.IDENTIFIER, (packetContext, packetByteBuf) -> {
            ConfigPacket p = new ConfigPacket(packetByteBuf);
            model.resuestConfig((ServerPlayerEntity) packetContext.getPlayer(), p.pos, p.block, p.hardness, p.tool);
        });

        ServerSidePacketRegistry.INSTANCE.register(BreakPacket.IDENTIFIER, (packetContext, packetByteBuf) -> {
            model.breakBlock(packetContext.getPlayer(), new BreakPacket(packetByteBuf).pos);
        });

        ServerSidePacketRegistry.INSTANCE.register(EndPacket.IDENTIFIER, (packetContext, packetByteBuf) -> {
            model.end(packetContext.getPlayer(), new EndPacket(packetByteBuf).blocksBroken);
        });
    }
}
