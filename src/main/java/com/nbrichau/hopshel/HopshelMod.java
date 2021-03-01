package com.nbrichau.hopshel;

import com.nbrichau.hopshel.client.gui.screen.inventory.HopshelScreen;
import com.nbrichau.hopshel.client.renderer.entity.HopshelRenderer;
import com.nbrichau.hopshel.entity.HopshelEntity;
import com.nbrichau.hopshel.inventory.container.HopshelContainer;
import com.nbrichau.hopshel.world.gen.feature.HopshelBurrowFeature;
import net.minecraft.block.Block;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placement.NoiseDependant;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("hopshel")
public class HopshelMod {
	public static final Logger LOGGER = LogManager.getLogger();
	public static final String MODID = "hopshel";

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, MODID);
	public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MODID);
	public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID);

	public static final RegistryObject<EntityType<HopshelEntity>> HOPSHEL_ENTITY = ENTITY_TYPES
			.register("hopshel", () -> EntityType.Builder.create(HopshelEntity::new, EntityClassification.CREATURE)
					.size(0.5F, 0.5F)
					.build(new ResourceLocation(MODID, "hopshel").toString()));

	public static final RegistryObject<ContainerType<HopshelContainer>> HOPSHEL_CONTAINER = CONTAINERS.register("hopshel_container", () -> IForgeContainerType.create((windowId, inv, data) -> {
		return new HopshelContainer(windowId, inv.player.getEntityWorld(), inv, inv.player, data.readInt());
	}));

	public static final ConfiguredFeature<?, ?> hopshel_burrow_configured = new HopshelBurrowFeature(NoFeatureConfig.field_236558_a_).withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG).withPlacement(Features.Placements.HEIGHTMAP_PLACEMENT).withPlacement(Placement.COUNT_NOISE.configure(new NoiseDependant(-0.8, 1, 0)));


	public HopshelMod() {
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		modEventBus.addListener(this::setup);
		modEventBus.addListener(this::clientSetup);

		ENTITY_TYPES.register(modEventBus);
		BLOCKS.register(modEventBus);
		ITEMS.register(modEventBus);
		TILES.register(modEventBus);
		CONTAINERS.register(modEventBus);


		MinecraftForge.EVENT_BUS.register(this);
	}

	private void setup(final FMLCommonSetupEvent event) {
		event.enqueueWork(() -> GlobalEntityTypeAttributes.put(HOPSHEL_ENTITY.get(), HopshelEntity.registerAttributes().create()));
	}

	private void clientSetup(final FMLClientSetupEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(HOPSHEL_ENTITY.get(), HopshelRenderer::new);
		ScreenManager.registerFactory(HOPSHEL_CONTAINER.get(), HopshelScreen::new);
	}

	@SubscribeEvent
	public static void registerFeatures(RegistryEvent.Register<Feature<?>> event) {
		Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, new ResourceLocation(MODID, "hopshel_burrow"), hopshel_burrow_configured);
	}

	@SubscribeEvent
	public void addBiomeFeature(final BiomeLoadingEvent event) {
		ResourceLocation name = event.getName();
		if (name == null) {
			return;
		}
		if(name.equals(Biomes.END_HIGHLANDS.getLocation())) {
			event.getGeneration().getFeatures(GenerationStage.Decoration.LOCAL_MODIFICATIONS).add(() -> hopshel_burrow_configured);
		}
	}

	public static class HopshelGroup extends ItemGroup {
		public static final HopshelGroup instance = new HopshelGroup(ItemGroup.GROUPS.length, "hopshelgroup");

		public HopshelGroup(int index, String label) {
			super(index, label);
		}

		@Override
		public ItemStack createIcon() {
			return new ItemStack(Items.STICK);
		}
	}
}
