
package com.shnupbups.easyexcavate;

import com.shnupbups.easyexcavate.model.EasyExcavate;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;


public class EasyExcavateMod implements ModInitializer {

    public static EasyExcavate model;

    @Override
    public void onInitialize() {

        model = new EasyExcavate(FabricLoader.getInstance().getConfigDirectory());

        ServerSidePacketRegistry.INSTANCE.register(EasyExcavate.REQUEST_CONFIG, (packetContext, packetByteBuf) -> {
            model.resuestConfig((ServerPlayerEntity) packetContext.getPlayer(), packetByteBuf);
        });

        ServerSidePacketRegistry.INSTANCE.register(EasyExcavate.BREAK_BLOCK, (packetContext, packetByteBuf) -> {
            model.breakBock(packetContext.getPlayer(), packetByteBuf);
        });

        ServerSidePacketRegistry.INSTANCE.register(EasyExcavate.END, (packetContext, packetByteBuf) -> {
            model.end(packetContext.getPlayer(), packetByteBuf);
        });
    }
}
