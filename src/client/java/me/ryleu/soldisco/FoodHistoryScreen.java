package me.ryleu.soldisco;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.resources.ResourceLocation;

public class FoodHistoryScreen extends BaseUIModelScreen<FlowLayout> {
    public FoodHistoryScreen() {
        super(FlowLayout.class, DataSource.asset(ResourceLocation.fromNamespaceAndPath(SOLDisco.MOD_ID, "food_history_ui")));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.children().forEach(System.out::println);
        rootComponent.childById(ButtonComponent.class, "the-button").onPress(button -> {
            if (minecraft != null && minecraft.player != null) {

                ((IPlayer) minecraft.player).soldisco$getFoodHistory().iterator().forEachRemaining(item ->
                        SOLDisco.LOGGER.info("{} has {}", minecraft.player.getName(), item));
            }

            System.out.println("click");
        });
    }
}
