package com.xinyihl.ymadditions;

import com.xinyihl.ymadditions.common.container.GUIContainerHandler;
import com.xinyihl.ymadditions.common.integration.TheOneProbe;
import com.xinyihl.ymadditions.common.network.PacketClientToServer;
import com.xinyihl.ymadditions.common.network.PacketServerToClient;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, dependencies = "required-after:appliedenergistics2@[v0.56,);after:theoneprobe;after:crafttweaker")
public class YMAdditions {
    @Mod.Instance
    public static YMAdditions instance;
    public SimpleNetworkWrapper networkWrapper;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MOD_ID);
        networkWrapper.registerMessage(PacketClientToServer.class, PacketClientToServer.class, 0, Side.SERVER);
        networkWrapper.registerMessage(PacketServerToClient.class, PacketServerToClient.class, 1, Side.CLIENT);
        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", TheOneProbe.class.getName());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(YMAdditions.instance, new GUIContainerHandler());
    }
}
