package com.xinyihl.ymadditions.common.title;

import ae2.api.networking.GridHelper;
import ae2.api.networking.GridFlags;
import ae2.api.networking.IGridNode;
import ae2.api.networking.IGridConnection;
import ae2.api.util.AECableType;
import ae2.core.AEConfig;
import com.xinyihl.ymadditions.Configurations;
import com.xinyihl.ymadditions.api.entity.Network;
import com.xinyihl.ymadditions.common.data.DataStorage;
import com.xinyihl.ymadditions.common.integration.crt.NetHubPowerUsage;
import com.xinyihl.ymadditions.common.registry.Registry;
import com.xinyihl.ymadditions.common.title.base.TileMeBase;
import com.xinyihl.ymadditions.common.utils.BlockPosDim;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class TileNetworkHub extends TileMeBase {
    private boolean isHead = false;
    private UUID networkUuid = null;
    private boolean isConnected = false;
    private double power = Configurations.GENERAL_CONFIG.powerBase;
    private Integer surplusChannels;
    private IGridConnection connection = null;

    public TileNetworkHub() {
        this.mainNode.setFlags(GridFlags.DENSE_CAPACITY);
    }

    @Override
    public ItemStack getVisualItemStack() {
        return Registry.itemNetworkHub == null ? ItemStack.EMPTY : new ItemStack(Registry.itemNetworkHub);
    }

    @Nonnull
    @Override
    public AECableType getCableConnectionType(EnumFacing side) {
        return AECableType.DENSE_SMART;
    }

    protected void onTick(){
        if (this.networkUuid != null) {
            DataStorage storage = DataStorage.get(this.world);
            Network network = storage.getNetwork(this.networkUuid);
            if (network == null) {
                this.unsetAll();
                return;
            }

            BlockPosDim pos = network.getSendPos();
            if (!this.isHead && pos!= null && this.getPos().equals(pos.toBlockPos())) {
                this.setHead(true);
            }

            if (this.isHead) {
                if (this.isConnected) {
                    this.setConnected(!network.getReceivePos().isEmpty());
                }
                int howMany = 0;
                IGridNode node = this.getActionableNode();
                if (node != null) {
                    for (IGridConnection gc : node.getConnections()) {
                        howMany = Math.max(gc.getUsedChannels(), howMany);
                    }
                }
                this.surplusChannels = Math.max(this.getDenseChannelCapacity() - howMany, 0);
            }

            if (!this.isHead && this.connection == null) {
                this.setupConnection(network);
            }
        }
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasUniqueId("networkUuid")) this.networkUuid = tag.getUniqueId("networkUuid");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound tag) {
        super.writeToNBT(tag);
        if (this.networkUuid != null) tag.setUniqueId("networkUuid", this.networkUuid);
        return tag;
    }

    @Override
    public NBTTagCompound getSyncData(NBTTagCompound tag) {
        if (this.networkUuid != null) tag.setUniqueId("networkUuid", this.networkUuid);
        tag.setBoolean("isConnected", this.isConnected);
        tag.setBoolean("isHead", this.isHead);
        tag.setDouble("power", this.power);
        return tag;
    }

    @Override
    public void doSyncFrom(NBTTagCompound tag) {
        if (tag.hasUniqueId("networkUuid")) this.networkUuid = tag.getUniqueId("networkUuid");
        this.isConnected = tag.getBoolean("isConnected");
        this.isHead = tag.getBoolean("isHead");
        this.power = tag.getDouble("power");
    }

    @Override
    public void addProbeInfo(Consumer<String> consumer, Function<String, String> loc) {
        super.addProbeInfo(consumer, loc);
        consumer.accept(loc.apply("tile_network_hub.state." + this.isConnected()));
        int usedChannels = 0;
        if (this.isHead()) {
            usedChannels = this.getSurplusChannels();
        } else if (this.networkUuid != null) {
            DataStorage storage = DataStorage.get(this.world);
            Network network = storage.getNetwork(this.networkUuid);
            BlockPosDim pos = null;
            if (network != null) {
                pos = network.getSendPos();
            }
            if (pos != null) {
                World thatWorld = DimensionManager.getWorld(pos.getDimension());
                if (thatWorld != null && thatWorld.isBlockLoaded(pos.toBlockPos())) {
                    TileEntity tile = thatWorld.getTileEntity(pos.toBlockPos());
                    if (tile instanceof TileNetworkHub that) {
                        usedChannels = that.getSurplusChannels();
                    }
                }
            }
        }
        consumer.accept(loc.apply("tile_network_hub.channels") + " " + usedChannels);

        if (Configurations.GENERAL_CONFIG.doNetworkUUIDShow) {
            UUID uuid = this.getNetworkUuid();
            consumer.accept(loc.apply("tile_network_hub.network") + " " + (uuid == null ? "Unknown" : uuid.toString()));
        }
    }

    public void setupConnection(Network network) {
        if (this.world.isRemote) return;
        BlockPosDim pos = network.getSendPos();
        if (pos == null) return;
        World thatWorld = DimensionManager.getWorld(pos.getDimension());
        if (thatWorld == null || !thatWorld.isBlockLoaded(pos.toBlockPos())) return;
        TileEntity tile = thatWorld.getTileEntity(pos.toBlockPos());
        if (!(tile instanceof TileNetworkHub that)) {
            DataStorage.get(thatWorld).removeNetwork(this.networkUuid);
            return;
        }
        power = NetHubPowerUsage.calcNetHubPowerUsage(this.getPos(), that.getPos(), this.world.provider.getDimension(), thatWorld.provider.getDimension());
        IGridNode thisNode = this.getActionableNode();
        IGridNode thatNode = that.getActionableNode();
        if (thisNode == null || thatNode == null) return;
        try {
            this.connection = GridHelper.createConnection(thisNode, thatNode);
            this.setConnected(true);
            that.setConnected(true);
            this.mainNode.setIdlePowerUsage(power);
            network.addReceivePos(new BlockPosDim(this.getPos(), this.world.provider.getDimension()));
            this.sync();
            that.sync();
        } catch (RuntimeException e) {
            this.unsetAll();
        }
    }

    public void breakConnection() {
        if (this.world.isRemote) return;
        DataStorage storage = DataStorage.get(this.world);
        Network network = storage.getNetwork(this.networkUuid);
        if (network == null) {
            this.unsetAll();
            return;
        }
        if (this.isHead) {
            for (BlockPosDim pos : new HashSet<>(network.getReceivePos())) {
                World thatWorld = DimensionManager.getWorld(pos.getDimension());
                TileEntity tile = thatWorld.getTileEntity(pos.toBlockPos());
                if (tile instanceof TileNetworkHub) {
                    ((TileNetworkHub) tile).breakConnection();
                    ((TileNetworkHub) tile).sync();
                }
            }
            storage.removeNetwork(this.networkUuid);
        } else {
            network.removeReceivePos(new BlockPosDim(this.getPos(), this.world.provider.getDimension()));
        }
        storage.markDirty();
        this.unsetAll();
    }

    public void unsetAll() {
        this.setHead(false);
        this.setConnected(false);
        this.setNetworkUuid(null);
        if (this.connection != null) {
            this.connection.destroy();
            this.connection = null;
        }
        this.mainNode.setIdlePowerUsage(0);
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        DataStorage storage = DataStorage.get(this.world);
        Network network = storage.getNetwork(this.networkUuid);
        if (network == null) {
            this.unsetAll();
            return;
        }
        if (this.connection != null) {
            this.connection.destroy();
            this.connection = null;
        }
        this.setConnected(false);
        network.removeReceivePos(new BlockPosDim(this.getPos(), this.world.provider.getDimension()));
        storage.markDirty();
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

    public double getPower() {
        return power;
    }

    @Nullable
    public UUID getNetworkUuid() {
        return this.networkUuid;
    }

    public void setNetworkUuid(UUID networkUuid) {
        this.networkUuid = networkUuid;
    }

    public Integer getSurplusChannels() {
        return surplusChannels;
    }

    private int getDenseChannelCapacity() {
        return 32 * AEConfig.instance().getChannelMode().getCableCapacityFactor();
    }
}
