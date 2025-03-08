package com.xinyihl.ymadditions.common.integration.crt;

import com.xinyihl.ymadditions.Configurations;
import com.xinyihl.ymadditions.common.api.NetworkHubPowerUsage;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.ymadditions.NetHubPowerUsage")
public class NetHubPowerUsage {
    public static NetworkHubPowerUsage netHubPowerUsage  = (context, isOtherDim) -> (Configurations.GENERAL_CONFIG.powerBase + Configurations.GENERAL_CONFIG.powerDistanceMultiplier * context * Math.log(context * context + 3)) * (isOtherDim ? Configurations.GENERAL_CONFIG.otherBimMultiplier : 0);

    @ZenMethod
    public static void calcNetHubPowerUsage(NetworkHubPowerUsage fun) {
        netHubPowerUsage = fun;
    }
}
