package net.droingo.fishtourn.item;

import net.droingo.fishtourn.network.MemorialHeadEffectPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MemorialFireworkItem extends Item {
    public MemorialFireworkItem(Settings settings) {
        super(settings.maxCount(16));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!(world instanceof ServerWorld serverWorld)) {
            return TypedActionResult.success(stack, true);
        }

        Vec3d center = user.getPos()
                .add(user.getRotationVector().multiply(4.0D))
                .add(0.0D, 5.5D, 0.0D);

        serverWorld.spawnParticles(
                ParticleTypes.FIREWORK,
                center.x,
                center.y,
                center.z,
                80,
                0.8D,
                0.8D,
                0.8D,
                0.08D
        );

        serverWorld.playSound(
                null,
                center.x,
                center.y,
                center.z,
                SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH,
                SoundCategory.PLAYERS,
                1.0F,
                0.9F
        );

        serverWorld.playSound(
                null,
                center.x,
                center.y,
                center.z,
                SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST,
                SoundCategory.PLAYERS,
                1.4F,
                0.75F
        );

        float yaw = user.getYaw() + 90.0F;

        for (ServerPlayerEntity player : serverWorld.getPlayers()) {
            if (player.squaredDistanceTo(center) > 128.0D * 128.0D) {
                continue;
            }

            ServerPlayNetworking.send(
                    player,
                    new MemorialHeadEffectPayload(center.x, center.y, center.z, yaw)
            );
        }

        user.sendMessage(
                Text.literal("A memorial firework lights the sky.")
                        .formatted(Formatting.GOLD),
                true
        );

        if (!user.getAbilities().creativeMode) {
            stack.decrement(1);
        }

        return TypedActionResult.success(stack, false);
    }
}