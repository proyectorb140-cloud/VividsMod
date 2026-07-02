package com.vividsmod.entity;

import com.vividsmod.entity.ai.VividIdleActionGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.pathfinder.BlockPathTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Base común para todas las razas de Vivids (Vivid Normal, Vivito, Vivids del Baño, y
 * cualquier raza futura).
 *
 * Responsabilidades que viven aquí (compartidas por todas las razas):
 *  - Manejo de tamaño (grande / pequeño) y su efecto en hitbox y atributos.
 *  - Sistema de ira "neutral mob" (como abejas/lobos): se enoja al recibir daño de un
 *    jugador, ataca, y luego de un tiempo vuelve a ser amigable.
 *  - Estados de animación sincronizados (idle, caminar, atacar, acción especial).
 *  - Goals genéricos: evitar lava, abrir puertas, mirar al jugador, caminar en
 *    grupo, defender aliados.
 *
 * Lo que SÍ debe implementar cada subclase (raza):
 *  - createAttributes(): valores base de vida/velocidad/daño de esa raza.
 *  - registerGoals(): además de llamar a los goals comunes de aquí, agrega los
 *    goals específicos de su comportamiento (ej. VividNormalEntity agrega
 *    "seguir si tiene pan"; VividBathEntity agrega partículas de burbujas).
 *  - getAmbientSound()/getHurtSound()/getDeathSound(): sonidos propios.
 */
public abstract class AbstractVividEntity extends PathfinderMob implements NeutralMob {

