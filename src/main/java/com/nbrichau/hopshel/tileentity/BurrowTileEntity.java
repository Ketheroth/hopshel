package com.nbrichau.hopshel.tileentity;

import com.nbrichau.hopshel.block.ModBlocks;
import com.nbrichau.hopshel.entity.HopshelEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BurrowTileEntity extends TileEntity implements ITickableTileEntity {
	private final List<BurrowOccupant> burrowOccupants = new ArrayList<>();

	public BurrowTileEntity() {
		super(ModBlocks.hopshel_burrow_tile.get());
	}

	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		this.burrowOccupants.clear();
		ListNBT listnbt = nbt.getList("burrowOccupants", 10);
		for (int i = 0; i < listnbt.size(); ++i) {
			CompoundNBT compoundnbt = listnbt.getCompound(i);
			BurrowOccupant occupant = new BurrowOccupant(compoundnbt.getCompound("EntityData"), compoundnbt.getInt("TicksInBurrow"));
			this.burrowOccupants.add(occupant);
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.put("burrowOccupants", this.getBurrowOccupants());
		return super.write(compound);
	}

	private ListNBT getBurrowOccupants() {
		ListNBT listnbt = new ListNBT();
		for (BurrowOccupant occupant : burrowOccupants) {
//			occupant.occupant.remove("UUID");
			CompoundNBT compoundnbt = new CompoundNBT();
			compoundnbt.put("EntityData", occupant.entityData);
			compoundnbt.putInt("TicksInBurrow", occupant.ticksInBurrow);
			listnbt.add(compoundnbt);
		}
		return listnbt;
	}

	public void tryEnterBurrow(Entity entity, int initialTicksInBurrow) {
		if (burrowOccupants.size() < 2) {
			entity.stopRiding();
			entity.removePassengers();
			CompoundNBT compoundnbt = new CompoundNBT();
			//save if custom name ?
			entity.writeUnlessPassenger(compoundnbt);
			burrowOccupants.add(new BurrowOccupant(compoundnbt, initialTicksInBurrow));
			if (world != null) {
				BlockPos blockpos = this.getPos();
				world.playSound(null, blockpos.getX(), blockpos.getY(), blockpos.getZ(), SoundEvents.BLOCK_BEEHIVE_ENTER, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
			entity.remove(true);
		}
	}

	private boolean trySpawnHopshel(BurrowOccupant occupant) {
		BlockPos blockpos = this.getPos();
		CompoundNBT compoundnbt = occupant.entityData;
//		compoundnbt.remove("Passengers");
//		compoundnbt.remove("Leash");
//		compoundnbt.remove("UUID");
		if (!world.getBlockState(blockpos.up()).getCollisionShape(world, blockpos.up()).isEmpty()) {
			return false;
		} else {
			Entity entity = EntityType.loadEntityAndExecute(compoundnbt, world, (entity1) -> entity1);
			if (entity != null) {
				if (entity instanceof HopshelEntity) {
					entity.setLocationAndAngles(blockpos.getX(), blockpos.up().getY() + 1.0F, blockpos.getZ(), entity.rotationYaw, entity.rotationPitch);
				}
				world.playSound(null, blockpos, SoundEvents.BLOCK_BEEHIVE_EXIT, SoundCategory.BLOCKS, 1.0F, 1.0F);
				return world.addEntity(entity);
			} else {
				return false;
			}
		}
	}

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
		if (world != null && !world.isRemote()) {
			Iterator<BurrowOccupant> iterator = this.burrowOccupants.iterator();
			if (iterator.hasNext()) {
				for (BurrowOccupant occupant = iterator.next(); iterator.hasNext(); occupant.ticksInBurrow++) {
					if (occupant.ticksInBurrow >= BurrowOccupant.MIN_OCCUPATION_TICKS) {
						if (this.trySpawnHopshel(occupant)) {
							iterator.remove();
						}
					}
				}
			}
		}
	}

	public boolean isFull() {
		return burrowOccupants.size() >= 2;
	}

	static class BurrowOccupant {
		private final static int MIN_OCCUPATION_TICKS = 1000;
		private final CompoundNBT entityData;
		private int ticksInBurrow;

		private BurrowOccupant(CompoundNBT entityData, int ticksInBurrow) {
			this.entityData = entityData;
			this.ticksInBurrow = ticksInBurrow;
		}
	}
}
