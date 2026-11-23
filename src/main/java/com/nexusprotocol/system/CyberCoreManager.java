package com.nexusprotocol.system;

import com.nexusprotocol.network.SyncCyberPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CyberCoreManager {
    public static final String[] MODE_NAMES = {"РЕАКТИВНЫЙ РАНЕЦ", "ГИПЕР-СКОРОСТЬ", "БОЕВОЙ ЩИТ"};
    public static final int[] MODE_COLORS = {0xFF7A29, 0x2CE8FF, 0x55FF77};

    private static class State {
        int mode = 0;
        float energy = 100f;
        final float maxEnergy = 100f;
    }

    private static final Map<UUID, State> STATES = new HashMap<>();
    private static int tickCount = 0;

    private static State get(UUID id) {
        return STATES.computeIfAbsent(id, k -> new State());
    }

    public static void cycleMode(ServerPlayerEntity player) {
        State st = get(player.getUuid());
        st.mode = (st.mode + 1) % 3;
        player.getServerWorld().playSound(null, player.getBlockPos(), SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 0.8f, 1.4f);
        player.sendMessage(Text.literal("⚙ Режим Кибер-Ядра: " + MODE_NAMES[st.mode]).formatted(Formatting.AQUA), true);
        sync(player);
    }

    public static void triggerPulse(ServerPlayerEntity player) {
        State st = get(player.getUuid());
        if (st.energy < 35f) {
            player.sendMessage(Text.literal("Недостаточно энергии Кибер-Ядра (нужно 35)!").formatted(Formatting.RED), true);
            return;
        }
        st.energy -= 35f;
        ServerWorld world = player.getServerWorld();

        if (st.mode == 0) {
            // Реактивный прыжок вверх и вперед
            Vec3d look = player.getRotationVector();
            player.setVelocity(look.x * 1.4, 1.25, look.z * 1.4);
            player.velocityModified = true;
            player.fallDistance = 0;
            world.spawnParticles(ParticleTypes.FLAME, player.getX(), player.getY(), player.getZ(), 35, 0.4, 0.2, 0.4, 0.1);
            world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.PLAYERS, 1.0f, 1.0f);
        } else if (st.mode == 1) {
            // Гипер-рывок
            Vec3d look = player.getRotationVector();
            player.setVelocity(look.x * 2.5, 0.25, look.z * 2.5);
            player.velocityModified = true;
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 100, 3));
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1, player.getZ(), 40, 0.5, 0.5, 0.5, 0.2);
            world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.8f);
        } else {
            // ЭМИ-импульс щита: отбрасывает всех врагов и даёт сопротивление
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 120, 2));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 120, 2));
            Box box = player.getBoundingBox().expand(6.5);
            for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, box, ent -> ent != player && ent.isAlive())) {
                Vec3d push = e.getPos().subtract(player.getPos()).normalize().multiply(1.8).add(0, 0.5, 0);
                e.setVelocity(push);
                e.velocityModified = true;
                e.damage(world.getDamageSources().magic(), 8.0f);
            }
            world.spawnParticles(ParticleTypes.SONIC_BOOM, player.getX(), player.getY() + 1, player.getZ(), 1, 0, 0, 0, 0);
            world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 0.8f, 1.5f);
        }
        sync(player);
    }

    public static void tick(MinecraftServer server) {
        tickCount++;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            State st = get(p.getUuid());
            if (st.energy < st.maxEnergy) {
                st.energy = Math.min(st.maxEnergy, st.energy + 0.35f);
            }
            if (tickCount % 20 == 0 && st.mode == 1) {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 1, false, false, false));
            }
            if (tickCount % 5 == 0) {
                sync(p);
            }
        }
    }

    public static void sync(ServerPlayerEntity player) {
        State st = get(player.getUuid());
        ServerPlayNetworking.send(player, new SyncCyberPayload(st.mode, st.energy, st.maxEnergy));
    }
}
