package com.xinyihl.ymadditions.client.gui;

import com.mojang.authlib.GameProfile;
import com.xinyihl.ymadditions.Tags;
import com.xinyihl.ymadditions.YMAdditions;
import com.xinyihl.ymadditions.client.api.IListItem;
import com.xinyihl.ymadditions.client.api.IShowObject;
import com.xinyihl.ymadditions.client.component.ListCtrl;
import com.xinyihl.ymadditions.client.control.IconButton;
import com.xinyihl.ymadditions.client.control.ListItem;
import com.xinyihl.ymadditions.common.container.ContainerNetworkHub;
import com.xinyihl.ymadditions.common.data.NetworkStatus;
import com.xinyihl.ymadditions.common.network.PacketClientToServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.xinyihl.ymadditions.common.network.PacketClientToServer.ClientToServer.BUTTON_ACTION;

public class GuiNetworkHubUser extends GuiContainer {
    private final ContainerNetworkHub containerNetworkHub;

    private ListCtrl<User> listCtrl;
    private IconButton netScreen;
    //private IconButton userScreen;

    public GuiNetworkHubUser(ContainerNetworkHub containerNetworkHub) {
        super(containerNetworkHub);
        this.containerNetworkHub = containerNetworkHub;
        this.xSize = 200;
        this.ySize = 159;
    }

    public List<Rectangle> getExtraAreas() {
        List<Rectangle> extraAreas = new ArrayList<>();
        extraAreas.add(new Rectangle(this.netScreen.x, this.netScreen.y, this.netScreen.width, this.netScreen.height));
        return extraAreas;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.netScreen = new IconButton(99998, guiLeft + 201, guiTop + 5, 40, 146);
        //this.userScreen = new IconButton(99999, guiLeft + 20, guiTop - 20, 60, 146);

        //this.userScreen.setSelected(true);

        this.buttonList.add(this.netScreen);
        //this.buttonList.add(this.userScreen);


        NetHandlerPlayClient nethandlerplayclient = this.mc.player.connection;
        this.listCtrl = new ListCtrl<User>(this.mc, guiLeft + 20, guiTop + 15, 165, 136, 20, nethandlerplayclient.getPlayerInfoMap().stream().map(n -> new User(n.getGameProfile(), containerNetworkHub.getSelectedNetwork())).collect(Collectors.toList())) {
            @Override
            protected IListItem<User> getItem(Object id, String text, User o, int x, int y, int width, int height) {
                return new ListItem<User>(id, text, o, x, y, width, height) {
                    @Override
                    public void draw(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
                        int i = 1;
                        if (get().getPerm() == 0) {
                            i = 4;
                        }
                        if (get().getPerm() == 1) {
                            i = 5;
                        }
                        if (get().getPerm() == 2) {
                            i = 5;
                        }
                        if (isMouseOver(mouseX, mouseY)) {
                            i = 2;
                        }
                        mc.getTextureManager().bindTexture(new ResourceLocation(Tags.MOD_ID, "textures/gui/widgets.png"));
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        this.drawTexturedModalRect(this.x, this.y + 3, 0, i * 14, this.width / 2, 14);
                        this.drawTexturedModalRect(this.x + this.width / 2, this.y + 3, 200 - this.width / 2, i * 14, this.width / 2, 14);
                        this.drawCenteredString(mc.fontRenderer, this.text, this.x + this.width / 2, this.y + (this.height - 8) / 2, 0xFFFFFFFF);
                    }

                    @Override
                    public List<String> getTooltip() {
                        List<String> tooltip = new ArrayList<>();
                        if (get().getPerm() == -1) {
                            tooltip.add("gui.ymadditions.network_hub.user.user");
                        }
                        if (get().getPerm() == 0) {
                            tooltip.add("gui.ymadditions.network_hub.user.user");
                        }
                        if (get().getPerm() == 1) {
                            tooltip.add("gui.ymadditions.network_hub.user.admin");
                        }
                        if (get().getPerm() == 2) {
                            tooltip.add("gui.ymadditions.network_hub.user.owner");
                        }
                        tooltip.add("gui.ymadditions.network_hub.user.tooltip");
                        return tooltip;
                    }

                    @Override
                    public void click() {
                        NBTTagCompound tag = new NBTTagCompound();
                        tag.setInteger("button", 2);
                        tag.setUniqueId("user", get().getId());
                        if(Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_LSHIFT) {
                            tag.setBoolean("isShifting", true);
                        }
                        YMAdditions.instance.networkWrapper.sendToServer(new PacketClientToServer(BUTTON_ACTION, tag));
                    }
                };
            }
        };


        this.listCtrl.setScroll(true);
        this.listCtrl.refresh();

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();


        boolean isSelected = !containerNetworkHub.selectedNetwork.equals(new UUID(0, 0));
        if (isSelected) {
            this.listCtrl.draw(mouseX, mouseY, partialTicks);
        } else {
            this.drawCenteredString(mc.fontRenderer, I18n.format("gui.ymadditions.network_hub.user.no_selected"), guiLeft + xSize / 2, 100, 0xFFFFFFFF);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        this.mc.getTextureManager().bindTexture(new ResourceLocation(Tags.MOD_ID, "textures/gui/background.png"));
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

//        this.mc.getTextureManager().bindTexture(new ResourceLocation(Tags.MOD_ID, "textures/gui/network_hub_user.png"));
//        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }


    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.drawCenteredString(mc.fontRenderer, I18n.format("tile.ymadditions.network_hub.name"), xSize / 2, 4, 0xFFFFFFFF);
    }

    @Override
    protected void actionPerformed(@Nonnull GuiButton ba) {
        if (netScreen.id == ba.id) {
            this.mc.displayGuiScreen(new GuiNetworkHubCore(containerNetworkHub));
        }
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

    private static class User extends NetworkPlayerInfo implements IShowObject {

        private final NetworkStatus net;

        public User(GameProfile profile, NetworkStatus net) {
            super(profile);
            this.net = net;
        }

        public int getPerm() {
            Integer i = net.getUserPrem(getGameProfile().getId());
            if (net.getOwner().equals(getGameProfile().getId())) {
                return 2;
            }
            if (i == null) {
                return -1;
            }
            return i;
        }

        @Override
        public UUID getId() {
            return getGameProfile().getId();
        }

        @Override
        public String getText() {
            return getGameProfile().getName();
        }
    }
}
