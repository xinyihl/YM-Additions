package com.xinyihl.ymadditions.common.container;

import com.xinyihl.ymadditions.YMAdditions;
import com.xinyihl.ymadditions.common.api.IContaierTickable;
import com.xinyihl.ymadditions.common.api.IInputHandler;
import com.xinyihl.ymadditions.common.data.DataStorage;
import com.xinyihl.ymadditions.common.data.NetworkStatus;
import com.xinyihl.ymadditions.common.data.NetworkUser;
import com.xinyihl.ymadditions.common.network.PacketServerToClient;
import com.xinyihl.ymadditions.common.title.TileNetworkHub;
import com.xinyihl.ymadditions.common.utils.BlockPosDim;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.xinyihl.ymadditions.common.network.PacketServerToClient.ServerToClient.DELETE_NETWORKS;
import static com.xinyihl.ymadditions.common.network.PacketServerToClient.ServerToClient.UPDATE_GUI_SELECTED_NETWORK;

public class ContainerNetworkHub extends Container implements IInputHandler, IContaierTickable {

    public EntityPlayer player;
    public TileNetworkHub networkHub;
    public final Map<UUID, NetworkStatus> networks;
    public UUID selectedNetwork;
    public DataStorage storage;

    public ContainerNetworkHub(EntityPlayer player, TileNetworkHub networkHub) {
        this.player = player;
        this.networkHub = networkHub;
        this.storage = DataStorage.get(networkHub.getWorld());
        this.networks = storage.getNetworks();
        this.selectedNetwork = networkHub.getNetworkUuid();
    }

    public NetworkStatus getSelectedNetwork() {
        return networks.getOrDefault(selectedNetwork, new NetworkStatus(new UUID(0, 0), "Unknown", false, new BlockPosDim(0, 0, 0, 0)));
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.getDistanceSq(this.networkHub.getPos()) <= 64;
    }

    @Override
    public void update() {
        if (player.world.isRemote) return;
        if (!(player.world.getTileEntity(networkHub.getPos()) instanceof TileNetworkHub)) {
            player.closeScreen();
            return;
        }
        if (!getSelectedNetwork().hasPermission(player, 0)) {
            player.closeScreen();
        }
        getSelectedNetwork().setNeedTellClient(true);
    }

    private static void userPermHelper(EntityPlayer player, NetworkStatus network, UUID uuid, String name) {
        if (network.getOwner().equals(uuid)) {
            return;
        }
        NetworkUser.Perm n = network.getUserPrem(uuid);
        if (n == null && network.hasPermission(player, 2)) {
            network.addUser(uuid, name, NetworkUser.Perm.USER);
        } else if (n == NetworkUser.Perm.USER && network.hasPermission(player, 3)) {
            network.setUserPrem(uuid, NetworkUser.Perm.ADMIN);
        } else if (n == NetworkUser.Perm.USER && network.hasPermission(player, 2)) {
            network.removeUser(uuid);
        } else if (n == NetworkUser.Perm.ADMIN && network.hasPermission(player, 3)) {
            network.removeUser(uuid);
        } else {
            player.sendStatusMessage(new TextComponentTranslation("statusmessage.ymadditions.info.nopermission"), true);
        }
        network.setNeedTellClient(true);
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
                this.syncSelected((EntityPlayerMP) player);
                break;
            }
            case 1: { // 创建网络
                String name = compound.getString("name");
                NetworkStatus network = this.storage.addNetwork(new NetworkStatus(player.getGameProfile().getId(), name, false, new BlockPosDim(networkHub.getPos(), networkHub.getWorld().provider.getDimension()) ));
                network.setNeedTellClient(true);
                network.addUser(player.getGameProfile().getId(), player.getGameProfile().getName(), NetworkUser.Perm.OWNER);
                this.networkHub.setHead(true);
                this.networkHub.setNetworkUuid(network.getUuid());
                this.selectedNetwork = network.getUuid();
                this.syncSelected((EntityPlayerMP) player);
                this.networkHub.sync();
                break;
            }
            case 2: { // 修改玩家权限
                UUID uuid = compound.getUniqueId("user");
                String name = compound.getString("name");
                NetworkStatus network = this.storage.getNetwork(selectedNetwork);
                if (network != null && network.hasPermission(player, 2)) {
                    if(compound.hasKey("isShifting")) {
                        List<NetworkStatus> networks = this.storage.getPlayerNetworks(this.player, 2);
                        networks.forEach(network1 -> userPermHelper(player, network1, uuid, name));
                    } else {
                        userPermHelper(player, network, uuid, name);
                    }
                } else {
                    player.sendStatusMessage(new TextComponentTranslation("statusmessage.ymadditions.info.nopermission"), true);
                }
                this.networkHub.sync();
                break;
            }
            case 996: { // 删除网络
                NetworkStatus network = this.storage.getNetwork(this.selectedNetwork);
                if (network != null && network.hasPermission(this.player, 1)) {
                    storage.removeNetwork(this.selectedNetwork);
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setUniqueId("networkUuid", this.selectedNetwork);
                    YMAdditions.instance.networkWrapper.sendToAll(new PacketServerToClient(DELETE_NETWORKS, tag));
                    this.selectedNetwork = selectedNetwork.equals(networkHub.getNetworkUuid()) ? new UUID(0, 0) : networkHub.getNetworkUuid();
                    this.syncSelected((EntityPlayerMP) player);
                } else {
                    this.player.sendStatusMessage(new TextComponentTranslation("statusmessage.ymadditions.info.nopermission"), true);
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
                    this.player.sendStatusMessage(new TextComponentTranslation("statusmessage.ymadditions.info.nopermission"), true);
                }
                this.networkHub.sync();
                break;
            }
            case 998: { // 断开连接
                NetworkStatus network = this.storage.getNetwork(this.networkHub.getNetworkUuid());
                if (network != null && network.hasPermission(this.player, 0)) {
                    this.networkHub.breakConnection();
                    this.selectedNetwork = new UUID(0, 0);
                    this.syncSelected((EntityPlayerMP) player);
                } else {
                    this.player.sendStatusMessage(new TextComponentTranslation("statusmessage.ymadditions.info.nopermission"), true);
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
                    this.player.sendStatusMessage(new TextComponentTranslation("statusmessage.ymadditions.info.nopermission"), true);
                }
                this.networkHub.sync();
                break;
            }
        }
    }

    public void syncSelected(EntityPlayerMP player){
        NBTTagCompound tag = new NBTTagCompound();
        tag.setUniqueId("networkUuid", this.selectedNetwork);
        YMAdditions.instance.networkWrapper.sendTo(new PacketServerToClient(UPDATE_GUI_SELECTED_NETWORK, tag), player);
    }
}
