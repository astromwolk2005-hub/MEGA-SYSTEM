package com.nexusprotocol.system;

import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OrbitalStrikeManager {
    private static class Strike {
        final RegistryKey<World> dimension;
        final Vec3d target;
        int ticksLeft = 60; // 3 секунды наведения

        Strike(RegistryKey<World> dimension, Vec3d target) {
            this.dimension = dimension;
            this.target = target;
        }
    }

    private static final List<Strike> ACTIVE = new ArrayList<>();

    public static void callStrike(ServerWorld world, Vec3d pos) {
        ACTIVE.add(new Strike(world.getRegistryKey(), pos));
        world.playSound(null, BlockPos.ofFloored(pos), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1.5f, 1.8f);
    }

    public static void tick(MinecraftServer server) {
        Iterator<Strike> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Strike s = it.next();
            ServerWorld world = server.getWorld(s.dimension);
            if (world == null) {
                it.remove();
                continue;
            }

            s.ticksLeft--;

            // Лазерный луч наведения с орбиты
            for (double y = 0; y < 60; y += 1.5) {
                world.spawnParticles(ParticleTypes.END_ROD, s.target.x, s.target.y + y, s.target.z, 1, 0.05, 0, 0.05, 0);
            }

            // Сужающееся кольцо прицела на земле
            double r = 0.5 + (s.ticksLeft / 60.0) * 5.0;
            for (int i = 0; i < 16; i++) {
                double a = Math.PI * 2 * i / 16.0;
                world.spawnParticles(ParticleTypes.FLAME,
                        s.target.x + Math.cos(a) * r, s.target.y + 0.2, s.target.z + Math.sin(a) * r,
                        1, 0, 0, 0, 0);
            }

            if (s.ticksLeft == 20) {
                world.playSound(null, BlockPos.ofFloored(s.target), SoundEvents.ENTITY_WARDEN_SONIC_CHARGE, SoundCategory.PLAYERS, 2.0f, 1.2f);
            }

            if (s.ticksLeft <= 0) {
                detonate(world, s.target);
                it.remove();
            }
        }
    }

    private static void detonate(ServerWorld world, Vec3d pos) {
        // Гигантский ионный столб с неба
        for (double y = 0; y < 70; y += 1.0) {
            world.spawnParticles(ParticleTypes.SONIC_BOOM, pos.x, pos.y + y, pos.z, 1, 0, 0, 0, 0);
            world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.x, pos.y + y, pos.z, 12, 1.2, 0.4, 1.2, 0.05);
        }

        world.createExplosion(null, pos.x, pos.y + 0.5, pos.z, 6.0f, true, World.ExplosionSourceType.TNT);
        world.playSound(null, BlockPos.ofFloored(pos), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 3.0f, 0.5f);

        Box box = new Box(pos, pos).expand(9.0);
        for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            e.setOnFireFor(8);
            e.damage(world.getDamageSources().magic(), 30.0f);
        }
    }
}
