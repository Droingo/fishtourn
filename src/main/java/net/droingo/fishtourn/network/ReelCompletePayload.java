package net.droingo.fishtourn.network;

import net.droingo.fishtourn.FishingTournament;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ReelCompletePayload(int bobberId) implements CustomPayload {
    public static final Id<ReelCompletePayload> ID = new Id<>(
            Identifier.of(FishingTournament.MOD_ID, "reel_complete")
    );

    public static final PacketCodec<PacketByteBuf, ReelCompletePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER,
            ReelCompletePayload::bobberId,
            ReelCompletePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}