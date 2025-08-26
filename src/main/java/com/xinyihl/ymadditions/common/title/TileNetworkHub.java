package com.xinyihl.ymadditions.common.title;

import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridConnection;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.core.AEConfig;
import com.xinyihl.ymadditions.Configurations;
import com.xinyihl.ymadditions.api.entity.Network;
import com.xinyihl.ymadditions.common.data.DataStorage;
import com.xinyihl.ymadditions.common.integration.crt.NetHubPowerUsage;
import com.xinyihl.ymadditions.common.registry.Registry;
import com.xinyihl.ymadditions.common.title.base.TileMeBase;
import com.xinyihl.ymadditions.common.utils.BlockPosDim;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.minecraftforge.common.util.Constants.BlockFlags.RERENDER_MAIN_THREAD;

public class TileNetworkHub extends TileMeBase implements ITickable {
    private boolean isHead = false;
    private UUID networkUuid = null;
    private boolean isConnected = false;
    private double power = Configurations.GENERAL_CONFIG.powerBase;
    private IGridConnection connection = null;
    private int tickCounter = 0;
    private Integer surplusChannels;
    private long lastWorldTime = -1;

    public TileNetworkHub() {
        super();
        this.proxy.setFlags(GridFlags.DENSE_CAPACITY);
    }

    @Override
    public ItemStack getVisualItemStack() {
        return new ItemStack(Registry.itemNetworkHub);
    }

    @Nonnull
    @Override
    public AECableType getCableConnectionType(@Nonnull AEPartLocation aePartLocation) {
        return AECableType.DENSE_SMART;
    }

    @Override
    public void update() {
        if (this.world.isRemote) return;
        long totalWorldTime = this.world.getTotalWorldTime();
        if (this.lastWorldTime != totalWorldTime) {
            this.lastWorldTime = totalWorldTime;
            this.tickCounter = (this.tickCounter + 1) % 20;
            if (this.tickCounter % 20 == 0) {
                this.onTick();
                this.sync();
            }
        }
    }

    private void onTick(){
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
                for (IGridConnection gc : this.getActionableNode().getConnections()) {
                    howMany = Math.max(gc.getUsedChannels(), howMany);
                }
                this.surplusChannels = Math.max(AEConfig.instance().getDenseChannelCapacity() - howMany, 0);
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
        tag.setBoolean("isHead", this.isHead);
        if (this.networkUuid != null) tag.setUniqueId("networkUuid", this.networkUuid);
        tag.setBoolean("isConnected", this.isConnected);
        tag.setDouble("power", this.power);
        return tag;
    }

    @Override
    public void doSyncFrom(NBTTagCompound tag) {
        this.isHead = tag.getBoolean("isHead");
        if (tag.hasUniqueId("networkUuid")) this.networkUuid = tag.getUniqueId("networkUuid");
        this.isConnected = tag.getBoolean("isConnected");
        this.power = tag.getDouble("power");
        if (this.world.isRemote) {
            IBlockState state = this.world.getBlockState(this.getPos());
            this.world.notifyBlockUpdate(this.pos, state, state, RERENDER_MAIN_THREAD);
        }
    }

    @Override
    public void addProbeInfo(Consumer<String> consumer, Function<String, String> loc) {
        super.addProbeInfo(consumer, loc);
        consumer.accept(loc.apply("tile_network_hub.state." + this.isConnected()));
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
        if (!(tile instanceof TileNetworkHub)) {
            DataStorage.get(thatWorld).removeNetwork(this.networkUuid);
            return;
        }
        TileNetworkHub that = (TileNetworkHub) tile;
        power = NetHubPowerUsage.calcNetHubPowerUsage(this.getPos(), that.getPos(), this.world.provider.getDimension(), thatWorld.provider.getDimension());
        try {
            this.connection = AEApi.instance().grid().createGridConnection(this.getActionableNode(), that.getActionableNode());
            this.setConnected(true);
            that.setConnected(true);
            this.getProxy().setIdlePowerUsage(power);
            network.addReceivePos(new BlockPosDim(this.getPos(), this.world.provider.getDimension()));
            this.sync();
            that.sync();
        } catch (FailedConnectionException e) {
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
        this.getProxy().setIdlePowerUsage(0);
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
}
