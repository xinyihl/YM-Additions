package com.xinyihl.ymadditions.common.data;

import com.xinyihl.ymadditions.Configurations;
import com.xinyihl.ymadditions.client.api.IShowObject;
import com.xinyihl.ymadditions.common.utils.BlockPosDim;
import com.xinyihl.ymadditions.common.utils.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.*;

public class NetworkStatus implements IShowObject {
    @Nonnull
    private UUID uuid = new UUID(0, 0);
    @Nonnull
    private UUID owner = new UUID(0, 0);
    @Nonnull
    private final Map<UUID, Integer> users = new HashMap<>();
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
        networkStatus.updateFromNBT(tag);
        return networkStatus;
    }

    public void updateFromNBT(NBTTagCompound tag) {
        this.uuid = Objects.requireNonNull(tag.getUniqueId("u"));
        this.owner = Objects.requireNonNull(tag.getUniqueId("o"));
        this.networkName = tag.getString("n");
        this.isPublic = tag.getBoolean("i");
        this.pos = BlockPosDim.readFromNBT(tag.getCompoundTag("p"));
        this.surplusChannels = tag.getInteger("sc");
        this.users.clear();
        NBTTagList list = tag.getTagList("us", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound nbt = list.getCompoundTagAt(i);
            this.users.put(nbt.getUniqueId("u"), nbt.getInteger("v"));
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setUniqueId("u", this.uuid);
        tag.setUniqueId("o", this.owner);
        tag.setString("n", this.networkName);
        tag.setBoolean("i", this.isPublic);
        tag.setTag("p", this.pos.writeToNBT(new NBTTagCompound()));
        tag.setInteger("sc", this.surplusChannels);
        NBTTagList list = new NBTTagList();
        for (Map.Entry<UUID, Integer> entry : this.users.entrySet()) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setUniqueId("u", entry.getKey());
            nbt.setInteger("v", entry.getValue());
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
    public UUID getOwner() {
        return owner;
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

    public Set<UUID> getUsers() {
        return users.keySet();
    }

    public Integer getUserPrem(UUID uuid) {
        return users.get(uuid);
    }

    public void removeUser(UUID uuid) {
        this.users.remove(uuid);
    }

    public void addUser(UUID uuid, int perm) {
        this.users.put(uuid, perm);
    }

    public void setUser(UUID uuid, int perm) {
        this.users.replace(uuid, perm);
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
     * @param level 0(op, 拥有者, 管理员, 成员, 公开) 1(op, 拥有者, 管理员, 成员) 2(op, 拥有者, 管理员) 3(op, 拥有者)
     * @return 是否有权限操作以及维度判断
     */
    public boolean hasPermission(@Nonnull EntityPlayer player, int level) {
        if (owner.equals(new UUID(0, 0))) return true;
        boolean isOp = Utils.isPlayerOp(player);
        boolean isUser = this.users.getOrDefault(player.getGameProfile().getId(), -1) == 0;
        boolean isAdmin = this.users.getOrDefault(player.getGameProfile().getId(), -1) == 1;
        boolean isOwner = this.owner.equals(player.getGameProfile().getId());
        if (!this.checkDimension(player.world.provider.getDimension())) return false;
        switch (level) {
            case 0:
                return isOp || isOwner || isAdmin || isUser || this.isPublic;
            case 1:
                return isOp || isOwner || isAdmin || isUser;
            case 2:
                return isOp || isOwner || isAdmin;
            case 3:
                return isOp || isOwner;
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
