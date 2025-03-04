package com.xinyihl.ymadditions.common.registry;

import com.xinyihl.ymadditions.Tags;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Objects;

import static com.xinyihl.ymadditions.common.registry.BlocksAndItems.*;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public class ModelRegistry {

    @SubscribeEvent
    public static void registerModel(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(itemNetworkHub, 0, new ModelResourceLocation(Objects.requireNonNull(itemNetworkHub.getRegistryName()), "inventory"));
    }
}
