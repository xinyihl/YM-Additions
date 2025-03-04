package com.xinyihl.ymadditions.common.api.data;

import com.xinyihl.ymadditions.Configurations;
import com.xinyihl.ymadditions.Tags;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class NetworkHubDataStorage extends WorldSavedData {
    private static final String DATA_NAME = Tags.MOD_ID + "_NHDS";
    private static final String DATA_NAME_DIM = Tags.MOD_ID + "_NHDS_DIM";
    private final Map<UUID, NetworkStatus> networks = new LinkedHashMap<>();

    public NetworkHubDataStorage(String name) {
        super(name);
    }

    public static NetworkHubDataStorage getDim(World world) {
        NetworkHubDataStorage data = (NetworkHubDataStorage) world.getPerWorldStorage().getOrLoadData(NetworkHubDataStorage.class, DATA_NAME_DIM);
        if (data == null) {
            data = new NetworkHubDataStorage(DATA_NAME_DIM);
            world.getPerWorldStorage().setData(DATA_NAME_DIM, data);
        }
        return data;
    }

    public static NetworkHubDataStorage getGlobal(World world) {
        NetworkHubDataStorage data = null;
        if (world.getMapStorage() != null) {
            data = (NetworkHubDataStorage) world.getMapStorage().getOrLoadData(NetworkHubDataStorage.class, DATA_NAME);
        }
        if (data == null) {
            data = new NetworkHubDataStorage(DATA_NAME);
            if (world.getMapStorage() != null) {
                world.getMapStorage().setData(DATA_NAME, data);
            }
        }
        return data;
    }

    public static NetworkHubDataStorage get(World world) {
        if (Configurations.GENERAL_CONFIG.canRDimension) {
            return getGlobal(world);
        } else {
            return getDim(world);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        networks.clear();
        NBTTagList list = nbt.getTagList("networks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            NetworkStatus net = NetworkStatus.readFromNBT(tag);
            networks.put(net.getUuid(), net);
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        for (NetworkStatus network : networks.values()) {
            NBTTagCompound tag = new NBTTagCompound();
            list.appendTag(network.writeToNBT(tag));
        }
        nbt.setTag("networks", list);
        return nbt;
    }

    public void updateFromNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList("networks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            UUID uuid = tag.getUniqueId("u");
            NetworkStatus net = networks.get(uuid);
            if (net != null) {
                net.updateFromNBT(tag);
            } else {
                NetworkStatus netNew = NetworkStatus.readFromNBT(tag);
                networks.put(netNew.getUuid(), netNew);
            }
        }
    }

    public NetworkStatus addNetwork(NetworkStatus network) {
        networks.put(network.getUuid(), network);
        markDirty();
        return network;
    }

    public void removeNetwork(UUID netId) {
        if (networks.remove(netId) != null) markDirty();
    }

    @Nullable
    public NetworkStatus getNetwork(UUID netId) {
        return networks.get(netId);
    }

    @Nonnull
    public Map<UUID, NetworkStatus> getNetworks() {
        return networks;
    }

    @Nonnull
    public List<NetworkStatus> getNeedUpdateNetworks(UUID player) {
        return networks.values().stream().filter(p -> {
            if (p.isPublic() || p.getOwner().equals(player)) {
                return p.isNeedTellClient();
            }
            return false;
        }).collect(Collectors.toList());
    }
}
