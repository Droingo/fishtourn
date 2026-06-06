package net.droingo.fishtourn.mixin;

import net.droingo.fishtourn.compat.FishOfThievesCompat;
import net.droingo.fishtourn.component.FishDataComponent;
import net.droingo.fishtourn.component.ModComponents;
import net.droingo.fishtourn.fish.CastZone;
import net.droingo.fishtourn.fish.FishItemFactory;
import net.droingo.fishtourn.fish.FishingZoneDetector;
import net.droingo.fishtourn.fish.TournamentBobberAccess;
import net.droingo.fishtourn.item.ModItems;
import net.droingo.fishtourn.reel.ReelingManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin extends ProjectileEntity implements TournamentBobberAccess {
    private boolean fishtourn$zoneLandingChecked = false;
    private boolean fishtourn$deepZoneHit = false;

    @Shadow
    private int hookCountdown;

    @Shadow
    private int waitCountdown;

    @Shadow
    private int fishTravelCountdown;

    protected FishingBobberEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean fishtourn$hasFishHooked() {
        return this.hookCountdown > 0;
    }

    @Override
    public void fishtourn$keepFishHooked() {
        this.hookCountdown = Math.max(this.hookCountdown, 100);
    }

    /**
     * Vanilla removes fishing bobbers if the owner is not holding Items.FISHING_ROD.
     * This makes the tournament rod count as a valid fishing rod for that check.
     */
    @Redirect(
            method = "removeIfInvalid",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"
            )
    )
    private boolean fishtourn$tournamentRodKeepsBobberValid(ItemStack stack, Item item) {
        if (item == Items.FISHING_ROD && stack.isOf(ModItems.TOURNAMENT_ROD)) {
            return true;
        }

        return stack.isOf(item);
    }

    private boolean fishtourn$biteNotified = false;

    @Inject(method = "tick", at = @At("TAIL"))
    private void fishtourn$checkZoneLanding(CallbackInfo ci) {
        if (this.getWorld().isClient()) {
            return;
        }

        if (this.fishtourn$zoneLandingChecked) {
            return;
        }

        if (!this.isTouchingWater()) {
            return;
        }

        CastZone zone = FishingZoneDetector.detect(this.getWorld(), this.getBlockPos());
        this.fishtourn$zoneLandingChecked = true;

        if (zone != CastZone.DEEP) {
            return;
        }

        this.fishtourn$deepZoneHit = true;

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    this.getX(),
                    this.getY() + 0.25D,
                    this.getZ(),
                    12,
                    0.45D,
                    0.35D,
                    0.45D,
                    0.02D
            );
        }

        if (this.getOwner() instanceof PlayerEntity player) {
            player.sendMessage(
                    Text.literal("Deep Zone Hit!")
                            .formatted(Formatting.GREEN, Formatting.BOLD),
                    true
            );
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void fishtourn$makeTournamentRodBitesFaster(CallbackInfo ci) {
        if (this.getWorld().isClient()) {
            return;
        }

        if (!this.isTouchingWater()) {
            return;
        }

        if (!fishtourn$isTournamentRodOwner()) {
            return;
        }

        // Do not alter an active bite.
        if (this.hookCountdown > 0) {
            return;
        }

        /*
         * Important:
         * Do NOT reduce these counters to 0 here.
         *
         * Vanilla has special transition logic when its own tick decrements:
         * waitCountdown -> fishTravelCountdown
         * fishTravelCountdown -> hookCountdown
         *
         * If we force the counter to 0 at TAIL, vanilla may miss that transition.
         */
        if (this.waitCountdown > 1) {
            this.waitCountdown--;
        }

        if (this.fishTravelCountdown > 1) {
            this.fishTravelCountdown--;
        }
    }

    private boolean fishtourn$isTournamentRodOwner() {
        if (!(this.getOwner() instanceof PlayerEntity player)) {
            return false;
        }

        return player.getMainHandStack().isOf(ModItems.TOURNAMENT_ROD)
                || player.getOffHandStack().isOf(ModItems.TOURNAMENT_ROD);
    }

    @ModifyArg(
            method = "use(Lnet/minecraft/item/ItemStack;)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ItemEntity;<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V"
            ),
            index = 4
    )
    private ItemStack fishtourn$addFishStatsToCaughtFish(ItemStack stack) {
        if (this.getWorld().isClient()) {
            return stack;
        }

        CastZone zone = this.fishtourn$deepZoneHit
                ? CastZone.DEEP
                : FishingZoneDetector.detect(this.getWorld(), this.getBlockPos());

        ItemStack catchStack = FishOfThievesCompat
                .rollFishOfThievesCatch(this.getWorld().getRandom())
                .orElse(stack);

        boolean applied = FishItemFactory.applyGeneratedFishData(
                catchStack,
                this.getWorld().getRandom(),
                zone
        );

        if (applied && this.getOwner() instanceof PlayerEntity player && this.getWorld() instanceof ServerWorld serverWorld) {
            FishDataComponent fishData = catchStack.get(ModComponents.FISH_DATA);

            if (fishData != null) {
                fishtourn$spawnCatchParticles(serverWorld, player, fishData.rarity());

                serverWorld.playSound(
                        null,
                        player.getBlockPos(),
                        SoundEvents.ENTITY_PLAYER_LEVELUP,
                        SoundCategory.PLAYERS,
                        0.35F,
                        1.65F
                );
            }
        }

        if (this.getOwner() instanceof ServerPlayerEntity serverPlayer) {
            ReelingManager.stopSession(serverPlayer.getUuid());
        }

        if (applied && zone == CastZone.DEEP && this.getOwner() instanceof PlayerEntity player) {
            player.sendMessage(
                    Text.literal("Deep Zone Catch!")
                            .formatted(Formatting.AQUA, Formatting.BOLD),
                    true
            );
        }

        return catchStack;
    }

    private void fishtourn$spawnCatchParticles(ServerWorld world, PlayerEntity player, String rarity) {
        Vector3f color = switch (rarity.toLowerCase(Locale.ROOT)) {
            case "uncommon" -> new Vector3f(0.25F, 1.0F, 0.25F);
            case "rare" -> new Vector3f(0.25F, 0.9F, 1.0F);
            case "legendary" -> new Vector3f(1.0F, 0.25F, 1.0F);
            default -> new Vector3f(1.0F, 1.0F, 1.0F);
        };

        DustParticleEffect dust = new DustParticleEffect(color, 1.25F);

        world.spawnParticles(
                dust,
                player.getX(),
                player.getY() + 1.1D,
                player.getZ(),
                36,
                0.55D,
                0.75D,
                0.55D,
                0.02D
        );
    }
}