package org.millenaire.client;

import javax.annotation.Nonnull;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.millenaire.entities.Citizen;

public class CitizenRenderer extends MobRenderer<Citizen, VillagerModel<Citizen>> {
    private static final ResourceLocation VILLAGER_BASE_SKIN = new ResourceLocation(
            "textures/entity/villager/villager.png");

    public CitizenRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5f);
    }

    @Override
    @Nonnull
    public ResourceLocation getTextureLocation(@Nonnull Citizen entity) {
        if (entity instanceof org.millenaire.common.entity.MillVillager) {
            org.millenaire.common.entity.MillVillager millVillager = (org.millenaire.common.entity.MillVillager) entity;
            if (millVillager.texture != null) {
                return millVillager.texture;
            }
        }
        return VILLAGER_BASE_SKIN;
    }
}
