package com.vividsmod.entity.ai;

import com.vividsmod.entity.AbstractVividEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Si un Vivid cercano de la misma clase (misma raza) está enojado y tiene un
 * objetivo, este Vivid copia ese objetivo y se une a la pelea. Esto cubre el
 * requisito "defender a otros Vivids de su misma raza" y, junto a
 * AbstractVividEntity#isChildLike(), también protege a los Vivitos cercanos
 * cuando estos son atacados (ver uso en VividNormalEntity).
 */
public class VividDefendAlliesGoal<T extends AbstractVividEntity> extends TargetGoal {

    private final Class<T> allyClass;
    private LivingEntity foundTarget;

    public VividDefendAlliesGoal(AbstractVividEntity vivid, Class<T> allyClass) {
        super(vivid, false);
        this.allyClass = allyClass;
    }

    @Override
    public boolean canUse() {
        AABB searchArea = mob.getBoundingBox().inflate(16.0, 8.0, 16.0);
        List<T> allies = mob.level().getEntitiesOfClass(allyClass, searchArea,
                ally -> ally != mob && ally.isAlive());

        for (T ally : allies) {
            LivingEntity allyTarget = ally.getTarget();
            if (allyTarget != null && allyTarget.isAlive()) {
                this.foundTarget = allyTarget;
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        mob.setTarget(foundTarget);
        super.start();
    }
}
