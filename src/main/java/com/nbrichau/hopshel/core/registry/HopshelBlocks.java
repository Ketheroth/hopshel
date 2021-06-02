package com.nbrichau.hopshel.core.registry;

import com.nbrichau.hopshel.common.block.HopshelBurrow;
import com.nbrichau.hopshel.core.HopshelMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.GravelBlock;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class HopshelBlocks {

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HopshelMod.MODID);

	public static final RegistryObject<GravelBlock> ENDSTONE_GRAVEL = BLOCKS.register("endstone_gravel", () -> new GravelBlock(AbstractBlock.Properties.copy(Blocks.GRAVEL)));
	public static final RegistryObject<Block> HOPSHEL_BURROW = BLOCKS.register("hopshel_burrow", () -> new HopshelBurrow(AbstractBlock.Properties.copy(Blocks.STONE)));

}
