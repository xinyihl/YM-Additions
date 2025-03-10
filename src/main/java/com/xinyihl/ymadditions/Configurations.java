package com.xinyihl.ymadditions;

import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MOD_ID, name = Tags.MOD_NAME)
public class Configurations {

    @Config.Comment("GeneralConfig")
    public static final GeneralConfig GENERAL_CONFIG = new GeneralConfig();

    public static class GeneralConfig {
        @Config.Comment("ME无线收发器是否可以跨维度（修改会导致原网络丢失）")
        public boolean canRDimension = true;
        @Config.Comment("节点能量消耗 = (powerBase + powerDistanceMultiplier * Distance * ln(Distance^2 + 3)) * otherDimMultiplier (AE/t)\n主节点为全部连接相加\n\n基础耗电量")
        public double powerBase = 1D;
        @Config.Comment("耗电乘数")
        public double powerDistanceMultiplier = 0.1D;
        @Config.Comment("跨纬度耗电乘数")
        public double otherDimMultiplier = 100D;
        @Config.Comment("是否在TOP上显示网络UUID")
        public boolean doNetworkUUIDShow = false;
    }
}
