package com.nexusprotocol.system;

import net.minecraft.entity.Entity;
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

public class SingularityManager {
    private static class BlackHole {
        final RegistryKey<World> dim;
        final Vec3d center;
        int ticksLeft = 90; // 4.5 секунды

        BlackHole(RegistryKey<World> dim, Vec3d center) {
            this.dim = dim;
            this.center = center;
        }
    }

    private static final List<BlackHole> HOLES = new ArrayList<>();

    public static void spawn(ServerWorld world, Vec3d pos) {
        HOLES.add(new BlackHole(world.getRegistryKey(), pos));
        world.playSound(null, BlockPos.ofFloored(pos), SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.PLAYERS, 1.2f, 0.5f);
    }

    public static void tick(MinecraftServer server) {
        Iterator<BlackHole> it = HOLES.iterator();
        while (it.hasNext()) {
            BlackHole bh = it.next();
            ServerWorld world = server.getWorld(bh.dim);
            if (world == null) {
                it.remove();
                continue;
            }

            bh.ticksLeft--;
            world.spawnParticles(ParticleTypes.PORTAL, bh.center.x, bh.center.y, bh.center.z, 30, 1.2, 1.2, 1.2, 0.5);
            world.spawnParticles(ParticleTypes.SQUID_INK, bh.center.x, bh.center.y, bh.center.z, 6, 0.3, 0.3, 0.3, 0.02);

            Box box = new Box(bh.center, bh.center).expand(9.5);
            for (Entity e : world.getEntitiesByClass(Entity.class, box, Entity::isAlive)) {
                Vec3d pull = bh.center.subtract(e.getPos()).normalize().multiply(0.55);
                e.setVelocity(pull);
                e.velocityModified = true;
                if (bh.ticksLeft % 15 == 0 && e instanceof LivingEntity le) {
                    le.damage(world.getDamageSources().outOfWorld(), 4.0f);
                }
            }

            if (bh.ticksLeft <= 0) {
                world.createExplosion(null, bh.center.x, bh.center.y, bh.center.z, 4.0f, false, World.ExplosionSourceType.TNT);
                world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, bh.center.x, bh.center.y, bh.center.z, 2, 0.5, 0.5, 0.5, 0);
                it.remove();
            }
        }
    }
}
