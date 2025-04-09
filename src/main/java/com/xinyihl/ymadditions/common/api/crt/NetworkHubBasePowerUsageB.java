package com.xinyihl.ymadditions.common.api.crt;

import crafttweaker.annotations.ZenRegister;
import net.minecraft.util.math.BlockPos;
import stanhebben.zenscript.annotations.ZenClass;

import java.util.Map;

@ZenRegister
@FunctionalInterface
@ZenClass("mods.ymadditions.NetworkHubBasePowerUsageB")
public interface NetworkHubBasePowerUsageB {
    double apply(BlockPos a, Map<BlockPos, Boolean> context);
}
