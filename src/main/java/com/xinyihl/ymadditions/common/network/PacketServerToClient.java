package com.xinyihl.ymadditions.common.network;

import com.xinyihl.ymadditions.common.api.data.NetworkHubDataStorage;
import com.xinyihl.ymadditions.common.container.NetworkHubContainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
    public IMessage onMessage(PacketServerToClient message, MessageContext ctx) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(() -> {
            World world = Minecraft.getMinecraft().world;
            NetworkHubDataStorage storage = NetworkHubDataStorage.get(world);
            switch (ServerToClient.valueOf(message.type)) {
                case UPDATE_NETWORKS: {
                    storage.updateFromNBT(message.compound);
                    break;
                }
                case DELETE_NETWORK: {
                    storage.removeNetwork(message.compound.getUniqueId("networkUuid"));
                    break;
                }
                case UPDATE_GUI_SELECTED_NETWORK: {
                    Container container = mc.player.openContainer;
                    if (container instanceof NetworkHubContainer) {
                        ((NetworkHubContainer) container).selectedNetwork = message.compound.getUniqueId("networkUuid");
                    }
                    break;
                }
            }

        });
        return null;
    }

    public static enum ServerToClient {
        UPDATE_NETWORKS,
        DELETE_NETWORK,
        UPDATE_GUI_SELECTED_NETWORK
    }
}
