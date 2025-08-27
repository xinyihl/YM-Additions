package com.xinyihl.ymadditions.client.control;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class StringRenderUtil {
    public static void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color, float scale)
    {
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1.0f);
        fontRendererIn.drawStringWithShadow(text, (x / scale - (float) fontRendererIn.getStringWidth(text) / 2), (float)y / scale, color);
        GlStateManager.popMatrix();
    }

    public static int drawString(FontRenderer fontRendererIn, String text, int x, int y, int color, float scale)
    {
        int r;
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1.0f);
        r = fontRendererIn.drawString(text, (float)x / scale, (float)y / scale, color, false);
        GlStateManager.popMatrix();
        return r;
    }
}
