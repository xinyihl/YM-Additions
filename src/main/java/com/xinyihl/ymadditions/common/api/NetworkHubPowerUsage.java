package com.xinyihl.ymadditions.common.api;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;

@ZenRegister
@FunctionalInterface
@ZenClass("mods.ymadditions.NetworkHubPowerUsage")
public interface NetworkHubPowerUsage {
    double apply(double context, boolean isOtherDim);
}
