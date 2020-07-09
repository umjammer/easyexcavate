
package com.shnupbups.easyexcavate.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.shnupbups.easyexcavate.EasyExcavateMod;
import com.shnupbups.easyexcavate.model.EasyExcavate;
import com.shnupbups.easyexcavate.EasyExcavateClientMod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


@Mixin(Block.class)
public abstract class BlockBreakMixin {

    @Inject(at = @At(value = "HEAD"), method = "onBreak")
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo cbinfo) {
        if (world.isClient() &&
            (EasyExcavateClientMod.keybind.isPressed() && !EasyExcavateMod.model.reverseBehavior() ||
             !EasyExcavateClientMod.keybind.isPressed() && EasyExcavateMod.model.reverseBehavior()) &&
            player.isUsingEffectiveTool(state) && player.getHungerManager().getFoodLevel() > 0) {
            MinecraftClient.getInstance()
                    .getNetworkHandler()
                    .getConnection()
                    .send(EasyExcavate.createRequestPacket(pos,
                                                    state.getBlock(),
                                                    state.getHardness(world, pos),
                                                    player.getMainHandStack()));
        }
    }
}
