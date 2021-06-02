package com.nbrichau.hopshel.core.registry;

import com.nbrichau.hopshel.common.world.gen.feature.HopshelBurrowFeature;
import com.nbrichau.hopshel.core.HopshelMod;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placement.NoiseDependant;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class HopshelFeatures {

	public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, HopshelMod.MODID);

	public static final RegistryObject<Feature<NoFeatureConfig>> HOPSHEL_BURROW = FEATURES.register("hopshel_burrow", () -> new HopshelBurrowFeature(NoFeatureConfig.CODEC));

	public static void onBiomeLoading(final BiomeLoadingEvent event) {
		ResourceLocation name = event.getName();
		if (name == null) {
			return;
		}
		if (name.equals(Biomes.END_HIGHLANDS.location())) {
			event.getGeneration().getFeatures(GenerationStage.Decoration.LOCAL_MODIFICATIONS).add(() -> HopshelFeatures.Configured.HOPSHEL_BURROW);
		}
	}

	public static final class Configured {

		public static final ConfiguredFeature<?, ?> HOPSHEL_BURROW = HopshelFeatures.HOPSHEL_BURROW.get().configured(IFeatureConfig.NONE)
				.decorated(Features.Placements.HEIGHTMAP_SQUARE).decorated(Placement.COUNT_NOISE.configured(new NoiseDependant(-0.8, 1, 0)));

		private static <FC extends IFeatureConfig> ConfiguredFeature<FC, ?> register(String name, ConfiguredFeature<FC, ?> configuredFeature) {
			return Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, name, configuredFeature);
		}

		public static void registerConfiguredFeatures() {
			register("hopshel_burrow", HOPSHEL_BURROW);
		}

	}

}
