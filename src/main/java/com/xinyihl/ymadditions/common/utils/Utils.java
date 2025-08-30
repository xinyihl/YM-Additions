package com.xinyihl.ymadditions.common.utils;

import com.xinyihl.ymadditions.YMAdditions;
import com.xinyihl.ymadditions.api.ISyncable;
import com.xinyihl.ymadditions.common.network.PacketClientToServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

import static com.xinyihl.ymadditions.common.network.PacketClientToServer.ClientToServer.BUTTON_ACTION;

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

    public static void openGui(EntityPlayer player, int modGuiId, World world, int x, int y, int z) {
        FMLNetworkHandler.openGui(player, YMAdditions.instance, modGuiId, world, x, y, z);
        if (player instanceof EntityPlayerMP && !(player instanceof FakePlayer)) {
            if (player.openContainer instanceof ISyncable) {
                ((ISyncable) player.openContainer).sync();
            }
        }
    }

    public static void sendAction(String type) {
        sendAction(type, new NBTTagCompound());
    }

    public static void sendAction(String type, NBTTagCompound message) {
        message.setString("type", type);
        YMAdditions.instance.networkWrapper.sendToServer(new PacketClientToServer(BUTTON_ACTION, message));
    }

    public static void sendAction(String type, UUID uuid) {
        NBTTagCompound message = new NBTTagCompound();
        message.setUniqueId("uuid", uuid);
        sendAction(type, message);
    }

    public static void sendAction(String type, String string) {
        NBTTagCompound message = new NBTTagCompound();
        message.setString("string", string);
        sendAction(type, message);
    }
}
