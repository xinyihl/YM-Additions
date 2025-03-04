package com.xinyihl.ymadditions.common.network;

import com.xinyihl.ymadditions.common.api.IInputHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class PacketClientToServer implements IMessage, IMessageHandler<PacketClientToServer, IMessage> {
    private String type;
    private NBTTagCompound compound;

    public PacketClientToServer() {
    }

    public PacketClientToServer(ClientToServer type, @Nullable NBTTagCompound compound) {
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
    public IMessage onMessage(PacketClientToServer message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        player.server.addScheduledTask(() -> {
            switch (ClientToServer.valueOf(message.type)) {
                case BUTTON_ACTION: {
                    if (player.openContainer instanceof IInputHandler) {
                        ((IInputHandler) player.openContainer).onGuiAtion(message.compound);
                    }
                    break;
                }
            }
        });
        return null;
    }

    public static enum ClientToServer {
        BUTTON_ACTION
    }
}
