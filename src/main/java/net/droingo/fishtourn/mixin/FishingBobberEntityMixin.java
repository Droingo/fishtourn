package net.droingo.fishtourn.mixin;

import net.droingo.fishtourn.fish.FishItemFactory;
import net.droingo.fishtourn.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin extends ProjectileEntity {
    protected FishingBobberEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
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
            FishItemFactory.applyGeneratedFishData(stack, this.getWorld().getRandom());
        }

        return stack;
    }
}