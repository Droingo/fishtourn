package net.droingo.fishtourn.item;

import net.minecraft.block.Block;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class TrophyBlockItem extends BlockItem {
    public TrophyBlockItem(Block block, Settings settings) {
        super(block, settings.maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        ItemStack headStack = user.getEquippedStack(EquipmentSlot.HEAD);

        if (!headStack.isEmpty()) {
            return TypedActionResult.pass(stack);
        }

        if (!world.isClient()) {
            ItemStack equippedStack = stack.copy();
            equippedStack.setCount(1);

            user.equipStack(EquipmentSlot.HEAD, equippedStack);

            if (!user.getAbilities().creativeMode) {
                stack.decrement(1);
            }

            world.playSound(
                    null,
                    user.getX(),
                    user.getY(),
                    user.getZ(),
                    SoundEvents.ITEM_ARMOR_EQUIP_GOLD,
                    SoundCategory.PLAYERS,
                    0.8F,
                    1.15F
            );
        }

        return TypedActionResult.success(stack, world.isClient());
    }
}