package com.nbrichau.hopshel.core;

import com.nbrichau.hopshel.client.gui.screen.inventory.HopshelScreen;
import com.nbrichau.hopshel.client.renderer.entity.HopshelRenderer;
import com.nbrichau.hopshel.core.registry.*;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("hopshel")
public class HopshelMod {

	public static final Logger LOGGER = LogManager.getLogger();
	public static final String MODID = "hopshel";

	public HopshelMod() {
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		modEventBus.addListener(this::setup);
		modEventBus.addListener(this::clientSetup);
		modEventBus.addListener(HopshelFeatures::onBiomeLoading);
		modEventBus.addListener(HopshelEntityTypes::registerAttributes);

		HopshelItems.ITEMS.register(modEventBus);
		HopshelBlocks.BLOCKS.register(modEventBus);
		HopshelEntityTypes.ENTITY_TYPES.register(modEventBus);
		HopshelTileEntities.TILES.register(modEventBus);
		HopshelContainerTypes.CONTAINERS.register(modEventBus);

		MinecraftForge.EVENT_BUS.register(this);
	}

	private void setup(final FMLCommonSetupEvent event) {
		event.enqueueWork(HopshelFeatures.Configured::registerConfiguredFeatures);
	}

	private void clientSetup(final FMLClientSetupEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(HopshelEntityTypes.HOPSHEL.get(), HopshelRenderer::new);
		ScreenManager.register(HopshelContainerTypes.HOPSHEL_CONTAINER.get(), HopshelScreen::new);
	}

	public static class HopshelGroup extends ItemGroup {

		public static final HopshelGroup instance = new HopshelGroup(ItemGroup.TABS.length, "hopshelgroup");

		public HopshelGroup(int index, String label) {
			super(index, label);
		}

		@Override
		public ItemStack makeIcon() {
			return new ItemStack(Items.STICK);
		}

	}

}
