package me.ryleu.soldisco;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOLDisco implements ModInitializer {
	public static final String MOD_ID = "soldisco";
	public static final me.ryleu.soldisco.SOLDiscoConfig CONFIG = me.ryleu.soldisco.SOLDiscoConfig.createAndLoad();
	private static Expression foodFormula = new ExpressionBuilder(CONFIG.formula()).variable("foodsEaten").build();

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static double applyFoodFormula(int foodsEaten) {
		// we don't need checks because those are done by owo-lib
        return Math.max(
				foodFormula.setVariable("foodsEaten", foodsEaten).evaluate(),
				CONFIG.minHp()
		);
    }

    @Override
	public void onInitialize() {
		LOGGER.info("{} loaded!", MOD_ID);

		// register foodhistory command
		CommandRegistrationCallback.EVENT.register(
				(commandDispatcher, commandBuildContext, commandSelection) ->
						FoodHistoryCommand.register(commandDispatcher, commandBuildContext)
		);

		// update the max health formula when the config changes
		CONFIG.subscribeToFormula(
				newFormula ->
						foodFormula = new ExpressionBuilder(newFormula)
								.variable("foodsEaten")
								.build()
		);
	}
}