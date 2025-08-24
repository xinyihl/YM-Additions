package com.xinyihl.ymadditions.api.crt;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.world.IBlockPos;
import stanhebben.zenscript.annotations.ZenClass;

@ZenRegister
@FunctionalInterface
@ZenClass("mods.ymadditions.NetworkHubPowerUsageB")
public interface NetworkHubPowerUsageB {
    double apply(IBlockPos thisBlockPos, IBlockPos thatBlockPos, int thisDimension, int thatDimension);
}
