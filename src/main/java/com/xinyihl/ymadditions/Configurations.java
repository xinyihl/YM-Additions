package com.xinyihl.ymadditions;

import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MOD_ID, name = Tags.MOD_NAME)
public class Configurations {

    @Config.Comment("GeneralConfig")
    public static final GeneralConfig GENERAL_CONFIG = new GeneralConfig();

    public static class GeneralConfig {
        @Config.Comment("ME无线收发器是否可以跨维度")
        public boolean canRDimension = false;
        @Config.Comment("子节点能量消耗 = powerBase + powerDistanceMultiplier * Distance * ln(Distance^2 + 3) (AE/t)")
        public double powerBase = 1000D;
        public double powerDistanceMultiplier = 0.5D;
        @Config.Comment("主节点能量消耗 = powerHeadBase * size (子节点个数)")
        public double powerHeadBase = 10000D;
        @Config.Comment("是否在TOP上显示网络UUID")
        public boolean doNetworkUUIDShow = false;
    }
}
