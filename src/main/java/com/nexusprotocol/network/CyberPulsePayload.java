package com.nexusprotocol.network;

import com.nexusprotocol.NexusMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record CyberPulsePayload() implements CustomPayload {
    public static final Id<CyberPulsePayload> ID = new Id<>(NexusMod.id("cyber_pulse"));
    public static final PacketCodec<RegistryByteBuf, CyberPulsePayload> CODEC =
            PacketCodec.unit(new CyberPulsePayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
