package com.vividsmod.entity;

import com.vividsmod.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.EnumSet;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

/**
 * Vivito: versión bebé del Vivid. Más pequeño, más rápido, muy juguetón,
 * nunca ataca primero, y siempre aparece cerca de un Vivid Normal (ver
 * ModEntitySpawns / lógica de spawn en grupo).
 *
 * Nota de diseño: a diferencia de las otras razas, el Vivito no alterna entre
 * "grande" y "pequeño": conceptualmente ES la forma pequeña dentro del ciclo
 * de vida de los Vivids. Por eso fijamos su tamaño a pequeño siempre.
 */
public class VivitoEntity extends AbstractVividEntity {

    public VivitoEntity(EntityType<? extends VivitoEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractVividEntity.createBaseAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D); // nunca ataca
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(0.55f);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                               @Nullable SpawnGroupData spawnGroupData, @Nullable net.minecraft.nbt.CompoundTag tag) {
        this.setLarge(false);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, tag);
    }

    @Override
    protected void registerGoals() {
        registerCommonGoals();
        // Nunca ataca: quitamos cualquier goal de ataque heredado no aplica aquí porque
        // ATTACK_DAMAGE es 0, así que el MeleeAttackGoal no hace daño relevante; además
        // nunca se le asigna target por HurtByTargetGoal.setAlertOthers() de otro Vivid
        // (solo se defiende huyendo, ver FollowMobGoal hacia el Vivid Normal más cercano).
        this.goalSelector.addGoal(1, new FollowNearestVividNormalGoal(this, 1.25D, 3.0F, 10.0F));
    }

    /**
     * Goal simple: el Vivito busca al VividNormalEntity vivo más cercano dentro
     * de areaSize bloques y camina hacia él manteniendo stopDistance de holgura.
     * Se mantiene como clase interna porque es exclusiva del comportamiento del Vivito.
     */
    private static class FollowNearestVividNormalGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private final VivitoEntity vivito;
        private final double speedModifier;
        private final float stopDistance;
        private final float areaSize;
        private VividNormalEntity parent;
        private int timeToRecalcPath;

        FollowNearestVividNormalGoal(VivitoEntity vivito, double speedModifier, float stopDistance, float areaSize) {
            this.vivito = vivito;
            this.speedModifier = speedModifier;
            this.stopDistance = stopDistance;
            this.areaSize = areaSize;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            java.util.List<VividNormalEntity> nearby = vivito.level().getEntitiesOfClass(
                    VividNormalEntity.class,
                    vivito.getBoundingBox().inflate(areaSize),
                    VividNormalEntity::isAlive);
            if (nearby.isEmpty()) return false;
            parent = nearby.get(0);
            double closest = parent.distanceToSqr(vivito);
            for (VividNormalEntity candidate : nearby) {
                double d = candidate.distanceToSqr(vivito);
                if (d < closest) {
                    closest = d;
                    parent = candidate;
                }
            }
            return vivito.distanceToSqr(parent) > (stopDistance * stopDistance);
        }

        @Override
        public boolean canContinueToUse() {
            return parent != null && parent.isAlive()
                    && vivito.distanceToSqr(parent) > (stopDistance * stopDistance)
                    && vivito.distanceToSqr(parent) < (areaSize * areaSize * 4);
        }

        @Override
        public void start() {
            timeToRecalcPath = 0;
        }

        @Override
        public void stop() {
            parent = null;
        }

        @Override
        public void tick() {
            if (parent == null) return;
            vivito.getLookControl().setLookAt(parent, 10.0F, 30.0F);
            if (--timeToRecalcPath <= 0) {
                timeToRecalcPath = 10;
                vivito.getNavigation().moveTo(parent, speedModifier);
            }
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        // El Vivito nunca ataca de verdad, ni siquiera si por HurtByTargetGoal
        // termina "persiguiendo" a quien lo golpeó (con ATTACK_DAMAGE en 0 esto
        // ya sería inofensivo, pero lo bloqueamos explícitamente para que quede
        // 100% garantizado por el requisito "nunca ataca primero").
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.VIVITO_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return ModSounds.VIVID_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.VIVID_DEATH.get();
    }

    @Override
    public boolean removeWhenFarAway(double distanceSq) {
        return false;
    }
}
