package com.xinyihl.ymadditions.client.gui;

import com.xinyihl.ymadditions.client.component.ListCtrl;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestGui extends GuiContainer {
    public TestGui(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    private ListCtrl listCtrl;

    @Override
    public void initGui() {
        List<TestString> list = new ArrayList<>(Arrays.asList(new TestString("111"), new TestString("222"), new TestString("333"), new TestString("444"), new TestString("555"), new TestString("666"), new TestString("777"), new TestString("888"), new TestString("9999")));
        listCtrl = new ListCtrl<>(this.mc, guiLeft + 200, guiTop + 100, 80, 100, 20, list);
        listCtrl.setScroll(true);
        listCtrl.setScrollByItem(true);
        listCtrl.setIsSelected(true);
        listCtrl.refresh();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        listCtrl.draw(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        listCtrl.handleMouseInput(i, j);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        listCtrl.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        listCtrl.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        listCtrl.mouseReleased(mouseX, mouseY, state);
    }
}
