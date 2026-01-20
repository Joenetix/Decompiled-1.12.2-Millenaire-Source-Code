package org.millenaire.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.millenaire.MillenaireRevived;
import org.millenaire.core.block.PanelBlock;
import org.millenaire.core.entity.TileEntityPanel;

public class PanelBlockEntityRenderer implements BlockEntityRenderer<TileEntityPanel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(MillenaireRevived.MODID,
            "textures/entity/panels/default.png");
    private final PanelModel model;

    public PanelBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        // We create the model manually here or register a layer.
        // For simplicity in this revived version without full layer registration setup
        // yet,
        // we can instantiate the parts directly or ideally use the context if we
        // registered the layer.
        // Let's create it manually to avoid complex registration steps in ClientSetup
        // for now.
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("board",
                CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -12.0F, -1.0F, 24.0F, 24.0F, 2.0F),
                PartPose.ZERO);
        ModelPart root = LayerDefinition.create(meshdefinition, 64, 32).bakeRoot();
        this.model = new PanelModel(root);
    }

    @Override
    public void render(TileEntityPanel te, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
            int packedLight, int packedOverlay) {
        poseStack.pushPose();

        // Center the render in the block
        poseStack.translate(0.5D, 0.5D, 0.5D);

        // Get facing direction and calculate rotation
        // The FACING property indicates which direction the panel faces outward
        // After rotation, translate(-0.4375) in local Z will push the panel to the
        // correct wall
        Direction facing = te.getBlockState().getValue(PanelBlock.FACING);

        // Rotation mapping:
        // - NORTH facing: panel faces north, attached to SOUTH wall (need to move +Z)
        // -> rotate 180°
        // - SOUTH facing: panel faces south, attached to NORTH wall (need to move -Z)
        // -> rotate 0°
        // - WEST facing: panel faces west, attached to EAST wall (need to move +X) ->
        // rotate 90°
        // - EAST facing: panel faces east, attached to WEST wall (need to move -X) ->
        // rotate -90°
        float rotation = switch (facing) {
            case NORTH -> 0;
            case SOUTH -> 180;
            case WEST -> -90;
            case EAST -> 90;
            default -> 0;
        };

        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        // Push panel back against wall (in local -Z direction, which becomes world
        // direction based on rotation)
        poseStack.translate(0.0D, 0.0D, -0.4375D);

        // Render the board
        poseStack.pushPose();
        poseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entitySolid(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F,
                1.0F);

        poseStack.popPose();

        // Render Text
        renderText(te, poseStack, bufferSource, packedLight);

        poseStack.popPose();
    }

    private void renderText(TileEntityPanel te, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Font font = Minecraft.getInstance().font;
        String[] lines = te.getRenderText();

        poseStack.pushPose();

        // Position text on the board
        // Original: translate(0.0, 0.25, 0.046666667F)
        // scale(0.010416667F, -0.010416667F, 0.010416667F);
        poseStack.translate(0.0D, 0.25D, 0.05D); // Slightly in front of board
        float scale = 0.010416667F;
        poseStack.scale(scale, -scale, scale);

        int color = 0x000000; // Black text

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line != null && !line.isEmpty()) {
                float x = -font.width(line) / 2.0F;
                float y = i * 10 - 35; // Adjust Y lines
                font.drawInBatch(line, x, y, color, false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0,
                        packedLight);
            }
        }

        poseStack.popPose();
    }
}
