package com.vividsmod.client.model;

import com.vividsmod.entity.AbstractVividEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/**
 * Modelo cúbico del Vivid: un cuerpo en forma de puño, dos dedos levantados
 * (índice y medio) que hacen de orejas/antenas, un pulgar usado como gorra,
 * y dos piernas.
 *
 * El mismo modelo se reutiliza para las 3 razas (Vivid Normal, Vivito, Vivids
 * del Baño); lo que cambia entre ellas es la textura (ver VividRenderer) y,
 * en el caso del Vivito, la escala global (manejada por
 * AbstractVividEntity#getDimensions, no aquí).
 *
 * UV de la textura (64x64), debe coincidir con gen_textures.py / el archivo
 * .png real que pongas en textures/entity/:
 *   body   -> texOffset(0,0)   tamaño 8x10x8
 *   legL   -> texOffset(0,18)  tamaño 4x6x4
 *   legR   -> texOffset(16,18) tamaño 4x6x4
 *   index  -> texOffset(32,18) tamaño 3x6x3
 *   middle -> texOffset(44,18) tamaño 3x7x3
 *   thumb  -> texOffset(0,28)  tamaño 5x2x5
 */
public class VividModel<T extends AbstractVividEntity> extends EntityModel<T> {

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart legLeft;
    private final ModelPart legRight;
    private final ModelPart fingerIndex;
    private final ModelPart fingerMiddle;
    private final ModelPart thumb;

    public VividModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.legLeft = root.getChild("leg_left");
        this.legRight = root.getChild("leg_right");
        this.fingerIndex = body.getChild("finger_index");
        this.fingerMiddle = body.getChild("finger_middle");
        this.thumb = body.getChild("thumb");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();

        PartDefinition body = parts.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F),
                PartPose.offset(0.0F, 14.0F, 0.0F));

        // Dedos (índice y medio) nacen de la parte superior del cuerpo
        body.addOrReplaceChild("finger_index",
                CubeListBuilder.create().texOffs(32, 18).addBox(-1.5F, -6.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offsetAndRotation(-2.0F, -10.0F, 0.0F, 0.0F, 0.0F, -0.12F));

        body.addOrReplaceChild("finger_middle",
                CubeListBuilder.create().texOffs(44, 18).addBox(-1.5F, -7.0F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offsetAndRotation(1.5F, -10.0F, 0.0F, 0.0F, 0.0F, 0.1F));

        // Pulgar como gorra hacia atrás: plano, sobre la parte trasera-superior
        body.addOrReplaceChild("thumb",
                CubeListBuilder.create().texOffs(0, 28).addBox(-2.5F, -1.5F, -2.0F, 5.0F, 2.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, -9.5F, 2.0F, -0.35F, 0.0F, 0.0F));

        parts.addOrReplaceChild("leg_left",
                CubeListBuilder.create().texOffs(0, 18).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offset(-2.2F, 14.0F, 0.0F));

        parts.addOrReplaceChild("leg_right",
                CubeListBuilder.create().texOffs(16, 18).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offset(2.2F, 14.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                           float ageInTicks, float netHeadYaw, float headPitch) {
        // Reset
        body.xRot = 0; body.yRot = 0; body.zRot = 0; body.y = 14.0F; body.x = 0; body.z = 0;
        legLeft.xRot = 0; legRight.xRot = 0;
        fingerIndex.xRot = 0; fingerMiddle.xRot = 0;

        boolean walking = limbSwingAmount > 1.0E-3F;
        AbstractVividEntity.Action action = entity.getAction();

        // El "cuello" del Vivid es todo el cuerpo (la cabeza-puño); lo giramos
        // levemente para mirar al jugador, salvo si está durmiendo/sentado.
        if (action != AbstractVividEntity.Action.SLEEP) {
            body.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.6F;
            body.xRot = headPitch * ((float) Math.PI / 180F) * 0.6F;
        }

        switch (action) {
            case SIT -> {
                body.y += 3.0F;
                legLeft.xRot = -(float) Math.PI / 2.2F;
                legRight.xRot = -(float) Math.PI / 2.2F;
            }
            case SLEEP -> {
                root.xRot = (float) Math.PI / 2.0F;
                body.y += 2.0F;
            }
            case WAVE -> {
                float wave = Mth.sin(ageInTicks * 0.6F) * 0.6F;
                fingerIndex.zRot = -0.12F - wave;
                fingerMiddle.zRot = 0.1F + wave * 0.5F;
            }
            case JUMP -> body.y -= 2.0F;
            default -> { /* idle / caminar normal, ver abajo */ }
        }

        // Animación de caminar (piernas) — no aplica si está sentado/durmiendo
        if (action != AbstractVividEntity.Action.SIT && action != AbstractVividEntity.Action.SLEEP) {
            legLeft.xRot += Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
            legRight.xRot += Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        }

        // Balanceo idle sutil de los dedos, como si "respirara"
        if (action == AbstractVividEntity.Action.NONE) {
            float idle = Mth.sin(ageInTicks * 0.08F) * 0.03F;
            fingerIndex.zRot = -0.12F + idle;
            fingerMiddle.zRot = 0.1F - idle;
        }

        // Animación de ataque: un golpe rápido de todo el cuerpo hacia adelante
        if (entity.attackAnimationState.isStarted()) {
            float attackProgress = getAnimationProgress(entity, ageInTicks, 10);
            body.xRot += Mth.sin(attackProgress * (float) Math.PI) * 0.6F;
        }

        // Reacción al recibir daño: leve sacudida lateral
        if (entity.hurtAnimationState.isStarted()) {
            float hurtProgress = getAnimationProgress(entity, ageInTicks, 8);
            body.zRot += Mth.sin(hurtProgress * (float) Math.PI * 3) * 0.15F * (1.0F - hurtProgress);
        }
    }

    private float getAnimationProgress(T entity, float ageInTicks, int totalTicks) {
        // Aproximación simple sin depender de AnimationState#getTimeInMillis (varía entre versiones);
        // basta para dar sensación de golpe/impacto. Se puede refinar con keyframes más adelante.
        return Mth.clamp((entity.tickCount % totalTicks) / (float) totalTicks, 0.0F, 1.0F);
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack poseStack,
                                com.mojang.blaze3d.vertex.VertexConsumer buffer,
                                int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
