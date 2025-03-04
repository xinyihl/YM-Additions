package com.xinyihl.ymadditions.common.event;


import com.xinyihl.ymadditions.Tags;
import com.xinyihl.ymadditions.YMAdditions;
import com.xinyihl.ymadditions.common.api.IContaierTickable;
import com.xinyihl.ymadditions.common.api.data.NetworkHubDataStorage;
import com.xinyihl.ymadditions.common.api.data.NetworkStatus;
import com.xinyihl.ymadditions.common.network.PacketServerToClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

import java.util.List;

import static com.xinyihl.ymadditions.common.network.PacketServerToClient.ServerToClient.UPDATE_NETWORKS;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class EventHandler {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if (event.side.isClient()) return;
        if (event.phase != Phase.START) return;
        if (!(event.player instanceof EntityPlayerMP)) return;
        Container container = event.player.openContainer;
        if (container instanceof IContaierTickable) {
            ((IContaierTickable) container).update();
        }
        if (event.player.world.getTotalWorldTime() % 20 != 0) return;
        World world = event.player.world;
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        NetworkHubDataStorage storage = NetworkHubDataStorage.get(world);
        List<NetworkStatus> networks = storage.getNeedUpdateNetworks(player.getGameProfile().getId());
        if (!networks.isEmpty()) {
            NBTTagCompound tag = new NBTTagCompound();
            NBTTagList list = new NBTTagList();
            for (NetworkStatus network : networks) {
                list.appendTag(network.writeToNBT(new NBTTagCompound()));
                network.setNeedTellClient(false);
            }
            tag.setTag("networks", list);
            YMAdditions.instance.networkWrapper.sendTo(new PacketServerToClient(UPDATE_NETWORKS, tag), player);
        }
    }
}

