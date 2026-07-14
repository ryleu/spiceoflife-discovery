package me.ryleu.soldisco;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

@Modmenu(modId = SOLDisco.MOD_ID)
@Config(name = "soldisco",wrapperName = "SOLDiscoConfig")
@SuppressWarnings("unused")
public class ConfigModel {
    @PredicateConstraint("minHpPredicate")
    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    public double minHp = 1;

    @PredicateConstraint("maxHpPredicate")
    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    public double maxHp = -1;

    public static boolean minHpPredicate(double d) {
        return d >= 1;
    }

    public static boolean maxHpPredicate(double d) {
        return d == -1 || d >= 1;
    }

    @Hook
    @PredicateConstraint("formulaPredicate")
    @Sync(Option.SyncMode.OVERRIDE_CLIENT)
    public String formula = "6 + 2 * floor(foodsEaten / 5)";

    public static boolean formulaPredicate(String string) {
        try {
            Expression expression = new ExpressionBuilder(string)
                    .variable("foodsEaten")
                    .build()
                    .setVariable("foodsEaten", 0);
            return expression.validate().isValid();
        } catch (Exception e){
            return false;
        }
    }
}
