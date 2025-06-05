package com.xinyihl.ymadditions.client.gui;

import com.xinyihl.ymadditions.Tags;
import com.xinyihl.ymadditions.YMAdditions;
import com.xinyihl.ymadditions.client.api.IListItem;
import com.xinyihl.ymadditions.client.component.ListCtrl;
import com.xinyihl.ymadditions.client.control.ListItem;
import com.xinyihl.ymadditions.client.control.MyButton;
import com.xinyihl.ymadditions.client.control.MyLockIconButton;
import com.xinyihl.ymadditions.common.api.BlockPosDim;
import com.xinyihl.ymadditions.common.api.NetworkStatus;
import com.xinyihl.ymadditions.common.container.ContainerNetworkHub;
import com.xinyihl.ymadditions.common.network.PacketClientToServer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLockIconButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.xinyihl.ymadditions.common.network.PacketClientToServer.ClientToServer.BUTTON_ACTION;

/* 由魔法数字组成的 GUI （；´д｀）ゞ */
@SideOnly(Side.CLIENT)
public class GuiNetworkHub extends GuiContainer {
    private static String searchNet = "";
    private final ContainerNetworkHub containerNetworkHub;
    private ListCtrl<NetworkStatus> listCtrl;
    private GuiTextField searchField;
    private GuiLockIconButton lockButton;
    private GuiButton createButton;
    private GuiButton deleteButton;
    private GuiButton connectButton;
    private GuiButton disConnectButton;
    private GuiTextField textField;
    private boolean isCreating = false;

    public GuiNetworkHub(ContainerNetworkHub containerNetworkHub) {
        super(containerNetworkHub);
        this.containerNetworkHub = containerNetworkHub;
        this.xSize = 200;
        this.ySize = 159;
    }

