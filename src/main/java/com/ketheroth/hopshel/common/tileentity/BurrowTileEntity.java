package com.ketheroth.hopshel.common.tileentity;

import com.ketheroth.hopshel.common.entity.HopshelEntity;
import com.ketheroth.hopshel.core.registry.HopshelTileEntities;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
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
public class BurrowTileEntity extends TileEntity implements ITickableTileEntity, IInventory {

	private final List<BurrowOccupant> burrowOccupants = new ArrayList<>();
	private ItemStackHandler items = new ItemStackHandler(9);
	private int transferCooldown;

	public BurrowTileEntity() {
		super(HopshelTileEntities.HOPSHEL_BURROW.get());
		this.transferCooldown = 20;
	}

	@Override
	public void load(BlockState state, CompoundNBT nbt) {
		super.load(state, nbt);
		items.deserializeNBT(nbt.getCompound("Inventory"));
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
		compound.put("Inventory", items.serializeNBT());
		return super.save(compound);
	}

	@Override
	public void tick() {
		if (level != null && !level.isClientSide()) {
			transferCooldown--;
			if (transferCooldown <= 0) {
				//transfer to
			}
			Iterator<BurrowOccupant> occupants = this.burrowOccupants.iterator();
			while (occupants.hasNext()) {
				BurrowOccupant occupant = occupants.next();
				if (occupant.tick(this.items)) {
					this.setChanged();
				}
				if (occupant.isInventoryEmpty()) {
					if (this.trySpawnHopshel(occupant)) {
						System.out.println("removing occupant");
						occupants.remove();
						this.setChanged();
					}
				}
			}
		}
	}

	public ItemStackHandler getInventory() {
		return this.items;
	}

	/*-------------------- Hopshel in burrow --------------------*/

	private ListNBT getBurrowOccupants() {
		ListNBT listnbt = new ListNBT();
		for (BurrowOccupant occupant : burrowOccupants) {
//			occupant.occupant.remove("UUID");
			CompoundNBT compoundnbt = new CompoundNBT();
			compoundnbt.put("EntityData", occupant.entityData);
			compoundnbt.putInt("TicksInBurrow", occupant.transferCooldown);
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
			this.setChanged();
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

	/*-------------------- IInventory --------------------*/

	@Override
	public int getContainerSize() {
		return items.getSlots();
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < items.getSlots(); i++) {
			if (!items.getStackInSlot(i).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getItem(int index) {
		return items.getStackInSlot(index);
	}

	@Override
	public ItemStack removeItem(int index, int amount) {
		ItemStack itemStack = items.extractItem(index, amount, false);
		if (!itemStack.isEmpty()) {
			this.setChanged();
		}
		return itemStack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		ItemStack itemStack = items.getStackInSlot(index);
		if (itemStack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		this.items.setStackInSlot(index, ItemStack.EMPTY);
		return itemStack;
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		items.setStackInSlot(index, stack);
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		return false;
	}

	@Override
	public void clearContent() {
		for (int i = 0; i < items.getSlots(); i++) {
			items.setStackInSlot(i, ItemStack.EMPTY);
		}
	}

	static class BurrowOccupant {

		private final CompoundNBT entityData;
		private int transferCooldown;

		private BurrowOccupant(CompoundNBT entityData, int initialTicks) {
			this.entityData = entityData;
			this.transferCooldown = initialTicks;
		}

		/**
		 * apply a tick to the burrow occupant
		 *
		 * @param burrowInventory the inventory of the burrow
		 * @return true if the burrow inventory have changed
		 */
		private boolean tick(ItemStackHandler burrowInventory) {
			transferCooldown--;
			if (transferCooldown <= 0) {
				ItemStackHandler hopshelInventory = this.getInventory();
				for (int hopshelSlot = 0; hopshelSlot < hopshelInventory.getSlots(); hopshelSlot++) {
					if (!hopshelInventory.getStackInSlot(hopshelSlot).isEmpty()) {
						System.out.println("transferring stack " + hopshelSlot + " " + hopshelInventory.getStackInSlot(hopshelSlot));
						ItemStack remainingStack = hopshelInventory.getStackInSlot(hopshelSlot).copy();
						for (int burrowSlot = 0; burrowSlot < burrowInventory.getSlots() && !remainingStack.isEmpty(); burrowSlot++) {
							remainingStack = burrowInventory.insertItem(burrowSlot, remainingStack, false);
						}
						if (remainingStack.getCount() != hopshelInventory.getStackInSlot(hopshelSlot).getCount()) {
							hopshelInventory.setStackInSlot(hopshelSlot, ItemStack.EMPTY);
							this.entityData.put("Inventory", hopshelInventory.serializeNBT());
							transferCooldown = 20;
							return true;
						}
					}
				}
			}
			return false;
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
