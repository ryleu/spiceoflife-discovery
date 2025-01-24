package me.ryleu.soldisco;

import com.mojang.brigadier.CommandDispatcher;
import me.ryleu.soldisco.component.IFoodHistory;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.spongepowered.include.com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.Iterator;

public class FoodHistoryCommand {
    public static final int PERMISSION_LEVEL = 2;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(
                Commands.literal("foodhistory")
                        .requires(
                                source -> source.hasPermission(
                                        PERMISSION_LEVEL
                                )
                        )
                        .then(
                                Commands.literal("clear")
                                        .executes(
                                                commandContext -> clearFoods(
                                                        commandContext.getSource(),
                                                        ImmutableList.of(
                                                                commandContext.getSource().getPlayerOrException()
                                                        )
                                                )
                                        )
                                        .then(
                                                Commands.argument(
                                                        "targets",
                                                                EntityArgument.players()
                                                        )
                                                        .executes(
                                                                commandContext -> clearFoods(
                                                                        commandContext.getSource(),
                                                                        EntityArgument.getPlayers(
                                                                                commandContext,
                                                                                "targets"
                                                                        )
                                                                )
                                                        )
                                                        .then(
                                                                Commands.argument(
                                                                        "food",
                                                                                ResourceArgument.resource(
                                                                                        context,
                                                                                        Registries.ITEM
                                                                                )
                                                                        )
                                                                        .executes(
                                                                                commandContext -> clearFood(
                                                                                        commandContext.getSource(),
                                                                                        EntityArgument.getPlayers(
                                                                                                commandContext,
                                                                                                "targets"
                                                                                        ),
                                                                                        ResourceArgument.getResource(
                                                                                                commandContext,
                                                                                                "food",
                                                                                                Registries.ITEM
                                                                                        )
                                                                                )
                                                                        )
                                                        )
                                        )
                        )
                        .then(
                                Commands.literal("add")
                                        .then(
                                                Commands.argument(
                                                                "food",
                                                                ResourceArgument.resource(
                                                                        context,
                                                                        Registries.ITEM
                                                                )
                                                        )
                                                        .executes(
                                                                commandContext -> addFood(
                                                                        commandContext.getSource(),
                                                                        ImmutableList.of(commandContext.getSource().getPlayerOrException()),
                                                                        ResourceArgument.getResource(
                                                                                commandContext,
                                                                                "food",
                                                                                Registries.ITEM
                                                                        )
                                                                )
                                                        )
                                                        .then(
                                                                Commands.argument(
                                                                        "targets",
                                                                        EntityArgument.players()
                                                                )
                                                                        .executes(
                                                                                commandContext -> addFood(
                                                                                        commandContext.getSource(),
                                                                                        EntityArgument.getPlayers(
                                                                                                commandContext,
                                                                                                "targets"
                                                                                        ),
                                                                                        ResourceArgument.getResource(
                                                                                                commandContext,
                                                                                                "food",
                                                                                                Registries.ITEM
                                                                                        )
                                                                                )
                                                                        )
                                                        )
                                        )
                        )
                        .then(
                                Commands.literal("get")
                                        .executes(
                                                commandContext -> getFood(
                                                        commandContext.getSource(),
                                                        commandContext.getSource().getPlayerOrException()
                                                )
                                        )
                                        .then(
                                                Commands.argument(
                                                        "target",
                                                        EntityArgument.player()
                                                )
                                                        .executes(
                                                                commandContext -> getFood(
                                                                        commandContext.getSource(),
                                                                        EntityArgument.getPlayer(
                                                                                commandContext,
                                                                                "target"
                                                                        )
                                                                )
                                                        )
                                        )
                        )
                        .then(
                                Commands.literal("query")
                                        .then(
                                                Commands.argument(
                                                        "food",
                                                        ResourceArgument.resource(
                                                                context,
                                                                Registries.ITEM
                                                        )
                                                )
                                                        .executes(
                                                                commandContext -> queryFood(
                                                                        commandContext.getSource(),
                                                                        ImmutableList.of(commandContext.getSource().getPlayerOrException()),
                                                                        ResourceArgument.getResource(
                                                                                commandContext,
                                                                                "food",
                                                                                Registries.ITEM
                                                                        )
                                                                )
                                                        )
                                                        .then(
                                                                Commands.argument(
                                                                        "targets",
                                                                        EntityArgument.players()
                                                                )
                                                                        .executes(
                                                                                commandContext -> queryFood(
                                                                                        commandContext.getSource(),
                                                                                        EntityArgument.getPlayers(commandContext, "targets"),
                                                                                        ResourceArgument.getResource(
                                                                                                commandContext,
                                                                                                "food",
                                                                                                Registries.ITEM
                                                                                        )
                                                                                )
                                                                        )
                                                        )
                                        )
                        )
        );
    }

    private static int addFood(
            CommandSourceStack sourceStack,
            Collection<? extends Player> players,
            Holder<Item> foodHolder
    ) {
        Item food = foodHolder.value();
        final int result = (int) players.stream().filter(player ->
                ((IPlayer) player).soldisco$getFoodHistory().add(food)
        ).count();

        Component foodName = food.getDefaultInstance().getDisplayName();

        if (result == 0) {
            if (players.size() == 1) {
                sourceStack.sendFailure(
                        Component.translatable(
                                "commands.soldisco.foodhistory.add.failed.single",
                                players.iterator().next().getDisplayName(),
                                foodName
                        )
                );
            } else {
                sourceStack.sendFailure(
                        Component.translatable(
                                "commands.soldisco.foodhistory.add.failed.multiple",
                                players.size(),
                                foodName
                        )
                );
            }
        } else if (players.size() == 1) {
            sourceStack.sendSuccess(
                    () -> Component.translatable(
                            "commands.soldisco.foodhistory.add.success.single",
                            foodName,
                            players.iterator().next().getDisplayName()
                    ),
                    true
            );
        } else {
            sourceStack.sendSuccess(
                    () -> Component.translatable(
                            "commands.soldisco.foodhistory.add.success.multiple",
                            foodName,
                            result
                    ),
                    true
            );
        }

        return result;
    }

    private static int clearFoods(
            CommandSourceStack sourceStack,
            Collection<? extends Player> players
    ) {
        final int result = (int) players.stream().filter((player -> {
            IFoodHistory foodHistory = ((IPlayer) player).soldisco$getFoodHistory();
            boolean success = !foodHistory.isEmpty();
            foodHistory.clear();
            return success;
        })).count();

        if (result == 0) {
            if (players.size() == 1) {
                sourceStack.sendFailure(
                        Component.translatable(
                                "commands.soldisco.foodhistory.clear.everything.failed.single",
                                players.iterator().next().getDisplayName()
                        )
                );
            } else {
                sourceStack.sendFailure(
                        Component.translatable(
                                "commands.soldisco.foodhistory.clear.everything.failed.multiple",
                                players.size()
                        )
                );
            }
        } else if (players.size() == 1) {
            sourceStack.sendSuccess(
                    () -> Component.translatable(
                            "commands.soldisco.foodhistory.clear.everything.success.single",
                            players.iterator().next().getDisplayName()
                    ),
                    true

            );
        } else {
            sourceStack.sendSuccess(
                    () -> Component.translatable(
                            "commands.soldisco.foodhistory.clear.everything.success.multiple",
                            result
                    ),
                    true
            );
        }

        return result;
    }

    private static int clearFood(
            CommandSourceStack sourceStack,
            Collection<? extends Player> players,
            Holder<Item> foodHolder
    ) {
        Item food = foodHolder.value();
        final int result = (int) players.stream().filter(player ->
                ((IPlayer) player).soldisco$getFoodHistory().remove(food)
        ).count();

        Component foodName = food.getDefaultInstance().getDisplayName();

        if (result == 0) {
            if (players.size() == 1) {
                sourceStack.sendFailure(
                        Component.translatable(
                                "commands.soldisco.foodhistory.clear.specific.failed.single",
                                players.iterator().next().getDisplayName(),
                                foodName
                        )
                );
            } else {
                sourceStack.sendFailure(
                        Component.translatable(
                                "commands.soldisco.foodhistory.clear.specific.failed.multiple",
                                players.size(),
                                foodName
                        )
                );
            }
        } else if (players.size() == 1) {
            sourceStack.sendSuccess(
                    () -> Component.translatable(
                            "commands.soldisco.foodhistory.clear.specific.success.single",
                            foodName,
                            players.iterator().next().getDisplayName()
                    ),
                    true
            );
        } else {
            sourceStack.sendSuccess(
                    () -> Component.translatable(
                            "commands.soldisco.foodhistory.clear.specific.success.multiple",
                            foodName,
                            result
                    ),
                    true
            );
        }

        return result;
    }

    private static int getFood(
            CommandSourceStack sourceStack,
            Player player
    ) {
        IFoodHistory foodHistory = ((IPlayer) player).soldisco$getFoodHistory();

        MutableComponent component = Component.literal("[");

        Iterator<Item> iterator = foodHistory.iterator();
        while (iterator.hasNext()) {
            Item food = iterator.next();
            component.append(
                    Component.literal(StringTag.quoteAndEscape(food.toString()))
                            .withStyle(ChatFormatting.GREEN)
            );
            if (iterator.hasNext()) {
                component.append(", ");
            }
        }

        component.append("]");

        sourceStack.sendSuccess(
                () -> Component.translatable(
                        "commands.soldisco.foodhistory.get",
                        player.getDisplayName(),
                        foodHistory.size(),
                        component
                ),
                false
        );

        return foodHistory.size();
    }

    private static int queryFood(
            CommandSourceStack sourceStack,
            Collection<? extends Player> players,
            Holder<Item> foodHolder
    ) {
        Item food = foodHolder.value();
        final int result = (int) players.stream().filter(player ->
                ((IPlayer) player).soldisco$getFoodHistory().contains(food)
        ).count();

        Component foodName = food.getDefaultInstance().getDisplayName();

        if (result == 0) {
            if (players.size() == 1) {
                sourceStack.sendFailure(
                        Component.translatable(
                                "commands.soldisco.foodhistory.query.failed.single",
                                players.iterator().next().getDisplayName(),
                                foodName
                        )
                );
            } else {
                sourceStack.sendFailure(
                        Component.translatable(
                                "commands.soldisco.foodhistory.query.failed.multiple",
                                players.size(),
                                foodName
                        )
                );
            }
        } else if (players.size() == 1) {
            sourceStack.sendSuccess(
                    () -> Component.translatable(
                            "commands.soldisco.foodhistory.query.success.single",
                            foodName,
                            players.iterator().next().getDisplayName()
                    ),
                    true
            );
        } else {
            sourceStack.sendSuccess(
                    () -> Component.translatable(
                            "commands.soldisco.foodhistory.query.success.multiple",
                            result,
                            foodName
                    ),
                    true
            );
        }

        return result;
    }
}
