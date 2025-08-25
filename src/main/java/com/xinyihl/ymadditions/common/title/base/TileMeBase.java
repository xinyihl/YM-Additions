package com.xinyihl.ymadditions.common.title.base;

import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.util.Platform;
import com.xinyihl.ymadditions.api.IHasProbeInfo;
import com.xinyihl.ymadditions.api.IReadyable;
import com.xinyihl.ymadditions.common.event.EventHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class TileMeBase extends TileEntityBase implements IActionHost, IGridProxyable, IHasProbeInfo, IReadyable {

    protected final AENetworkProxy proxy = new AENetworkProxy(this, "aeProxy", this.getVisualItemStack(), true);

    public abstract ItemStack getVisualItemStack();

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound tag) {
        super.readFromNBT(tag);
        proxy.readFromNBT(tag);
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
        EventHandler.enqueue(this);
    }

    @Override
    public void onReady() {
        if (!isInvalid()) {
            proxy.onReady();
            Platform.notifyBlocksOfNeighbors(world, pos);
        }
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
