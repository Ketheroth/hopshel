package com.ketheroth.hopshel.core.registry;

import com.ketheroth.hopshel.common.world.gen.feature.HopshelBurrowFeature;
import com.ketheroth.hopshel.core.HopshelMod;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.*;
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
			event.getGeneration().addFeature(GenerationStage.Decoration.LOCAL_MODIFICATIONS, HopshelConfiguredFeatures.HOPSHEL_BURROW);
		}
	}

}
