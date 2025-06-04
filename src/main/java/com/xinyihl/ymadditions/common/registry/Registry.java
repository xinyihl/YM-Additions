package com.xinyihl.ymadditions.common.registry;

import com.xinyihl.ymadditions.Tags;
import com.xinyihl.ymadditions.common.block.BlockNetworkHub;
import com.xinyihl.ymadditions.common.item.YMItemBlock;
import com.xinyihl.ymadditions.common.title.TileNetworkHub;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class Registry {

    public static Block blockNetworkHub;
    public static Item itemNetworkHub;
    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs("yudream_tab") {
        @Nonnull
        public ItemStack createIcon() {
            return new ItemStack(itemNetworkHub);
        }
    };

    @SubscribeEvent
    public static void registerItem(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(itemNetworkHub = new YMItemBlock(blockNetworkHub));
    }

    @SubscribeEvent
    public static void registerBlock(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(blockNetworkHub = new BlockNetworkHub());
        GameRegistry.registerTileEntity(TileNetworkHub.class, new ResourceLocation(Tags.MOD_ID, "tile_network_hub"));
    }

    @SubscribeEvent
    public static void registerModel(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(itemNetworkHub, 0, new ModelResourceLocation(Objects.requireNonNull(itemNetworkHub.getRegistryName()), "inventory"));
    }
}
