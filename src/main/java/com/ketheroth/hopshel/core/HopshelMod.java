package com.ketheroth.hopshel.core;

import com.ketheroth.hopshel.client.renderer.entity.HopshelRenderer;
import com.ketheroth.hopshel.core.registry.*;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.client.event.ColorHandlerEvent;
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
//		modEventBus.addListener(HopshelFeatures::onBiomeLoading);
		modEventBus.addListener(HopshelEntityTypes::registerAttributes);
		modEventBus.addListener(this::onItemColor);

		HopshelItems.ITEMS.register(modEventBus);
		HopshelBlocks.BLOCKS.register(modEventBus);
		HopshelEntityTypes.ENTITY_TYPES.register(modEventBus);
		HopshelTileEntities.TILES.register(modEventBus);
		HopshelFeatures.FEATURES.register(modEventBus);

		MinecraftForge.EVENT_BUS.addListener(HopshelFeatures::onBiomeLoading);//EventPriority.HIGH,
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void setup(final FMLCommonSetupEvent event) {
		event.enqueueWork(HopshelConfiguredFeatures::registerConfiguredFeatures);
	}

	private void clientSetup(final FMLClientSetupEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(HopshelEntityTypes.HOPSHEL.get(), HopshelRenderer::new);
	}

	private void onItemColor(ColorHandlerEvent.Item event) {
		event.getItemColors().register((stack, i) -> 0x8963A7, HopshelItems.HOPSHEL_SPAWN_EGG.get());
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
