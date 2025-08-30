package com.xinyihl.ymadditions.api;

import net.minecraft.nbt.NBTTagCompound;

public interface IActionHandler {
    void onAction(String type, NBTTagCompound data);
}
