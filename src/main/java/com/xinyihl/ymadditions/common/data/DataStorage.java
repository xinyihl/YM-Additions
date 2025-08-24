package com.xinyihl.ymadditions.common.data;

import com.xinyihl.ymadditions.Tags;
import com.xinyihl.ymadditions.YMAdditions;
import com.xinyihl.ymadditions.api.ISyncable;
import com.xinyihl.ymadditions.api.entity.Group;
import com.xinyihl.ymadditions.api.entity.Network;
import com.xinyihl.ymadditions.api.entity.User;
import com.xinyihl.ymadditions.common.network.PacketServerToClient;
import com.xinyihl.ymadditions.common.utils.BlockPosDim;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static com.xinyihl.ymadditions.common.network.PacketServerToClient.ServerToClient.WORLD_DATA_SYNC;

public class DataStorage extends WorldSavedData {
    private final Map<UUID, Network> networks = new LinkedHashMap<>();
    private final Map<UUID, Group> groups = new LinkedHashMap<>();

    public DataStorage(String name) {
        super(name);
    }

    public static DataStorage get(World world) {
        DataStorage data = null;
        if (world.getMapStorage() != null) {
            data = (DataStorage) world.getMapStorage().getOrLoadData(DataStorage.class, Tags.MOD_ID + "_data");
        }
        if (data == null) {
            data = new DataStorage(Tags.MOD_ID + "_data");
            if (world.getMapStorage() != null) {
                world.getMapStorage().setData(Tags.MOD_ID + "_data", data);
            }
        }
        return data;
    }

    @Nullable
    public Network getNetwork(UUID network) {
        return networks.get(network);
    }

    public Map<UUID, Network> getNetworks() {
        return networks;
    }

    public Map<UUID, Group> getGroups() {
        return groups;
    }

    public void removeNetwork(UUID network) {
        networks.remove(network);
    }

    @Nullable
    public Group getGroup(UUID group) {
        return groups.get(group);
    }

    @Nullable
    public Group getGroupByOwner(User owner) {
        return this.groups.values().stream().filter(v -> v.isOwner(owner)).findFirst().orElse(null);
    }

    private Group getOrCreateGroup(User owner) {
        Group group = this.getGroupByOwner(owner);
        if (group == null) {
            group = Group.create(owner);
            this.groups.put(group.getUuid(), group);
        }
        return group;
    }

    public Network createNetwork(EntityPlayer player, String name, boolean overt, BlockPosDim sendPos) {
        User owner = User.create(player, User.Perm.OWNER);
        Group group = this.getOrCreateGroup(owner);
        Network network = Network.create(group.getUuid(), name, overt, sendPos);
        this.networks.put(network.getUuid(), network);
        this.markDirty();
        return network;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        networks.clear();
        NBTTagList list1 = nbt.getTagList("networks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list1.tagCount(); i++) {
            Network net = Network.create(list1.getCompoundTagAt(i));
            networks.put(net.getUuid(), net);
        }
        groups.clear();
        NBTTagList list2 = nbt.getTagList("groups", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list2.tagCount(); i++) {
            Group group = Group.create(list2.getCompoundTagAt(i));
            groups.put(group.getUuid(), group);
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound tag) {
        NBTTagList list1 = new NBTTagList();
        NBTTagList list2 = new NBTTagList();
        for (Network network : networks.values()) {
            list1.appendTag(network.to(new NBTTagCompound()));
        }
        for (Group group : groups.values()) {
            list2.appendTag(group.to(new NBTTagCompound()));
        }
        tag.setTag("networks", list1);
        tag.setTag("groups", list2);
        return tag;
    }

    public NBTTagCompound getSyncDataPlayer(NBTTagCompound tag, EntityPlayer player) {
        NBTTagList list1 = new NBTTagList();
        NBTTagList list2 = new NBTTagList();
        for (Network network : networks.values().stream().filter(v -> v.hasPermission(player, User.Perm.USER)).collect(Collectors.toList())) {
            list1.appendTag(network.to(new NBTTagCompound()));
        }
        for (Group group : groups.values()) {
            list2.appendTag(group.deepCopy().injectOnlinePlayers().to(new NBTTagCompound()));
        }
        tag.setTag("networks", list1);
        tag.setTag("groups", list2);
        return tag;
    }


    public void doSyncFrom(NBTTagCompound tag) {
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

            Network net = networks.get(uuid);
            if (net != null) {
                net.of(nbt);
            } else {
                Network newNet = Network.create(nbt);
                networks.put(newNet.getUuid(), newNet);
            }
        }
        networks.keySet().removeIf(uuid -> !uuidsToKeep.contains(uuid));
    }

    private void updateGroups(NBTTagCompound tag) {
        NBTTagList list = tag.getTagList("groups", Constants.NBT.TAG_COMPOUND);
        Set<UUID> uuidsToKeep = new HashSet<>();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound nbt = list.getCompoundTagAt(i);
            UUID uuid = nbt.getUniqueId("uuid");
            uuidsToKeep.add(uuid);
            Group group = groups.get(uuid);
            if (group != null) {
                group.of(nbt);
            } else {
                Group newGroup = Group.create(nbt);
                groups.put(newGroup.getUuid(), newGroup);
            }
        }
        groups.keySet().removeIf(uuid -> !uuidsToKeep.contains(uuid));
    }
}
