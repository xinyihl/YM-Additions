package com.xinyihl.ymadditions.common.api.crt;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;

@ZenRegister
@FunctionalInterface
@ZenClass("mods.ymadditions.NetworkHubPowerUsageA")
public interface NetworkHubPowerUsageA {
    double apply(double context, boolean isOtherDim);
}
