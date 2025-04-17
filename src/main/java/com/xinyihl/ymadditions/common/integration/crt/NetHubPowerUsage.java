package com.xinyihl.ymadditions.common.integration.crt;

import com.xinyihl.ymadditions.Configurations;
import com.xinyihl.ymadditions.common.api.crt.NetworkHubPowerUsageA;
import com.xinyihl.ymadditions.common.api.crt.NetworkHubPowerUsageB;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.ymadditions.NetHubPowerUsage")
public class NetHubPowerUsage {
    public static boolean useA = true;
    public static NetworkHubPowerUsageA netHubPowerUsageA  = (context, isOtherDim) -> (Configurations.GENERAL_CONFIG.powerBase + Configurations.GENERAL_CONFIG.powerDistanceMultiplier * context * Math.log(context * context + 3)) * (isOtherDim ? Configurations.GENERAL_CONFIG.otherDimMultiplier : 1);
    public static NetworkHubPowerUsageB netHubPowerUsageB  = (a, b, ad, bd) -> {
        int dx = a.getX() - b.getX();
        int dy = a.getY() - b.getY();
        int dz = a.getZ() - b.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        return (Configurations.GENERAL_CONFIG.powerBase + Configurations.GENERAL_CONFIG.powerDistanceMultiplier * dist * Math.log(dist * dist + 3)) * (ad == bd ? Configurations.GENERAL_CONFIG.otherDimMultiplier : 1);
    };

    @ZenMethod
    public static void calcNetHubPowerUsage(NetworkHubPowerUsageA fun) {
        useA = true;
        netHubPowerUsageA = fun;
    }

    @ZenMethod
    public static void calcNetHubPowerUsage(NetworkHubPowerUsageB fun) {
        useA = false;
        netHubPowerUsageB = fun;
    }
}
