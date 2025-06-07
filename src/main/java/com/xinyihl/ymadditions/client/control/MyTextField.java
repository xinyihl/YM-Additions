package com.xinyihl.ymadditions.client.control;

import com.xinyihl.ymadditions.Tags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class MyTextField extends GuiTextField {
    private final Minecraft mc;
    public MyTextField(@Nonnull Minecraft mc, int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
        super(componentId, fontrendererObj, x + 3, y + 2, par5Width, par6Height);
        this.mc = mc;
    }

    public void drawTextBox() {
        if (this.getVisible()) {
            mc.getTextureManager().bindTexture(new ResourceLocation(Tags.MOD_ID, "textures/gui/widgets.png"));
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            this.drawTexturedModalRect(this.x - 3, this.y - 2, 0, 3 * this.height, this.width / 2, this.height);
            this.drawTexturedModalRect((this.x + this.width / 2) - 3, this.y - 2, 200 - this.width / 2, 3 * this.height, this.width / 2, this.height);
        }
        super.drawTextBox();
    }
}
