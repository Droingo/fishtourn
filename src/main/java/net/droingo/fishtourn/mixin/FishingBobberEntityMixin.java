package net.droingo.fishtourn.mixin;

import net.droingo.fishtourn.fish.CastZone;
import net.droingo.fishtourn.fish.FishItemFactory;
import net.droingo.fishtourn.fish.FishingZoneDetector;
import net.droingo.fishtourn.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin extends ProjectileEntity {
    private boolean fishtourn$zoneLandingChecked = false;
    private boolean fishtourn$deepZoneHit = false;

    protected FishingBobberEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

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
                    this.getY() + 0.25,
                    this.getZ(),
                    12,
                    0.45,
                    0.35,
                    0.45,
                    0.02
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

    @Inject(method = "removeIfInvalid", at = @At("HEAD"), cancellable = true)
    private void fishtourn$allowTournamentRodBobber(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        boolean holdingTournamentRod = player.getMainHandStack().isOf(ModItems.TOURNAMENT_ROD)
                || player.getOffHandStack().isOf(ModItems.TOURNAMENT_ROD);

        if (!holdingTournamentRod) {
            return;
        }

        if (player.isRemoved() || !player.isAlive() || this.squaredDistanceTo(player) > 1024.0D) {
            return;
        }

        cir.setReturnValue(false);
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
        if (!this.getWorld().isClient()) {
            CastZone zone = this.fishtourn$deepZoneHit
                    ? CastZone.DEEP
                    : FishingZoneDetector.detect(this.getWorld(), this.getBlockPos());

            boolean applied = FishItemFactory.applyGeneratedFishData(
                    stack,
                    this.getWorld().getRandom(),
                    zone
            );

            if (applied && zone == CastZone.DEEP && this.getOwner() instanceof PlayerEntity player) {
                player.sendMessage(
                        Text.literal("Deep Zone Catch!")
                                .formatted(Formatting.AQUA, Formatting.BOLD),
                        true
                );
            }
        }

        return stack;
    }
}