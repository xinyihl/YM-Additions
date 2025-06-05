package com.xinyihl.ymadditions.client.control;

import com.xinyihl.ymadditions.Tags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiLockIconButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class MyLockIconButton extends GuiLockIconButton {
    public MyLockIconButton(int buttonId, int x, int y) {
        super(buttonId, x, y);
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            mc.getTextureManager().bindTexture(new ResourceLocation(Tags.MOD_ID, "textures/gui/widgets.png"));
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            boolean flag = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            Icon guilockiconbutton$icon;

            if (this.isLocked()) {
                if (!this.enabled) {
                    guilockiconbutton$icon = Icon.LOCKED_DISABLED;
                } else if (flag) {
                    guilockiconbutton$icon = Icon.LOCKED_HOVER;
                } else {
                    guilockiconbutton$icon = Icon.LOCKED;
                }
            } else if (!this.enabled) {
                guilockiconbutton$icon = Icon.UNLOCKED_DISABLED;
            } else if (flag) {
                guilockiconbutton$icon = Icon.UNLOCKED_HOVER;
            } else {
                guilockiconbutton$icon = Icon.UNLOCKED;
            }

            this.drawTexturedModalRect(this.x, this.y, guilockiconbutton$icon.getX(), guilockiconbutton$icon.getY(), this.width, this.height);
        }
    }

    @SideOnly(Side.CLIENT)
    enum Icon {
        LOCKED(0, 146),
        LOCKED_HOVER(0, 166),
        LOCKED_DISABLED(0, 186),
        UNLOCKED(20, 146),
        UNLOCKED_HOVER(20, 166),
        UNLOCKED_DISABLED(20, 186);

        private final int x;
        private final int y;

        Icon(int xIn, int yIn) {
            this.x = xIn;
            this.y = yIn;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }
    }
}
