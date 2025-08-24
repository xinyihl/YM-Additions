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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Group {

    private final Set<User> users = new HashSet<>();
    private UUID uuid = null;
    private User owner = null;

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

    public static Group empty() {
        return new Group();
    }

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

    @Nullable
    public User getUser(User user) {
        if (this.owner.equals(user)) return this.owner;
        return users.stream().filter(u -> u.isUser(user)).findFirst().orElse(null);
    }

    public void removeUser(User user) {
        users.remove(user);
    }

    public boolean isOwner(User user) {
        return owner.isOwner() && owner.isUser(user);
    }

    public boolean hasPermission(@Nonnull EntityPlayer player, User.Perm perm) {
        if (Utils.isPlayerOp(player)) return true;
        User user = this.getUser(User.create(player.getGameProfile()));
        if (user == null) return false;
        User.Perm userPerm = user.getPerm();
        if (userPerm == null) return false;
        return userPerm.ordinal() >= perm.ordinal();
    }

    public Group of(NBTTagCompound tag) {
        if (tag.hasUniqueId("uuid")) this.uuid = tag.getUniqueId("uuid");
        if (tag.hasKey("owner")) this.owner = User.create(tag.getCompoundTag("owner"));
        if (tag.hasKey("users")) {
            NBTTagList list = tag.getTagList("users", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                this.users.add(User.create(list.getCompoundTagAt(i)));
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
    public Set<User> getUsers() {
        return users;
    }
}
