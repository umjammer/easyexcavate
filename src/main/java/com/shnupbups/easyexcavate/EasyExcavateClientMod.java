
package com.shnupbups.easyexcavate;

import com.shnupbups.easyexcavate.model.EasyExcavate;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;


public class EasyExcavateClientMod implements ClientModInitializer {
    public static KeyBinding keybind;

    @Override
    public void onInitializeClient() {
        keybind = new KeyBinding(EasyExcavate.KEY_BIND_ID, InputUtil.Type.KEYSYM, EasyExcavate.KEY_BIND_CODE, EasyExcavate.KEY_BIND_CATEGORY);
        KeyBindingHelper.registerKeyBinding(keybind);

        ClientSidePacketRegistry.INSTANCE.register(EasyExcavate.START, (packetContext, packetByteBuf) -> {
            EasyExcavateMod.model.start(packetContext.getPlayer(), packetByteBuf);
        });
    }
}
