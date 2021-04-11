package com.nbrichau.hopshel.world.gen.feature;

import com.mojang.serialization.Codec;
import com.nbrichau.hopshel.HopshelMod;
import com.nbrichau.hopshel.block.ModBlocks;
import com.nbrichau.hopshel.entity.HopshelEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.SpawnReason;
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
	public boolean place(ISeedReader reader, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig config) {
		if (reader.getBlockState(pos).getBlock().is(Blocks.AIR) && reader.getBlockState(pos.below()).getBlock().is(Blocks.END_STONE)) {
			BlockPos posBurrow = pos.below();
			HopshelMod.LOGGER.debug("Generation of the feature at position : " + posBurrow.toShortString());
			BlockState hopshel_burrow = ModBlocks.hopshel_burrow.get().defaultBlockState();
			BlockState endstone_gravel = ModBlocks.endstone_gravel.get().defaultBlockState();
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
					this.tryPlaceBlockUp(reader, posBurrow.offset(i, 0, j), endstone_gravel);
				}
			}
			this.tryPlaceBlockUp(reader, posBurrow.offset(-1, 0, 1), Blocks.END_STONE.defaultBlockState());
			reader.setBlock(posBurrow, hopshel_burrow, 2);
			int rnd = rand.nextInt(10);
			System.out.println("--" + rnd);
			if (rnd >= 1) {
				this.tryPlaceBlockUp(reader, posBurrow.south(2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(1, 0, 2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(2, 0, 1), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.east(2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.north(2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(-1, 0, -2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.west(2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(-1, 0, 1), endstone_gravel);
			}
			if (rnd >= 3) {
				this.tryPlaceBlockUp(reader, posBurrow.south(3), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(2, 0, -1), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(1, 0, -2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(-2, 0, -1), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(-2, 0, 1), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(-2, 0, 2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(-1, 0, 2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(-1, 0, 3), endstone_gravel);
			}
			if (rnd >= 5) {
				this.tryPlaceBlockUp(reader, posBurrow.offset(1, 0, 3), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(2, 0, 2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.east(3), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(3, 0, -1), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(2, 0, -2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.north(3), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(-1, 0, -3), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(-2, 0, -2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(-3, 0, -1), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.west(3), endstone_gravel);
			}
			if (rnd >= 7) {
				this.tryPlaceBlockUp(reader, posBurrow.south(4), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(1, 0, 4), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(2, 0, 3), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(3, 0, 2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(3, 0, 1), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(2, 0, -3), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(1, 0, -3), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(-3, 0, 1), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(-3, 0, 2), endstone_gravel);
				this.tryPlaceBlockUp(reader, posBurrow.offset(-2, 0, 3), endstone_gravel);
			}
			this.spawnHopshel(reader, posBurrow, rand);
			return true;
		}
		return false;
	}

	private void spawnHopshel(ISeedReader world, BlockPos pos, Random rand) {
		BlockPos offset = pos.above();
		for (int i = 0; i < rand.nextInt(2); i++) {
			HopshelEntity hopshel = HopshelMod.hopshel_entity.get().create(world.getLevel());
			if (hopshel != null) {
				hopshel.moveTo(offset.getX() + 0.5F, offset.getY() + 0.1F, offset.getZ() + 0.5F, 0.0F, 0.0F);
				hopshel.finalizeSpawn(world, world.getCurrentDifficultyAt(offset), SpawnReason.STRUCTURE, null, null);
				hopshel.setBurrowPos(pos);
				world.addFreshEntity(hopshel);
			}
		}
	}

	private void tryPlaceBlockUp(ISeedReader world, BlockPos pos, BlockState blockState) {
		if (world.getBlockState(pos.above()).is(Blocks.END_STONE)) {
			this.tryPlaceBlockUp(world, pos.above(), blockState);
		} else {
			world.setBlock(pos, blockState, 2);
		}
	}
}
