package com.nbrichau.hopshel.block;

import com.nbrichau.hopshel.HopshelMod;
import com.nbrichau.hopshel.tileentity.BurrowTileEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.GravelBlock;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;

import static com.nbrichau.hopshel.HopshelMod.BLOCKS;
import static com.nbrichau.hopshel.HopshelMod.TILES;

@Mod.EventBusSubscriber(modid = HopshelMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBlocks {
	public static final RegistryObject<GravelBlock> endstone_gravel = BLOCKS.register("endstone_gravel", () -> new GravelBlock(AbstractBlock.Properties.copy(Blocks.GRAVEL)));

	public static final RegistryObject<Block> hopshel_burrow = BLOCKS.register("hopshel_burrow", () -> new HopshelBurrow(AbstractBlock.Properties.copy(Blocks.STONE)));
	public static final RegistryObject<TileEntityType<?>> hopshel_burrow_tile = TILES.register("hopshel_burrow", () -> TileEntityType.Builder.of(BurrowTileEntity::new, hopshel_burrow.get()).build(null));
}
