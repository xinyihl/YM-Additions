package com.xinyihl.ymadditions.client.component;

import com.xinyihl.ymadditions.client.api.IListItem;
import com.xinyihl.ymadditions.client.api.IText;
import com.xinyihl.ymadditions.common.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class ListCtrl<T extends IText> extends Gui {

    public final int width;
    public final int height;
    public final int x;
    public final int y;
    private final int itemHeight;
    public final Minecraft mc;

    private boolean scrollByItem = false;
    private boolean scroll = false;
    private boolean isSelected = false;
    private final Collection<T> items;
    private String filter = "";
    private final List<IListItem<T>> drawItems = new ArrayList<>();
    private Object selected;

    private int scrollOffset;
    private int listHeight;
    private int maxScrollOffset;

    private int scrollBarHeight;
    private int scrollBarY;
    private boolean isScrolling;

    private int scrollWidth = 3;

    public ListCtrl(Minecraft mc, int x, int y, int width, int height, int itemHeight, Collection<T> items) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.itemHeight = itemHeight == 0 ? 1 : itemHeight;
        this.items = items;
        this.mc = mc;
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

    public void setSelected(Object selected) {
        this.selected = selected;
    }

    public void setScrollOffset(int scrollOffset) {
        this.scrollOffset = scrollOffset;
    }

    public void setFilter(String filter) {
        this.filter = Utils.escapeExprSpecialWord(filter);
    }

    private int lastItemsSize = -1;

    public void draw(int mouseX, int mouseY, float partialTicks) {

        if (this.lastItemsSize != this.items.size()) {
            this.lastItemsSize = this.items.size();
            this.refresh();
        }

        int scaleFactor = new ScaledResolution(mc).getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(this.x * scaleFactor, mc.displayHeight - (this.y + this.height) * scaleFactor, this.width * scaleFactor, this.height * scaleFactor);
        for (IListItem<T> item : this.drawItems) {
            item.draw(mc, mouseX, mouseY, partialTicks);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        this.listHeight = (int) items.stream().filter(n -> Pattern.compile(this.filter).matcher(n.getText()).find()).count() * itemHeight;
        this.maxScrollOffset = Math.max(this.listHeight - height, 0);

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

        for (IListItem<T> item : this.drawItems) {
            if (item.isMouseOver(mouseX, mouseY)) {
                String tooltip = item.getTooltip();
                if (tooltip != null && !tooltip.isEmpty()) {
                    GuiUtils.drawHoveringText(Collections.singletonList(I18n.format(tooltip)), mouseX, mouseY, 9999, 9999, -1, mc.fontRenderer);
                }
            }
        }
    }

    public void refresh() {
        this.drawItems.clear();
        int drawSize = this.height / this.itemHeight + 2;
        List<T> filter = this.items.stream().filter(n -> Pattern.compile(this.filter).matcher(n.getText()).find()).collect(Collectors.toList());
        for (int i = 0; i < drawSize; i++) {
            int index = this.scrollOffset / this.itemHeight + i;
            if (index >= filter.size()) break;
            IListItem<T> item = this.getItem(filter.get(index).getId(), filter.get(index).getText(), filter.get(index), this.x, this.y + i * this.itemHeight - this.scrollOffset % this.itemHeight, this.width - (this.scroll ? scrollWidth + 2 : 0), this.itemHeight);
            if (isSelected)
                item.setSelected(this.selected.equals(item.getId()));
            this.drawItems.add(item);
        }
    }

    public void handleMouseInput(int mouseX, int mouseY) {
        if (mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height){
            int mouseWheel = Mouse.getEventDWheel();
            if (mouseWheel != 0) {
                this.scrollOffset = MathHelper.clamp(this.scrollOffset - ((int) Math.signum(mouseWheel) * (this.scrollByItem ? this.itemHeight : 3)), 0, this.maxScrollOffset);
                this.refresh();
            }
        }
    }

    protected abstract IListItem<T> getItem(Object id, String text, T o, int x, int y, int width, int height);

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
                item.playPressSound(this.mc.getSoundHandler());
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
