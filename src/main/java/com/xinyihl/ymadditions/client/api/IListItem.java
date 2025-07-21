package com.xinyihl.ymadditions.client.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;

import java.util.List;

public interface IListItem<T> {
    T get();

    List<String> getTooltip();

    void draw(Minecraft mc, int mouseX, int mouseY, float partialTicks);

    void setSelected(boolean selected);

    Object getId();

    void click();

    void playPressSound(SoundHandler soundHandlerIn);

    boolean isMouseOver(int mouseX, int mouseY);
}
