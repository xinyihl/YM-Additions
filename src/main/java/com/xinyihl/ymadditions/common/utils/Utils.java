package com.xinyihl.ymadditions.common.utils;

import com.xinyihl.ymadditions.YMAdditions;
import com.xinyihl.ymadditions.common.data.DataStorage;
import com.xinyihl.ymadditions.common.network.PacketServerToClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import org.apache.commons.lang3.StringUtils;

import static com.xinyihl.ymadditions.common.network.PacketServerToClient.ServerToClient.WORLD_DATA_SYNC;

public class Utils {
    public static boolean isPlayerOp(EntityPlayer player) {
        MinecraftServer server = player.getServer();
        boolean isOp = false;
        if (server != null) {
            isOp = server.getPlayerList().canSendCommands(player.getGameProfile());
        }
        return isOp;
    }

    public static String escapeExprSpecialWord(String keyword) {
        if (StringUtils.isNotBlank(keyword)) {
            String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
            for (String key : fbsArr) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\" + key);
                }
            }
        }
        return keyword;
    }

    public static void syncDataStorage(EntityPlayerMP player) {
        WorldServer worldserver = DimensionManager.getWorld(0);
        DataStorage storage = DataStorage.get(worldserver);
        YMAdditions.instance.networkWrapper.sendTo(new PacketServerToClient(WORLD_DATA_SYNC, storage.getSyncDataPlayer(new NBTTagCompound(), player)), player);
    }
}
