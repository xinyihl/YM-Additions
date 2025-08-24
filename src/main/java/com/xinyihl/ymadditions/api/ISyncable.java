package com.xinyihl.ymadditions.api;

import net.minecraft.nbt.NBTTagCompound;

public interface ISyncable {

    void sync();

    NBTTagCompound getSyncData(NBTTagCompound tag);

    void doSyncFrom(NBTTagCompound tag);
}
