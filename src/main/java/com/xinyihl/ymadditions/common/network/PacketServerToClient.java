package com.xinyihl.ymadditions.common.network;

import com.xinyihl.ymadditions.common.container.ContainerNetworkHub;
import com.xinyihl.ymadditions.common.data.NetworkHubDataStorage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketServerToClient implements IMessage, IMessageHandler<PacketServerToClient, IMessage> {
    private String type;
    private NBTTagCompound compound;

    public PacketServerToClient() {

    }

    public PacketServerToClient(ServerToClient type, NBTTagCompound compound) {
        this.type = type.name();
        this.compound = compound;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        type = ByteBufUtils.readUTF8String(buf);
        compound = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, type);
        ByteBufUtils.writeTag(buf, compound);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onMessage(PacketServerToClient message, MessageContext ctx) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(() -> {
            World world = Minecraft.getMinecraft().world;
            NetworkHubDataStorage storage = NetworkHubDataStorage.get(world);
            switch (ServerToClient.valueOf(message.type)) {
                case INIT_NETWORKS: {
                    storage.readFromNBT(message.compound);
                    break;
                }
                case UPDATE_NETWORKS: {
                    storage.updateFromNBT(message.compound);
                    break;
                }
                case DELETE_NETWORKS: {
                    storage.removeNetwork(message.compound.getUniqueId("networkUuid"));
                    break;
                }
                case UPDATE_GUI_SELECTED_NETWORK: {
                    Container container = mc.player.openContainer;
                    if (container instanceof ContainerNetworkHub) {
                        ((ContainerNetworkHub) container).selectedNetwork = message.compound.getUniqueId("networkUuid");
                    }
                    break;
                }
            }

        });
        return null;
    }

    public static enum ServerToClient {
        INIT_NETWORKS,
        UPDATE_NETWORKS,
        DELETE_NETWORKS,
        UPDATE_GUI_SELECTED_NETWORK
    }
}
