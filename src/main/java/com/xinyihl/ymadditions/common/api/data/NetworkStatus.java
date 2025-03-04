package com.xinyihl.ymadditions.common.api.data;

import com.xinyihl.ymadditions.common.utils.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class NetworkStatus {
    @Nonnull
    private final List<BlockPos> targetPos = new ArrayList<>();
    @Nonnull
    private UUID uuid = new UUID(0, 0);
    @Nonnull
    private UUID owner = new UUID(0, 0);
    @Nonnull
    private String networkName = "Unknown";
    private boolean needTellClient = false;
    private boolean isPublic = false;
    private int dimensionId = 0;
    private int surplusChannels = 0;
    @Nonnull
    private BlockPos pos = new BlockPos(0, 0, 0);

    private NetworkStatus() {
    }

    public NetworkStatus(@Nonnull UUID owner, @Nonnull String networkName, boolean isPublic, int dimensionId, @Nonnull BlockPos pos) {
        this.uuid = UUID.randomUUID();
        this.owner = owner;
        this.networkName = networkName;
        this.isPublic = isPublic;
        this.dimensionId = dimensionId;
        this.pos = pos;
    }

    public static NetworkStatus readFromNBT(NBTTagCompound tag) {
        NetworkStatus networkStatus = new NetworkStatus();
        networkStatus.uuid = Objects.requireNonNull(tag.getUniqueId("u"));
        networkStatus.owner = Objects.requireNonNull(tag.getUniqueId("o"));
        networkStatus.networkName = tag.getString("n");
        networkStatus.isPublic = tag.getBoolean("i");
        networkStatus.dimensionId = tag.getInteger("d");
        networkStatus.pos = BlockPos.fromLong(tag.getLong("p"));
        networkStatus.surplusChannels = tag.getInteger("sc");

        NBTTagList list = tag.getTagList("tp", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound nbt = list.getCompoundTagAt(i);
            networkStatus.addTargetPos(BlockPos.fromLong(nbt.getLong("t")));
        }
        return networkStatus;
    }

    public void updateFromNBT(NBTTagCompound tag) {
        this.networkName = tag.getString("n");
        this.isPublic = tag.getBoolean("i");
        this.surplusChannels = tag.getInteger("sc");

        this.targetPos.clear();
        NBTTagList list = tag.getTagList("tp", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound nbt = list.getCompoundTagAt(i);
            this.addTargetPos(BlockPos.fromLong(nbt.getLong("t")));
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setUniqueId("u", this.uuid);
        tag.setUniqueId("o", this.owner);
        tag.setString("n", this.networkName);
        tag.setBoolean("i", this.isPublic);
        tag.setInteger("d", this.dimensionId);
        tag.setLong("p", this.pos.toLong());
        tag.setInteger("sc", this.surplusChannels);
        NBTTagList list = new NBTTagList();
        for (BlockPos pos : targetPos) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setLong("t", pos.toLong());
            list.appendTag(nbt);
        }
        tag.setTag("tp", list);
        return tag;
    }

    @Nonnull
    public UUID getUuid() {
        return uuid;
    }

    @Nonnull
    public String getNetworkName() {
        return networkName;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public int getDimensionId() {
        return dimensionId;
    }

    @Nonnull
    public BlockPos getPos() {
        return pos;
    }

    @Nonnull
    public List<BlockPos> getTargetPos() {
        return targetPos;
    }

    public void addTargetPos(@Nonnull BlockPos pos) {
        targetPos.add(pos);
    }

    public void removeTargetPos(@Nonnull BlockPos pos) {
        targetPos.remove(pos);
    }

    public boolean hasPermission(@Nonnull EntityPlayer player, int level) {
        //todo 权限系统
        boolean isOp = Utils.isPlayerOp(player);
        switch (level) {
            case 0: return isOp || this.isPublic || player.getGameProfile().getId().equals(this.owner);
            case 1: return isOp || player.getGameProfile().getId().equals(this.owner);
            //case 2: return player.getGameProfile().getId().equals(this.owner);
            default: return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof NetworkStatus) {
            NetworkStatus e = (NetworkStatus) o;
            return e.getUuid().equals(this.uuid);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetPos, uuid, owner, networkName, isPublic, dimensionId, surplusChannels, pos);
    }

    @Override
    public String toString() {
        return "NetworkStatus{" +
                "uuid=" + uuid +
                ", owner=" + owner +
                ", networkName='" + networkName + '\'' +
                ", isPublic=" + isPublic +
                ", dimensionId=" + dimensionId +
                ", pos=" + pos +
                '}';
    }

    public int getSurplusChannels() {
        return surplusChannels;
    }

    public void setSurplusChannels(int surplusChannels) {
        this.surplusChannels = surplusChannels;
    }

    public boolean isNeedTellClient() {
        return needTellClient;
    }

    public void setNeedTellClient(boolean needTellClient) {
        this.needTellClient = needTellClient;
    }
}
