package com.nexusprotocol.command;

import com.nexusprotocol.system.OrbitalStrikeManager;
import com.nexusprotocol.world.OrbitalStationDimension;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class NexusCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("nexus")
                    .then(CommandManager.literal("station")
                            .executes(ctx -> {
                                ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                                OrbitalStationDimension.toggleStation(p);
                                return 1;
                            }))
                    .then(CommandManager.literal("strike")
                            .requires(src -> src.hasPermissionLevel(2))
                            .executes(ctx -> {
                                ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
                                OrbitalStrikeManager.callStrike(p.getServerWorld(),
                                        p.getPos().add(p.getRotationVector().multiply(12.0)));
                                ctx.getSource().sendFeedback(() ->
                                        Text.literal("🛰 Орбитальный удар вызван!").formatted(Formatting.RED), false);
                                return 1;
                            }))
            );
        });
    }
}
