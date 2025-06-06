package com.xinyihl.ymadditions.common.command;

import com.mojang.authlib.GameProfile;
import com.xinyihl.ymadditions.YMAdditions;
import com.xinyihl.ymadditions.common.data.DataStorage;
import com.xinyihl.ymadditions.common.data.NetworkStatus;
import com.xinyihl.ymadditions.common.network.PacketServerToClient;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.xinyihl.ymadditions.common.network.PacketServerToClient.ServerToClient.DELETE_NETWORKS;

public class YMCommand extends CommandBase {

    @Override
    @Nonnull
    public String getName() {
        return "ym";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "/ym <add|rm|list> [networkUUID] [player name]";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            throw new CommandException("commands.ym.error.no_args");
        }

        switch (args[0].toLowerCase()) {
            case "add":
                handleAdd(server, sender, args);
                break;
            case "rm":
                handleRemove(server, sender, args);
                break;
            case "list":
                handleList(server, sender, args);
                break;
            default:
                throw new CommandException("commands.ym.error.unknown");
        }
    }

    private void handleAdd(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 3) {
            throw new CommandException("commands.ym.error.args");
        }

        UUID network = UUID.fromString(args[1]);
        GameProfile player = server.getPlayerProfileCache().getGameProfileForUsername(args[2]);

        if (player == null) {
            throw new CommandException("commands.ym.error.player_no_found");
        }

        DataStorage storage = DataStorage.get(sender.getEntityWorld());
        NetworkStatus networkStatus = storage.getNetwork(network);

        if (networkStatus == null) {
            throw new CommandException("commands.ym.error.unknown_network");
        }

        if (networkStatus.hasPermission((EntityPlayer) sender, 2)) {
            networkStatus.addUser(player.getId());
            networkStatus.setNeedTellClient(true);
            sender.sendMessage(new TextComponentTranslation("commands.ym.info.add_success"));
        } else {
            throw new CommandException("commands.ym.error.no_perm_network");
        }
    }

    private void handleRemove(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 3) {
            throw new CommandException("commands.ym.error.args");
        }

        UUID network = UUID.fromString(args[1]);
        GameProfile player = server.getPlayerProfileCache().getGameProfileForUsername(args[2]);

        if (player == null) {
            throw new CommandException("commands.ym.error.player_no_found");
        }

        DataStorage storage = DataStorage.get(sender.getEntityWorld());
        NetworkStatus networkStatus = storage.getNetwork(network);

        if (networkStatus == null) {
            throw new CommandException("commands.ym.error.unknown_network");
        }

        if (networkStatus.hasPermission((EntityPlayer) sender, 2)) {
            networkStatus.removeUser(player.getId());
            EntityPlayerMP p = server.getPlayerList().getPlayerByUsername(player.getName());
            if (p != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setUniqueId("networkUuid", network);
                YMAdditions.instance.networkWrapper.sendTo(new PacketServerToClient(DELETE_NETWORKS, tag), p);
            }
            sender.sendMessage(new TextComponentTranslation("commands.ym.info.remove_success"));
        } else {
            throw new CommandException("commands.ym.error.no_perm_network");
        }
    }

    private void handleList(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            throw new CommandException("commands.ym.error.args");
        }
        UUID network = UUID.fromString(args[1]);
        DataStorage storage = DataStorage.get(sender.getEntityWorld());
        NetworkStatus networkStatus = storage.getNetwork(network);
        if (networkStatus == null) {
            throw new CommandException("commands.ym.error.unknown_network");
        }
        if (networkStatus.hasPermission((EntityPlayer) sender, 2)) {
            sender.sendMessage(new TextComponentString("Users:"));
            networkStatus.getUsers().forEach(user -> sender.sendMessage(new TextComponentString(server.getPlayerProfileCache().getProfileByUUID(user).getName())));
        } else {
            throw new CommandException("commands.ym.error.no_perm_network");
        }
    }

    @Override
    @Nonnull
    public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, Arrays.asList("add", "rm", "list"));
        } else if (args.length == 2 && ("add".equalsIgnoreCase(args[0]) || "rm".equalsIgnoreCase(args[0]) || "list".equalsIgnoreCase(args[0]))) {
            DataStorage storage = DataStorage.get(sender.getEntityWorld());
            List<NetworkStatus> networkStatusList = storage.getPlayerNetworks((EntityPlayer) sender);
            return getListOfStringsMatchingLastWord(args, networkStatusList.stream().map(NetworkStatus::getUuid).map(UUID::toString).collect(Collectors.toList()));
        } else if (args.length == 3 && ("add".equalsIgnoreCase(args[0]) || "rm".equalsIgnoreCase(args[0]))) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }
        return super.getTabCompletions(server, sender, args, targetPos);
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
