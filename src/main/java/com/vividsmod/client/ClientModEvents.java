package com.vividsmod.client;

import com.vividsmod.Vivids;
import com.vividsmod.client.model.VividModel;
import com.vividsmod.client.renderer.VividRenderer;
import com.vividsmod.registry.ModEntityTypes;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;

/**
 * Registro de todo lo relacionado al cliente (modelos, renderers).
 * Vive en un paquete "client" separado para que nunca se cargue en un
 * servidor dedicado (buena práctica de Forge).
 */
@Mod.EventBusSubscriber(modid = Vivids.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class ClientModEvents {

    public static final ModelLayerLocation VIVID_NORMAL_LAYER =
            new ModelLayerLocation(new ResourceLocation(Vivids.MOD_ID, "vivid_normal"), "main");
    public static final ModelLayerLocation VIVITO_LAYER =
            new ModelLayerLocation(new ResourceLocation(Vivids.MOD_ID, "vivito"), "main");
    public static final ModelLayerLocation VIVID_BATH_LAYER =
            new ModelLayerLocation(new ResourceLocation(Vivids.MOD_ID, "vivid_bath"), "main");

    private static final ResourceLocation TEX_NORMAL = new ResourceLocation(Vivids.MOD_ID, "textures/entity/vivid_normal.png");
    private static final ResourceLocation TEX_VIVITO = new ResourceLocation(Vivids.MOD_ID, "textures/entity/vivito.png");
    private static final ResourceLocation TEX_BATH = new ResourceLocation(Vivids.MOD_ID, "textures/entity/vivid_bath.png");

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(VIVID_NORMAL_LAYER, VividModel::createBodyLayer);
        event.registerLayerDefinition(VIVITO_LAYER, VividModel::createBodyLayer);
        event.registerLayerDefinition(VIVID_BATH_LAYER, VividModel::createBodyLayer);
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.VIVID_NORMAL.get(),
                ctx -> new VividRenderer<>(ctx, TEX_NORMAL, 0.6F, new ResourceLocation(Vivids.MOD_ID, "vivid_normal")));
        event.registerEntityRenderer(ModEntityTypes.VIVITO.get(),
                ctx -> new VividRenderer<>(ctx, TEX_VIVITO, 0.35F, new ResourceLocation(Vivids.MOD_ID, "vivito")));
        event.registerEntityRenderer(ModEntityTypes.VIVID_BATH.get(),
                ctx -> new VividRenderer<>(ctx, TEX_BATH, 0.6F, new ResourceLocation(Vivids.MOD_ID, "vivid_bath")));
    }
}
