package net.droingo.fishtourn.network;

import net.droingo.fishtourn.FishingTournament;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ReelInputPayload(float amount, boolean clockwise) implements CustomPayload {
    public static final Id<ReelInputPayload> ID = new Id<>(
            Identifier.of(FishingTournament.MOD_ID, "reel_input")
    );

    public static final PacketCodec<PacketByteBuf, ReelInputPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.FLOAT,
            ReelInputPayload::amount,
            PacketCodecs.BOOL,
            ReelInputPayload::clockwise,
            ReelInputPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}