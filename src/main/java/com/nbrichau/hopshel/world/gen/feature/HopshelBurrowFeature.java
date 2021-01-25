package com.nbrichau.hopshel.world.gen.feature;

import com.mojang.serialization.Codec;
import com.nbrichau.hopshel.block.ModBlocks;
import net.minecraft.block.BlockState;
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
			BlockPos posBurrow = pos.down();
			System.out.println("I generate : " + posBurrow.getCoordinatesAsString());
			BlockState hopshel_burrow = ModBlocks.hopshel_burrow.get().getDefaultState();
			BlockState endstone_gravel = ModBlocks.endstone_gravel.get().getDefaultState();
			/*       Sz
			        4 4
			      4 3 2 2 4
			    4 3 1 1 2 2 4
			    4 1 0 0 1 2 4
			Ex  3 1 0 x 0 1 3
			    3 2 0 0 0 2 3
			      3 2 1 1 3
			      4 4 3 3
			 */
			for (int i = -1; i < 2; i++) {
				for (int j = -1; j < 2; j++) {
					this.tryPlaceBlockUp(reader, posBurrow.add(i, 0, j), endstone_gravel);
				}
			}
			this.tryPlaceBlockUp(reader, posBurrow.add(-1, 0, 1), Blocks.END_STONE.getDefaultState());
			reader.setBlockState(posBurrow, hopshel_burrow, 2);
			int rnd = rand.nextInt(10);
			System.out.println("--" + rnd);
			if (rnd >= 1) {
				this.tryPlaceBlockUp(reader, posBurrow.south(2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(1, 0, 2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(2, 0, 1), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.east(2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.north(2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(-1, 0, -2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.west(2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(-1, 0, 1), endstone_gravel);
			}
			if (rnd >= 3) {
				this.tryPlaceBlockUp(reader, posBurrow.south(3), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(2, 0, -1), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(1, 0, -2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(-2, 0, -1), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(-2, 0, 1), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(-2, 0, 2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(-1, 0, 2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(-1, 0, 3), endstone_gravel);
			}
			if (rnd >= 5) {
				this.tryPlaceBlockUp(reader, posBurrow.add(1, 0, 3), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(2, 0, 2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.east(3), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(3, 0, -1), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(2, 0, -2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.north(3), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(-1, 0, -3), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(-2, 0, -2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(-3, 0, -1), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.west(3), endstone_gravel);
			}
			if (rnd >= 7) {
				this.tryPlaceBlockUp(reader, posBurrow.south(4), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(1, 0, 4), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(2, 0, 3), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(3, 0, 2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(3, 0, 1), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(2, 0, -3), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(1, 0, -3), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(-3, 0, 1), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(-3, 0, 2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.add(-2, 0, 3), endstone_gravel);
			}
		}
		return false;
	}

	private void tryPlaceBlockUp(ISeedReader world, BlockPos pos, BlockState blockState) {
		if (world.getBlockState(pos.up()).isIn(Blocks.END_STONE)) {
			this.tryPlaceBlockUp(world, pos.up(), blockState);
		} else {
			world.setBlockState(pos, blockState, 2);
		}
	}
}
