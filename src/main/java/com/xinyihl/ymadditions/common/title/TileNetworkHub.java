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
import com.xinyihl.ymadditions.common.data.DataStorage;
import com.xinyihl.ymadditions.common.data.NetworkStatus;
import com.xinyihl.ymadditions.common.integration.crt.NetHubPowerUsage;
import com.xinyihl.ymadditions.common.network.PacketServerToClient;
import com.xinyihl.ymadditions.common.registry.Registry;
import com.xinyihl.ymadditions.common.utils.BlockPosDim;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.xinyihl.ymadditions.common.network.PacketServerToClient.ServerToClient.DELETE_NETWORKS;

public class TileNetworkHub extends TitleMeBase implements ITickable {
    private boolean isHead = false;
    private UUID networkUuid = new UUID(0, 0);
    private boolean isConnected = false;
    private double power = Configurations.GENERAL_CONFIG.powerBase;
    //无需同步&保存
    private IGridConnection connection;
    private int tickCounter = 0;
    private int lastSurplusChannels;

    public TileNetworkHub() {
        super();
        this.proxy.setFlags(GridFlags.DENSE_CAPACITY, GridFlags.PREFERRED);
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
    public void onLoad() {
        this.setConnected(this.connection != null);
    }

    @Override
    public void update() {
        if (this.world.isRemote) return;
        this.tickCounter = (this.tickCounter + 1) % 20;
        if (this.tickCounter % 20 == 0) {
            if (!this.networkUuid.equals(new UUID(0, 0))) {

                DataStorage storage = DataStorage.get(this.world);
                NetworkStatus network = storage.getNetwork(this.networkUuid);
                if (network == null) {
                    this.unsetAll();
                    return;
                }

                if (!this.isHead && this.getPos().equals(network.getPos().toBlockPos())) {
                    this.setHead(true);
                }

                if (this.isHead) {
                    if(this.isConnected){
                        this.setConnected(!network.getTargetPos().isEmpty());
                    }
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
                }

                if (!this.isHead && this.connection == null) {
                    this.setupConnection(network);
                }

                this.sync();
            }
        }
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.isHead = tag.getBoolean("isHead");
        this.networkUuid = tag.getUniqueId("networkUuid");
        this.isConnected = tag.getBoolean("isConnected");
        this.power = tag.getDouble("power");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("isHead", this.isHead);
        tag.setUniqueId("networkUuid", this.networkUuid);
        tag.setBoolean("isConnected", this.isConnected);
        tag.setDouble("power", this.power);
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
        BlockPosDim pos = network.getPos();
        World thatWorld = DimensionManager.getWorld(pos.getDimension());
        if (!thatWorld.isBlockLoaded(pos.toBlockPos())) return;
        TileEntity tile = thatWorld.getTileEntity(pos.toBlockPos());
        if (!(tile instanceof TileNetworkHub)) {
            DataStorage.get(thatWorld).removeNetwork(this.networkUuid);
            return;
        }
        TileNetworkHub that = (TileNetworkHub) tile;
        if (NetHubPowerUsage.useA) {
            int dx = this.getPos().getX() - that.getPos().getX();
            int dy = this.getPos().getY() - that.getPos().getY();
            int dz = this.getPos().getZ() - that.getPos().getZ();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            power = NetHubPowerUsage.netHubPowerUsageA.apply(dist, thatWorld.provider.getDimension() != this.world.provider.getDimension());
        } else {
            power = NetHubPowerUsage.netHubPowerUsageB.apply(CraftTweakerMC.getIBlockPos(this.getPos()), CraftTweakerMC.getIBlockPos(that.getPos()), this.world.provider.getDimension(), thatWorld.provider.getDimension());
        }

        try {
            this.connection = AEApi.instance().grid().createGridConnection(this.getActionableNode(), that.getActionableNode());
            this.setConnected(true);
            that.setConnected(true);
            this.getProxy().setIdlePowerUsage(power);
            network.addTargetPos(new BlockPosDim(this.getPos(), this.world.provider.getDimension()));
        } catch (FailedConnectionException e) {
            this.unsetAll();
        }
    }

    public void breakConnection() {
        if (this.world.isRemote) return;
        DataStorage storage = DataStorage.get(this.world);
        NetworkStatus network = storage.getNetwork(this.networkUuid);
        if (network == null) {
            this.unsetAll();
            return;
        }
        if (this.isHead) {
            for (BlockPosDim pos : new HashSet<>(network.getTargetPos())) {
                World thatWorld = DimensionManager.getWorld(pos.getDimension());
                TileEntity tile = thatWorld.getTileEntity(pos.toBlockPos());
                if (tile instanceof TileNetworkHub) {
                    ((TileNetworkHub) tile).breakConnection();
                }
            }
            storage.removeNetwork(this.networkUuid);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setUniqueId("networkUuid", this.networkUuid);
            YMAdditions.instance.networkWrapper.sendToAll(new PacketServerToClient(DELETE_NETWORKS, tag));
        } else {
            network.removeTargetPos(new BlockPosDim(this.getPos(), this.world.provider.getDimension()));
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
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        DataStorage storage = DataStorage.get(this.world);
        NetworkStatus network = storage.getNetwork(this.networkUuid);
        if (network == null) {
            this.unsetAll();
            return;
        }
        network.removeTargetPos(new BlockPosDim(this.getPos(), this.world.provider.getDimension()));
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

    public UUID getNetworkUuid() {
        return this.networkUuid;
    }

    public void setNetworkUuid(UUID networkUuid) {
        this.networkUuid = networkUuid;
    }
}
