package me.ryleu.soldisco;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.HashSet;
import java.util.Optional;

public class FoodHistory extends HashSet<Item> {
    private final Player player;

    public FoodHistory(Player player) {
        this.player = player;
    }

    public float getMaxHealth() {
        return (float) SOLDisco.applyFoodFormula(size());
    }

    public static FoodHistory read(Player player, CompoundTag compoundTag) {
        FoodHistory foodHistory = new FoodHistory(player);

        // first, make sure the tag has our list
        if (compoundTag.contains(SOLDisco.NBT_FOOD_HISTORY_ID, Tag.TAG_LIST)) {
            // if it does, we get it as a list of strings
            ListTag foodListTag = compoundTag.getList(SOLDisco.NBT_FOOD_HISTORY_ID, Tag.TAG_STRING);
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

                item.ifPresent(foodHistory::add);
            }
        }

        return foodHistory;
    }

    public void write(CompoundTag compoundTag) {
        ListTag foodListTag = new ListTag();
        for (Item foodItem : this) {
            // get the namespaced ids for all of our foods
            foodListTag.add(StringTag.valueOf(foodItem.toString()));
        }
        compoundTag.put(SOLDisco.NBT_FOOD_HISTORY_ID, foodListTag);
    }

    @Override
    public boolean add(Item food) {
        boolean result = super.add(food);
        if (result && player != null) {
            updateMaxHealth();
        }
        return result;
    }

    @Override
    public boolean remove(Object food) {
        boolean result = super.remove(food);
        if (result && player != null) {
            updateMaxHealth();
        }
        return result;
    }

    @Override
    public void clear() {
        super.clear();
        if (player != null) {
            updateMaxHealth();
        }
    }

    public void updateMaxHealth() {
        ((IPlayer) player).spiceoflife_discovery$updateMaxHealth();
    }
}
