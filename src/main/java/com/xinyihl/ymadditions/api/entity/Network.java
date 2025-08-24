package com.xinyihl.ymadditions.api.entity;

import com.xinyihl.ymadditions.Configurations;
import com.xinyihl.ymadditions.api.IListObject;
import com.xinyihl.ymadditions.common.data.DataStorage;
import com.xinyihl.ymadditions.common.utils.BlockPosDim;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Network implements IListObject {

    private final Set<BlockPosDim> receivePos = new HashSet<>();
    private UUID uuid = null;
    private UUID owner = null; // Group UUID
    private String name = "Unknown";
    private boolean overt = false;
    private BlockPosDim sendPos = null;

    private Network() {

    }

    public static Network create(NBTTagCompound tag) {
        return new Network().of(tag);
    }

    public static Network create(UUID owner, String name, boolean overt, BlockPosDim sendPos) {
        Network network = new Network();
        network.uuid = UUID.randomUUID();
        network.owner = owner;
        network.name = name;
        network.overt = overt;
        network.sendPos = sendPos;
        return network;
    }

    public static Network empty() {
        return new Network();
    }

    private boolean checkDimension(int dimension) {
        return Configurations.GENERAL_CONFIG.canRDimension || dimension == this.sendPos.getDimension();
    }

    public boolean hasPermission(@Nonnull EntityPlayer player, User.Perm perm) {
        if (uuid == null) return true;
        if (!this.checkDimension(player.world.provider.getDimension())) return false;
        DataStorage storage = DataStorage.get(player.world);
        Group group = storage.getGroup(owner);
        if (group == null) return false;
        if (overt && perm == User.Perm.USER) return true;
        return group.hasPermission(player, perm);
    }

    public Network of(NBTTagCompound tag) {
        if (tag.hasUniqueId("uuid")) this.uuid = tag.getUniqueId("uuid");
        if (tag.hasUniqueId("owner")) this.owner = tag.getUniqueId("owner");
        if (tag.hasKey("name")) this.name = tag.getString("name");
        if (tag.hasKey("overt")) this.overt = tag.getBoolean("overt");
        if (tag.hasKey("sendPos")) this.sendPos = BlockPosDim.readFromNBT(tag.getCompoundTag("sendPos"));
        return this;
    }

    public NBTTagCompound to(NBTTagCompound tag) {
        if (this.uuid != null) tag.setUniqueId("uuid", this.uuid);
        if (this.owner != null) tag.setUniqueId("owner", this.owner);
        tag.setString("name", this.name);
        tag.setBoolean("overt", this.overt);
        if (this.sendPos != null) tag.setTag("sendPos", this.sendPos.writeToNBT(new NBTTagCompound()));
        return tag;
    }

    public Network deepCopy() {
        return Network.create(this.to(new NBTTagCompound()));
    }

    @Override
    @Nullable
    public UUID getUuid() {
        return uuid;
    }

    @Nullable
    public UUID getOwner() {
        return owner;
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    public boolean isOvert() {
        return overt;
    }

    public void setOvert(boolean overt) {
        this.overt = overt;
    }

    @Nullable
    public BlockPosDim getSendPos() {
        return sendPos;
    }

    @Nonnull
    public Set<BlockPosDim> getReceivePos() {
        return receivePos;
    }

    public void addReceivePos(BlockPosDim receivePos) {
        this.receivePos.add(receivePos);
    }

    public void removeReceivePos(BlockPosDim receivePos) {
        this.receivePos.remove(receivePos);
    }
}
