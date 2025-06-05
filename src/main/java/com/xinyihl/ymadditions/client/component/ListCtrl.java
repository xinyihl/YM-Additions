package com.xinyihl.ymadditions.client.component;

import com.xinyihl.ymadditions.client.api.IListItem;
import com.xinyihl.ymadditions.client.api.IText;
import com.xinyihl.ymadditions.client.control.ListItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class ListCtrl<T extends IText> extends Gui{

    public final int width;
    public final int height;
    public final int x;
    public final int y;
    private final int itemHeight;
    public final Minecraft mc;

    private boolean scrollByItem = false;
    private boolean scroll = false;
    private boolean isSelected = false;

    private final List<T> items;
    private final List<IListItem<T>> drawItems = new ArrayList<>();
    private int selected;

    private int scrollOffset;
    private final int listHeight;
    private final int maxScrollOffset;

    private int scrollBarHeight;
    private int scrollBarY;
    private boolean isScrolling;

    private int scrollWidth = 3;

    public ListCtrl(Minecraft mc, int x, int y, int width, int height, int itemHeight, List<T> items) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.itemHeight = itemHeight == 0 ? 1 : itemHeight;
        this.items = items;
        this.mc = mc;
        this.listHeight = items.size() * itemHeight;
        this.maxScrollOffset = Math.max(this.listHeight - height, 0);
    }

    public void setScrollByItem(boolean scrollByItem) {
        this.scrollByItem = scrollByItem;
    }

    public void setScroll(boolean scroll) {
        this.scroll = scroll;
    }

    public void setScrollWidth(int width) {
        this.scrollWidth = width;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    public void draw(int mouseX, int mouseY, float partialTicks) {
        int scaleFactor = new ScaledResolution(mc).getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(this.x * scaleFactor, mc.displayHeight - (this.y + this.height) * scaleFactor, this.width * scaleFactor, this.height * scaleFactor);
        for (IListItem<T> item : this.drawItems) {
            item.draw(mc, mouseX, mouseY, partialTicks);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        if (this.scroll) {
            this.scrollBarHeight = this.height;
            if (this.maxScrollOffset > 0) {
                this.scrollBarHeight = (int) ((float) this.height * this.height / this.listHeight);
                this.scrollBarHeight = Math.max(10, this.scrollBarHeight);
            }
            this.scrollBarY = this.y;
            if (this.maxScrollOffset > 0) {
                this.scrollBarY += (int) ((float) this.scrollOffset / this.maxScrollOffset * (this.height - this.scrollBarHeight));
            }
            drawRect(this.x + this.width - this.scrollWidth - 1, this.y,          this.x + this.width - 1, this.y + this.height, 0xFF9C9C9C);
            drawRect(this.x + this.width - this.scrollWidth - 1, this.scrollBarY, this.x + this.width - 1, this.scrollBarY + this.scrollBarHeight, 0xFF373737);
        }
    }

    public void refresh() {
        this.drawItems.clear();
        int drawSize = this.height / this.itemHeight + 2;
        for (int i = 0; i < drawSize; i++) {
            int index = this.scrollOffset / this.itemHeight + i;
            if (index >= this.items.size()) break;
            IListItem<T> item = this.getItem(index, this.items.get(index).getText(), this.items.get(index), this.x, this.y + i * this.itemHeight - this.scrollOffset % this.itemHeight, this.width - (this.scroll ? scrollWidth + 2 : 0), this.itemHeight);
            item.setSelected(this.selected == index);
            this.drawItems.add(item);
        }
    }

    public void handleMouseInput(int mouseX, int mouseY) {
        if (mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height){
            int mouseWheel = Mouse.getEventDWheel();
            if (mouseWheel != 0) {
                this.scrollOffset = MathHelper.clamp(this.scrollOffset - ((int) Math.signum(mouseWheel) * (this.scrollByItem ? this.itemHeight : 1)), 0, this.maxScrollOffset);
                this.refresh();
            }
        }
    }

    private IListItem<T> getItem(int id, String text, T o, int x, int y, int width, int height){
        return new ListItem<>(id, text, o, x, y, width, height);
    }

    private int lastMouseY;

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.scroll && !this.isScrolling && this.isMouseOverScrollBar(mouseX, mouseY)) {
            this.lastMouseY = mouseY;
            this.isScrolling = true;
        }
        boolean r = false;
        for (IListItem<T> item : this.drawItems) {
            if (item.isMouseOver(mouseX, mouseY)){
                item.click();
                if (isSelected) {
                    this.setSelected(item.getId());
                    r = true;
                }
            }
        }
        if (r) {
            this.refresh();
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (this.scroll && this.isScrolling) {
            this.isScrolling = false;
        }
    }

    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (this.scroll && this.isScrolling && this.maxScrollOffset > 0) {
            this.scrollOffset = MathHelper.clamp(this.scrollOffset + mouseY - this.lastMouseY, 0, this.maxScrollOffset);
            this.lastMouseY = mouseY;
            this.refresh();
        }
    }

    private boolean isMouseOverScrollBar(int mouseX, int mouseY) {
        int scrollLeft = this.x + this.width - this.scrollWidth - 1;
        int scrollRight = this.x + this.width - 1;
        int scrollTop = this.scrollBarY;
        int scrollBottom = this.scrollBarY + this.scrollBarHeight;
        return mouseX >= scrollLeft && mouseX <= scrollRight && mouseY >= scrollTop && mouseY <= scrollBottom;
    }
}
