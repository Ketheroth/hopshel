package com.nbrichau.hopshel.common.item;

import com.nbrichau.hopshel.core.HopshelMod;
import com.nbrichau.hopshel.core.registry.HopshelEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.common.util.Constants;

import java.util.Objects;

public class HopshelSpawnEgg extends Item {

	public HopshelSpawnEgg() {
		super(new Item.Properties()
				.stacksTo(1)
				.tab(HopshelMod.HopshelGroup.instance));
	}

	/**
	 * Called when this item is used when targeting a Block
	 */
	@Override
	public ActionResultType useOn(ItemUseContext context) {
		World world = context.getLevel();
		if (world.isClientSide) {
			return ActionResultType.SUCCESS;
		} else {
			ItemStack itemstack = context.getItemInHand();
			BlockPos blockpos = context.getClickedPos();
			Direction direction = context.getClickedFace();
			BlockState blockstate = world.getBlockState(blockpos);
			TileEntity tileentity = world.getBlockEntity(blockpos);
			if (tileentity instanceof MobSpawnerTileEntity) {
				AbstractSpawner abstractspawner = ((MobSpawnerTileEntity) tileentity).getSpawner();
				abstractspawner.setEntityId(HopshelEntityTypes.HOPSHEL.get());
				tileentity.setChanged();
				world.sendBlockUpdated(blockpos, blockstate, blockstate, Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
				itemstack.shrink(1);
				return ActionResultType.SUCCESS;
			}

			BlockPos blockpos1;
			if (blockstate.getCollisionShape(world, blockpos).isEmpty()) {
				blockpos1 = blockpos;
			} else {
				blockpos1 = blockpos.relative(direction);
			}

			if (HopshelEntityTypes.HOPSHEL.get().spawn((ServerWorld) world, itemstack, context.getPlayer(), blockpos1, SpawnReason.SPAWN_EGG, true, !Objects.equals(blockpos, blockpos1) && direction == Direction.UP) != null) {
				itemstack.shrink(1);
			}

			return ActionResultType.SUCCESS;
		}
	}
}
