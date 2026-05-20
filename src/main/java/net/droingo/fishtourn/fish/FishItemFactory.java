package net.droingo.fishtourn.fish;

import net.droingo.fishtourn.component.FishDataComponent;
import net.droingo.fishtourn.component.ModComponents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.random.Random;

public final class FishItemFactory {
    private FishItemFactory() {
    }

    public static ItemStack createGeneratedFish(Item fishItem, Random random) {
        ItemStack stack = new ItemStack(fishItem);
        applyGeneratedFishData(stack, random);
        return stack;
    }

    public static boolean applyGeneratedFishData(ItemStack stack, Random random) {
        if (!isSupportedFish(stack)) {
            return false;
        }

        if (stack.contains(ModComponents.FISH_DATA)) {
            return false;
        }

        FishDataComponent fishData = FishGenerator.generate(stack.getItem(), random);

        stack.set(ModComponents.FISH_DATA, fishData);
        stack.set(DataComponentTypes.CUSTOM_NAME, createDisplayName(fishData));

        return true;
    }

    public static boolean isSupportedFish(ItemStack stack) {
        return stack.isOf(Items.COD)
                || stack.isOf(Items.SALMON)
                || stack.isOf(Items.TROPICAL_FISH)
                || stack.isOf(Items.PUFFERFISH);
    }

    public static MutableText createDisplayName(FishDataComponent fishData) {
        return Text.literal(fishData.species())
                .formatted(getRarityFormatting(fishData.rarity()));
    }

    public static Formatting getRarityFormatting(String rarity) {
        return switch (rarity.toLowerCase()) {
            case "uncommon" -> Formatting.GREEN;
            case "rare" -> Formatting.AQUA;
            case "legendary" -> Formatting.LIGHT_PURPLE;
            default -> Formatting.WHITE;
        };
    }
}