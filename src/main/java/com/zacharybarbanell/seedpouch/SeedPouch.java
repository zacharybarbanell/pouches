package com.zacharybarbanell.seedpouch;

//import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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

        public static final RegistryObject<Item> SEED_POUCH_ITEM = ITEMS.register("seed_pouch",
                        () -> new PouchItem(
                                        new Item.Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(1),
                                        () -> ForgeRegistries.ITEMS.tags().getTag(Tags.Items.SEEDS)));

        public static final RegistryObject<MenuType<PouchContainer>> SEED_POUCH_MENU = MENU_TYPES.register(
                        "seed_pouch",
                        () -> PouchContainer.getMenuType((PouchItem) SEED_POUCH_ITEM.get()));

        public static final RegistryObject<Item> SAPLING_POUCH_ITEM = ITEMS.register("sapling_pouch",
                        () -> new PouchItem(
                                        new Item.Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(1),
                                        () -> ForgeRegistries.ITEMS.tags().getTag(ItemTags.SAPLINGS)));

        public static final RegistryObject<MenuType<PouchContainer>> SAPLING_POUCH_MENU = MENU_TYPES.register(
                        "sapling_pouch",
                        () -> PouchContainer.getMenuType((PouchItem) SAPLING_POUCH_ITEM.get()));

        public static final RegistryObject<Item> DIRT_POUCH_ITEM = ITEMS.register("dirt_pouch",
                        () -> new PouchItem(
                                        new Item.Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(1),
                                        () -> Arrays.asList(Items.DIRT)));

        public static final RegistryObject<MenuType<PouchContainer>> DIRT_POUCH_MENU = MENU_TYPES.register(
                        "dirt_pouch",
                        () -> PouchContainer.getMenuType((PouchItem) DIRT_POUCH_ITEM.get()));

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
                                        MenuScreens.register(SAPLING_POUCH_MENU.get(), PouchScreen::new);
                                        MenuScreens.register(DIRT_POUCH_MENU.get(), PouchScreen::new);
                                });
        }

        @SubscribeEvent
        public void onPickupItem(EntityItemPickupEvent event) {
                if (PouchItem.onPickupItem(event.getItem(), event.getEntity())) {
                        event.setCanceled(true);
                }
        }
}
