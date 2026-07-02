package com.vividsmod.registry;

import com.vividsmod.Vivids;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Vivids.MOD_ID);

    public static final RegistryObject<CreativeModeTab> VIVIDS_TAB = CREATIVE_MODE_TABS.register("vivids_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.vividsmod.vivids_tab"))
                    .icon(() -> new ItemStack(ModItems.VIVID_NORMAL_SPAWN_EGG.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.VIVID_NORMAL_SPAWN_EGG.get());
                        output.accept(ModItems.VIVITO_SPAWN_EGG.get());
                        output.accept(ModItems.VIVID_BATH_SPAWN_EGG.get());
                        output.accept(ModItems.SOAP.get());
                        output.accept(ModItems.BUBBLE.get());
                        output.accept(ModItems.TOWEL.get());
                    })
                    .build());
}
