package com.xinyihl.ymadditions.common.data;

import com.xinyihl.ymadditions.Configurations;
import com.xinyihl.ymadditions.client.api.IListObject;
import com.xinyihl.ymadditions.common.utils.BlockPosDim;
import com.xinyihl.ymadditions.common.utils.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.*;

public class NetworkStatus implements IListObject {
    @Nonnull
    private UUID uuid = new UUID(0, 0);
    @Nonnull
    private UUID owner = new UUID(0, 0);
    @Nonnull
    private final List<NetworkUser> users = new ArrayList<>();
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
        NetworkStatus network = new NetworkStatus();
        network.uuid = Objects.requireNonNull(tag.getUniqueId("u"));
        network.owner = Objects.requireNonNull(tag.getUniqueId("o"));
        network.networkName = tag.getString("n");
        network.isPublic = tag.getBoolean("i");
        network.pos = BlockPosDim.readFromNBT(tag.getCompoundTag("p"));
        network.surplusChannels = tag.getInteger("sc");
        NBTTagList list = tag.getTagList("us", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound nbt = list.getCompoundTagAt(i);
            network.users.add(NetworkUser.readFromNBT(nbt.getCompoundTag("n")));
        }
        return network;
    }

    public NetworkStatus updateFromNBT(NBTTagCompound tag) {
        this.uuid = Objects.requireNonNull(tag.getUniqueId("u"));
        this.owner = Objects.requireNonNull(tag.getUniqueId("o"));
        this.networkName = tag.getString("n");
        this.isPublic = tag.getBoolean("i");
        this.pos = BlockPosDim.readFromNBT(tag.getCompoundTag("p"));
        this.surplusChannels = tag.getInteger("sc");
        Set<NetworkUser> change = new HashSet<>();
        NBTTagList list = tag.getTagList("us", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound nbt = list.getCompoundTagAt(i);
            NetworkUser user = NetworkUser.readFromNBT(nbt.getCompoundTag("n"));
            change.add(user);
            if (this.users.contains(user)){
                this.users.get(this.users.indexOf(user)).updateFromNBT(nbt.getCompoundTag("n"));
            } else {
                this.users.add(user);
            }
        }
        this.users.removeIf(e -> !change.contains(e));
        return this;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setUniqueId("u", this.uuid);
        tag.setUniqueId("o", this.owner);
        tag.setString("n", this.networkName);
        tag.setBoolean("i", this.isPublic);
        tag.setTag("p", this.pos.writeToNBT(new NBTTagCompound()));
        tag.setInteger("sc", this.surplusChannels);
        NBTTagList list = new NBTTagList();
        for (NetworkUser user : this.users) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setTag("n", user.writeToNBT(new NBTTagCompound()));
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

    public List<NetworkUser> getUsers() {
        return users;
    }

    public NetworkUser.Perm getUserPrem(UUID uuid) {
        NetworkUser user = users.stream().filter(user1 -> user1.getUuid().equals(uuid)).findFirst().orElse(null);
        return user == null ? null : user.getPerm();
    }

    public void removeUser(UUID uuid) {
        this.users.removeIf(user -> user.getUuid().equals(uuid));
    }

    public void addUser(UUID uuid, String name, NetworkUser.Perm perm) {
        this.users.add(new NetworkUser(perm, uuid, name));
    }

    public void setUserPrem(UUID uuid, NetworkUser.Perm perm) {
        NetworkUser user = users.stream().filter(user1 -> user1.getUuid().equals(uuid)).findFirst().orElse(null);
        if (user == null) {
            return;
        }
        user.setPerm(perm);
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
        NetworkUser user = this.users.stream().filter(user1 -> user1.getUuid().equals(player.getGameProfile().getId())).findFirst().orElse(null);
        boolean isUser = user != null && user.getPerm() == NetworkUser.Perm.USER;
        boolean isAdmin = user != null && user.getPerm() == NetworkUser.Perm.ADMIN;
        boolean isOwner = user != null && user.getPerm() == NetworkUser.Perm.OWNER;
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

    public NetworkStatus deepCopy() {
        return NetworkStatus.readFromNBT(this.writeToNBT(new NBTTagCompound()));
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
