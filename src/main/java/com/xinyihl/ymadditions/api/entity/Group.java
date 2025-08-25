package com.xinyihl.ymadditions.api.entity;

import com.xinyihl.ymadditions.common.utils.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class Group {

    private final List<User> users = new ArrayList<>();
    private UUID uuid = null;
    private User owner = null;

    public Group injectOnlinePlayers() {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null) {
            Arrays.stream(server.getPlayerList().getOnlinePlayerProfiles())
                    .map(User::create)
                    .filter(user -> !(users.contains(user) || user.equals(owner)))
                    .forEach(users::add);
        }
        return this;
    }

    private Group() {

    }

    public static Group create(NBTTagCompound tag) {
        return new Group().of(tag);
    }

    public static Group create(User owner) {
        Group group = new Group();
        group.uuid = UUID.randomUUID();
        group.owner = owner;
        return group;
    }

    public void removeUser(User user) {
        users.remove(user);
    }

    public static Group empty() {
        return new Group();
    }

    public boolean hasPermission(@Nonnull EntityPlayer player, User.Perm perm) {
        if (Utils.isPlayerOp(player)) return true;
        User user = this.getUser(User.create(player.getGameProfile()));
        if (user == null) return false;
        User.Perm userPerm = user.getPerm();
        if (userPerm == null) return false;
        return userPerm.ordinal() >= perm.ordinal();
    }

    public Group injectOwnerToUsers() {
        if (!users.contains(this.owner)) {
            users.add(0, this.owner);
        }
        return this;
    }

    @Nullable
    public User getUser(User user) {
        if (user.equals(this.owner)) return this.owner;
        return users.stream().filter(u -> u.isUser(user)).findFirst().orElse(null);
    }

    public void addUser(User user) {
        if (!users.contains(user)) this.users.add(user);
    }

    public boolean isOwner(User user) {
        return owner != null && owner.isOwner() && owner.isUser(user);
    }

    public void update(NBTTagCompound tag) {
        if (tag.hasUniqueId("uuid")) this.uuid = tag.getUniqueId("uuid");
        if (tag.hasKey("owner")) this.owner = User.create(tag.getCompoundTag("owner"));
        if (tag.hasKey("users")) {
            NBTTagList list = tag.getTagList("users", Constants.NBT.TAG_COMPOUND);
            Set<UUID> uuidsToKeep = new HashSet<>();
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound nbt = list.getCompoundTagAt(i);
                UUID uuid = nbt.getUniqueId("uuid");
                if (uuid == null) continue;
                uuidsToKeep.add(uuid);
                User user = this.users.stream().filter(u -> uuid.equals(u.getUuid())).findFirst().orElse(null);
                if (user != null) {
                    user.of(nbt);
                } else {
                    User newUser = User.create(nbt);
                    this.users.add(newUser);
                }
            }
            this.users.removeIf(user -> !uuidsToKeep.contains(user.getUuid()));
        }
    }

    public Group of(NBTTagCompound tag) {
        if (tag.hasUniqueId("uuid")) this.uuid = tag.getUniqueId("uuid");
        if (tag.hasKey("owner")) this.owner = User.create(tag.getCompoundTag("owner"));
        if (tag.hasKey("users")) {
            this.users.clear();
            NBTTagList list = tag.getTagList("users", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                this.addUser(User.create(list.getCompoundTagAt(i)));
            }
        }
        return this;
    }

    public NBTTagCompound to(NBTTagCompound tag) {
        if (this.uuid != null) tag.setUniqueId("uuid", this.uuid);
        if (this.owner != null) tag.setTag("owner", this.owner.to(new NBTTagCompound()));
        NBTTagList list = new NBTTagList();
        for (User user : this.users) {
            list.appendTag(user.to(new NBTTagCompound()));
        }
        tag.setTag("users", list);
        return tag;
    }

    public Group deepCopy() {
        return Group.create(this.to(new NBTTagCompound()));
    }

    @Nullable
    public UUID getUuid() {
        return uuid;
    }

    @Nullable
    public User getOwner() {
        return owner;
    }

    @Nonnull
    public List<User> getUsers() {
        return users;
    }
}
