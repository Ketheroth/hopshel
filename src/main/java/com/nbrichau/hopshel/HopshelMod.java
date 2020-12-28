package com.nbrichau.hopshel;

import com.nbrichau.hopshel.client.renderer.entity.HopshelRenderer;
import com.nbrichau.hopshel.entity.HopshelEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

@Mod("hopshel")
public class HopshelMod {
//	private static final Logger LOGGER = LogManager.getLogger();
	public static final String MODID = "hopshel";

//	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HopshelMod.MODID);
//	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HopshelMod.MODID);
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, HopshelMod.MODID);

	public static final RegistryObject<EntityType<HopshelEntity>> HOPSHEL_ENTITY = ENTITY_TYPES
			.register("hopshel", () -> EntityType.Builder.create(HopshelEntity::new, EntityClassification.CREATURE)
					.size(0.5F, 0.5F)
					.build(new ResourceLocation(HopshelMod.MODID, "hopshel").toString()));

	public HopshelMod() {
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		modEventBus.addListener(this::setup);
		modEventBus.addListener(this::clientSetup);

		ENTITY_TYPES.register(modEventBus);
//		BLOCKS.register(modEventBus);
//		ITEMS.register(modEventBus);

		MinecraftForge.EVENT_BUS.register(this);
	}

	private void setup(final FMLCommonSetupEvent event) {
		event.enqueueWork(() -> GlobalEntityTypeAttributes.put(HOPSHEL_ENTITY.get(), HopshelEntity.registerAttributes().create()));
	}

	private void clientSetup(final FMLClientSetupEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(HOPSHEL_ENTITY.get(), HopshelRenderer::new);
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
