package com.nexusprotocol.client;

import com.nexusprotocol.network.CyberModePayload;
import com.nexusprotocol.network.CyberPulsePayload;
import com.nexusprotocol.network.SyncCyberPayload;
import com.nexusprotocol.system.CyberCoreManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class NexusClient implements ClientModInitializer {
    private static int cyberMode = 0;
    private static float energy = 100f;
    private static float maxEnergy = 100f;

    @Override
    public void onInitializeClient() {
        KeyBinding modeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.nexusprotocol.mode", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.nexusprotocol"));
        KeyBinding pulseKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.nexusprotocol.pulse", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.nexusprotocol"));

        ClientPlayNetworking.registerGlobalReceiver(SyncCyberPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    cyberMode = payload.mode();
                    energy = payload.energy();
                    maxEnergy = Math.max(1f, payload.maxEnergy());
                })
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            while (modeKey.wasPressed()) ClientPlayNetworking.send(new CyberModePayload());
            while (pulseKey.wasPressed()) ClientPlayNetworking.send(new CyberPulsePayload());
        });

        // Кастомный Sci-Fi HUD в левом нижнем углу
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden || client.player.isSpectator()) return;

            int h = client.getWindow().getScaledHeight();
            int x = 10;
            int y = h - 48;
            int w = 164;
            int boxH = 38;
            int color = CyberCoreManager.MODE_COLORS[cyberMode % 3];

            context.fill(x, y, x + w, y + boxH, 0xB80B101A);
            context.drawBorder(x, y, w, boxH, 0xFF000000 | color);

            context.drawTextWithShadow(client.textRenderer,
                    "NEXUS [" + CyberCoreManager.MODE_NAMES[cyberMode % 3] + "]",
                    x + 6, y + 5, color);

            int barX = x + 6;
            int barY = y + 18;
            int barW = w - 12;
            float ratio = Math.min(1f, energy / maxEnergy);
            context.fill(barX, barY, barX + barW, barY + 7, 0xFF1A2233);
            context.fill(barX, barY, barX + (int) (barW * ratio), barY + 7, 0xFF000000 | color);

            context.drawTextWithShadow(client.textRenderer,
                    "[R] Режим  |  [G] Импульс (" + (int) energy + "%)",
                    x + 6, y + 27, 0xFFCCDDEE);
        });
    }
}
