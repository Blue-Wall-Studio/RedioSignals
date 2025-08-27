package org.BlueWallStudio.argest.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DebugCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("argest")
                .then(CommandManager.literal("debug")
                        .executes(DebugCommand::toggleDebug)
                )
        );
    }

    private static int toggleDebug(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("Команду может использовать только игрок"));
            return 0;
        }

        DebugManager.getInstance().toggleDebug(player);
        return 1;
    }
}
