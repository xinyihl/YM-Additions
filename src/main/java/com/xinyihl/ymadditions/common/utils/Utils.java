package com.xinyihl.ymadditions.common.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class Utils {
    public static boolean isPlayerOp(EntityPlayer player) {
        MinecraftServer server = player.getServer();
        boolean isOp = false;
        if (server != null) {
            isOp = server.getPlayerList().canSendCommands(player.getGameProfile());
        }
        return isOp;
    }
}
