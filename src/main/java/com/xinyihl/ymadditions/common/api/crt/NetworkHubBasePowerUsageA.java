package com.xinyihl.ymadditions.common.api.crt;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;

import java.util.Map;

@ZenRegister
@FunctionalInterface
@ZenClass("mods.ymadditions.NetworkHubBasePowerUsageA")
public interface NetworkHubBasePowerUsageA {
    double apply(Map<Double, Boolean> context);
}
