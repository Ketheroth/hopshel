package com.nbrichau.hopshel.core.registry;

import com.nbrichau.hopshel.common.entity.HopshelEntity;
import com.nbrichau.hopshel.core.HopshelMod;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class HopshelEntityTypes {

	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, HopshelMod.MODID);

	public static final RegistryObject<EntityType<HopshelEntity>> HOPSHEL = ENTITY_TYPES
			.register("hopshel", () -> EntityType.Builder.of(HopshelEntity::new, EntityClassification.AMBIENT)
					.sized(0.5F, 0.5F)
					.build(new ResourceLocation(HopshelMod.MODID, "hopshel").toString()));

	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(HopshelEntityTypes.HOPSHEL.get(), HopshelEntity.registerAttributes().build());
	}

}
