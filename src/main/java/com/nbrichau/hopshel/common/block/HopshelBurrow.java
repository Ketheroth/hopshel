package com.nbrichau.hopshel.common.block;

import com.nbrichau.hopshel.common.tileentity.BurrowTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class HopshelBurrow extends Block {
	public HopshelBurrow(Properties properties) {
		super(properties);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new BurrowTileEntity();
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (worldIn.isClientSide()) {
			return ActionResultType.SUCCESS;
		}
		TileEntity tile = worldIn.getBlockEntity(pos);
		if (!(tile instanceof BurrowTileEntity)) {
			return ActionResultType.FAIL;
		}
		if (player.getItemInHand(handIn).getItem().equals(Items.STICK)) {
			System.out.println("occupant amount : " + ((BurrowTileEntity) tile).occupantAmount());
		}
		return ActionResultType.SUCCESS;
	}
}
