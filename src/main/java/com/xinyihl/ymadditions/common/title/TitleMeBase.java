package com.xinyihl.ymadditions.common.title;

import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.me.helpers.MachineSource;
import com.xinyihl.ymadditions.Configurations;
import com.xinyihl.ymadditions.common.api.IHasProbeInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.minecraftforge.common.util.Constants.BlockFlags.RERENDER_MAIN_THREAD;

public abstract class TitleMeBase extends TileEntity implements IActionHost, IGridProxyable, IHasProbeInfo {

    protected final AENetworkProxy proxy = new AENetworkProxy(this, "aeProxy", getVisualItemStack(), true);
    protected final IActionSource source;

    public TitleMeBase() {
        this.source = new MachineSource(this);
        this.proxy.setIdlePowerUsage(Configurations.GENERAL_CONFIG.powerBase);
        //this.proxy.setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    public abstract ItemStack getVisualItemStack();

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
        } else {
            if (this.world.isBlockLoaded(this.getPos())) {
                IBlockState state = this.world.getBlockState(this.getPos());
                this.world.notifyBlockUpdate(this.pos, state, state, RERENDER_MAIN_THREAD);
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
        if (!this.world.isRemote) {
            SPacketUpdateTileEntity packet = this.getUpdatePacket();
            PlayerChunkMapEntry trackingEntry = ((WorldServer)this.world).getPlayerChunkMap().getEntry(this.pos.getX() >> 4, this.pos.getZ() >> 4);
            if (trackingEntry != null) {
                for (EntityPlayerMP player : trackingEntry.getWatchingPlayers()) {
                    player.connection.sendPacket(packet);
                }
            }
            this.markDirty();
        }
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
