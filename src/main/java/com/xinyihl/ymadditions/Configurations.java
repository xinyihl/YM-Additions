package com.xinyihl.ymadditions;

import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MOD_ID, name = Tags.MOD_NAME)
public class Configurations {

    @Config.Comment("GeneralConfig")
    public static final GeneralConfig GENERAL_CONFIG = new GeneralConfig();

    public static class GeneralConfig {
        @Config.Comment("Can ME wireless transceivers be cross-dimensional\nME无线收发器是否可以跨维度")
        public boolean canRDimension = true;
        @Config.Comment("Nodal energy consumption = (powerBase + powerDistanceMultiplier * Distance * ln(Distance^2 + 3)) * otherDimMultiplier (AE/t)")
        public double powerBase = 1000D;
        public double powerDistanceMultiplier = 0.1D;
        public double otherDimMultiplier = 10D;
        @Config.Comment("Whether or not to display the network UUID on the TOP\n是否在TOP上显示网络UUID")
        public boolean doNetworkUUIDShow = false;
    }
}
