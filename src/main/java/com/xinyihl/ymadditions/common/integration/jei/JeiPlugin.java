package com.xinyihl.ymadditions.common.integration.jei;

import com.xinyihl.ymadditions.client.gui.GuiNetworkHubCore;
import com.xinyihl.ymadditions.client.gui.GuiNetworkHubUser;
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
        registry.addAdvancedGuiHandlers(new IAdvancedGuiHandler<GuiNetworkHubCore>() {
            @Nonnull
            @Override
            public Class<GuiNetworkHubCore> getGuiContainerClass() {
                return GuiNetworkHubCore.class;
            }
            @Override
            public List<Rectangle> getGuiExtraAreas(@Nonnull GuiNetworkHubCore guiContainer) {
                return guiContainer.getExtraAreas();
            }
        });
        registry.addAdvancedGuiHandlers(new IAdvancedGuiHandler<GuiNetworkHubUser>() {
            @Nonnull
            @Override
            public Class<GuiNetworkHubUser> getGuiContainerClass() {
                return GuiNetworkHubUser.class;
            }
            @Override
            public List<Rectangle> getGuiExtraAreas(@Nonnull GuiNetworkHubUser guiContainer) {
                return guiContainer.getExtraAreas();
            }
        });
    }
}
