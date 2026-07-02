package com.vividsmod.entity;

import com.vividsmod.entity.ai.VividDefendAlliesGoal;
import com.vividsmod.registry.ModEntityTypes;
import com.vividsmod.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

/**
 * Vivid Normal: la raza más común. Amigable y curioso, sigue al jugador si le
 * da pan, y se enoja/ataca si lo golpean (ver lógica de ira en AbstractVividEntity).
 */
public class VividNormalEntity extends AbstractVividEntity {

    public VividNormalEntity(EntityType<? extends VividNormalEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractVividEntity.createBaseAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    @Override
    protected void registerGoals() {
        registerCommonGoals();
        // Sigue al jugador si tiene pan en la mano (requisito: "si le da comida, lo sigue")
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.1D, Ingredient.of(Items.BREAD), false));
        // Defiende a otros Vivids Normales y a los Vivitos cercanos
        this.targetSelector.addGoal(2, new VividDefendAlliesGoal<>(this, VividNormalEntity.class));
        this.targetSelector.addGoal(3, new VividDefendAlliesGoal<>(this, VivitoEntity.class));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.VIVID_AMBIENT.get();
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
    protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        this.playSound(ModSounds.VIVID_STEP.get(), 0.5F, 1.0F);
    }

    /**
     * Requisito: "Vivito: siempre aparece cerca de un Vivid Normal".
     * Cuando un Vivid Normal spawnea de forma natural (no por huevo de spawn,
     * comando, etc.), hay una probabilidad de que también aparezca un Vivito
     * junto a él, igual que las crías de animales vanilla aparecen con sus padres.
     */
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                         MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData,
                                         @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, tag);

        if (spawnType == MobSpawnType.NATURAL && this.random.nextFloat() < 0.35F) {
            VivitoEntity vivito = ModEntityTypes.VIVITO.get().create(level.getLevel());
            if (vivito != null) {
                vivito.moveTo(this.getX() + (this.random.nextDouble() - 0.5) * 2.0,
                        this.getY(), this.getZ() + (this.random.nextDouble() - 0.5) * 2.0,
                        0.0F, 0.0F);
                vivito.finalizeSpawn(level, difficulty, MobSpawnType.NATURAL, null, null);
                level.addFreshEntity(vivito);
            }
        }
        return result;
    }
}
