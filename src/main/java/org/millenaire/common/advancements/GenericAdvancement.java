package org.millenaire.common.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.MillenaireRevived;

public class GenericAdvancement extends SimpleCriterionTrigger<AlwaysTrueCriterionInstance> {
    private final String key;
    private final ResourceLocation triggerRL;

    public GenericAdvancement(String key) {
        this.key = key;
        this.triggerRL = new ResourceLocation(MillenaireRevived.MODID, key);
    }

    @Override
    public ResourceLocation getId() {
        return this.triggerRL;
    }

    @Override
    protected AlwaysTrueCriterionInstance createInstance(JsonObject json, ContextAwarePredicate player,
            DeserializationContext context) {
        return new AlwaysTrueCriterionInstance(this.triggerRL, player);
    }

    public String getKey() {
        return this.key;
    }

    public void grant(ServerPlayer player) {
        this.trigger(player, (instance) -> true);

        // Also update Millenaire stats
        MillAdvancements.addToStats(player, this.key);

        // Notify client
        org.millenaire.common.network.ServerSender.sendAdvancementEarned(player, this.key);
    }
}
