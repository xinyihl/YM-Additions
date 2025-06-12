package com.xinyihl.ymadditions.client.control;

import com.xinyihl.ymadditions.Tags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class IconButton extends GuiButton {

    int textureX;
    int textureY;

    boolean selected;

    public IconButton(int buttonId, int x, int y, int textureX, int textureY) {
        super(buttonId, x, y, 20, 20, "");
        this.textureX = textureX;
        this.textureY = textureY;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            mc.getTextureManager().bindTexture(new ResourceLocation(Tags.MOD_ID, "textures/gui/widgets.png"));
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            if (this.selected || this.hovered) {
                this.drawTexturedModalRect(this.x, this.y, textureX, textureY + 20, this.width, this.height);
                return;
            }
            this.drawTexturedModalRect(this.x, this.y, textureX, textureY, this.width, this.height);
        }
    }
}
