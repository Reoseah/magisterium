package io.github.reoseah.magisterium;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class MagisteriumGameRules {
    public static final GameRules.Key<GameRules.BooleanRule> ENABLE_MAGISTERIUM_PLAYGROUNDS = GameRuleRegistry.register( //
            "enableMagisteriumPlaygrounds", //
            GameRules.Category.MISC, //
            GameRuleFactory.createBooleanRule(true) //
    );

    public static void initialize() {
        // NO-OP
    }
}
