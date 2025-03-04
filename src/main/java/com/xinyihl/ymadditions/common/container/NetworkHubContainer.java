package com.xinyihl.ymadditions.common.container;

import com.xinyihl.ymadditions.YMAdditions;
import com.xinyihl.ymadditions.common.api.IContaierTickable;
import com.xinyihl.ymadditions.common.api.IInputHandler;
import com.xinyihl.ymadditions.common.api.data.NetworkHubDataStorage;
import com.xinyihl.ymadditions.common.api.data.NetworkStatus;
import com.xinyihl.ymadditions.common.network.PacketServerToClient;
import com.xinyihl.ymadditions.common.title.TileNetworkHub;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;

import java.util.Map;
import java.util.UUID;

import static com.xinyihl.ymadditions.common.network.PacketServerToClient.ServerToClient.*;

public class NetworkHubContainer extends Container implements IInputHandler, IContaierTickable {

    public EntityPlayer player;
    public TileNetworkHub networkHub;
    public final Map<UUID, NetworkStatus> networks;
    public UUID selectedNetwork;
    public NetworkHubDataStorage storage;

    public NetworkHubContainer(EntityPlayer player, TileNetworkHub networkHub) {
        this.player = player;
        this.networkHub = networkHub;
        this.storage = NetworkHubDataStorage.get(networkHub.getWorld());
        this.networks = storage.getNetworks();
        this.selectedNetwork = networkHub.getNetworkUuid();
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.getDistanceSq(this.networkHub.getPos()) <= 64;
    }

    @Override
    public void update() {
        if (player.world.isRemote) return;
        if (!(player.world.getTileEntity(networkHub.getPos()) instanceof TileNetworkHub)) {
            ((EntityPlayerMP) player).closeContainer();
        }
    }

    @Override
    public void onGuiAtion(NBTTagCompound compound) {
        if (this.player.world.isRemote) return;
        int button = compound.getInteger("button");
        switch (button) {
            case 0: { // 切换选择的网络
                UUID uuid = compound.getUniqueId("networkUuid");
                NetworkStatus network = this.storage.getNetwork(uuid);
                if (network != null && network.hasPermission(this.player, 0)) {
                    this.selectedNetwork = uuid;
                }
                NBTTagCompound tag = new NBTTagCompound();
                tag.setUniqueId("networkUuid", this.selectedNetwork);
                YMAdditions.instance.networkWrapper.sendTo(new PacketServerToClient(UPDATE_GUI_SELECTED_NETWORK, tag), (EntityPlayerMP) player);
                break;
            }
            case 1: { // 创建网络
                String name = compound.getString("name");
                NetworkStatus network = this.storage.addNetwork(new NetworkStatus(player.getGameProfile().getId(), name, false, networkHub.getWorld().provider.getDimension(), networkHub.getPos()));
                this.networkHub.setHead(true);
                this.networkHub.setNetworkUuid(network.getUuid());
                this.selectedNetwork = network.getUuid();
                NBTTagCompound tag = new NBTTagCompound();
                tag.setUniqueId("networkUuid", this.selectedNetwork);
                YMAdditions.instance.networkWrapper.sendTo(new PacketServerToClient(UPDATE_GUI_SELECTED_NETWORK, tag), (EntityPlayerMP) player);
                this.networkHub.sync();
                break;
            }
            case 996: { // 删除网络
                NetworkStatus network = this.storage.getNetwork(this.selectedNetwork);
                if (network != null && network.hasPermission(this.player, 1)) {
                    storage.removeNetwork(this.selectedNetwork);
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setUniqueId("networkUuid", this.selectedNetwork);
                    YMAdditions.instance.networkWrapper.sendToAll(new PacketServerToClient(DELETE_NETWORK, tag));
                    this.selectedNetwork = new UUID(0, 0);
                } else {
                    this.player.sendStatusMessage(new TextComponentString(I18n.format("statusmessage.ymadditions.info.nopermission")), true);
                }
                this.networkHub.sync();
                break;
            }
            case 997: { // 连接网络
                NetworkStatus network = this.storage.getNetwork(this.selectedNetwork);
                if (network != null && network.hasPermission(this.player, 0)) {
                    if (!this.networkHub.getNetworkUuid().equals(this.selectedNetwork)) {
                        this.networkHub.breakConnection();
                    }
                    this.networkHub.setNetworkUuid(this.selectedNetwork);
                } else {
                    this.player.sendStatusMessage(new TextComponentString(I18n.format("statusmessage.ymadditions.info.nopermission")), true);
                }
                this.networkHub.sync();
                break;
            }
            case 998: { // 断开连接
                NetworkStatus network = this.storage.getNetwork(this.networkHub.getNetworkUuid());
                if (network != null && network.hasPermission(this.player, 0)) {
                    this.networkHub.breakConnection();
                    this.selectedNetwork = new UUID(0, 0);
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setUniqueId("networkUuid", this.selectedNetwork);
                    YMAdditions.instance.networkWrapper.sendTo(new PacketServerToClient(UPDATE_GUI_SELECTED_NETWORK, tag), (EntityPlayerMP) player);
                } else {
                    this.player.sendStatusMessage(new TextComponentString(I18n.format("statusmessage.ymadditions.info.nopermission")), true);
                }
                this.networkHub.sync();
                break;
            }
            case 999: { // 切换网络是否公开
                NetworkStatus network = this.storage.getNetwork(selectedNetwork);
                if (network != null && network.hasPermission(this.player, 1)) {
                    network.setPublic(!network.isPublic());
                    network.setNeedTellClient(true);
                } else {
                    this.player.sendStatusMessage(new TextComponentString(I18n.format("statusmessage.ymadditions.info.nopermission")), true);
                }
                this.networkHub.sync();
                break;
            }
        }
    }
}
