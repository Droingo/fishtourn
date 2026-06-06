package net.droingo.fishtourn.item;

import net.droingo.fishtourn.memorial.MemorialRocketManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
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

        Vec3d look = user.getRotationVector();

        Vec3d startPos = new Vec3d(
                user.getX() + look.x * 0.7D,
                user.getEyeY() - 0.2D,
                user.getZ() + look.z * 0.7D
        );

        Vec3d velocity = look.multiply(0.25D).add(0.0D, 0.55D, 0.0D);

        MemorialRocketManager.launch(
                serverWorld,
                startPos,
                velocity,
                user.getYaw() + 90.0F
        );

        user.sendMessage(
                Text.literal("A memorial rocket rises into the sky.")
                        .formatted(Formatting.GOLD),
                true
        );

        if (!user.getAbilities().creativeMode) {
            stack.decrement(1);
        }

        user.getItemCooldownManager().set(this, 20);

        return TypedActionResult.success(stack, false);
    }
}