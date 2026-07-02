package com.vividsmod.registry;

import com.vividsmod.Vivids;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Items propios del mod: huevos de spawn y objetos que pueden soltar/usar los Vivids.
 * "Flor" y "Pan" reutilizan los items vanilla (no hace falta registrarlos de nuevo).
 */
public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Vivids.MOD_ID);

    // ---- Huevos de spawn (uno por raza) ----
    public static final RegistryObject<Item> VIVID_NORMAL_SPAWN_EGG = ITEMS.register("vivid_normal_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.VIVID_NORMAL, 0xDDBB88, 0x6B4A2B, new Item.Properties()));

    public static final RegistryObject<Item> VIVITO_SPAWN_EGG = ITEMS.register("vivito_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.VIVITO, 0xF2E28C, 0xC9A227, new Item.Properties()));

    public static final RegistryObject<Item> VIVID_BATH_SPAWN_EGG = ITEMS.register("vivid_bath_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.VIVID_BATH, 0xE9F6FA, 0x6EC3D6, new Item.Properties()));

    // ---- Objetos únicos del mod ----
    public static final RegistryObject<Item> SOAP = ITEMS.register("soap",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BUBBLE = ITEMS.register("bubble",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TOWEL = ITEMS.register("towel",
            () -> new Item(new Item.Properties()));
}
