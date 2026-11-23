package com.nexusprotocol;

import com.nexusprotocol.command.NexusCommands;
import com.nexusprotocol.network.CyberModePayload;
import com.nexusprotocol.network.CyberPulsePayload;
import com.nexusprotocol.network.SyncCyberPayload;
import com.nexusprotocol.registry.ModItems;
import com.nexusprotocol.system.CyberCoreManager;
import com.nexusprotocol.system.GravityGunManager;
import com.nexusprotocol.system.OrbitalStrikeManager;
import com.nexusprotocol.system.SingularityManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NexusMod implements ModInitializer {
    public static final String MOD_ID = "nexusprotocol";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModItems.register();
        NexusCommands.register();

        // Сетевые пакеты 1.21.1
        PayloadTypeRegistry.playC2S().register(CyberModePayload.ID, CyberModePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CyberPulsePayload.ID, CyberPulsePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncCyberPayload.ID, SyncCyberPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(CyberModePayload.ID, (payload, context) ->
                context.server().execute(() -> CyberCoreManager.cycleMode(context.player()))
        );

        ServerPlayNetworking.registerGlobalReceiver(CyberPulsePayload.ID, (payload, context) ->
                context.server().execute(() -> CyberCoreManager.triggerPulse(context.player()))
        );

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                CyberCoreManager.sync(handler.getPlayer())
        );

        // Главный цикл всех техно-систем
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            OrbitalStrikeManager.tick(server);
            GravityGunManager.tick(server);
            SingularityManager.tick(server);
            CyberCoreManager.tick(server);
        });

        LOGGER.info("NEXUS: Орбитальный Протокол активирован!");
    }
}
