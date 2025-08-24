package com.xinyihl.ymadditions.api.entity;

import com.mojang.authlib.GameProfile;
import com.xinyihl.ymadditions.api.IListObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class User implements IListObject {

    private UUID uuid;
    private String name;
    private Perm perm;


    private User() {

    }

    public static User create(NBTTagCompound tag) {
        return new User().of(tag);
    }

    public static User create(UUID uuid, String name, Perm perm) {
        User user = new User();
        user.uuid = uuid;
        user.name = name;
        user.perm = perm;
        return user;
    }

    public static User create(UUID uuid, String name) {
        return User.create(uuid, name, User.Perm.NONE);
    }

    public static User create(GameProfile profile, Perm perm) {
        return User.create(profile.getId(), profile.getName(), perm);
    }

    public static User create(GameProfile profile) {
        return User.create(profile.getId(), profile.getName(), Perm.NONE);
    }

    public static User create(EntityPlayer player, Perm perm) {
        return User.create(player.getGameProfile(), perm);
    }

    public static User create(EntityPlayer player) {
        return User.create(player.getGameProfile(), Perm.NONE);
    }

    public static User empty() {
        return new User();
    }

    public boolean isOwner() {
        return this.perm == Perm.OWNER;
    }

    public boolean isAdmin() {
        return this.perm == Perm.ADMIN;
    }

    public boolean isMember() {
        return this.perm == Perm.USER;
    }

    public User of(NBTTagCompound tag) {
        if (tag.hasUniqueId("uuid")) this.uuid = tag.getUniqueId("uuid");
        if (tag.hasKey("name")) this.name = tag.getString("name");
        if (tag.hasKey("perm")) this.perm = Perm.valueOf(tag.getString("perm"));
        return this;
    }

    public NBTTagCompound to(NBTTagCompound tag) {
        if (this.uuid != null) tag.setUniqueId("uuid", this.uuid);
        tag.setString("name", this.name);
        tag.setString("perm", this.perm.name());
        return tag;
    }

    public User deepCopy() {
        return User.create(this.to(new NBTTagCompound()));
    }

    @Override
    @Nullable
    public UUID getUuid() {
        return uuid;
    }

    @Override
    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public Perm getPerm() {
        return perm;
    }

    public User setPerm(Perm perm) {
        this.perm = perm;
        return this;
    }

    public boolean isUser(User user) {
        return this.equals(user);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(uuid, user.uuid) && Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name);
    }

    public enum Perm {
        NONE,
        USER,
        ADMIN,
        OWNER
    }
}
