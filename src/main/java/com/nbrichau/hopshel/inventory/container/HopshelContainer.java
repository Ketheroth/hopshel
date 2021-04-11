package com.nbrichau.hopshel.inventory.container;

import com.nbrichau.hopshel.HopshelMod;
import com.nbrichau.hopshel.entity.HopshelEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.Optional;

public class HopshelContainer extends Container {
	private HopshelEntity hopshelEntity;
	private PlayerEntity playerEntity;
	private IItemHandler playerInventory;

	public HopshelContainer(int windowId, World world, PlayerInventory playerInventory, PlayerEntity playerEntity, int hopshelId) {
		super(HopshelMod.HOPSHEL_CONTAINER.get(), windowId);
		hopshelEntity = (HopshelEntity) world.getEntity(hopshelId);
		this.playerEntity = playerEntity;
		this.playerInventory = new InvWrapper(playerInventory);
		if (hopshelEntity != null) {
			hopshelEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
				for (int i = 0; i < 8; i++) {
					addSlot(new SlotItemHandler(h, i, 17 + i * 18, 19));
				}
			});
		}
		layoutPlayerInventorySlot(8, 51);
	}

	@Override
	public boolean stillValid(PlayerEntity playerIn) {
		if (hopshelEntity == null) {
			return false;
		}
		return hopshelEntity.distanceTo(playerEntity) < 8.0f;
	}

	@Override
	public void removed(PlayerEntity playerIn) {
		hopshelEntity.setInventoryClosed();
		super.removed(playerIn);
	}

	private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
		for (int i = 0; i < amount; i++) {
			addSlot(new SlotItemHandler(handler, index, x, y));
			x += dx;
			index++;
		}
		return index;
	}

	private int addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
		for (int i = 0; i < verAmount; i++) {
			index = addSlotRange(handler, index, x, y, horAmount, dx);
			y += dy;
		}
		return index;
	}

	private void layoutPlayerInventorySlot(int leftCol, int topRow) {
		addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);
		topRow += 58;
		addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
	}

	@Override
	public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			Optional<IItemHandler> hopshelInventory = hopshelEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).resolve();
			if (!hopshelInventory.isPresent()) {
				return ItemStack.EMPTY;
			}
			if (index < hopshelInventory.get().getSlots()) {
				if (!this.moveItemStackTo(itemstack1, hopshelInventory.get().getSlots(), this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemstack1, 0, hopshelInventory.get().getSlots(), false)) {
				return ItemStack.EMPTY;
			}
			if (itemstack1.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}
		return itemstack;
	}
}
