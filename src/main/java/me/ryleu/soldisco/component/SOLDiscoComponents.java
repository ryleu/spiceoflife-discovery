package me.ryleu.soldisco.component;

import me.ryleu.soldisco.FoodHistory;
import me.ryleu.soldisco.SOLDisco;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentFactory;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;

public final class SOLDiscoComponents implements EntityComponentInitializer {
    public static final ComponentKey<IFoodHistory> FOOD_HISTORY_COMPONENT_KEY =
            ComponentRegistry.getOrCreate(ResourceLocation.fromNamespaceAndPath(SOLDisco.MOD_ID, "food_history"), IFoodHistory.class);

    @Override
    public void registerEntityComponentFactories(@NotNull EntityComponentFactoryRegistry registry) {
        registry.registerFor(Player.class, FOOD_HISTORY_COMPONENT_KEY, new ComponentFactory<>() {
            @Override
            public @NotNull IFoodHistory createComponent(@NotNull Player player) {
                return new FoodHistory(player);
            }
        });
        registry.registerFor(ServerPlayer.class, FOOD_HISTORY_COMPONENT_KEY, new ComponentFactory<>() {
            @Override
            public @NotNull IFoodHistory createComponent(@NotNull ServerPlayer player) {
                return new FoodHistory(player);
            }
        });
    }
}