    // ---------------------------------------------------------------
    // Datos sincronizados cliente-servidor
    // ---------------------------------------------------------------
    private static final EntityDataAccessor<Boolean> DATA_LARGE =
            SynchedEntityData.defineId(AbstractVividEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ACTION =
            SynchedEntityData.defineId(AbstractVividEntity.class, EntityDataSerializers.INT);

    /** Acciones especiales visuales que puede reproducir un Vivid amigable. */
    public enum Action { NONE, SIT, SLEEP, WAVE, JUMP }

    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(25, 45);
    private int remainingPersistentAngerTime;
    @Nullable
    private UUID persistentAngerTarget;

    // Estados de animación (usados por el renderer en el cliente, ver VividModel)
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState hurtAnimationState = new AnimationState();
    public final AnimationState specialActionAnimationState = new AnimationState();
    private int attackAnimationTimeout = 0;
    private Action lastAction = Action.NONE;

    protected AbstractVividEntity(EntityType<? extends AbstractVividEntity> type, Level level) {
        super(type, level);
    }

    /**
     * Atributos base compartidos por cualquier raza de Vivid. Cada subclase
     * debería llamar a esto y luego afinar los valores particulares de su raza.
     */
    public static AttributeSupplier.Builder createBaseAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 0.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_LARGE, true);
        this.entityData.define(DATA_ACTION, Action.NONE.ordinal());
    }

    // ---------------------------------------------------------------
    // Tamaño (grande / pequeño)
    // ---------------------------------------------------------------

    public boolean isLarge() {
        return this.entityData.get(DATA_LARGE);
    }

    public void setLarge(boolean large) {
        this.entityData.set(DATA_LARGE, large);
        this.refreshDimensions();
    }

    /**
     * Grande ~2.5 bloques, pequeño ~1.8 bloques (tamaño del jugador).
     * El EntityType se registra con el tamaño "grande"; aquí escalamos hacia
     * abajo para el tamaño "pequeño" en vez de mantener dos EntityType separados,
     * lo que simplifica agregar más razas en el futuro.
     */
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        EntityDimensions base = super.getDimensions(pose);
        if (isLarge()) {
            return base;
        }
        float scale = 1.8f / 2.5f;
        return base.scale(scale);
    }

    @Override
    public void finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                               @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag tag) {
        boolean large = this.getRandom().nextBoolean();
        this.setLarge(large);
        applySizeAttributeModifiers(large);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, tag);
    }

    /**
     * Ajusta vida/velocidad/daño según el tamaño elegido:
     * grande = más vida y daño, más lento. pequeño = menos vida, más rápido.
     * Se llama una sola vez al spawnear (y también se puede llamar manualmente
     * si cambias el tamaño desde un comando o al cargar de NBT).
     */
    protected void applySizeAttributeModifiers(boolean large) {
        double healthMultiplier = large ? 1.6 : 0.85;
        double speedMultiplier = large ? 0.8 : 1.25;
        double damageMultiplier = large ? 1.4 : 0.9;

        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(
                createBaseAttributes().build().getValue(Attributes.MAX_HEALTH) * healthMultiplier);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(
                createBaseAttributes().build().getValue(Attributes.MOVEMENT_SPEED) * speedMultiplier);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(
                createBaseAttributes().build().getValue(Attributes.ATTACK_DAMAGE) * damageMultiplier);
        this.setHealth(this.getMaxHealth());
    }

    // ---------------------------------------------------------------
    // Acción especial (sit/sleep/wave/jump) sincronizada
    // ---------------------------------------------------------------

    public Action getAction() {
        return Action.values()[this.entityData.get(DATA_ACTION)];
    }

    public void setAction(Action action) {
        this.entityData.set(DATA_ACTION, action.ordinal());
    }

    public boolean isSitting() {
        return getAction() == Action.SIT;
    }

    public boolean isSleepingPose() {
        return getAction() == Action.SLEEP;
    }

    // ---------------------------------------------------------------
    // Ira (NeutralMob) - se enoja al recibir daño, ataca, luego se calma
    // ---------------------------------------------------------------

    public boolean isAngry() {
        return this.getTarget() != null;
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    @Override
    public void setRemainingPersistentAngerTime(int time) {
        this.remainingPersistentAngerTime = time;
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID target) {
        this.persistentAngerTarget = target;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    // ---------------------------------------------------------------
    // Goals comunes a toda raza. Las subclases llaman a esto desde su propio
    // registerGoals() y luego agregan lo específico de su comportamiento.
    // ---------------------------------------------------------------
    protected void registerCommonGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.15D, true));
        this.goalSelector.addGoal(2, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new VividIdleActionGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());

        // Evitar lava agresivamente
        this.setPathfindingMalus(BlockPathTypes.LAVA, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.updatePersistentAnger((ServerLevel) this.level(), true);
    }

    // ---------------------------------------------------------------
    // Animaciones (server-driven state, se leen en el cliente)
    // ---------------------------------------------------------------
    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            updateAnimationStates();
        }
    }

    private void updateAnimationStates() {
        // Idle vs caminar
        if (this.walkAnimation.speed() > 1.0E-5F && getAction() == Action.NONE) {
            walkAnimationState.startIfStopped(this.tickCount);
            idleAnimationState.stop();
        } else if (getAction() == Action.NONE) {
            idleAnimationState.startIfStopped(this.tickCount);
            walkAnimationState.stop();
        } else {
            idleAnimationState.stop();
            walkAnimationState.stop();
        }

        // Acción especial: reinicia la animación solo cuando la acción cambia
        Action current = getAction();
        if (current != Action.NONE && current != lastAction) {
            specialActionAnimationState.start(this.tickCount);
        } else if (current == Action.NONE) {
            specialActionAnimationState.stop();
        }
        lastAction = current;

        // Ataque
        if (attackAnimationTimeout > 0) {
            attackAnimationTimeout--;
        } else {
            attackAnimationState.stop();
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean result = super.doHurtTarget(target);
        if (result) {
            this.attackAnimationState.start(this.tickCount);
            this.attackAnimationTimeout = 10;
        }
        return result;
    }

    @Override
    public void handleEntityEvent(byte id) {
        super.handleEntityEvent(id);
        if (id == 2) { // hurt
            this.hurtAnimationState.start(this.tickCount);
        }
    }

    // ---------------------------------------------------------------
    // Movimiento en grupo: al spawnear en grupo, Minecraft ya intenta agrupar
    // mobs de superficie cercanos entre sí; reforzamos con follow range alto
    // y con que compartan target al defenderse (ver VividDefendAlliesGoal en
    // cada subclase).
    // ---------------------------------------------------------------

    @Override
    public boolean removeWhenFarAway(double distanceSq) {
        return false;
    }
}
