package org.millenaire.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

public class PanelModel extends Model {
    private final ModelPart board;

    public PanelModel(ModelPart root) {
        super(RenderType::entitySolid);
        this.board = root.getChild("board");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // 24x24 pixel board, centred.
        // The original logic was -12, -12, -1 (24, 24, 2).
        // In 1.20, we use 16x16 usually, but Millenaire panels are large (1.5 blocks).
        // Let's replicate the original size: 24 wide, 24 high, 2 deep.
        partdefinition.addOrReplaceChild("board",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-12.0F, -12.0F, -1.0F, 24.0F, 24.0F, 2.0F),
                PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
            float red, float green, float blue, float alpha) {
        board.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
