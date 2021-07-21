package com.nbrichau.hopshel.common.block;

import com.nbrichau.hopshel.common.tileentity.BurrowTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

// TODO: 18/07/2021 eject entities inside when broken
// TODO: 21/07/2021 comparator signal when inventory full
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
			return super.use(state, worldIn, pos, player, handIn, hit);
		}
		TileEntity tile = worldIn.getBlockEntity(pos);
		if (!(tile instanceof BurrowTileEntity)) {
			return ActionResultType.FAIL;
		}
		if (player.getItemInHand(handIn).getItem().equals(Items.DEBUG_STICK)) {
			BurrowTileEntity burrow = ((BurrowTileEntity) tile);
			System.out.println("occupant amount : " + burrow.occupantAmount());
			for (int i = 0; i < burrow.getInventory().getSlots(); i++) {
				System.out.println("Item " + i + " : " + burrow.getInventory().getStackInSlot(i));
			}
			return ActionResultType.SUCCESS;
		}
		return super.use(state, worldIn, pos, player, handIn, hit);
	}

	public void onRemove(BlockState currentState, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		TileEntity tile = world.getBlockEntity(pos);
		if (tile instanceof BurrowTileEntity) {
			BurrowTileEntity burrow = ((BurrowTileEntity) tile);
			// drop inventory contents
			for(int i = 0; i < burrow.getInventory().getSlots(); ++i) {
				ItemStack stack = burrow.getInventory().getStackInSlot(i);
				double itemWidth = EntityType.ITEM.getWidth();
				double d1 = 1.0D - itemWidth;
				double d2 = itemWidth / 2.0D;
				double x = Math.floor(pos.getX()) + RANDOM.nextDouble() * d1 + d2;
				double y = Math.floor(pos.getY()) + RANDOM.nextDouble() * d1;
				double z = Math.floor(pos.getZ()) + RANDOM.nextDouble() * d1 + d2;

				while(!stack.isEmpty()) {
					ItemEntity itemEntity = new ItemEntity(world, x, y, z, stack.split(RANDOM.nextInt(21) + 10));
					itemEntity.setDeltaMovement(RANDOM.nextGaussian() * 0.05D, RANDOM.nextGaussian() * 0.05D + 0.2D, RANDOM.nextGaussian() * 0.05D);
					world.addFreshEntity(itemEntity);
				}
			}
			world.updateNeighbourForOutputSignal(pos, this);
		}
		super.onRemove(currentState, world, pos, newState, isMoving);
	}
}
