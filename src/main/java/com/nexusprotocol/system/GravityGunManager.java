package com.nexusprotocol.system;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class GravityGunManager {
    private static final Map<UUID, UUID> HELD_ENTITY = new HashMap<>();

    public static void interact(ServerPlayerEntity player, boolean launch) {
        UUID playerId = player.getUuid();
        ServerWorld world = player.getServerWorld();

        if (HELD_ENTITY.containsKey(playerId)) {
            UUID targetId = HELD_ENTITY.remove(playerId);
            Entity target = world.getEntity(targetId);
            if (target != null && target.isAlive()) {
                if (launch) {
                    Vec3d vel = player.getRotationVector().multiply(2.8).add(0, 0.35, 0);
                    target.setVelocity(vel);
                    target.velocityModified = true;
                    world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 0.8f, 1.8f);
                    world.spawnParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getY() + 0.8, target.getZ(), 1, 0, 0, 0, 0);
                    player.sendMessage(Text.literal("⚡ Цель запущена!").formatted(Formatting.AQUA), true);
                } else {
                    player.sendMessage(Text.literal("Цель отпущена.").formatted(Formatting.GRAY), true);
                }
            }
            return;
        }

        // Поиск моба на линии взгляда
        Vec3d eye = player.getEyePos();
        Vec3d look = player.getRotationVector();
        Box search = player.getBoundingBox().stretch(look.multiply(12.0)).expand(2.0);

        LivingEntity found = world.getEntitiesByClass(LivingEntity.class, search,
                        e -> e != player && e.isAlive() && e.getPos().subtract(eye).normalize().dotProduct(look) > 0.82)
                .stream()
                .min(Comparator.comparingDouble(e -> e.squaredDistanceTo(player)))
                .orElse(null);

        if (found != null) {
            HELD_ENTITY.put(playerId, found.getUuid());
            world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1.0f, 1.6f);
            player.sendMessage(Text.literal("🧲 Захвачен: " + found.getName().getString() + " (Shift+ПКМ — швырнуть!)")
                    .formatted(Formatting.LIGHT_PURPLE), true);
        }
    }

    public static void tick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, UUID>> it = HELD_ENTITY.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, UUID> entry = it.next();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
            if (player == null || !player.isAlive()) {
                it.remove();
                continue;
            }
            ServerWorld world = player.getServerWorld();
            Entity target = world.getEntity(entry.getValue());
            if (target == null || !target.isAlive() || target.getWorld() != world) {
                it.remove();
                continue;
            }

            // Держим цель в 4 блоках перед глазами игрока
            Vec3d holdPos = player.getEyePos().add(player.getRotationVector().multiply(4.0));
            Vec3d pull = holdPos.subtract(target.getPos()).multiply(0.45);
            target.setVelocity(pull);
            target.velocityModified = true;
            target.fallDistance = 0;

            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 0.8, target.getZ(), 3, 0.3, 0.3, 0.3, 0.02);
        }
    }
}
