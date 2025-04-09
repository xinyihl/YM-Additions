package com.xinyihl.ymadditions.common.api.crt;

import crafttweaker.annotations.ZenRegister;
import net.minecraft.util.math.BlockPos;
import stanhebben.zenscript.annotations.ZenClass;

@ZenRegister
@FunctionalInterface
@ZenClass("mods.ymadditions.NetworkHubPowerUsageB")
public interface NetworkHubPowerUsageB {
    double apply(BlockPos a, BlockPos b, boolean isOtherDim);
}
