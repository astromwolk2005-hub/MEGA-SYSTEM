package com.nexusprotocol.world;

import com.nexusprotocol.NexusMod;
import net.minecraft.block.Blocks;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OrbitalStationDimension {
    public static final RegistryKey<World> STATION_KEY =
            RegistryKey.of(RegistryKeys.WORLD, NexusMod.id("orbital_station"));

    public static void toggleStation(ServerPlayerEntity player) {
        boolean inStation = player.getWorld().getRegistryKey() == STATION_KEY;
        ServerWorld target = player.getServer().getWorld(inStation ? World.OVERWORLD : STATION_KEY);
        if (target == null) return;

        BlockPos dest = inStation ? target.getSpawnPos() : new BlockPos(0, 120, 0);

        if (!inStation) {
            // Строим космическую платформу 7x7 из железа и морских фонарей
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    boolean border = Math.abs(dx) == 3 || Math.abs(dz) == 3;
                    target.setBlockState(dest.add(dx, -1, dz),
                            border ? Blocks.SEA_LANTERN.getDefaultState() : Blocks.IRON_BLOCK.getDefaultState());
                    target.setBlockState(dest.add(dx, 0, dz), Blocks.AIR.getDefaultState());
                    target.setBlockState(dest.add(dx, 1, dz), Blocks.AIR.getDefaultState());
                }
            }
        }

        player.teleport(target, dest.getX() + 0.5, dest.getY(), dest.getZ() + 0.5, player.getYaw(), player.getPitch());
        target.spawnParticles(ParticleTypes.END_ROD, dest.getX() + 0.5, dest.getY() + 1, dest.getZ() + 0.5, 50, 1.0, 1.0, 1.0, 0.15);
        target.playSound(null, dest, SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 0.7f, 1.2f);
        player.sendMessage(Text.literal(inStation
                        ? "🛰 Возврат на поверхность планеты."
                        : "🛰 Добро пожаловать на Орбитальную Станцию NEXUS!")
                .formatted(Formatting.AQUA, Formatting.BOLD), false);
    }
}
