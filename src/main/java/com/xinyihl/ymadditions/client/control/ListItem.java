package com.xinyihl.ymadditions.client.control;

import com.xinyihl.ymadditions.client.api.IListItem;
import com.xinyihl.ymadditions.client.api.IListObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.init.SoundEvents;

import java.util.Collections;
import java.util.List;

public abstract class ListItem<T extends IListObject> extends Gui implements IListItem<T> {

    protected final Object id;

    protected final String text;
    protected final List<String> tooltip;
    protected final T o;

    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public boolean selected;

    public ListItem(Object id, String text, T o, int x, int y, int width, int height) {
        this(id, text, Collections.emptyList(), o, x, y, width, height);
    }

    public ListItem(Object id, String text, List<String> tooltip, T o, int x, int y, int width, int height) {
        this.id = id;
        this.o = o;
        this.text = text;
        this.tooltip = tooltip;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public T get() {
        return o;
    }

    @Override
    public List<String> getTooltip() {
        return tooltip;
    }

    @Override
    public void draw(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        int color = 0x00000000;
        if (isMouseOver(mouseX, mouseY)) {
            color = 0x2DFFFFFF;
        }
        if (selected) {
            color = 0x3C505D94;
        }
        drawRect(this.x, this.y, this.x + this.width, this.y + height, color);
        this.drawCenteredString(mc.fontRenderer, this.text, this.x + this.width / 2, this.y + (this.height - 8) / 2, 0xFFFFFFFF);
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public void click() {

    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn)
    {
        soundHandlerIn.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean isMouseOver(int mouseX, int mouseY)
    {
        return mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
    }
}
