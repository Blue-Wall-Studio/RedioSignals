package org.BlueWallStudio.argest.debug;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.BlueWallStudio.argest.Argest;
import org.BlueWallStudio.argest.config.ModConfig;

public class CommandRegister {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
            CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
                CommandManager.literal("argest")
                        .then(CommandManager.literal("debug")
                                .executes(ctx -> toggleDebug(ctx.getSource())))
                        .then(CommandManager.literal("reload")
                                .executes(ctx -> reloadConfig(ctx.getSource()))));
    }

    private static int toggleDebug(ServerCommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("Команду может использовать только игрок"));
            return 0;
        }

        DebugManager.getInstance().toggleDebug(player);
        return 1;
    }

    private static int reloadConfig(ServerCommandSource context) {
        ModConfig.getInstance().reload();
        Argest.LOGGER.info("Config reloaded!");

        return 1;
    }
}
