package com.xinyihl.ymadditions.common.title.base;

import com.xinyihl.ymadditions.api.ISyncable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nonnull;

import static net.minecraftforge.common.util.Constants.BlockFlags.RERENDER_MAIN_THREAD;

public abstract class TileEntityBase extends TileEntity implements ISyncable, ITickable {

    private long lastWorldTime = -1;
    private int tickCounter = 0;

    @Override
    protected void setWorldCreate(@Nonnull final World worldIn) {
        setWorld(worldIn);
    }

    @Override
    public void update() {
        if (this.world.isRemote) return;
        long totalWorldTime = this.world.getTotalWorldTime();
        if (this.lastWorldTime != totalWorldTime) {
            this.lastWorldTime = totalWorldTime;
            this.tickCounter = (this.tickCounter + 1) % 20;
            if (this.tickCounter % 20 == 0) {
                this.onTick();
                this.sync();
            }
        }
    }

    protected abstract void onTick();

    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() {
        return this.getSyncData(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(@Nonnull NBTTagCompound tag) {
        this.doSyncFrom(tag);
    }

    @Override
    public void sync() {
        if (!this.world.isRemote) {
            SPacketUpdateTileEntity packet = this.getUpdatePacket();
            PlayerChunkMapEntry trackingEntry = ((WorldServer) this.world).getPlayerChunkMap().getEntry(this.pos.getX() >> 4, this.pos.getZ() >> 4);
            if (trackingEntry != null) {
                for (EntityPlayerMP player : trackingEntry.getWatchingPlayers()) {
                    player.connection.sendPacket(packet);
                }
            }
            this.markDirty();
        }
    }

    @Nonnull
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.getPos(), 2555, this.getUpdateTag());
    }

    @Override
    public final void onDataPacket(@Nonnull NetworkManager manager, @Nonnull SPacketUpdateTileEntity packet) {
        this.doSyncFrom(packet.getNbtCompound());
        if (this.world.isRemote) {
            IBlockState state = this.world.getBlockState(this.getPos());
            this.world.notifyBlockUpdate(this.pos, state, state, RERENDER_MAIN_THREAD);
        }
    }
}
