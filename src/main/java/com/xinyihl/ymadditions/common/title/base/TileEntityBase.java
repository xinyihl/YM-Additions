package com.xinyihl.ymadditions.common.title.base;

import com.xinyihl.ymadditions.api.ISyncable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

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

    @Nonnull
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.getPos(), 2555, this.getUpdateTag());
    }

    @Override
    public final void onDataPacket(@Nonnull NetworkManager manager, @Nonnull SPacketUpdateTileEntity packet) {
        this.doSyncFrom(packet.getNbtCompound());
    }

    @Override
    public boolean shouldRefresh(@Nonnull World world, @Nonnull BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }
}
