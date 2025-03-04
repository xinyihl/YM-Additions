package com.xinyihl.ymadditions.common.title;

import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.me.helpers.MachineSource;
import appeng.util.Platform;
import com.xinyihl.ymadditions.common.api.IHasProbeInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class TitleMeBase extends TileEntity implements IActionHost, IGridProxyable, IHasProbeInfo {

    protected final AENetworkProxy proxy = new AENetworkProxy(this, "aeProxy", getVisualItemStack(), true);
    protected final IActionSource source;

    public TitleMeBase() {
        this.source = new MachineSource(this);
        this.proxy.setIdlePowerUsage(100.0D);
        //this.proxy.setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    public abstract ItemStack getVisualItemStack();

    protected void notifyNeighbors() {
        if (this.proxy.isActive()) {
            try {
                this.proxy.getTick().wakeDevice(this.proxy.getNode());
            } catch (GridAccessException e) {
                //Ignore
            }
            Platform.notifyBlocksOfNeighbors(this.getWorld(), this.getPos());
        }
    }

    @Override
    protected void setWorldCreate(@Nonnull final World worldIn) {
        setWorld(worldIn);
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (!world.isRemote) {
            try {
                proxy.readFromNBT(tag);
            } catch (IllegalStateException e) {
                //Ignore
            }
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound tag) {
        super.writeToNBT(tag);
        proxy.writeToNBT(tag);
        return tag;
    }

    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(@Nonnull NBTTagCompound tag) {
        this.readFromNBT(tag);
    }

    public void sync() {
        this.markDirty();
        this.world.notifyBlockUpdate(this.pos, this.world.getBlockState(this.pos), this.world.getBlockState(this.pos), 3);
        this.notifyNeighbors();
    }

    @Nonnull
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.getPos(), 2555, this.getUpdateTag());
    }

    public final void onDataPacket(@Nonnull NetworkManager manager, @Nonnull SPacketUpdateTileEntity packet) {
        this.readFromNBT(packet.getNbtCompound());
    }

    @Nonnull
    @Override
    public IGridNode getActionableNode() {
        return proxy.getNode();
    }

    @Override
    public AENetworkProxy getProxy() {
        return proxy;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public void gridChanged() {

    }

    @Nullable
    @Override
    public IGridNode getGridNode(@Nonnull AEPartLocation aePartLocation) {
        return proxy.getNode();
    }

    @Nonnull
    @Override
    public AECableType getCableConnectionType(@Nonnull AEPartLocation aePartLocation) {
        return AECableType.SMART;
    }

    @Override
    public void securityBreak() {
        getWorld().destroyBlock(getPos(), true);
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        proxy.onChunkUnload();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        proxy.invalidate();
    }

    @Override
    public void validate() {
        super.validate();
        proxy.validate();
        proxy.onReady();
    }

    public void setOwner(EntityPlayer placer) {
        proxy.setOwner(placer);
    }

    public void addProbeInfo(Consumer<String> consumer, Function<String, String> loc) {
        if (this.proxy.isPowered()) {
            if (this.proxy.isActive()) {
                consumer.accept(loc.apply("tile_me_base.online"));
            } else {
                consumer.accept(loc.apply("tile_me_base.missing_channel"));
            }
        } else {
            consumer.accept(loc.apply("tile_me_base.offline"));
        }
    }
}
