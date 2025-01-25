package me.ryleu.soldisco;

import me.ryleu.soldisco.component.IFoodHistory;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import static me.ryleu.soldisco.SOLDisco.MOD_ID;
import static me.ryleu.soldisco.component.SOLDiscoComponents.FOOD_HISTORY_COMPONENT_KEY;

public class FoodHistory implements IFoodHistory, AutoSyncedComponent {
    private static final String NBT_FOOD_HISTORY_ID = MOD_ID + ":food_history";
    private final HashSet<Item> history = new HashSet<>();
    private final Player player;

    public FoodHistory(Player player) {
        this.player = player;
    }

    private void updateMaxHealth() {
        ((IPlayer) player).soldisco$updateMaxHealth();
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag compoundTag, HolderLookup.@NotNull Provider registryLookup) {
        // clear the current toAdd history before proceeding
        history.clear();

        // now to deserialize. first, make sure the tag has our list
        if (compoundTag.contains(NBT_FOOD_HISTORY_ID, Tag.TAG_LIST)) {
            // if it does, we get it as a list of strings
            ListTag foodListTag = compoundTag.getList(NBT_FOOD_HISTORY_ID, Tag.TAG_STRING);
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
                    SOLDisco.LOGGER.error("{} found in nbt, but not in registry when decoding data. Data will be removed!", itemNamespace);
                }

                item.ifPresent(this::add);
            }
        }

        FOOD_HISTORY_COMPONENT_KEY.sync(player);
        updateMaxHealth();
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag compoundTag, HolderLookup.@NotNull Provider registryLookup) {
        ListTag foodListTag = new ListTag();
        for (Item foodItem : history) {
            // get the namespaced ids for all of our foods
            foodListTag.add(StringTag.valueOf(foodItem.toString()));
        }
        compoundTag.put(NBT_FOOD_HISTORY_ID, foodListTag);
    }

    @Override
    public double getMaxHealth() {
        return SOLDisco.applyFoodFormula(size());
    }

    @Override
    public void sync() {
        FOOD_HISTORY_COMPONENT_KEY.sync(player, this);
        updateMaxHealth();
    }

    @Override
    public Iterator<Item> iterator() {
        return history.iterator();
    }

    @Override
    public Stream<Item> stream() {
        return history.stream();
    }

    @Override
    public boolean add(Item toAdd) {
        return add(toAdd, true);
    }

    @Override
    public boolean add(Item toAdd, boolean sync) {
        boolean result = history.add(toAdd);
        if (result && sync) {
            FOOD_HISTORY_COMPONENT_KEY.sync(
                    player,
                    (buf, p) ->
                            writeAddPacket(buf, toAdd)
            );
            updateMaxHealth();
        }
        return result;
    }

    @Override
    public boolean remove(Item toRemove) {
        return remove(toRemove, true);
    }

    @Override
    public boolean remove(Item toRemove, boolean sync) {
        boolean result = history.remove(toRemove);
        if (result && sync) {
            FOOD_HISTORY_COMPONENT_KEY.sync(
                    player,
                    (buf, p) ->
                            writeRemovePacket(buf, toRemove)
            );
            updateMaxHealth();
        }
        return result;
    }

    @Override
    public boolean contains(Item toQuery) {
        return history.contains(toQuery);
    }

    @Override
    public int size() {
        return history.size();
    }

    @Override
    public boolean isEmpty() {
        return history.isEmpty();
    }

    @Override
    public void clear() {
        history.clear();
        FOOD_HISTORY_COMPONENT_KEY.sync(player);
        updateMaxHealth();
    }

    @Override
    public boolean isRequiredOnClient() {
        return false;
    }

    @Override
    public boolean shouldSyncWith(ServerPlayer player) {
        return player == this.player; // only sync with the provider itself
    }

    @Override
    public void writeSyncPacket(RegistryFriendlyByteBuf buf, ServerPlayer player) {
        buf.writeBoolean(true); // true means a full re-send
        buf.writeInt(history.size()); // write how many ids we're about to send
        history.forEach(item -> { // write all integer ids
            buf.writeById(BuiltInRegistries.ITEM::getId, item);
        });
    }

    private void writeAddPacket(RegistryFriendlyByteBuf buf, Item toAdd) {
        buf.writeBoolean(false);
        buf.writeBoolean(true); // add is true
        buf.writeById(BuiltInRegistries.ITEM::getId, toAdd);
    }

    private void writeRemovePacket(RegistryFriendlyByteBuf buf, Item toRemove) {
        buf.writeBoolean(false);
        buf.writeBoolean(false); // remove is false
        buf.writeById(BuiltInRegistries.ITEM::getId, toRemove);
    }

    @Override
    public void applySyncPacket(RegistryFriendlyByteBuf buf) {
        // now we hope to god that our registries match, cuz otherwise we're mildly cooked (might have steak showing up
        //   as an acacia boat). i am not smart enough to do this properly
        if (buf.readBoolean()) {
            // we have to do a full sync, starting with clearing the current data
            history.clear();

            // next value is a long saying how many ids we're about to send
            int length = buf.readInt();
            while (length > 0) {
                length--;
                history.add(buf.readById(BuiltInRegistries.ITEM::byIdOrThrow));
            }

            // we *should* have equivalent food histories now

            return;
        }

        // no full sync, just apply the one id
        // first, we gotta find out what we're doing
        boolean add = buf.readBoolean();
        Item food = buf.readById(BuiltInRegistries.ITEM::byIdOrThrow);
        boolean result;
        if (add) {
            // ok now we add a food
            result = history.add(food);

            if (!result) {
                SOLDisco.LOGGER.error("tried to add new food {}, but it was already in the history!", food);
            }
        } else {
            // otherwise, remove a food
            result = history.remove(food);

            if (!result) {
                SOLDisco.LOGGER.error("tried to remove new food {}, but it was already not in the history!", food);
            }
        }
    }
}
