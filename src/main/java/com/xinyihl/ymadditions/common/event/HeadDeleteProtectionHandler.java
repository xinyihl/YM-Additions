package com.xinyihl.ymadditions.common.event;

import com.xinyihl.ymadditions.Tags;
import com.xinyihl.ymadditions.common.block.BlockNetworkHub;
import com.xinyihl.ymadditions.common.title.TileNetworkHub;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class HeadDeleteProtectionHandler {
    @SubscribeEvent
    public static void onPlayerBreakEvent(BlockEvent.BreakEvent event){
        World world = event.getWorld();
        if(world.isRemote) return;
        BlockPos pos = event.getPos();
        IBlockState state = event.getState();
        EntityPlayer player = event.getPlayer();
        if(state.getBlock() instanceof BlockNetworkHub){
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileNetworkHub) {
                TileNetworkHub hub = (TileNetworkHub) tile;
                if(hub.isHead() && hub.isConnected() || !player.isSneaking()) {
                    player.sendStatusMessage(new TextComponentTranslation("statusmessage.ymadditions.info.head_delete_protection"), true);
                    event.setCanceled(true);
                }
            }
        }
    }
}
