package com.xinyihl.ymadditions.common.container;

import com.xinyihl.ymadditions.YMAdditions;
import com.xinyihl.ymadditions.api.IContaierTickable;
import com.xinyihl.ymadditions.api.IInputHandler;
import com.xinyihl.ymadditions.api.ISyncable;
import com.xinyihl.ymadditions.api.entity.Group;
import com.xinyihl.ymadditions.api.entity.Network;
import com.xinyihl.ymadditions.api.entity.User;
import com.xinyihl.ymadditions.common.data.DataStorage;
import com.xinyihl.ymadditions.common.network.PacketServerToClient;
import com.xinyihl.ymadditions.common.title.TileNetworkHub;
import com.xinyihl.ymadditions.common.utils.BlockPosDim;
import com.xinyihl.ymadditions.common.utils.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.xinyihl.ymadditions.common.network.PacketServerToClient.ServerToClient.CONTAINER_SYNC;

public class ContainerNetworkHub extends Container implements IInputHandler, IContaierTickable, ISyncable {

    public final Map<UUID, Network> networks;
    public final Map<UUID, Group> groups;
    public EntityPlayer player;
    public TileNetworkHub networkHub;
    public UUID selectedNetwork;
    public DataStorage storage;
    public Integer surplusChannels;

    public ContainerNetworkHub(EntityPlayer player, TileNetworkHub networkHub) {
        this.player = player;
        this.networkHub = networkHub;
        this.storage = DataStorage.get(networkHub.getWorld());
        this.networks = storage.getNetworks();
        this.groups = storage.getGroups();
        this.selectedNetwork = networkHub.getNetworkUuid();
        if (networkHub.isHead()) {
            this.surplusChannels = networkHub.getSurplusChannels();
        } else if (this.selectedNetwork != null) {
            BlockPosDim pos = getSelected().getSendPos();
            if (pos != null) {
                World thatWorld = DimensionManager.getWorld(pos.getDimension());
                if (thatWorld != null && thatWorld.isBlockLoaded(pos.toBlockPos())) {
                    TileEntity tile = thatWorld.getTileEntity(pos.toBlockPos());
                    if (tile instanceof TileNetworkHub) {
                        TileNetworkHub that = (TileNetworkHub) tile;
                        this.surplusChannels = that.getSurplusChannels();
                    }
                }
            }
        } else {
            this.surplusChannels = 0;
        }
    }

    public Network getSelected() {
        return networks.getOrDefault(selectedNetwork, Network.empty());
    }

    public Group getGroup() {
        return groups.getOrDefault(this.getSelected().getOwner(), Group.empty());
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
        if (!getSelected().hasPermission(player, User.Perm.USER)) {
            player.closeScreen();
        }
    }

