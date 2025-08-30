package com.xinyihl.ymadditions.common.container;

import com.xinyihl.ymadditions.YMAdditions;
import com.xinyihl.ymadditions.api.IContaierTickable;
import com.xinyihl.ymadditions.api.IActionHandler;
import com.xinyihl.ymadditions.api.ISyncable;
import com.xinyihl.ymadditions.api.entity.Group;
import com.xinyihl.ymadditions.api.entity.Network;
import com.xinyihl.ymadditions.api.entity.User;
import com.xinyihl.ymadditions.common.data.DataStorage;
import com.xinyihl.ymadditions.common.network.PacketServerToClient;
import com.xinyihl.ymadditions.common.title.TileNetworkHub;
import com.xinyihl.ymadditions.common.utils.BlockPosDim;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.stream.Collectors;

import static com.xinyihl.ymadditions.common.network.PacketServerToClient.ServerToClient.CONTAINER_SYNC;

public class ContainerNetworkHub extends Container implements IActionHandler, IContaierTickable, ISyncable {

    public EntityPlayer player;
    public TileNetworkHub networkHub;
    public UUID selectedNetwork;
    public DataStorage storage;
    public Integer surplusChannels;
    public Map<UUID, Network> networks = new LinkedHashMap<>();
    public Map<UUID, Group> groups = new LinkedHashMap<>();

    public ContainerNetworkHub(EntityPlayer player, TileNetworkHub networkHub) {
        this.player = player;
        this.networkHub = networkHub;
        if (player.world.isRemote) return;
        this.storage = DataStorage.get(networkHub.getWorld());
        this.networks = storage.getNetworks();
        this.groups = storage.getGroups();
        this.selectedNetwork = networkHub.getNetworkUuid();
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
        this.sync();
    }

    @Override
    public void onAction(String type, NBTTagCompound compound) {
        if (this.player.world.isRemote) return;
        switch (type) {
            case "switch-network": { // 切换选择的网络
                UUID uuid = compound.getUniqueId("uuid");
                Network network = this.storage.getNetwork(uuid);
                if (network != null && network.hasPermission(this.player, User.Perm.USER)) {
                    this.selectedNetwork = uuid;
                }
                this.sync();
                break;
            }
            case "create-network": { // 创建网络
                String name = compound.getString("string");
                Network network = this.storage.createNetwork(player, name, false, new BlockPosDim(networkHub.getPos(), networkHub.getWorld().provider.getDimension()));
                this.networkHub.setHead(true);
                this.networkHub.setNetworkUuid(network.getUuid());
                this.selectedNetwork = network.getUuid();
                this.storage.markDirty();
                this.networkHub.sync();
                this.sync();
                break;
            }
            case "change-player-permission": { // 修改玩家权限
                UUID uuid = compound.getUniqueId("user");
                String name = compound.getString("name");
                Group group = this.getGroup();
                if (group != null && group.hasPermission(player, User.Perm.ADMIN)) {
                    User user = group.getUser(User.create(uuid, name));
                    if (user != null) {
                        if (user.getPerm() == User.Perm.ADMIN && group.hasPermission(player, User.Perm.OWNER)) {
                            group.removeUser(user);
                        }
                        if (user.getPerm() == User.Perm.USER) {
                            if (group.hasPermission(player, User.Perm.OWNER)) {
                                user.setPerm(User.Perm.ADMIN);
                            } else {
                                group.removeUser(user);
                            }
                        }
                        if (user.getPerm() == User.Perm.NONE) {
                            user.setPerm(User.Perm.USER);
                        }
                    } else {
                        group.addUser(User.create(uuid, name, User.Perm.USER));
                    }
                    this.storage.markDirty();
                    this.networkHub.sync();
                    this.sync();
                } else {
                    player.sendStatusMessage(new TextComponentTranslation("statusmessage.ymadditions.info.nopermission"), true);
                }
                break;
            }
            case "delete-network": { // 删除网络
                Network network = this.storage.getNetwork(this.selectedNetwork);
                if (network != null && network.hasPermission(this.player, User.Perm.ADMIN)) {
                    storage.removeNetwork(this.selectedNetwork);
                    this.selectedNetwork = Objects.equals(selectedNetwork, networkHub.getNetworkUuid()) ? null : networkHub.getNetworkUuid();
                    this.storage.markDirty();
                    this.networkHub.sync();
                    this.sync();
                } else {
                    this.player.sendStatusMessage(new TextComponentTranslation("statusmessage.ymadditions.info.nopermission"), true);
                }
                break;
            }
            case "connect-network": { // 连接网络
                Network network = this.storage.getNetwork(this.selectedNetwork);
                if (network != null && network.hasPermission(this.player, User.Perm.USER)) {
                    if (!Objects.equals(selectedNetwork, networkHub.getNetworkUuid())) {
                        this.networkHub.breakConnection();
                    }
                    this.storage.markDirty();
                    this.networkHub.setNetworkUuid(this.selectedNetwork);
                    this.networkHub.sync();
                    this.sync();
                } else {
                    this.player.sendStatusMessage(new TextComponentTranslation("statusmessage.ymadditions.info.nopermission"), true);
                }
                break;
            }
            case "disconnect-network": { // 断开连接
                Network network = this.storage.getNetwork(this.networkHub.getNetworkUuid());
                if (network != null && network.hasPermission(this.player, User.Perm.USER)) {
                    this.networkHub.breakConnection();
                    this.selectedNetwork = null;
                    this.storage.markDirty();
                    this.networkHub.sync();
                    this.sync();
                } else {
                    this.player.sendStatusMessage(new TextComponentTranslation("statusmessage.ymadditions.info.nopermission"), true);
                }
                break;
            }
            case "switch-public": { // 切换网络是否公开
                Network network = this.storage.getNetwork(selectedNetwork);
                if (network != null && network.hasPermission(this.player, User.Perm.ADMIN)) {
                    network.setOvert(!network.isOvert());
                    this.storage.markDirty();
                    this.networkHub.sync();
                    this.sync();
                } else {
                    this.player.sendStatusMessage(new TextComponentTranslation("statusmessage.ymadditions.info.nopermission"), true);
                }
                break;
            }
        }
    }

