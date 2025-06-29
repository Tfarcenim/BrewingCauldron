package tfar.brewingcauldron;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class BrewingCauldronConfig {

    public static final ForgeConfigSpec SPEC;

    public static final BrewingCauldronConfig INSTANCE;

    static {
        Pair<BrewingCauldronConfig,ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(BrewingCauldronConfig::new);
        INSTANCE = pair.getKey();
        SPEC = pair.getValue();
    }

    public final ForgeConfigSpec.BooleanValue HAS_GUI;

    public final ForgeConfigSpec.BooleanValue HAS_OVERLAY;
    public final ForgeConfigSpec.BooleanValue LIMIT_INGREDIENTS;

    public BrewingCauldronConfig(ForgeConfigSpec.Builder builder){
        builder.push("general");
        HAS_GUI = builder.define("has_gui",true);
        HAS_OVERLAY = builder.define("has_overlay",true);
        LIMIT_INGREDIENTS = builder.define("limit_ingredients",true);
        builder.pop();

    }
}
