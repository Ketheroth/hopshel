package com.ketheroth.hopshel.core.registry;

import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Features;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.placement.NoiseDependant;
import net.minecraft.world.gen.placement.Placement;

public class HopshelConfiguredFeatures {

	public static final ConfiguredFeature<?, ?> HOPSHEL_BURROW = HopshelFeatures.HOPSHEL_BURROW.get().configured(IFeatureConfig.NONE)
			.decorated(Features.Placements.HEIGHTMAP_SQUARE).decorated(Placement.COUNT_NOISE.configured(new NoiseDependant(-0.8, 1, 0)));

	private static <FC extends IFeatureConfig> ConfiguredFeature<FC, ?> register(String name, ConfiguredFeature<FC, ?> configuredFeature) {
		return Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, name, configuredFeature);
	}

	public static void registerConfiguredFeatures() {
		register("hopshel_burrow", HOPSHEL_BURROW);
	}

}
