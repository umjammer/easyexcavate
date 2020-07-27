
package com.shnupbups.easyexcavate.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.shnupbups.easyexcavate.EasyExcavateMod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


@Mixin(Block.class)
public abstract class BlockBreakMixin {

    @Inject(at = @At(value = "HEAD"), method = "onBreak")
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo cbinfo) {
        EasyExcavateMod.model.onBreak(world, pos, state, player);
    }
}
