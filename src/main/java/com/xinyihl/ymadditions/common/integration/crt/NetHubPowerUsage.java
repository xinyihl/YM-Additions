package com.xinyihl.ymadditions.common.integration.crt;

import com.xinyihl.ymadditions.Configurations;
import com.xinyihl.ymadditions.common.api.crt.NetworkHubPowerUsageA;
import com.xinyihl.ymadditions.common.api.crt.NetworkHubPowerUsageB;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.ymadditions.NetHubPowerUsage")
public class NetHubPowerUsage {
    public static boolean useA = true;
    public static NetworkHubPowerUsageA netHubPowerUsageA  = (context, isOtherDim) -> (Configurations.GENERAL_CONFIG.powerBase + Configurations.GENERAL_CONFIG.powerDistanceMultiplier * context * Math.log(context * context + 3)) * (isOtherDim ? Configurations.GENERAL_CONFIG.otherDimMultiplier : 1);
    public static NetworkHubPowerUsageB netHubPowerUsageB;

    static {
        if (Loader.isModLoaded("crafttweaker")) init();
    }

    @Optional.Method(modid = "crafttweaker")
    public static void init(){
        netHubPowerUsageB = (a, b, ad, bd) -> {
            int dx = a.getX() - b.getX();
            int dy = a.getY() - b.getY();
            int dz = a.getZ() - b.getZ();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            return (Configurations.GENERAL_CONFIG.powerBase + Configurations.GENERAL_CONFIG.powerDistanceMultiplier * dist * Math.log(dist * dist + 3)) * (ad == bd ? Configurations.GENERAL_CONFIG.otherDimMultiplier : 1);
        };
    }

    public static double calcNetHubPowerUsage(BlockPos thisBlockPos, BlockPos thatBlockPos, int thisDimension, int thatDimension) {
        if (NetHubPowerUsage.useA) {
            int dx = thisBlockPos.getX() - thatBlockPos.getX();
            int dy = thisBlockPos.getY() - thatBlockPos.getY();
            int dz = thisBlockPos.getZ() - thatBlockPos.getZ();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            return netHubPowerUsageA.apply(dist, thisDimension != thatDimension);
        } else {
            return netHubPowerUsageB.apply(CraftTweakerMC.getIBlockPos(thisBlockPos), CraftTweakerMC.getIBlockPos(thatBlockPos), thisDimension, thatDimension);
        }
    }

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
