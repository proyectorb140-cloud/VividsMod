package com.vividsmod.client.renderer;

import com.vividsmod.client.model.VividModel;
import com.vividsmod.entity.AbstractVividEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer genérico: recibe la textura de la raza como parámetro, así que
 * sirve para Vivid Normal, Vivito y Vivids del Baño sin duplicar código
 * (ver ClientModEvents donde se instancia una vez por raza con su textura).
 */
public class VividRenderer<T extends AbstractVividEntity> extends MobRenderer<T, VividModel<T>> {

    private final ResourceLocation texture;

    public VividRenderer(EntityRendererProvider.Context context, ResourceLocation texture, float shadowSize,
                          ResourceLocation modelId) {
        super(context, new VividModel<T>(context.bakeLayer(
                new net.minecraft.client.model.geom.ModelLayerLocation(modelId, "main"))), shadowSize);
        this.texture = texture;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }

    @Override
    protected float getFlipDegrees(T entity) {
        return 0.0F;
    }
}
