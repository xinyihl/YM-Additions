package com.xinyihl.ymadditions.common.network;

import com.xinyihl.ymadditions.api.ISyncable;
import com.xinyihl.ymadditions.common.data.DataStorage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
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
            switch (ServerToClient.valueOf(message.type)) {
                case CONTAINER_SYNC: {
                    Container container = mc.player.openContainer;
                    if (container instanceof ISyncable) {
                        ((ISyncable) container).doSyncFrom(message.compound);
                    }
                    break;
                }
                case WORLD_DATA_SYNC: {
                    DataStorage.get(Minecraft.getMinecraft().world).doSyncFrom(message.compound);
                    break;
                }
            }

        });
        return null;
    }

    public enum ServerToClient {
        CONTAINER_SYNC,
        WORLD_DATA_SYNC
    }
}
