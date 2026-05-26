package net.droingo.fishtourn.network;

import net.droingo.fishtourn.FishingTournament;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record OpenReelScreenPayload(int bobberId) implements CustomPayload {
    public static final Id<OpenReelScreenPayload> ID = new Id<>(
            Identifier.of(FishingTournament.MOD_ID, "open_reel_screen")
    );

    public static final PacketCodec<PacketByteBuf, OpenReelScreenPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER,
            OpenReelScreenPayload::bobberId,
            OpenReelScreenPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}