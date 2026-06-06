package net.droingo.fishtourn.network;

import net.droingo.fishtourn.FishingTournament;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MemorialHeadEffectPayload(
        double x,
        double y,
        double z,
        float yaw
) implements CustomPayload {
    public static final Id<MemorialHeadEffectPayload> ID =
            new Id<>(Identifier.of(FishingTournament.MOD_ID, "memorial_head_effect"));

    public static final PacketCodec<RegistryByteBuf, MemorialHeadEffectPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.DOUBLE,
                    MemorialHeadEffectPayload::x,
                    PacketCodecs.DOUBLE,
                    MemorialHeadEffectPayload::y,
                    PacketCodecs.DOUBLE,
                    MemorialHeadEffectPayload::z,
                    PacketCodecs.FLOAT,
                    MemorialHeadEffectPayload::yaw,
                    MemorialHeadEffectPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}