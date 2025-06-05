package com.xinyihl.ymadditions.client.control;

import com.xinyihl.ymadditions.client.api.IListItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public abstract class ListItem<T> extends Gui implements IListItem<T> {

    private final Object id;

    private final String text;
    private final T o;

    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public boolean selected;

    public ListItem(Object id, String text, T o, int x, int y, int width, int height) {
        this.id = id;
        this.o = o;
        this.text = text;
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
    public void draw(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        int color = 0xFF2B2B2B;
        if (isMouseOver(mouseX, mouseY)) {
            color = 0xFF323232;
        }
        if (selected) {
            color = 0xFF505D94;
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
    public boolean isMouseOver(int mouseX, int mouseY)
    {
        return mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
    }
}
