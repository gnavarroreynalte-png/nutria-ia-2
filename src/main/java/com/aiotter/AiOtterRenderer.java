package com.aiotter;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AiOtterRenderer extends MobRenderer<AiOtterEntity, AiOtterModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(AiOtterMod.MOD_ID, "textures/entity/ai_otter.png");

    public AiOtterRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new AiOtterModel(ctx.bakeLayer(AiOtterModel.LAYER)), 0.4f);
    }

    @Override
    public ResourceLocation getTextureLocation(AiOtterEntity entity) { return TEXTURE; }
}
