package com.xinyihl.ymadditions.common.utils;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class BlockPosDim extends BlockPos {
    private final int dimension;

    public BlockPosDim(int x, int y, int z, int dimension) {
        super(x, y, z);
        this.dimension = dimension;
    }

    public BlockPosDim(BlockPos blockPos, int dimension) {
        super(blockPos);
        this.dimension = dimension;
    }

    public static BlockPosDim readFromNBT(NBTTagCompound tag) {
        return new BlockPosDim(BlockPos.fromLong(tag.getLong("bPos")), tag.getInteger("dim"));
    }

    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setLong("bPos", this.toLong());
        tag.setInteger("dim", this.dimension);
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BlockPosDim)) return false;
        if (!super.equals(o)) return false;
        BlockPosDim that = (BlockPosDim) o;
        return dimension == that.dimension;
    }

    public BlockPos toBlockPos() {
        return new BlockPos(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), dimension);
    }

    public int getDimension() {
        return dimension;
    }
}
