package net.droingo.fishtourn.item;

import net.droingo.fishtourn.fish.TournamentBobberAccess;
import net.droingo.fishtourn.network.OpenReelScreenPayload;
import net.droingo.fishtourn.reel.ReelingManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class TournamentFishingRodItem extends FishingRodItem {
    private static final int MAX_USE_TICKS = 72_000;

    private static final int MIN_CAST_TICKS = 8;
    private static final int GOOD_CAST_TICKS = 18;
    private static final int PERFECT_START_TICKS = 26;
    private static final int PERFECT_END_TICKS = 34;
    private static final int OVERCAST_TICKS = 42;

    public TournamentFishingRodItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (user.fishHook != null) {
            if (!world.isClient() && user instanceof ServerPlayerEntity serverPlayer) {
                boolean hasFishHooked = user.fishHook instanceof TournamentBobberAccess bobberAccess
                        && bobberAccess.fishtourn$hasFishHooked();

                if (hasFishHooked) {
                    ReelingManager.startSession(serverPlayer, user.fishHook);

                    ServerPlayNetworking.send(
                            serverPlayer,
                            new OpenReelScreenPayload(user.fishHook.getId())
                    );

                    return TypedActionResult.consume(stack);
                }
            }

            // No fish is hooked yet, so let the player cancel/reel the bobber normally.
            return super.use(world, user, hand);
        }

        user.setCurrentHand(hand);

        if (!world.isClient()) {
            user.sendMessage(
                    Text.literal("Hold... release in the sweet spot!")
                            .formatted(Formatting.AQUA),
                    true
            );
        }

        return TypedActionResult.consume(stack);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) {
            return;
        }

        if (player.fishHook != null) {
            return;
        }

        int chargeTicks = getMaxUseTime(stack, user) - remainingUseTicks;
        CastResult result = getCastResult(chargeTicks);

        if (!world.isClient()) {
            FishingBobberEntity bobber = new FishingBobberEntity(player, world, 0, 0);

            Vec3d velocity = bobber.getVelocity();
            bobber.setVelocity(
                    velocity.x * result.velocityMultiplier(),
                    velocity.y * result.velocityMultiplier(),
                    velocity.z * result.velocityMultiplier()
            );

            world.spawnEntity(bobber);

            world.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.ENTITY_FISHING_BOBBER_THROW,
                    SoundCategory.NEUTRAL,
                    0.5F,
                    0.35F + result.velocityMultiplier() * 0.4F
            );

            stack.damage(1, player, LivingEntity.getSlotForHand(player.getActiveHand()));
            player.incrementStat(Stats.USED.getOrCreateStat(this));

            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.sendMessage(
                        Text.literal(result.displayName())
                                .formatted(result.formatting(), Formatting.BOLD),
                        true
                );
            }
        }
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) {
            return;
        }

        if (world.getTime() % 5 != 0) {
            return;
        }

        int chargeTicks = getMaxUseTime(stack, user) - remainingUseTicks;
        CastResult result = getCastResult(chargeTicks);

        player.sendMessage(
                Text.literal(getChargeBar(chargeTicks) + " " + result.displayName())
                        .formatted(result.formatting()),
                true
        );
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return MAX_USE_TICKS;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Tournament Rod").formatted(Formatting.AQUA));
        tooltip.add(Text.literal("Hold right-click to charge your cast.").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("Release in the sweet spot for max distance.").formatted(Formatting.DARK_GRAY));
        tooltip.add(Text.literal("Right-click during a bite to fight the fish.").formatted(Formatting.BLUE));
    }

    private static CastResult getCastResult(int chargeTicks) {
        if (chargeTicks < MIN_CAST_TICKS) {
            return new CastResult("Too Short", 0.55F, Formatting.RED);
        }

        if (chargeTicks < GOOD_CAST_TICKS) {
            return new CastResult("Weak Cast", 0.85F, Formatting.YELLOW);
        }

        if (chargeTicks < PERFECT_START_TICKS) {
            return new CastResult("Good Cast", 1.20F, Formatting.GREEN);
        }

        if (chargeTicks <= PERFECT_END_TICKS) {
            return new CastResult("Perfect Cast", 1.65F, Formatting.AQUA);
        }

        if (chargeTicks < OVERCAST_TICKS) {
            return new CastResult("Strong Cast", 1.30F, Formatting.GREEN);
        }

        return new CastResult("Overcast", 0.75F, Formatting.LIGHT_PURPLE);
    }

    private static String getChargeBar(int chargeTicks) {
        int filled = Math.min(10, Math.max(0, Math.round(chargeTicks / 4.2F)));

        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < 10; i++) {
            builder.append(i < filled ? "|" : ".");
        }
        builder.append("]");

        return builder.toString();
    }

    private record CastResult(String displayName, float velocityMultiplier, Formatting formatting) {
    }
}