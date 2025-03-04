package com.xinyihl.ymadditions.common.registry;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class BlocksAndItems {
    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs("yudream_tab") {
        public ItemStack createIcon() {
            return new ItemStack(itemNetworkHub);
        }
    };
    public static Block blockNetworkHub;
    public static Item itemNetworkHub;
}
