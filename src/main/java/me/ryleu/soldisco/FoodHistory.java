package me.ryleu.soldisco;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class FoodHistory {
    private final Set<Item> eatenFoods;

    public FoodHistory() {
        eatenFoods = new HashSet<>();
    }

    public float getMaxHealth() {
        return (float) SOLDisco.applyFoodFormula(eatenFoods.size());
    }

    public static FoodHistory read(CompoundTag compoundTag) {
        FoodHistory foodHistory = new FoodHistory();

        // first, make sure the tag has our list (list num is 9)
        if (compoundTag.contains(SOLDisco.NBT_FOOD_HISTORY_ID, 9)) {
            // if it does, we get it as a list of strings (string num is 8)
            ListTag foodListTag = compoundTag.getList(SOLDisco.NBT_FOOD_HISTORY_ID, 8);
            for (Tag entry : foodListTag) {
                // make sure the entry is a string (it is, but java is java)
                if (!(entry instanceof StringTag foodTag)) continue;

                // get the corresponding Item from the registry
                String itemNamespace = foodTag.getAsString();
                Optional<Item> item = Optional.empty();
                try {
                    ResourceLocation resourceLocation = ResourceLocation.read(itemNamespace).getOrThrow();
                    item = Optional.of(BuiltInRegistries.ITEM.get(resourceLocation));
                } catch (IllegalStateException e) {
                    // will probably only happen if mods are removed or updated
                    SOLDisco.LOGGER.error("{} found in nbt, but not in registry when decoding data", itemNamespace);
                }

                item.ifPresent(foodHistory.eatenFoods::add);
            }
        }

        return foodHistory;
    }

    public void write(CompoundTag compoundTag) {
        ListTag foodListTag = new ListTag();
        for (Item foodItem : eatenFoods) {
            // get the namespaced ids for all of our foods
            foodListTag.add(StringTag.valueOf(foodItem.toString()));
        }
        compoundTag.put(SOLDisco.NBT_FOOD_HISTORY_ID, foodListTag);
    }

    public boolean add(Item eatenFood) {
        return eatenFoods.add(eatenFood);
    }

    public void reset() {
        eatenFoods.clear();
    }
}
