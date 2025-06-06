package com.xinyihl.ymadditions.common.event;


import com.xinyihl.ymadditions.Tags;
import com.xinyihl.ymadditions.YMAdditions;
import com.xinyihl.ymadditions.common.api.IContaierTickable;
import com.xinyihl.ymadditions.common.data.DataStorage;
import com.xinyihl.ymadditions.common.data.NetworkStatus;
import com.xinyihl.ymadditions.common.network.PacketServerToClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.xinyihl.ymadditions.common.network.PacketServerToClient.ServerToClient.INIT_NETWORKS;
import static com.xinyihl.ymadditions.common.network.PacketServerToClient.ServerToClient.UPDATE_NETWORKS;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class NetWorkSyncHandler {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event){
        if(event.player.world.isRemote) return;
        initSyncDate((EntityPlayerMP) event.player);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event){
        if(event.player.world.isRemote) return;
        initSyncDate((EntityPlayerMP) event.player);
    }

    private static void initSyncDate(EntityPlayerMP player){
        World world = player.world;
        DataStorage storage = DataStorage.get(world);
        List<NetworkStatus> networks = storage.getPlayerNetworks(player);
        if (!networks.isEmpty()) {
            YMAdditions.instance.networkWrapper.sendTo(new PacketServerToClient(INIT_NETWORKS, networksToNBT(networks)), player);
        }
    }

    private static NBTTagCompound networksToNBT(List<NetworkStatus> networks) {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        for (NetworkStatus network : networks) {
            list.appendTag(network.writeToNBT(new NBTTagCompound()));
        }
        tag.setTag("networks", list);
        return tag;
    }

    @SubscribeEvent
    public static void onWorldTick(WorldTickEvent event) {
        if (event.side.isClient()) return;
        if (event.phase != Phase.START) return;
        if (!(event.world instanceof WorldServer)) return;
        if (event.world.getTotalWorldTime() % 20 != 0) return;
        DataStorage storage = DataStorage.get(event.world);
        if (event.world.getMinecraftServer() != null) {
            Set<NetworkStatus> updateNetworks = new HashSet<>();
            event.world.getMinecraftServer().getPlayerList().getPlayers().forEach(player -> {
                List<NetworkStatus> networks = storage.getPlayerNeedUpdateNetworks(player);
                if (!networks.isEmpty()) {
                    updateNetworks.addAll(networks);
                    YMAdditions.instance.networkWrapper.sendTo(new PacketServerToClient(UPDATE_NETWORKS, networksToNBT(networks)), player);
                }
            });
            updateNetworks.forEach((networkStatus) -> networkStatus.setNeedTellClient(false));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if (event.side.isClient()) return;
        if (event.phase != Phase.START) return;
        if (!(event.player instanceof EntityPlayerMP)) return;
        Container container = event.player.openContainer;
        if (container instanceof IContaierTickable) {
            ((IContaierTickable) container).update();
        }
    }
}

