package com.xinyihl.ymadditions.common.integration.jei;

import com.xinyihl.ymadditions.client.gui.NetworkHubGuiContainer;
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
        registry.addAdvancedGuiHandlers(new IAdvancedGuiHandler<NetworkHubGuiContainer>() {
            @Nonnull
            @Override
            public Class<NetworkHubGuiContainer> getGuiContainerClass() {
                return NetworkHubGuiContainer.class;
            }
            @Override
            public List<Rectangle> getGuiExtraAreas(@Nonnull NetworkHubGuiContainer guiContainer) {
                return guiContainer.getExtraAreas();
            }
        });
    }
}
