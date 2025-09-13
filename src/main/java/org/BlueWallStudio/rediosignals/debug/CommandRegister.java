package org.BlueWallStudio.rediosignals.debug;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.BlueWallStudio.rediosignals.RedioSignals;
import org.BlueWallStudio.rediosignals.config.ModConfig;

public class CommandRegister {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
            CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
                CommandManager.literal("rediosignals")
                        // Debug not for public version
                        //.then(CommandManager.literal("debug")
                                //.executes(ctx -> toggleDebug(ctx.getSource())))
                        .then(CommandManager.literal("reload")
                                .executes(ctx -> reloadConfig(ctx.getSource()))));
    }

    private static int toggleDebug(ServerCommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("Command can be used only by player"));
            return 0;
        }

        DebugManager.getInstance().toggleDebug(player);
        return 1;
    }

    private static int reloadConfig(ServerCommandSource context) {
        ModConfig.getInstance().reload();
        RedioSignals.LOGGER.info("Config reloaded!");

        return 1;
    }
}
