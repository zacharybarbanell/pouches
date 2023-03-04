package com.zacharybarbanell.seedpouch;

//import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;

//import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SeedPouch.MODID)
public class SeedPouch {
        // Define mod id in a common place for everything to reference
        public static final String MODID = "seedpouch";
        // Directly reference a slf4j logger
        // private static final Logger LOGGER = LogUtils.getLogger();
        // Create a Deferred Register to hold Items which will all be registered under
        // the "seedpouch" namespace
        public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
        // Create a Deferred Register to hold Menu Types which will all be registered
        // under
        // the "seedpouch" namespace
        public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(
                        ForgeRegistries.MENU_TYPES,
                        MODID);

        // Creates a new BlockItem with the id "seedpouch:example_block", combining the
        // namespace and path
        public static final RegistryObject<Item> SEED_POUCH_ITEM = ITEMS.register("seed_pouch",
                        () -> new PouchItem(
                                        new Item.Properties().tab(CreativeModeTab.TAB_MISC),
                                        () -> Arrays.asList(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS,
                                                        Items.BEETROOT_SEEDS)));

        public static final RegistryObject<MenuType<PouchContainer>> SEED_POUCH_MENU = MENU_TYPES.register(
                        "seed_pouch",
                        () -> PouchContainer.getMenuType((PouchItem) SEED_POUCH_ITEM.get()));

        public SeedPouch() {
                IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

                // Register the commonSetup method for modloading
                modEventBus.addListener(this::onClientSetup);

                // Register the Deferred Register to the mod event bus so items get registered
                ITEMS.register(modEventBus);
                // Register the Deferred Register to the mod event bus so menu types get
                // registered
                MENU_TYPES.register(modEventBus);

                // Register ourselves for server and other game events we are interested in
                MinecraftForge.EVENT_BUS.register(this);
        }

        public void onClientSetup(FMLClientSetupEvent event) {
                event.enqueueWork(
                                () -> {
                                        MenuScreens.register(SEED_POUCH_MENU.get(), PouchScreen::new);
                                });
        }
}
