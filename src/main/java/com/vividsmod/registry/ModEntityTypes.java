package com.vividsmod.registry;

import com.vividsmod.Vivids;
import com.vividsmod.entity.VividBathEntity;
import com.vividsmod.entity.VividNormalEntity;
import com.vividsmod.entity.VivitoEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registro central de todos los tipos de entidad "Vivid".
 * Para agregar una raza nueva: define su EntityType.Builder aquí y su clase de
 * entidad correspondiente en el paquete entity/.
 */
public class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Vivids.MOD_ID);

    // El tamaño base del EntityType es el del tamaño "grande"; el tamaño "pequeño"
    // se maneja dinámicamente dentro de AbstractVividEntity (ver VividSize).
    public static final RegistryObject<EntityType<VividNormalEntity>> VIVID_NORMAL =
            ENTITY_TYPES.register("vivid_normal", () -> EntityType.Builder.of(VividNormalEntity::new, MobCategory.CREATURE)
                    .sized(0.9f, 2.6f)
                    .clientTrackingRange(10)
                    .build("vivid_normal"));

    public static final RegistryObject<EntityType<VivitoEntity>> VIVITO =
            ENTITY_TYPES.register("vivito", () -> EntityType.Builder.of(VivitoEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.0f)
                    .clientTrackingRange(10)
                    .build("vivito"));

    public static final RegistryObject<EntityType<VividBathEntity>> VIVID_BATH =
            ENTITY_TYPES.register("vivid_bath", () -> EntityType.Builder.of(VividBathEntity::new, MobCategory.CREATURE)
                    .sized(0.9f, 2.6f)
                    .clientTrackingRange(10)
                    .build("vivid_bath"));
}
