package com.nbrichau.hopshel.world.gen.feature;

import com.mojang.serialization.Codec;
import com.nbrichau.hopshel.block.ModBlocks;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.Random;

public class HopshelBurrowFeature extends Feature<NoFeatureConfig> {

	public HopshelBurrowFeature(Codec<NoFeatureConfig> codec) {
		super(codec);
	}

	@Override
	public boolean generate(ISeedReader reader, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig config) {
		if (reader.getBlockState(pos).getBlock().matchesBlock(Blocks.AIR) && reader.getBlockState(pos.down()).getBlock().matchesBlock(Blocks.END_STONE)) {
			BlockPos posi = pos.down();
			System.out.println("I am generated in pos : " + posi.getCoordinatesAsString());
			reader.setBlockState(posi, ModBlocks.hopshel_burrow.get().getDefaultState(), 2);
			reader.setBlockState(posi.north(), ModBlocks.endstone_gravel.get().getDefaultState(), 2);
			reader.setBlockState(posi.south(), ModBlocks.endstone_gravel.get().getDefaultState(), 2);
			reader.setBlockState(posi.east(), ModBlocks.endstone_gravel.get().getDefaultState(), 2);
			reader.setBlockState(posi.west(), ModBlocks.endstone_gravel.get().getDefaultState(), 2);
		}
		return false;
	}
}
