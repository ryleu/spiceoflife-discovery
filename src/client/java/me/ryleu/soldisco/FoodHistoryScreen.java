package me.ryleu.soldisco;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.GridLayout;
import me.ryleu.soldisco.component.IFoodHistory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

import static me.ryleu.soldisco.SOLDisco.MOD_ID;

public class FoodHistoryScreen extends BaseUIModelScreen<FlowLayout> {
    private static final Integer COLUMNS = 3;

    public FoodHistoryScreen() {
        super(FlowLayout.class, DataSource.asset(ResourceLocation.fromNamespaceAndPath(MOD_ID, "food_history_ui")));
    }

    @Override
    protected void init() {
        super.init();

        if (this.uiAdapter == null || minecraft == null || minecraft.player == null) return;

        FlowLayout entryArea = this.uiAdapter.rootComponent.childById(FlowLayout.class, "entry-area");

        IFoodHistory foodHistory = ((IPlayer) minecraft.player).soldisco$getFoodHistory();
        Iterator<@NotNull Item> historyIterator = foodHistory
                .stream()
                .sorted(Comparator.comparing(Item::toString))
                .iterator();
        int num_rows = (int) Math.ceil(foodHistory.size() / (double) COLUMNS);

        for (int i = 0; i < num_rows; i++) {
            // for each row
            GridLayout row = this.model.expandTemplate(
                    GridLayout.class,
                    "history-row@%s:food_history_ui".formatted(MOD_ID),
                    Map.of("columns", COLUMNS.toString())
            );
            for (int j = 0; j < COLUMNS; j++) {
                // for each column in the row
                String itemId = "minecraft:air", itemName = "";
                if (historyIterator.hasNext()) {
                    Item item = historyIterator.next();
                    itemId = item.toString();
                    itemName = item.getDescriptionId();
                }

                row.child(
                        this.model.expandTemplate(
                                FlowLayout.class,
                                "history-entry@%s:food_history_ui".formatted(MOD_ID),
                                Map.of(
                                        "item-id", itemId,
                                        "item-name", itemName,
                                        "fill", String.valueOf(100 / COLUMNS)
                                )
                        ), 0, j
                );
            }
            entryArea.child(row);
        }
    }

    @Override
    protected void build(FlowLayout rootComponent) {
    }
}
