package com.xinyihl.ymadditions.common.title;

import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridConnection;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.core.AEConfig;
import com.xinyihl.ymadditions.Configurations;
import com.xinyihl.ymadditions.YMAdditions;
import com.xinyihl.ymadditions.common.api.data.NetworkHubDataStorage;
import com.xinyihl.ymadditions.common.api.data.NetworkStatus;
import com.xinyihl.ymadditions.common.network.PacketServerToClient;
import com.xinyihl.ymadditions.common.registry.BlocksAndItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.xinyihl.ymadditions.common.network.PacketServerToClient.ServerToClient.DELETE_NETWORK;

public class TileNetworkHub extends TitleMeBase implements ITickable {
    private boolean isHead = false;
    private UUID networkUuid = new UUID(0, 0);
    private UUID owner = new UUID(0, 0);
    private boolean isConnected = false;
    //无需同步&保存
    private IGridConnection connection;
    private int tickCounter = 0;
    private int lastSurplusChannels;

    public TileNetworkHub() {
        super();
        this.proxy.setFlags(GridFlags.DENSE_CAPACITY);
    }

    @Override
    public ItemStack getVisualItemStack() {
        return new ItemStack(BlocksAndItems.itemNetworkHub);
    }

    @Nonnull
    @Override
    public AECableType getCableConnectionType(@Nonnull AEPartLocation aePartLocation) {
        return AECableType.DENSE_SMART;
    }

    @Override
    public void setOwner(EntityPlayer placer) {
        super.setOwner(placer);
        this.owner = placer.getGameProfile().getId();
    }

    @Override
    public void onLoad() {
        this.setConnected(this.connection != null);
    }

    @Override
    public void update() {
        if (this.world.isRemote) return;
        this.tickCounter = (this.tickCounter + 1) % 20;
        if (this.tickCounter % 20 == 0) {
            if (!this.networkUuid.equals(new UUID(0, 0))) {
                NetworkHubDataStorage storage = NetworkHubDataStorage.get(this.world);
                NetworkStatus network = storage.getNetwork(this.networkUuid);
                if (network == null) {
                    this.unsetAll();
                    return;
                }
                if (this.isHead) {
                    this.setConnected(!network.getTargetPos().isEmpty());
                    this.getProxy().setIdlePowerUsage(Configurations.GENERAL_CONFIG.powerHeadBase * network.getTargetPos().size());
                    int howMany = 0;
                    for(IGridConnection gc : this.getActionableNode().getConnections()) {
                        howMany = Math.max(gc.getUsedChannels(), howMany);
                    }
                    int surplusChannels = Math.max(AEConfig.instance().getDenseChannelCapacity() - howMany, 0);
                    if (this.lastSurplusChannels != surplusChannels) {
                        this.lastSurplusChannels = surplusChannels;
                        network.setSurplusChannels(surplusChannels);
                        network.setNeedTellClient(true);
                    }
                } else {
                    if (this.getPos().equals(network.getPos())) {
                        this.setHead(true);
                        this.sync();
                    } else {
                        if (!this.isConnected) {
                            this.setupConnection(network);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.isHead = tag.getBoolean("isHead");
        this.networkUuid = tag.getUniqueId("networkUuid");
        this.owner = tag.getUniqueId("owner");
        this.isConnected = tag.getBoolean("isConnected");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("isHead", this.isHead);
        tag.setUniqueId("networkUuid", this.networkUuid);
        tag.setUniqueId("owner", this.owner);
        tag.setBoolean("isConnected", this.isConnected);
        return tag;
    }

    @Override
    public void addProbeInfo(Consumer<String> consumer, Function<String, String> loc) {
        super.addProbeInfo(consumer, loc);
        consumer.accept(loc.apply("tile_network_hub.state." + this.isConnected()));
        if (Configurations.GENERAL_CONFIG.doNetworkUUIDShow){
            consumer.accept(loc.apply("tile_network_hub.network") + " " + this.getNetworkUuid().toString());
        }
    }

    public void setupConnection(NetworkStatus network) {
        if (this.world.isRemote) return;
        BlockPos pos = network.getPos();
        TileEntity tile = this.world.getTileEntity(pos);
        if (!(tile instanceof TileNetworkHub)) {
            NetworkHubDataStorage.get(this.world).removeNetwork(this.networkUuid);
            return;
        }
        TileNetworkHub that = (TileNetworkHub) tile;
        int dx = this.getPos().getX() - that.getPos().getX();
        int dy = this.getPos().getY() - that.getPos().getY();
        int dz = this.getPos().getZ() - that.getPos().getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double power = Configurations.GENERAL_CONFIG.powerBase + Configurations.GENERAL_CONFIG.powerDistanceMultiplier * dist * Math.log(dist * dist + 3);
        try {
            this.connection = AEApi.instance().grid().createGridConnection(this.getActionableNode(), that.getActionableNode());
            this.setConnected(true);
            this.getProxy().setIdlePowerUsage(power);
            network.addTargetPos(this.getPos());
        } catch (FailedConnectionException e) {
            this.unsetAll();
        }
    }

    public void breakConnection() {
        if (this.world.isRemote) return;
        NetworkHubDataStorage storage = NetworkHubDataStorage.get(this.world);
        NetworkStatus network = storage.getNetwork(this.networkUuid);
        if (network == null) {
            this.unsetAll();
            return;
        }
        if (this.isHead) {
            for (BlockPos pos : network.getTargetPos()) {
                TileEntity tile = this.world.getTileEntity(pos);
                if (tile instanceof TileNetworkHub) {
                    ((TileNetworkHub) tile).unsetAll();
                }
            }
            storage.removeNetwork(this.networkUuid);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setUniqueId("networkUuid", this.networkUuid);
            YMAdditions.instance.networkWrapper.sendToAll(new PacketServerToClient(DELETE_NETWORK, tag));
        } else {
            network.removeTargetPos(this.getPos());
        }
        storage.markDirty();
        this.unsetAll();
    }

    public void unsetAll() {
        this.setHead(false);
        this.setConnected(false);
        this.setNetworkUuid(new UUID(0, 0));
        if (this.connection != null) {
            this.connection.destroy();
            this.connection = null;
        }
        this.getProxy().setIdlePowerUsage(100.0D);
        this.sync();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        this.breakConnection();
    }

    public boolean isConnected() {
        return this.isConnected;
    }

    public void setConnected(boolean connected) {
        this.isConnected = connected;
    }

    public boolean isHead() {
        return this.isHead;
    }

    public void setHead(boolean head) {
        this.isHead = head;
    }

    public UUID getOwner() {
        return owner;
    }

    public UUID getNetworkUuid() {
        return this.networkUuid;
    }

    public void setNetworkUuid(UUID networkUuid) {
        this.networkUuid = networkUuid;
    }
}
