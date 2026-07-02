package com.vividsmod.entity;

import com.vividsmod.entity.ai.VividDefendAlliesGoal;
import com.vividsmod.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * Vivids del Baño: raza separada, tranquila, que vive en baños abandonados,
 * alcantarillas y cuevas húmedas. Defienden a otros Vivids del Baño si son
 * atacados, pero no inician peleas.
 */
public class VividBathEntity extends AbstractVividEntity {

    public VividBathEntity(EntityType<? extends VividBathEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractVividEntity.createBaseAttributes()
                .add(Attributes.MAX_HEALTH, 22.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.ATTACK_DAMAGE, 2.5D);
    }

    @Override
    protected void registerGoals() {
        registerCommonGoals();
        this.targetSelector.addGoal(2, new VividDefendAlliesGoal<>(this, VividBathEntity.class));
    }

    @Override
    public void tick() {
        super.tick();
        // Pequeño detalle ambiental: burbujas ocasionales, ver requisito
        // "algunos tienen espuma / detalles de agua".
        if (this.level().isClientSide() && this.random.nextInt(30) == 0) {
            this.level().addParticle(ParticleTypes.BUBBLE,
                    this.getRandomX(0.4), this.getY() + this.getBbHeight() * 0.8, this.getRandomZ(0.4),
                    0.0, 0.05, 0.0);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.VIVID_BATH_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return ModSounds.VIVID_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.VIVID_DEATH.get();
    }
}
