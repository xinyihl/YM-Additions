package com.xinyihl.ymadditions.common.integration.crt;

import com.xinyihl.ymadditions.Configurations;
import com.xinyihl.ymadditions.common.api.crt.NetworkHubBasePowerUsageA;
import com.xinyihl.ymadditions.common.api.crt.NetworkHubBasePowerUsageB;
import com.xinyihl.ymadditions.common.api.crt.NetworkHubPowerUsageA;
import com.xinyihl.ymadditions.common.api.crt.NetworkHubPowerUsageB;
import crafttweaker.annotations.ZenRegister;
import net.minecraft.util.math.BlockPos;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Map;

@ZenRegister
@ZenClass("mods.ymadditions.NetHubPowerUsage")
public class NetHubPowerUsage {
    public static boolean useA = true;
    public static boolean useBaseA = true;
    public static NetworkHubPowerUsageA netHubPowerUsageA  = (context, isOtherDim) -> (Configurations.GENERAL_CONFIG.powerBase + Configurations.GENERAL_CONFIG.powerDistanceMultiplier * context * Math.log(context * context + 3)) * (isOtherDim ? Configurations.GENERAL_CONFIG.otherDimMultiplier : 1);
    public static NetworkHubBasePowerUsageA netHubBasePowerUsageA  = (contexts) -> {
        double power = 0.0;
        for (Map.Entry<Double, Boolean> entry : contexts.entrySet()) {
            power += (Configurations.GENERAL_CONFIG.powerBase + Configurations.GENERAL_CONFIG.powerDistanceMultiplier * entry.getKey() * Math.log(entry.getKey() * entry.getKey() + 3)) * (entry.getValue() ? Configurations.GENERAL_CONFIG.otherDimMultiplier : 1);
        }
        return power;
    };

    public static NetworkHubPowerUsageB netHubPowerUsageB  = (a, b,isOtherDim) -> {
        int dx = a.getX() - b.getX();
        int dy = a.getY() - b.getY();
        int dz = a.getZ() - b.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        return (Configurations.GENERAL_CONFIG.powerBase + Configurations.GENERAL_CONFIG.powerDistanceMultiplier * dist * Math.log(dist * dist + 3)) * (isOtherDim ? Configurations.GENERAL_CONFIG.otherDimMultiplier : 1);
    };
    public static NetworkHubBasePowerUsageB netHubBasePowerUsageB  = (base, contexts) -> {
        double power = 0.0;
        for (Map.Entry<BlockPos, Boolean> entry : contexts.entrySet()) {
            int dx = base.getX() - entry.getKey().getX();
            int dy = base.getY() - entry.getKey().getY();
            int dz = base.getZ() - entry.getKey().getZ();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            power += (Configurations.GENERAL_CONFIG.powerBase + Configurations.GENERAL_CONFIG.powerDistanceMultiplier * dist * Math.log(dist * dist + 3)) * (entry.getValue() ? Configurations.GENERAL_CONFIG.otherDimMultiplier : 1);
        }
        return power;
    };

    @ZenMethod
    public static void calcNetHubPowerUsageA(NetworkHubPowerUsageA fun) {
        useA = true;
        netHubPowerUsageA = fun;
    }

    @ZenMethod
    public static void calcNetHubBasePowerUsageA(NetworkHubBasePowerUsageA fun) {
        useBaseA = true;
        netHubBasePowerUsageA = fun;
    }

    @ZenMethod
    public static void calcNetHubPowerUsageB(NetworkHubPowerUsageB fun) {
        useA = false;
        netHubPowerUsageB = fun;
    }

    @ZenMethod
    public static void calcNetHubBasePowerUsageB(NetworkHubBasePowerUsageB fun) {
        useBaseA = false;
        netHubBasePowerUsageB = fun;
    }
}
