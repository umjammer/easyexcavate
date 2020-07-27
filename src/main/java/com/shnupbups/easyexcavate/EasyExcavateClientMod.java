
package com.shnupbups.easyexcavate;

import com.shnupbups.easyexcavate.model.StartPacket;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;


public class EasyExcavateClientMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(EasyExcavateMod.model.keyBinding);

        ClientSidePacketRegistry.INSTANCE.register(StartPacket.IDENTIFIER, (packetContext, packetByteBuf) -> {
            StartPacket p = new StartPacket(packetByteBuf);
            EasyExcavateMod.model.start(packetContext.getPlayer(), p.pos, p.block, p.hardness, p.tool, p.serverConfig);
        });
    }
}
