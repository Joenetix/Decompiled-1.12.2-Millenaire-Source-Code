package org.millenaire.common.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;

public class AlwaysTrueCriterionInstance extends AbstractCriterionTriggerInstance {
    public AlwaysTrueCriterionInstance(ResourceLocation rl, ContextAwarePredicate player) {
        super(rl, player);
    }

    public static AlwaysTrueCriterionInstance alwaysTrue(ResourceLocation rl) {
        return new AlwaysTrueCriterionInstance(rl, ContextAwarePredicate.ANY);
    }

    @Override
    public JsonObject serializeToJson(SerializationContext context) {
        return new JsonObject();
    }
}
