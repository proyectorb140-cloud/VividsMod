package com.vividsmod.entity.ai;

import com.vividsmod.entity.AbstractVividEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * Goal que hace que un Vivid amigable, cuando no tiene nada mejor que hacer,
 * elija al azar una "acción especial": sentarse, dormir, saludar a un jugador
 * cercano o saltar. Estas acciones son puramente visuales (no bloquean del
 * todo el movimiento salvo sentarse/dormir) y se comunican al cliente a
 * través de AbstractVividEntity#getAction() para que el renderer sepa qué
 * animación reproducir.
 */
public class VividIdleActionGoal extends Goal {

    private final AbstractVividEntity vivid;
    private int cooldown;
    private int actionTicksLeft;

    public VividIdleActionGoal(AbstractVividEntity vivid) {
        this.vivid = vivid;
        this.setFlags(EnumSet.of(Flag.LOOK));
        this.cooldown = 100 + vivid.getRandom().nextInt(200);
    }

    @Override
    public boolean canUse() {
        if (vivid.isAngry() || vivid.getTarget() != null) {
            return false;
        }
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        return vivid.getRandom().nextInt(4) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return actionTicksLeft > 0;
    }

    @Override
    public void start() {
        int roll = vivid.getRandom().nextInt(4);
        AbstractVividEntity.Action action;
        int duration;
        switch (roll) {
            case 0 -> { action = AbstractVividEntity.Action.SIT; duration = 100 + vivid.getRandom().nextInt(80); }
            case 1 -> { action = AbstractVividEntity.Action.SLEEP; duration = 160 + vivid.getRandom().nextInt(120); }
            case 2 -> { action = AbstractVividEntity.Action.WAVE; duration = 30; }
            default -> { action = AbstractVividEntity.Action.JUMP; duration = 15; }
        }

        // Saludar solo tiene sentido si hay un jugador cerca mirando/cercano
        if (action == AbstractVividEntity.Action.WAVE) {
            Player nearest = vivid.level().getNearestPlayer(vivid, 8.0);
            if (nearest == null) {
                action = AbstractVividEntity.Action.JUMP;
                duration = 15;
            }
        }

        this.actionTicksLeft = duration;
        vivid.setAction(action);
        cooldown = 200 + vivid.getRandom().nextInt(400);
    }

    @Override
    public void stop() {
        vivid.setAction(AbstractVividEntity.Action.NONE);
    }

    @Override
    public void tick() {
        actionTicksLeft--;
        if (vivid.getAction() == AbstractVividEntity.Action.WAVE) {
            Player nearest = vivid.level().getNearestPlayer(vivid, 8.0);
            if (nearest != null) {
                vivid.getLookControl().setLookAt(nearest, 30.0F, 30.0F);
            }
        }
    }

    @Override
    public boolean isInterruptable() {
        return true;
    }
}
