package com.nexusprotocol.network;

import com.nexusprotocol.NexusMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record CyberModePayload() implements CustomPayload {
    public static final Id<CyberModePayload> ID = new Id<>(NexusMod.id("cyber_mode"));
    public static final PacketCodec<RegistryByteBuf, CyberModePayload> CODEC =
            PacketCodec.unit(new CyberModePayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
