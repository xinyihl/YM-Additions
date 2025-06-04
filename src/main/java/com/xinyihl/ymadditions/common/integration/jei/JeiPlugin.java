package com.xinyihl.ymadditions.common.integration.jei;

import com.xinyihl.ymadditions.client.gui.GuiNetworkHub;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IAdvancedGuiHandler;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;

@JEIPlugin
public class JeiPlugin implements IModPlugin {
    @Override
    public void register(@Nonnull IModRegistry registry) {
        registry.addAdvancedGuiHandlers(new IAdvancedGuiHandler<GuiNetworkHub>() {
            @Nonnull
            @Override
            public Class<GuiNetworkHub> getGuiContainerClass() {
                return GuiNetworkHub.class;
            }
            @Override
            public List<Rectangle> getGuiExtraAreas(@Nonnull GuiNetworkHub guiContainer) {
                return guiContainer.getExtraAreas();
            }
        });
    }
}
