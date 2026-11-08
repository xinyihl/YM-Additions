package com.xinyihl.ymadditions.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

public abstract class GuiBase extends GuiContainer {

    public GuiBase(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    protected boolean isMouseOverTextField(GuiTextField textField, int mouseX, int mouseY) {
        return mouseX >= textField.x && mouseX < textField.x + textField.width && mouseY >= textField.y && mouseY < textField.y + textField.height;
    }

    protected boolean isMouseOverButton(GuiButton button, int mouseX, int mouseY) {
        return mouseX >= button.x && mouseY >= button.y && mouseX < button.x + button.width && mouseY < button.y + button.height;
    }
}