    @Override
    public void sync() {
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
        }
        YMAdditions.instance.networkWrapper.sendTo(new PacketServerToClient(CONTAINER_SYNC, this.getSyncData(new NBTTagCompound())), (EntityPlayerMP) player);
    }

    @Override
    public NBTTagCompound getSyncData(NBTTagCompound tag) {
        if (this.selectedNetwork != null) tag.setUniqueId("networkUuid", this.selectedNetwork);
        if (this.surplusChannels != null) tag.setInteger("surplusChannels", this.surplusChannels);
        NBTTagList list1 = new NBTTagList();
        NBTTagList list2 = new NBTTagList();
        for (Network network : this.networks.values().stream().filter(v -> v.hasPermission(player, User.Perm.USER)).collect(Collectors.toList())) {
            list1.appendTag(network.to(new NBTTagCompound()));
        }
        for (Group group : this.groups.values().stream().filter(u -> u.hasPermission(player, User.Perm.USER)).collect(Collectors.toList())) {
            list2.appendTag(group.deepCopy().injectOnlinePlayers().injectOwnerToUsers().to(new NBTTagCompound()));
        }
        tag.setTag("networks", list1);
        tag.setTag("groups", list2);
        return tag;
    }

    @Override
    public void doSyncFrom(NBTTagCompound tag) {
        this.selectedNetwork = tag.hasUniqueId("networkUuid") ? tag.getUniqueId("networkUuid") : null;
        this.surplusChannels = tag.hasKey("surplusChannels") ? tag.getInteger("surplusChannels") : null;
        this.updateNetworks(tag);
        this.updateGroups(tag);
    }

    private void updateNetworks(NBTTagCompound tag) {
        NBTTagList list = tag.getTagList("networks", Constants.NBT.TAG_COMPOUND);
        Set<UUID> uuidsToKeep = new HashSet<>();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound nbt = list.getCompoundTagAt(i);
            UUID uuid = nbt.getUniqueId("uuid");
            uuidsToKeep.add(uuid);

            Network net = this.networks.get(uuid);
            if (net != null) {
                net.update(nbt);
            } else {
                Network newNet = Network.create(nbt);
                this.networks.put(newNet.getUuid(), newNet);
            }
        }
        this.networks.keySet().removeIf(uuid -> !uuidsToKeep.contains(uuid));
    }

    private void updateGroups(NBTTagCompound tag) {
        NBTTagList list = tag.getTagList("groups", Constants.NBT.TAG_COMPOUND);
        Set<UUID> uuidsToKeep = new HashSet<>();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound nbt = list.getCompoundTagAt(i);
            UUID uuid = nbt.getUniqueId("uuid");
            uuidsToKeep.add(uuid);
            Group group = this.groups.get(uuid);
            if (group != null) {
                group.update(nbt);
            } else {
                Group newGroup = Group.create(nbt);
                this.groups.put(newGroup.getUuid(), newGroup);
            }
        }
        this.groups.keySet().removeIf(uuid -> !uuidsToKeep.contains(uuid));
    }
}
