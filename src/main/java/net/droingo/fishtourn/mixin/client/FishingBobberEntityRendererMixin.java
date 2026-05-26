package net.droingo.fishtourn.mixin.client;

import net.droingo.fishtourn.item.ModItems;
import net.minecraft.client.render.entity.FishingBobberEntityRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FishingBobberEntityRenderer.class)
public abstract class FishingBobberEntityRendererMixin {
    @Redirect(
            method = "getHandPos",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"
            )
    )
    private boolean fishtourn$treatTournamentRodAsFishingRod(ItemStack stack, Item item) {
        if (item == Items.FISHING_ROD && stack.isOf(ModItems.TOURNAMENT_ROD)) {
            return true;
        }

        return stack.isOf(item);
    }
}