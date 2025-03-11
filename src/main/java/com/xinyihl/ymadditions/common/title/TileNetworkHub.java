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
import com.xinyihl.ymadditions.common.api.data.BlockPosDim;
import com.xinyihl.ymadditions.common.api.data.NetworkHubDataStorage;
import com.xinyihl.ymadditions.common.api.data.NetworkStatus;
import com.xinyihl.ymadditions.common.integration.crt.NetHubPowerUsage;
import com.xinyihl.ymadditions.common.network.PacketServerToClient;
import com.xinyihl.ymadditions.common.registry.BlocksAndItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.xinyihl.ymadditions.common.block.BlockNetworkHub.CONNECT;
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
                    if(this.isConnected){
                        double power = 0.0D;
                        for (BlockPosDim pos : network.getTargetPos()) {
                            int dx = this.getPos().getX() - pos.getX();
                            int dy = this.getPos().getY() - pos.getY();
                            int dz = this.getPos().getZ() - pos.getZ();
                            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                            power += NetHubPowerUsage.netHubPowerUsage.apply(dist, pos.getDimension() != this.world.provider.getDimension());
                        }
                        this.getProxy().setIdlePowerUsage(power);

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
                        this.setConnected(!network.getTargetPos().isEmpty());
                    }
                } else {
                    if (this.getPos().equals(network.getPos())) {
                        this.setHead(true);
                    } else {
                        if (!this.isConnected) {
                            this.setupConnection(network);
                        }
                    }
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
        BlockPosDim pos = network.getPos();
        World thatWorld = DimensionManager.getWorld(pos.getDimension());
        TileEntity tile = thatWorld.getTileEntity(new BlockPos(pos));
        if (!(tile instanceof TileNetworkHub)) {
            NetworkHubDataStorage.get(thatWorld).removeNetwork(this.networkUuid);
            return;
        }
        TileNetworkHub that = (TileNetworkHub) tile;
        int dx = this.getPos().getX() - that.getPos().getX();
        int dy = this.getPos().getY() - that.getPos().getY();
        int dz = this.getPos().getZ() - that.getPos().getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double power = NetHubPowerUsage.netHubPowerUsage.apply(dist, thatWorld.provider.getDimension() != this.world.provider.getDimension());
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
        NetworkHubDataStorage storage = NetworkHubDataStorage.get(this.world);
        NetworkStatus network = storage.getNetwork(this.networkUuid);
        if (network == null) {
            this.unsetAll();
            return;
        }
        if (this.isHead) {
            for (BlockPosDim pos : network.getTargetPos()) {
                World thatWorld = DimensionManager.getWorld(pos.getDimension());
                TileEntity tile = thatWorld.getTileEntity(new BlockPos(pos));
                if (tile instanceof TileNetworkHub) {
                    ((TileNetworkHub) tile).breakConnection();
                }
            }
            storage.removeNetwork(this.networkUuid);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setUniqueId("networkUuid", this.networkUuid);
            YMAdditions.instance.networkWrapper.sendToAll(new PacketServerToClient(DELETE_NETWORK, tag));
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
        this.sync();
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        NetworkHubDataStorage storage = NetworkHubDataStorage.get(this.world);
        NetworkStatus network = storage.getNetwork(this.networkUuid);
        if (network == null) {
            this.unsetAll();
            return;
        }
        network.removeTargetPos(new BlockPosDim(this.getPos(), this.world.provider.getDimension()));
        storage.markDirty();
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
        if (connected != this.isConnected) {
            this.isConnected = connected;
            if(this.world.isBlockLoaded(this.getPos())) {
                IBlockState state = this.world.getBlockState(this.getPos());
                if(state.getBlock() == BlocksAndItems.blockNetworkHub) {
                    this.markDirty();
                    this.world.notifyBlockUpdate(this.pos, state, state.withProperty(CONNECT, connected), 3);
                }
            }
        }
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
