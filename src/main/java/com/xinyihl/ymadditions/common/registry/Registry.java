package com.xinyihl.ymadditions.common.registry;

import com.xinyihl.ymadditions.Tags;
import com.xinyihl.ymadditions.common.block.*;
import com.xinyihl.ymadditions.common.item.*;
import com.xinyihl.ymadditions.common.title.*;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import static com.xinyihl.ymadditions.common.registry.BlocksAndItems.*;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class Registry {

    @SubscribeEvent
    public static void registerItem(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(itemNetworkHub = new YMItemBlock(blockNetworkHub));
    }

    @SubscribeEvent
    public static void registerBlock(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(blockNetworkHub = new BlockNetworkHub());
        GameRegistry.registerTileEntity(TileNetworkHub.class, new ResourceLocation(Tags.MOD_ID, "tile_network_hub"));
    }
}
