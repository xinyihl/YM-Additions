package com.xinyihl.ymadditions.common.block;

import com.xinyihl.ymadditions.Tags;
import com.xinyihl.ymadditions.YMAdditions;
import com.xinyihl.ymadditions.common.api.data.NetworkHubDataStorage;
import com.xinyihl.ymadditions.common.api.data.NetworkStatus;
import com.xinyihl.ymadditions.common.container.GUIContainerHandler;
import com.xinyihl.ymadditions.common.title.TileNetworkHub;
import com.xinyihl.ymadditions.common.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.xinyihl.ymadditions.common.registry.BlocksAndItems.CREATIVE_TAB;

public class BlockNetworkHub extends Block {
    public static final PropertyBool CONNECT = PropertyBool.create("connect");

    public BlockNetworkHub() {
        super(Material.ROCK);
        this.setCreativeTab(CREATIVE_TAB);
        this.setRegistryName(new ResourceLocation(Tags.MOD_ID, "network_hub"));
        this.setTranslationKey(Tags.MOD_ID + ".network_hub");
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setDefaultState(this.blockState.getBaseState().withProperty(CONNECT, false));
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(CONNECT, meta == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(CONNECT) ? 1 : 0;
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CONNECT);
    }

    @Nonnull
    @Override
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, @Nonnull BlockPos pos) {
        boolean connect = false;
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileNetworkHub) {
            TileNetworkHub tn = (TileNetworkHub) te;
            connect = tn.isConnected();
        }
        return super.getActualState(state, worldIn, pos).withProperty(CONNECT, connect);
    }

    @Override
    public boolean onBlockActivated(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileNetworkHub) {
                NetworkStatus network = NetworkHubDataStorage.get(worldIn).getNetwork(((TileNetworkHub) te).getNetworkUuid());
                if (Utils.isPlayerOp(playerIn) || playerIn.getGameProfile().getId().equals(((TileNetworkHub) te).getOwner()) || (network != null && network.hasPermission(playerIn, 0))) {
                    playerIn.openGui(YMAdditions.instance, GUIContainerHandler.GUI_NETWORK_HUB, worldIn, pos.getX(), pos.getY(), pos.getZ());
                } else {
                    playerIn.sendStatusMessage(new TextComponentTranslation("statusmessage.ymadditions.info.nopermission"), true);
                }
            }
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileNetworkHub && placer instanceof EntityPlayer) {
                TileNetworkHub tn = (TileNetworkHub) te;
                tn.setOwner((EntityPlayer) placer);
                tn.getActionableNode();
            }
        }
    }

    @Override
    public boolean hasTileEntity(@Nonnull IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState iBlockState) {
        return new TileNetworkHub();
    }

    @Override
    public void breakBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        super.breakBlock(world, pos, state);

    }
}
