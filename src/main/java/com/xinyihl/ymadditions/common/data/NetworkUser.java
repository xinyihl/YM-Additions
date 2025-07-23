package com.xinyihl.ymadditions.common.data;

import com.mojang.authlib.GameProfile;
import com.xinyihl.ymadditions.client.api.IShowObject;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Objects;
import java.util.UUID;

public class NetworkUser implements IShowObject {

    private int perm;

    private UUID uuid;
    private String name;

    private NetworkUser(){

    }

    public NetworkUser(Perm perm, UUID uuid, String name) {
        this.perm = perm.ordinal();
        this.uuid = uuid;
        this.name = name;
    }

    public static NetworkUser readFromNBT(NBTTagCompound tag) {
        return new NetworkUser().updateFromNBT(tag);
    }

    public NetworkUser updateFromNBT(NBTTagCompound tag) {
        this.perm = tag.getInteger("p");
        this.uuid = Objects.requireNonNull(tag.getUniqueId("u"));
        this.name = Objects.requireNonNull(tag.getString("n"));
        return this;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setInteger("p", this.perm);
        tag.setUniqueId("u", this.uuid);
        tag.setString("n", this.name);
        return tag;
    }

    public GameProfile getProfile() {
        return new GameProfile(this.uuid, this.name);
    }

    public Perm getPerm() {
        return Perm.values()[this.perm];
    }

    public void setPerm(Perm perm) {
        this.perm = perm.ordinal();
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public UUID getId() {
        return uuid;
    }

    @Override
    public String getText() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NetworkUser)) return false;
        NetworkUser user = (NetworkUser) o;
        return Objects.equals(uuid, user.uuid) && Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name);
    }

    public static enum Perm {
        NONE,
        USER,
        ADMIN,
        OWNER
    }
}
