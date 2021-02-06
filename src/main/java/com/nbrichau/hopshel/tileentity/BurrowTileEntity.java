package com.nbrichau.hopshel.tileentity;

import com.nbrichau.hopshel.block.ModBlocks;
import com.nbrichau.hopshel.entity.HopshelEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BurrowTileEntity extends TileEntity implements ITickableTileEntity {
//	private final List<BurrowTileEntity.Hopshel> hopshels = new ArrayList<>();

	public BurrowTileEntity() {
		super(ModBlocks.hopshel_burrow_tile.get());
	}

	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
//		this.hopshels.clear();
//		ListNBT listnbt = nbt.getList("Hopshels", 10);
//
//		for (int i = 0; i < listnbt.size(); ++i) {
//			CompoundNBT compoundnbt = listnbt.getCompound(i);
//			BurrowTileEntity.Hopshel hopshel = new BurrowTileEntity.Hopshel(compoundnbt.getCompound("EntityData"), compoundnbt.getInt("TicksInBurrow"), compoundnbt.getInt("MinOccupationTicks"));
//			this.hopshels.add(hopshel);
//		}
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
//		compound.put("Hopshels", this.getHopshels());
		return compound;
	}

//	private ListNBT getHopshels() {
//		ListNBT listnbt = new ListNBT();
//		for (BurrowTileEntity.Hopshel hopshel : hopshels) {
//			hopshel.entityData.remove("UUID");
//			CompoundNBT compoundnbt = new CompoundNBT();
//			compoundnbt.put("EntityData", hopshel.entityData);
//			compoundnbt.putInt("TicksInBurrow", hopshel.ticksInBurrow);
//			compoundnbt.putInt("MinOccupationTicks", hopshel.minOccupationTicks);
//			listnbt.add(compoundnbt);
//		}
//		return listnbt;
//	}

//	public void tryEnterBurrow(Entity entity, int initialTicksInBurrow) {
//		if (hopshels.size() < 2) {
//			entity.stopRiding();
//			entity.removePassengers();
//			CompoundNBT compoundnbt = new CompoundNBT();
//			entity.writeUnlessPassenger(compoundnbt);
//			hopshels.add(new BurrowTileEntity.Hopshel(compoundnbt, initialTicksInBurrow, 1000));
//			if (world != null) {
//				BlockPos blockpos = this.getPos();
//				world.playSound(null, blockpos.getX(), blockpos.getY(), blockpos.getZ(), SoundEvents.BLOCK_BEEHIVE_ENTER, SoundCategory.BLOCKS, 1.0F, 1.0F);
//			}
//			entity.remove();
//		}
//	}

//	private boolean trySpawnHopshel(BlockState blockState, BurrowTileEntity.Hopshel hopshel, @Nullable List<Entity> entities) {
//		BlockPos blockpos = this.getPos();
//		CompoundNBT compoundnbt = hopshel.entityData;
//		compoundnbt.remove("Passengers");
//		compoundnbt.remove("Leash");
//		compoundnbt.remove("UUID");
//		if (!world.getBlockState(blockpos.up()).getCollisionShape(world, blockpos.up()).isEmpty()) {
//			return false;
//		} else {
//			Entity entity = EntityType.loadEntityAndExecute(compoundnbt, world, (entity1) -> entity1);
//			if (entity != null) {
//				if (entity instanceof HopshelEntity) {
//					HopshelEntity hopshelEntity = (HopshelEntity) entity;
////					this.growInBurrow(hopshel.ticksInBurrow, hopshelEntity);
//					if (entities != null) {
//						entities.add(hopshelEntity);
//					}
//					double posY = (double) blockpos.getY() + 1.0D - (double) (entity.getHeight() / 2.0F);
//					entity.setLocationAndAngles(blockpos.getX(), posY, blockpos.getZ(), entity.rotationYaw, entity.rotationPitch);
//				}
//				world.playSound(null, blockpos, SoundEvents.BLOCK_BEEHIVE_EXIT, SoundCategory.BLOCKS, 1.0F, 1.0F);
//				return world.addEntity(entity);
//			} else {
//				return false;
//			}
//		}
//	}

//	private void growInBurrow(int tickInBurrow, HopshelEntity hopshelEntity) {
//		int i = hopshelEntity.getGrowingAge();
//		if (i < 0) {
//			hopshelEntity.setGrowingAge(Math.min(0, i + tickInBurrow));
//		} else if (i > 0) {
//			hopshelEntity.setGrowingAge(Math.max(0, i - tickInBurrow));
//		}
//		hopshelEntity.setInLove(Math.max(0, hopshelEntity.func_234178_eO_() - tickInBurrow));
//	}

	@Override
	public void tick() {
//		if (!world.isRemote) {
//			Iterator<BurrowTileEntity.Hopshel> iterator = this.hopshels.iterator();
//			BurrowTileEntity.Hopshel hopshel;
//			for (BlockState blockstate = this.getBlockState(); iterator.hasNext(); hopshel.ticksInBurrow++) {
//				hopshel = iterator.next();
//				if (hopshel.ticksInBurrow > hopshel.minOccupationTicks) {
//					if (this.trySpawnHopshel(blockstate, hopshel, null)) {
//						iterator.remove();
//					}
//				}
//			}
////			this.sendDebugData();
//		}
	}

//	public void printHopshels() {
//		System.out.println("Il y a " + hopshels.size() + " HopshelEntity");
//	}

//	static class Hopshel {
//		private final CompoundNBT entityData;
//		private final int minOccupationTicks;
//		private int ticksInBurrow;
//
//		private Hopshel(CompoundNBT nbt, int ticksInBurrow, int minOccupationTicks) {
//			nbt.remove("UUID");
//			this.entityData = nbt;
//			this.ticksInBurrow = ticksInBurrow;
//			this.minOccupationTicks = minOccupationTicks;
//		}
//	}
}
