package net.droingo.fishtourn.network;

import net.droingo.fishtourn.FishingTournament;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ReelSyncPayload(int bobberId, float progress, float tension, String difficulty) implements CustomPayload {
    public static final Id<ReelSyncPayload> ID = new Id<>(
            Identifier.of(FishingTournament.MOD_ID, "reel_sync")
    );

    public static final PacketCodec<PacketByteBuf, ReelSyncPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER,
            ReelSyncPayload::bobberId,
            PacketCodecs.FLOAT,
            ReelSyncPayload::progress,
            PacketCodecs.FLOAT,
            ReelSyncPayload::tension,
            PacketCodecs.STRING,
            ReelSyncPayload::difficulty,
            ReelSyncPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}