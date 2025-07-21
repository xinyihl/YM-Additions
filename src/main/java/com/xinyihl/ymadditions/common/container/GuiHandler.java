package com.xinyihl.ymadditions.common.container;

import com.xinyihl.ymadditions.client.gui.GuiNetworkHubCore;
import com.xinyihl.ymadditions.common.title.TileNetworkHub;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;
import java.util.Objects;

public class GuiHandler implements IGuiHandler {

    public static final int GUI_NETWORK_HUB = 1;

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GUI_NETWORK_HUB) {
            return new ContainerNetworkHub(player, (TileNetworkHub) Objects.requireNonNull(player.world.getTileEntity(new BlockPos(x, y, z))));
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GUI_NETWORK_HUB) {
            return new GuiNetworkHubCore(new ContainerNetworkHub(player, (TileNetworkHub) Objects.requireNonNull(player.world.getTileEntity(new BlockPos(x, y, z)))));
        }
        return null;
    }
}
