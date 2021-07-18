package com.nbrichau.hopshel.common.tileentity;

import com.nbrichau.hopshel.common.entity.HopshelEntity;
import com.nbrichau.hopshel.core.registry.HopshelTileEntities;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BurrowTileEntity extends TileEntity implements ITickableTileEntity {

	private final List<BurrowOccupant> burrowOccupants = new ArrayList<>();

	public BurrowTileEntity() {
		super(HopshelTileEntities.HOPSHEL_BURROW.get());
	}

	@Override
	public void load(BlockState state, CompoundNBT nbt) {
		super.load(state, nbt);
		this.burrowOccupants.clear();
		ListNBT listnbt = nbt.getList("burrowOccupants", 10);
		for (int i = 0; i < listnbt.size(); ++i) {
			CompoundNBT compoundnbt = listnbt.getCompound(i);
			BurrowOccupant occupant = new BurrowOccupant(compoundnbt.getCompound("EntityData"), compoundnbt.getInt("TicksInBurrow"));
			this.burrowOccupants.add(occupant);
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT compound) {
		compound.put("burrowOccupants", this.getBurrowOccupants());
		return super.save(compound);
	}

	@Override
	public void tick() {
		if (level != null && !level.isClientSide()) {
			Iterator<BurrowOccupant> occupants = this.burrowOccupants.iterator();
			while (occupants.hasNext()) {
				BurrowOccupant occupant = occupants.next();
				occupant.tick();
				if (occupant.isInventoryEmpty()) {
					if (this.trySpawnHopshel(occupant)) {
						System.out.println("removing occupant");
						occupants.remove();
					}
				}
			}
		}
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

	public void tryEnterBurrow(Entity entity) {
		if (burrowOccupants.size() < 2) {
			entity.stopRiding();
			entity.ejectPassengers();
			CompoundNBT compoundnbt = new CompoundNBT();
			//save if custom name ?
			entity.save(compoundnbt);
			burrowOccupants.add(new BurrowOccupant(compoundnbt, 0));
			if (level != null) {
				BlockPos blockpos = this.getBlockPos();
				level.playSound(null, blockpos.getX(), blockpos.getY(), blockpos.getZ(), SoundEvents.BEEHIVE_ENTER, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
			entity.remove(true);
		}
	}

	private boolean trySpawnHopshel(BurrowOccupant occupant) {
		BlockPos blockpos = this.getBlockPos();
		CompoundNBT compoundnbt = occupant.entityData;
//		compoundnbt.remove("Passengers");
//		compoundnbt.remove("Leash");
//		compoundnbt.remove("UUID");
		BlockPos above = blockpos.above();
		if (level == null || !level.getBlockState(above).getCollisionShape(level, above).isEmpty()) {
			return false;
		}
		Entity entity = EntityType.loadEntityRecursive(compoundnbt, level, (entity1) -> entity1);
		if (!(entity instanceof HopshelEntity)) {
			return false;
		}
		entity.moveTo(blockpos.getX() + 0.5D, blockpos.above().getY() + 0.5F, blockpos.getZ() + 0.5D, entity.yRot, entity.xRot);
		((HopshelEntity) entity).resetCountdowns();
		((HopshelEntity) entity).setBurrowPos(blockpos);
		level.playSound(null, blockpos, SoundEvents.BEEHIVE_EXIT, SoundCategory.BLOCKS, 1.0F, 1.0F);
		return level.addFreshEntity(entity);
	}

	public boolean isFull() {
		return burrowOccupants.size() >= 2;
	}
	public int occupantAmount() {
		return burrowOccupants.size();
	}

	static class BurrowOccupant {

		private final static int ONE_SECOND = 20;
		private final CompoundNBT entityData;
		private int ticksInBurrow;

		private BurrowOccupant(CompoundNBT entityData, int initialTicks) {
			this.entityData = entityData;
			this.ticksInBurrow = initialTicks;
		}

		private void tick() {
			ticksInBurrow++;
			if (ticksInBurrow >= ONE_SECOND) {
				ticksInBurrow = 0;
				ItemStackHandler items = this.getInventory();
				for (int i = 0; i < items.getSlots(); i++) {
					if (!items.getStackInSlot(i).isEmpty()) {
						System.out.println("removing stack " + i + " " + items.getStackInSlot(i));
						items.setStackInSlot(i, ItemStack.EMPTY);
						// TODO: 18/07/2021 put the stack in the burrow instead of removing it
						this.entityData.put("Inventory", items.serializeNBT());
						return;
					}
				}
			}
		}

		private ItemStackHandler getInventory() {
			ItemStackHandler items = new ItemStackHandler();
			if (entityData.contains("Inventory")) {
				items.deserializeNBT(entityData.getCompound("Inventory"));
			}
			return items;
		}

		private boolean isInventoryEmpty() {
			ItemStackHandler items = getInventory();
			for (int i = 0; i < items.getSlots(); i++) {
				if (!items.getStackInSlot(i).isEmpty()) {
					return false;
				}
			}
			return true;
		}

	}

}
