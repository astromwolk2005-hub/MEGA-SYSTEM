package com.nexusprotocol.network;

import com.nexusprotocol.NexusMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record SyncCyberPayload(int mode, float energy, float maxEnergy) implements CustomPayload {
    public static final Id<SyncCyberPayload> ID = new Id<>(NexusMod.id("sync_cyber"));

    public static final PacketCodec<RegistryByteBuf, SyncCyberPayload> CODEC = new PacketCodec<>() {
        @Override
        public SyncCyberPayload decode(RegistryByteBuf buf) {
            return new SyncCyberPayload(buf.readVarInt(), buf.readFloat(), buf.readFloat());
        }

        @Override
        public void encode(RegistryByteBuf buf, SyncCyberPayload v) {
            buf.writeVarInt(v.mode());
            buf.writeFloat(v.energy());
            buf.writeFloat(v.maxEnergy());
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
