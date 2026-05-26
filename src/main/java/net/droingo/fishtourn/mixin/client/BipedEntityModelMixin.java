package net.droingo.fishtourn.mixin.client;

import net.droingo.fishtourn.block.ModBlocks;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> {
    @Shadow
    @Final
    public ModelPart rightArm;

    @Shadow
    @Final
    public ModelPart leftArm;

    @Inject(method = "setAngles", at = @At("TAIL"))
    private void fishtourn$poseArmsForTrophy(
            T entity,
            float limbAngle,
            float limbDistance,
            float animationProgress,
            float headYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        if (!(entity instanceof PlayerEntity player)) {
            return;
        }

        boolean holdingTrophyMain = player.getMainHandStack().isOf(ModBlocks.TROPHY_ITEM);
        boolean holdingTrophyOff = player.getOffHandStack().isOf(ModBlocks.TROPHY_ITEM);

        if (!holdingTrophyMain && !holdingTrophyOff) {
            return;
        }

        boolean rightHanded = player.getMainArm() == Arm.RIGHT;

        boolean trophyInRightHand = holdingTrophyMain && rightHanded
                || holdingTrophyOff && !rightHanded;

        boolean trophyInLeftHand = holdingTrophyMain && !rightHanded
                || holdingTrophyOff && rightHanded;

        // Chest-height presentation pose.
        float forwardPitch = -0.75F;
        float inwardRoll = 0.22F;

        this.rightArm.pitch = MathHelper.lerp(0.85F, this.rightArm.pitch, forwardPitch);
        this.leftArm.pitch = MathHelper.lerp(0.85F, this.leftArm.pitch, forwardPitch);

        this.rightArm.yaw = MathHelper.lerp(0.85F, this.rightArm.yaw, -0.18F);
        this.leftArm.yaw = MathHelper.lerp(0.85F, this.leftArm.yaw, 0.18F);

        this.rightArm.roll = MathHelper.lerp(0.85F, this.rightArm.roll, inwardRoll);
        this.leftArm.roll = MathHelper.lerp(0.85F, this.leftArm.roll, -inwardRoll);

        // Slightly favor the hand actually holding the trophy.
        if (trophyInRightHand) {
            this.rightArm.pitch = -0.82F;
            this.rightArm.yaw = -0.12F;
            this.rightArm.roll = 0.12F;
        }

        if (trophyInLeftHand) {
            this.leftArm.pitch = -0.82F;
            this.leftArm.yaw = 0.12F;
            this.leftArm.roll = -0.12F;
        }
    }
}