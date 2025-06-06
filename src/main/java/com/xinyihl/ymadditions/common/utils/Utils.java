package com.xinyihl.ymadditions.common.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.StringUtils;

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
            String[] fbsArr = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|" };
            for (String key : fbsArr) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\" + key);
                }
            }
        }
        return keyword;
    }
}
