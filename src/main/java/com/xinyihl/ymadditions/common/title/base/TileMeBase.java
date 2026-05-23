package com.xinyihl.ymadditions.common.title.base;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.util.Platform;
import com.xinyihl.ymadditions.api.IHasProbeInfo;
import com.xinyihl.ymadditions.api.IReadyable;
import com.xinyihl.ymadditions.common.event.EventHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class TileMeBase extends TileEntityBase implements IActionHost, IInWorldGridNodeHost, IHasProbeInfo, IReadyable {

    protected final IManagedGridNode mainNode = GridHelper.createManagedNode(this, new NodeListener())
            .setInWorldNode(true)
            .setTagName("aeProxy")
            .setExposedOnSides(EnumSet.allOf(EnumFacing.class));

    public abstract ItemStack getVisualItemStack();

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.mainNode.loadFromNBT(tag);
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound tag) {
        super.writeToNBT(tag);
        this.mainNode.saveToNBT(tag);
        return tag;
    }

    @Nullable
    @Override
    public IGridNode getActionableNode() {
        return this.mainNode.getNode();
    }

    @Override
    public IGridNode getGridNode(EnumFacing side) {
        return this.mainNode.getNode();
    }

    @Nonnull
    @Override
    public AECableType getCableConnectionType(EnumFacing side) {
        return AECableType.SMART;
    }

    public void securityBreak() {
        getWorld().destroyBlock(getPos(), true);
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        this.mainNode.destroy();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        this.mainNode.destroy();
    }

    @Override
    public void validate() {
        super.validate();
        EventHandler.enqueue(this);
    }

    @Override
    public void onReady() {
        if (!isInvalid()) {
            this.mainNode.setVisualRepresentation(this.getVisualItemStack());
            this.mainNode.create(world, pos);
            Platform.notifyBlocksOfNeighbors(world, pos);
        }
    }

    public void setOwner(EntityPlayer placer) {
        this.mainNode.setOwningPlayer(placer);
    }

    public void addProbeInfo(Consumer<String> consumer, Function<String, String> loc) {
        if (this.mainNode.isPowered()) {
            if (this.mainNode.isActive()) {
                consumer.accept(loc.apply("tile_me_base.online"));
            } else {
                consumer.accept(loc.apply("tile_me_base.missing_channel"));
            }
        } else {
            consumer.accept(loc.apply("tile_me_base.offline"));
        }
    }

    private static class NodeListener implements IGridNodeListener<TileMeBase> {
        @Override
        public void onSaveChanges(TileMeBase tile, IGridNode node) {
            tile.markDirty();
        }

        @Override
        public void onStateChanged(TileMeBase tile, IGridNode node, State state) {
            tile.sync();
        }
    }
}
