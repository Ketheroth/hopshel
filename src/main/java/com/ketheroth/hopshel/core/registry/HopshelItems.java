package com.ketheroth.hopshel.core.registry;

import com.ketheroth.hopshel.common.item.HopshelSpawnEgg;
import com.ketheroth.hopshel.core.HopshelMod;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class HopshelItems {

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HopshelMod.MODID);

	public static final RegistryObject<Item> ENDSTONE_GRAVEL = ITEMS.register("endstone_gravel", () -> new BlockItem(HopshelBlocks.ENDSTONE_GRAVEL.get(), new Item.Properties().tab(HopshelMod.HopshelGroup.instance)));
	public static final RegistryObject<Item> HOPSHEL_BURROW = ITEMS.register("hopshel_burrow", () -> new BlockItem(HopshelBlocks.HOPSHEL_BURROW.get(), new Item.Properties().tab(HopshelMod.HopshelGroup.instance)));
	public static final RegistryObject<Item> HOPSHEL_SPAWN_EGG = ITEMS.register("hopshel_spawn_egg", HopshelSpawnEgg::new);

}
