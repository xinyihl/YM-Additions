package com.xinyihl.ymadditions.client.api;

import net.minecraft.client.Minecraft;

public interface IListItem<T> {
    T get();

    void draw(Minecraft mc, int mouseX, int mouseY, float partialTicks);

    void setSelected(boolean selected);

    Object getId();

    void click();

    boolean isMouseOver(int mouseX, int mouseY);
}