    @Override
    public void onGuiAtion(NBTTagCompound compound) {
        if (this.player.world.isRemote) return;
        int button = compound.getInteger("button");
        switch (button) {
            case 0: { // 切换选择的网络
                UUID uuid = compound.getUniqueId("networkUuid");
                Network network = this.storage.getNetwork(uuid);
                if (network != null && network.hasPermission(this.player, User.Perm.USER)) {
                    this.selectedNetwork = uuid;
                }
                this.sync();
                break;
            }
            case 1: { // 创建网络
                String name = compound.getString("name");
                Network network = this.storage.createNetwork(player, name, false, new BlockPosDim(networkHub.getPos(), networkHub.getWorld().provider.getDimension()));
                this.networkHub.setHead(true);
                this.networkHub.setNetworkUuid(network.getUuid());
                this.selectedNetwork = network.getUuid();
                this.sync();
                this.networkHub.sync();
                Utils.syncDataStorage((EntityPlayerMP) player);
                break;
            }
            case 2: { // 修改玩家权限
                UUID uuid = compound.getUniqueId("user");
                String name = compound.getString("name");
                Group group = this.getGroup();
                if (group != null && group.hasPermission(player, User.Perm.ADMIN)) {
                    User user = group.getUser(User.create(uuid, name));
                    if (user != null) {
                        if (user.getPerm() == User.Perm.NONE) {
                            user.setPerm(User.Perm.USER);
                        }
                        if (user.getPerm() == User.Perm.USER) {
                            if (group.hasPermission(player, User.Perm.OWNER)) {
                                user.setPerm(User.Perm.ADMIN);
                            } else {
                                group.removeUser(user);
                            }
                        }
                        if (user.getPerm() == User.Perm.ADMIN && group.hasPermission(player, User.Perm.OWNER)) {
                            group.removeUser(user);
                        }
                    }
                } else {
                    player.sendStatusMessage(new TextComponentTranslation("statusmessage.ymadditions.info.nopermission"), true);
                }
                this.networkHub.sync();
                Utils.syncDataStorage((EntityPlayerMP) player);
                break;
            }
            case 996: { // 删除网络
                Network network = this.storage.getNetwork(this.selectedNetwork);
                if (network != null && network.hasPermission(this.player, User.Perm.ADMIN)) {
                    storage.removeNetwork(this.selectedNetwork);
                    this.selectedNetwork = Objects.equals(selectedNetwork, networkHub.getNetworkUuid()) ? null : networkHub.getNetworkUuid();
                    this.sync();
                } else {
                    this.player.sendStatusMessage(new TextComponentTranslation("statusmessage.ymadditions.info.nopermission"), true);
                }
                this.networkHub.sync();
                Utils.syncDataStorage((EntityPlayerMP) player);
                break;
            }
            case 997: { // 连接网络
                Network network = this.storage.getNetwork(this.selectedNetwork);
                if (network != null && network.hasPermission(this.player, User.Perm.USER)) {
                    if (!Objects.equals(selectedNetwork, networkHub.getNetworkUuid())) {
                        this.networkHub.breakConnection();
                    }
                    this.networkHub.setNetworkUuid(this.selectedNetwork);
                } else {
                    this.player.sendStatusMessage(new TextComponentTranslation("statusmessage.ymadditions.info.nopermission"), true);
                }
                this.networkHub.sync();
                Utils.syncDataStorage((EntityPlayerMP) player);
                break;
            }
            case 998: { // 断开连接
                Network network = this.storage.getNetwork(this.networkHub.getNetworkUuid());
                if (network != null && network.hasPermission(this.player, User.Perm.USER)) {
                    this.networkHub.breakConnection();
                    this.selectedNetwork = new UUID(0, 0);
                    this.sync();
                } else {
                    this.player.sendStatusMessage(new TextComponentTranslation("statusmessage.ymadditions.info.nopermission"), true);
                }
                this.networkHub.sync();
                Utils.syncDataStorage((EntityPlayerMP) player);
                break;
            }
            case 999: { // 切换网络是否公开
                Network network = this.storage.getNetwork(selectedNetwork);
                if (network != null && network.hasPermission(this.player, User.Perm.ADMIN)) {
                    network.setOvert(!network.isOvert());
                } else {
                    this.player.sendStatusMessage(new TextComponentTranslation("statusmessage.ymadditions.info.nopermission"), true);
                }
                this.networkHub.sync();
                Utils.syncDataStorage((EntityPlayerMP) player);
                break;
            }
        }
    }

    @Override
    public void sync() {
        YMAdditions.instance.networkWrapper.sendTo(new PacketServerToClient(CONTAINER_SYNC, this.getSyncData(new NBTTagCompound())), (EntityPlayerMP) player);
    }

    @Override
    public NBTTagCompound getSyncData(NBTTagCompound tag) {
        if (this.selectedNetwork != null) tag.setUniqueId("networkUuid", this.selectedNetwork);
        return tag;
    }

    @Override
    public void doSyncFrom(NBTTagCompound tag) {
        this.selectedNetwork = tag.hasUniqueId("networkUuid") ? tag.getUniqueId("networkUuid") : null;
    }
}
