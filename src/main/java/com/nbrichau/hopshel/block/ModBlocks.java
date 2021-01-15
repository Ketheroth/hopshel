package com.nbrichau.hopshel.block;

import com.nbrichau.hopshel.HopshelMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.GravelBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;

import static com.nbrichau.hopshel.HopshelMod.BLOCKS;
import static com.nbrichau.hopshel.HopshelMod.ITEMS;

@Mod.EventBusSubscriber(modid = HopshelMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBlocks {
	public static final RegistryObject<GravelBlock> endstone_gravel = BLOCKS.register("endstone_gravel", () -> new GravelBlock(AbstractBlock.Properties.from(Blocks.GRAVEL)));
	private static final RegistryObject<Item> endstone_gravel_item = ITEMS.register("endstone_gravel", () -> new BlockItem(endstone_gravel.get(), new Item.Properties().group(HopshelMod.HopshelGroup.instance)));

	public static final RegistryObject<Block> hopshel_burrow = BLOCKS.register("hopshel_burrow", () -> new Block(AbstractBlock.Properties.from(Blocks.STONE)));
	private static final RegistryObject<Item> hopshel_burrow_item = ITEMS.register("hopshel_burrow", () -> new BlockItem(hopshel_burrow.get(), new Item.Properties().group(HopshelMod.HopshelGroup.instance)));
}