    public List<Rectangle> getExtraAreas() {
        List<Rectangle> extraAreas = new ArrayList<>();
        extraAreas.add(new Rectangle(this.lockButton.x, this.lockButton.y, this.lockButton.width, this.lockButton.height));
        return extraAreas;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.listCtrl = new ListCtrl<NetworkStatus>(this.mc, guiLeft + 7, guiTop + 35, 86, 116, 20, this.containerNetworkHub.networks.values()) {
            @Override
            protected IListItem<NetworkStatus> getItem(Object id, String text, NetworkStatus o, int x, int y, int width, int height) {
                return new ListItem<NetworkStatus>(id, text, o, x, y, width, height) {
                    @Override
                    public void click() {
                        NBTTagCompound tag = new NBTTagCompound();
                        tag.setInteger("button", 0);
                        tag.setUniqueId("networkUuid", get().getUuid());
                        YMAdditions.instance.networkWrapper.sendToServer(new PacketClientToServer(BUTTON_ACTION, tag));
                    }
                };
            }
        };
        this.listCtrl.setScroll(true);
        this.listCtrl.setIsSelected(true);
        this.listCtrl.setFilter(searchNet);
        this.listCtrl.setSelected(containerNetworkHub.selectedNetwork);
        this.listCtrl.refresh();

        this.searchField = new GuiTextField(1001, this.fontRenderer, guiLeft + 9, guiTop + 17, 86, 11);
        this.searchField.setVisible(true);
        this.searchField.setMaxStringLength(10);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setTextColor(16777215);
        this.searchField.setText(searchNet);
        this.searchField.setFocused(false);

        this.createButton = new MyButton(995, guiLeft + 106, guiTop + 113, 32, 11, I18n.format("gui.ymadditions.network_hub.button.create"));
        this.deleteButton = new MyButton(996, guiLeft + 106, guiTop + 133, 32, 11, I18n.format("gui.ymadditions.network_hub.button.delete"));
        this.connectButton = new MyButton(997, guiLeft + 154, guiTop + 113, 32, 11, I18n.format("gui.ymadditions.network_hub.button.connect"));
        this.disConnectButton = new MyButton(998, guiLeft + 154, guiTop + 133, 32, 11, I18n.format("gui.ymadditions.network_hub.button.disconnect"));
        this.lockButton = new MyLockIconButton(999, guiLeft + 201, guiTop + 12);
        this.textField = new GuiTextField(1000, this.fontRenderer, guiLeft + 108, guiTop + 113, 29, 11);

        if (this.containerNetworkHub.networkHub.isConnected()) {
            this.createButton.enabled = false;
        }

        if (this.containerNetworkHub.networkHub.isHead()) {
            this.createButton.enabled = false;
            this.connectButton.enabled = false;
            this.disConnectButton.enabled = false;
        }

        this.lockButton.setLocked(!this.selected().isPublic());

        this.textField.setVisible(false);
        this.textField.setMaxStringLength(10);
        this.textField.setEnableBackgroundDrawing(false);

        this.buttonList.add(this.createButton);
        this.buttonList.add(this.deleteButton);
        this.buttonList.add(this.connectButton);
        this.buttonList.add(this.disConnectButton);
        this.buttonList.add(this.lockButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        boolean isHead = this.containerNetworkHub.networkHub.isHead();
        this.lockButton.setLocked(!selected().isPublic());
        this.createButton.enabled = !isHead && !this.containerNetworkHub.networkHub.isConnected();
        this.disConnectButton.enabled = !isHead;
        this.connectButton.enabled = !isHead;

        this.listCtrl.draw(mouseX, mouseY, partialTicks);

        this.textField.drawTextBox();
        this.searchField.drawTextBox();

        if (this.isMouseOverButton(lockButton, mouseX, mouseY)) {
            this.drawHoveringText(I18n.format("gui.ymadditions.network_hub.button.public.desc"), mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        this.mc.getTextureManager().bindTexture(new ResourceLocation(Tags.MOD_ID, "textures/gui/test.png"));
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private NetworkStatus selected() {
        return containerNetworkHub.networks.getOrDefault(containerNetworkHub.selectedNetwork, new NetworkStatus(new UUID(0, 0), "Unknown", true, new BlockPosDim(0, 0, 0, 0)));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        int rightPanelX = 103;
        int rightPanelY = 19;
        this.drawCenteredString(mc.fontRenderer, I18n.format("tile.ymadditions.network_hub.name"), xSize / 2, 4, 0xFFFFFFFF);
        this.fontRenderer.drawString(I18n.format("gui.ymadditions.network_hub.info.network_name") + " " + this.selected().getNetworkName(), rightPanelX, rightPanelY, 0xFFFFFF);
        this.fontRenderer.drawString(I18n.format("gui.ymadditions.network_hub.info.surplus_channels") + " " + this.selected().getSurplusChannels(), rightPanelX, rightPanelY + 15, 0xFFFFFF);
        this.fontRenderer.drawString(I18n.format("gui.ymadditions.network_hub.info.dimension_id") + " " + this.selected().getPos().getDimension(), rightPanelX, rightPanelY + 30, 0xFFFFFF);
        this.fontRenderer.drawString(I18n.format("gui.ymadditions.network_hub.info.public." + this.selected().isPublic()), rightPanelX, rightPanelY + 45, 0xFFFFFF);
        this.fontRenderer.drawString(I18n.format("gui.ymadditions.network_hub.info.state." + containerNetworkHub.networkHub.isConnected()), rightPanelX, rightPanelY + 60, 0xFFFFFF);
    }

    @Override
    protected void actionPerformed(@Nonnull GuiButton ba) {
        if (createButton.id == ba.id) {
            this.createButton.enabled = false;
            this.createButton.visible = false;
            this.isCreating = true;
            this.textField.setVisible(true);
            this.textField.setFocused(true);
            return;
        }

        if (containerNetworkHub.selectedNetwork.equals(new UUID(0, 0))) {
            return;
        }

        if (ba.id >= 900 && ba.id <= 999) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("button", ba.id);
            YMAdditions.instance.networkWrapper.sendToServer(new PacketClientToServer(BUTTON_ACTION, tag));
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (isCreating) {
            this.textField.textboxKeyTyped(typedChar, keyCode);
            if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
                this.connectButton.enabled = false;
                this.disConnectButton.enabled = false;
                this.createButton.visible = true;
                this.isCreating = false;
                this.textField.setVisible(false);

                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("button", 1);
                tag.setString("name", textField.getText());
                YMAdditions.instance.networkWrapper.sendToServer(new PacketClientToServer(BUTTON_ACTION, tag));
                this.textField.setText("");
            }
            return;
        }
        if (searchField.isFocused()) {
            if (this.searchField.textboxKeyTyped(typedChar, keyCode)) {
                searchNet = this.searchField.getText();
                this.listCtrl.setFilter(searchNet);
                this.listCtrl.setScrollOffset(0);
                this.listCtrl.refresh();
            }
            return;
        }
        super.keyTyped(typedChar, keyCode);
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
        if (isCreating && !isMouseOverTextField(this.textField, mouseX, mouseY)) {
            this.createButton.enabled = true;
            this.createButton.visible = true;
            this.isCreating = false;
            this.textField.setVisible(false);
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
        listCtrl.mouseClicked(mouseX, mouseY, mouseButton);
        this.searchField.setFocused(isMouseOverTextField(this.searchField, mouseX, mouseY));
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        listCtrl.mouseReleased(mouseX, mouseY, state);
    }

    private boolean isMouseOverTextField(GuiTextField textField, int mouseX, int mouseY) {
        return mouseX >= textField.x && mouseX < textField.x + textField.width && mouseY >= textField.y && mouseY < textField.y + textField.height;
    }

    private boolean isMouseOverButton(GuiButton button, int mouseX, int mouseY) {
        return mouseX >= button.x && mouseY >= button.y && mouseX < button.x + button.width && mouseY < button.y + button.height;
    }
}
