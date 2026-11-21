package com.aiotter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Modelo 64x64: cuerpo largo, cabeza con visor, antena, pantalla en el pecho, 4 patas y cola. */
public class AiOtterModel extends EntityModel<AiOtterEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(AiOtterMod.MOD_ID, "ai_otter"), "main");

    private final ModelPart root, head, tail;
    private final ModelPart[] legs = new ModelPart[4];

    public AiOtterModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.tail = root.getChild("tail");
        legs[0] = root.getChild("leg_fl");
        legs[1] = root.getChild("leg_fr");
        legs[2] = root.getChild("leg_bl");
        legs[3] = root.getChild("leg_br");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();

        PartDefinition body = r.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3f, -3f, -6f, 6f, 6f, 12f),
                PartPose.offset(0f, 17f, 0f));
        body.addOrReplaceChild("screen",
                CubeListBuilder.create().texOffs(0, 36).addBox(-2f, -2f, -7f, 4f, 3f, 1f),
                PartPose.ZERO);

        PartDefinition head = r.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 20).addBox(-2.5f, -2.5f, -5f, 5f, 5f, 5f),
                PartPose.offset(0f, 15f, -6f));
        head.addOrReplaceChild("visor",
                CubeListBuilder.create().texOffs(0, 32).addBox(-2.5f, -2f, -6f, 5f, 2f, 1f),
                PartPose.ZERO);
        head.addOrReplaceChild("antenna",
                CubeListBuilder.create().texOffs(30, 32).addBox(-0.5f, -5f, -2.5f, 1f, 2f, 1f),
                PartPose.ZERO);

        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 42).addBox(-1f, 0f, -1f, 2f, 4f, 2f);
        r.addOrReplaceChild("leg_fl", leg, PartPose.offset(2.5f, 20f, -4f));
        r.addOrReplaceChild("leg_fr", leg, PartPose.offset(-2.5f, 20f, -4f));
        r.addOrReplaceChild("leg_bl", leg, PartPose.offset(2.5f, 20f, 4f));
        r.addOrReplaceChild("leg_br", leg, PartPose.offset(-2.5f, 20f, 4f));

        r.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(20, 36).addBox(-1f, -1f, 0f, 2f, 2f, 8f),
                PartPose.offset(0f, 17f, 6f));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(AiOtterEntity e, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * ((float) Math.PI / 180f);
        head.xRot = headPitch * ((float) Math.PI / 180f);
        float swing = 1.4f * limbSwingAmount;
        legs[0].xRot = Mth.cos(limbSwing * 0.6662f) * swing;
        legs[3].xRot = Mth.cos(limbSwing * 0.6662f) * swing;
        legs[1].xRot = Mth.cos(limbSwing * 0.6662f + (float) Math.PI) * swing;
        legs[2].xRot = Mth.cos(limbSwing * 0.6662f + (float) Math.PI) * swing;
        tail.yRot = Mth.cos(ageInTicks * 0.25f) * 0.25f;
        tail.xRot = -0.2f + Mth.cos(limbSwing * 0.6662f) * 0.2f * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay,
                               float r, float g, float b, float a) {
        root.render(pose, buffer, light, overlay, r, g, b, a);
    }
}
