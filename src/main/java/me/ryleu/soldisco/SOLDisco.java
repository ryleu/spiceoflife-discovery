package me.ryleu.soldisco;

import me.ryleu.soldisco.command.FoodHistoryCommand;
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
		double toReturn = Math.max(
				foodFormula.setVariable("foodsEaten", foodsEaten).evaluate(),
				CONFIG.minHp()
		);

		// if the maxHp isn't -1, then we use it as a max
		double maxHp = CONFIG.maxHp();
		if (maxHp != -1) {
			toReturn = Math.min(maxHp, toReturn);
		}

        return toReturn;
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