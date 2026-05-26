package net.droingo.fishtourn.network;

import net.droingo.fishtourn.FishingTournament;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ReelFailPayload() implements CustomPayload {
    public static final Id<ReelFailPayload> ID = new Id<>(
            Identifier.of(FishingTournament.MOD_ID, "reel_fail")
    );

    public static final PacketCodec<PacketByteBuf, ReelFailPayload> CODEC =
            PacketCodec.unit(new ReelFailPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}