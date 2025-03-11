package com.xinyihl.ymadditions.common.api.data;

import com.xinyihl.ymadditions.common.utils.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class NetworkStatus {
    @Nonnull
    private UUID uuid = new UUID(0, 0);
    @Nonnull
    private UUID owner = new UUID(0, 0);
    @Nonnull
    private String networkName = "Unknown";
    private boolean isPublic = false;
    private int surplusChannels = 0;
    @Nonnull
    private BlockPosDim pos = new BlockPosDim(0, 0, 0, 0);

    @Nonnull
    private final Set<BlockPosDim> targetPos = new HashSet<>();
    private boolean needTellClient = false;

    private NetworkStatus() {
    }

    public NetworkStatus(@Nonnull UUID owner, @Nonnull String networkName, boolean isPublic, @Nonnull BlockPosDim pos) {
        this.uuid = UUID.randomUUID();
        this.owner = owner;
        this.networkName = networkName;
        this.isPublic = isPublic;
        this.pos = pos;
    }

    public static NetworkStatus readFromNBT(NBTTagCompound tag) {
        NetworkStatus networkStatus = new NetworkStatus();
        networkStatus.uuid = Objects.requireNonNull(tag.getUniqueId("u"));
        networkStatus.owner = Objects.requireNonNull(tag.getUniqueId("o"));
        networkStatus.networkName = tag.getString("n");
        networkStatus.isPublic = tag.getBoolean("i");
        networkStatus.pos = BlockPosDim.readFromNBT(tag.getCompoundTag("p"));
        networkStatus.surplusChannels = tag.getInteger("sc");
        return networkStatus;
    }

    public void updateFromNBT(NBTTagCompound tag) {
        this.networkName = tag.getString("n");
        this.isPublic = tag.getBoolean("i");
        this.surplusChannels = tag.getInteger("sc");
    }

    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setUniqueId("u", this.uuid);
        tag.setUniqueId("o", this.owner);
        tag.setString("n", this.networkName);
        tag.setBoolean("i", this.isPublic);
        tag.setTag("p", this.pos.writeToNBT(new NBTTagCompound()));
        tag.setInteger("sc", this.surplusChannels);
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

    @Nonnull
    public BlockPosDim getPos() {
        return pos;
    }

    @Nonnull
    public Set<BlockPosDim> getTargetPos() {
        return targetPos;
    }

    public void addTargetPos(@Nonnull BlockPosDim pos) {
        targetPos.add(pos);
    }

    public void removeTargetPos(@Nonnull BlockPosDim pos) {
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
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        return "NetworkStatus{" +
                "uuid=" + uuid +
                ", owner=" + owner +
                ", networkName='" + networkName + '\'' +
                ", isPublic=" + isPublic +
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
