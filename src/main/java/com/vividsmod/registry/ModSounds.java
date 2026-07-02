package com.vividsmod.registry;

import com.vividsmod.Vivids;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registro de sonidos personalizados.
 *
 * IMPORTANTE: registrar el SoundEvent aquí NO agrega el audio en sí.
 * Debes:
 *  1. Poner los archivos .ogg en src/main/resources/assets/vividsmod/sounds/
 *  2. Declarar cada entrada en src/main/resources/assets/vividsmod/sounds.json
 *     (ver el archivo de ejemplo incluido en el proyecto)
 */
public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Vivids.MOD_ID);

    public static final RegistryObject<SoundEvent> VIVID_AMBIENT = register("vivid_ambient");
    public static final RegistryObject<SoundEvent> VIVID_HURT = register("vivid_hurt");
    public static final RegistryObject<SoundEvent> VIVID_DEATH = register("vivid_death");
    public static final RegistryObject<SoundEvent> VIVID_ANGRY = register("vivid_angry");
    public static final RegistryObject<SoundEvent> VIVID_GREET = register("vivid_greet");
    public static final RegistryObject<SoundEvent> VIVID_STEP = register("vivid_step");
    public static final RegistryObject<SoundEvent> VIVID_PUNCH = register("vivid_punch");

    public static final RegistryObject<SoundEvent> VIVITO_AMBIENT = register("vivito_ambient");

    public static final RegistryObject<SoundEvent> VIVID_BATH_AMBIENT = register("vivid_bath_ambient");
    public static final RegistryObject<SoundEvent> VIVID_BATH_BUBBLE = register("vivid_bath_bubble");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Vivids.MOD_ID, name)));
    }
}
