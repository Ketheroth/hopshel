package com.nbrichau.hopshel.items;

import com.nbrichau.hopshel.HopshelMod;
import com.nbrichau.hopshel.block.ModBlocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;

import static com.nbrichau.hopshel.HopshelMod.ITEMS;

public class ModItems {
	public static final RegistryObject<Item> endstone_gravel_item = ITEMS.register("endstone_gravel", () -> new BlockItem(ModBlocks.endstone_gravel.get(), new Item.Properties().tab(HopshelMod.HopshelGroup.instance)));
	public static final RegistryObject<Item> hopshel_burrow_item = ITEMS.register("hopshel_burrow", () -> new BlockItem(ModBlocks.hopshel_burrow.get(), new Item.Properties().tab(HopshelMod.HopshelGroup.instance)));
	public static final RegistryObject<Item> hopshel_spawn_egg = ITEMS.register("hopshel_spawn_egg", HopshelSpawnEgg::new);
}
