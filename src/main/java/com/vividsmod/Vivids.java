package com.vividsmod;

import com.vividsmod.registry.ModCreativeModeTabs;
import com.vividsmod.registry.ModEntityTypes;
import com.vividsmod.registry.ModItems;
import com.vividsmod.registry.ModSounds;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Clase principal del mod "Vivids".
 *
 * Arquitectura general (para futuras razas/expansiones):
 *  - registry/         -> todos los DeferredRegister (entidades, items, sonidos, creative tabs)
 *  - entity/           -> entidades. AbstractVividEntity es la base de la que heredan
 *                         todas las razas (VividNormalEntity, VivitoEntity, VividBathEntity, ...)
 *  - entity/ai/        -> Goals de IA custom reutilizables entre razas
 *  - client/           -> todo lo que solo existe en el cliente (modelos, renderers)
 *
 * Para agregar una raza nueva en el futuro:
 *  1. Crear una clase que extienda AbstractVividEntity (ver VividNormalEntity como ejemplo).
 *  2. Registrarla en ModEntityTypes.
 *  3. Crear su textura 64x64 en assets/vividsmod/textures/entity/.
 *  4. Registrar su renderer en ClientModEvents (reusa VividModel/VividRenderer si la silueta
 *     es la misma, o crea un modelo nuevo si cambia la geometría).
 *  5. (Opcional) agregar su huevo de spawn en ModItems.
 */
@Mod(Vivids.MOD_ID)
public class Vivids {

    public static final String MOD_ID = "vividsmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Vivids() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Registrar todos los DeferredRegister del mod
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModCreativeModeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerEntityAttributes);
        modEventBus.addListener(this::registerSpawnPlacements);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Vivids mod inicializado");
    }

    /**
     * Cada mob de Forge necesita sus atributos base (vida, velocidad, ataque, etc.)
     * registrados aquí antes de poder spawnear. Cuando agregues una raza nueva,
     * agrega su línea correspondiente.
     */
    private void registerEntityAttributes(final EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.VIVID_NORMAL.get(), com.vividsmod.entity.VividNormalEntity.createAttributes().build());
        event.put(ModEntityTypes.VIVITO.get(), com.vividsmod.entity.VivitoEntity.createAttributes().build());
        event.put(ModEntityTypes.VIVID_BATH.get(), com.vividsmod.entity.VividBathEntity.createAttributes().build());
    }

    /**
     * Reglas de spawn natural en el mundo.
     * Vivid Normal: en la superficie, en llanuras/bosques/bosques de abedules (ver biome tags
     * agregados vía datapack en data/vividsmod/forge/biome_modifier o directamente por spawn en biomas).
     * Vivito y Vivids del Baño se spawnean de forma custom (cerca de un padre / en estructuras),
     * no por spawn aleatorio de superficie -> ver AbstractVividEntity y las estructuras.
     */
    private void registerSpawnPlacements(final net.minecraftforge.event.entity.SpawnPlacementRegisterEvent event) {
        event.register(ModEntityTypes.VIVID_NORMAL.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules,
                net.minecraftforge.event.entity.SpawnPlacementRegisterEvent.Operation.AND);
    }
}
