package com.xinyihl.ymadditions.common.api;

import com.xinyihl.ymadditions.Configurations;
import com.xinyihl.ymadditions.client.api.IText;
import com.xinyihl.ymadditions.common.utils.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.*;

public class NetworkStatus implements IText {
    @Nonnull
    private UUID uuid = new UUID(0, 0);
    @Nonnull
    private UUID owner = new UUID(0, 0);
    @Nonnull
    private final List<UUID> users = new ArrayList<>();
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
        NBTTagList list = tag.getTagList("us", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound nbt = list.getCompoundTagAt(i);
            networkStatus.users.add(nbt.getUniqueId("u"));
        }
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
        NBTTagList list = new NBTTagList();
        for (UUID uuid : this.users) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setUniqueId("u", uuid);
            list.appendTag(nbt);
        }
        tag.setTag("us", list);
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

    public List<UUID> getUsers() {
        return users;
    }

    public void removeUser(UUID uuid) {
        this.users.remove(uuid);
    }

    public void addUser(UUID uuid) {
        this.users.add(uuid);
    }

    public void addTargetPos(@Nonnull BlockPosDim pos) {
        targetPos.add(pos);
    }

    public void removeTargetPos(@Nonnull BlockPosDim pos) {
        targetPos.remove(pos);
    }

    private boolean checkDimension(int dimension) {
        return Configurations.GENERAL_CONFIG.canRDimension || dimension == this.pos.getDimension();
    }

    /**
     * @param player 待检测玩家
     * @param level 0(op, 公开, 拥有者, 成员) 1(op, 拥有者, 成员) 2(op, 拥有者)
     * @return 是否有权限操作以及维度判断
     */
    public boolean hasPermission(@Nonnull EntityPlayer player, int level) {
        //todo 权限系统
        boolean isOp = Utils.isPlayerOp(player);
        boolean isUser = this.users.contains(player.getGameProfile().getId());
        if (!this.checkDimension(player.world.provider.getDimension())) return false;
        switch (level) {
            case 0: return isOp || this.isPublic || player.getGameProfile().getId().equals(this.owner) || isUser;
            case 1: return isOp || player.getGameProfile().getId().equals(this.owner) || isUser;
            case 2: return isOp || player.getGameProfile().getId().equals(this.owner);
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

    @Override
    public Object getId() {
        return uuid;
    }

    @Override
    public String getText() {
        return networkName;
    }
}
