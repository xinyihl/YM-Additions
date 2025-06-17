package com.xinyihl.ymadditions.client.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;

public interface IListItem<T> {
    T get();

    String getTooltip();

    void draw(Minecraft mc, int mouseX, int mouseY, float partialTicks);

    void setSelected(boolean selected);

    Object getId();

    void click();

    void playPressSound(SoundHandler soundHandlerIn);

    boolean isMouseOver(int mouseX, int mouseY);
}
