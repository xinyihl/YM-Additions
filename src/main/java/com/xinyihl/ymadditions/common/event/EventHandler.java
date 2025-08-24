package com.xinyihl.ymadditions.common.event;


import appeng.util.Platform;
import com.xinyihl.ymadditions.Tags;
import com.xinyihl.ymadditions.api.IContaierTickable;
import com.xinyihl.ymadditions.api.IReadyable;
import com.xinyihl.ymadditions.common.utils.Utils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

import java.util.ArrayDeque;
import java.util.Deque;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class EventHandler {

    private static final Deque<IReadyable> readyQueue = new ArrayDeque<>();

    public static void enqueue(IReadyable tile) {
        if (Platform.isServer()) {
            readyQueue.offer(tile);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onServerTick(ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            while (!readyQueue.isEmpty()) {
                readyQueue.pop().onReady();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player.world.isRemote) return;
        Utils.syncDataStorage((EntityPlayerMP) event.player);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.player.world.isRemote) return;
        Utils.syncDataStorage((EntityPlayerMP) event.player);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if (event.side.isClient()) return;
        if (event.phase != Phase.START) return;
        if (!(event.player instanceof EntityPlayerMP)) return;
        if (event.player.world.getTotalWorldTime() % 10 == 0) {
            Container container = event.player.openContainer;
            if (container instanceof IContaierTickable) {
                ((IContaierTickable) container).update();
            }
        }
        if ((event.player.world.getTotalWorldTime() % (20 * 20)) == 0) {
            Utils.syncDataStorage((EntityPlayerMP) event.player);
        }
    }
}

