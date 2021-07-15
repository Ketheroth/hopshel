package com.nbrichau.hopshel.common.entity;

import com.nbrichau.hopshel.core.registry.HopshelItems;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;

// TODO: 01/03/2021 Make the entity go in the burrow
public class HopshelEntity extends AnimalEntity {

	private ItemStackHandler items = new ItemStackHandler(8);
	private int transferCooldown = 0;

	public HopshelEntity(EntityType<? extends AnimalEntity> type, World worldIn) {
		super(type, worldIn);
		this.setCanPickUpLoot(true);
	}

	public static AttributeModifierMap.MutableAttribute registerAttributes() {
		return MobEntity.createMobAttributes().add(Attributes.MAX_HEALTH, 18.0D).add(Attributes.MOVEMENT_SPEED, 0.3F);
	}

	@Override
	public ILivingEntityData finalizeSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
		System.out.println("I'm spawning in world");
		return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
	}

	@Override
	public void onRemovedFromWorld() {
		System.out.println("I'm removed from world");
		super.onRemovedFromWorld();
	}

	@Override
	public void addAdditionalSaveData(CompoundNBT compound) {
		super.addAdditionalSaveData(compound);
		compound.put("Inventory", items.serializeNBT());
	}

	@Override
	public void readAdditionalSaveData(CompoundNBT compound) {
		super.readAdditionalSaveData(compound);
		items.deserializeNBT(compound.getCompound("Inventory"));
	}

	@Nullable
	@Override
	public AgeableEntity getBreedOffspring(ServerWorld p_241840_1_, AgeableEntity p_241840_2_) {
		return null;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		goalSelector.addGoal(2, new SuckUpItemGoal());
	}

	@Override
	public ActionResultType mobInteract(PlayerEntity playerEntity, Hand handIn) {//onRightCLick
		if (playerEntity.getItemInHand(handIn).getItem().equals(Items.STICK)) {
			for (int i = 0; i < items.getSlots(); i++) {
				System.out.println("Item " + i + " : " + items.getStackInSlot(i).toString());
			}
		}
		return super.mobInteract(playerEntity, handIn);
	}

	@Override
	public ItemStack getPickedResult(RayTraceResult target) {
		return new ItemStack(HopshelItems.HOPSHEL_SPAWN_EGG.get());
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
	}

	@Override
	public void aiStep() {
		transferCooldown--;
		super.aiStep();
	}

	@Override
	protected void dropEquipment() {
		super.dropEquipment();
		if (items != null) {
			for (int i = 0; i < items.getSlots(); i++) {
				ItemStack itemstack = items.getStackInSlot(i);
				if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack)) {
					this.spawnAtLocation(itemstack);
				}
			}
		}
	}

	private boolean isInventoryFull() {
		for (int i = 0; i < items.getSlots(); i++) {
			ItemStack stackInSlot = items.getStackInSlot(i);
			if (stackInSlot.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public boolean canTakeItem(ItemStack itemStack) {
		for (int i = 0; i < items.getSlots(); i++) {
			ItemStack stackInSlot = items.getStackInSlot(i);
			if (stackInSlot.isEmpty() || (stackInSlot.sameItem(itemStack) && stackInSlot.getCount() < stackInSlot.getMaxStackSize())) {
				System.out.println("true");
				return true;
			}
		}
		System.out.println("false");
		return false;
	}

	private boolean isOnTransferCooldown() {
		return this.transferCooldown > 0;
	}

	public void setTransferCooldown(int ticks) {
		this.transferCooldown = ticks;
	}

	class SuckUpItemGoal extends Goal {

		@Override
		public boolean canUse() {
			List<ItemEntity> list = level.getEntitiesOfClass(ItemEntity.class, HopshelEntity.this.getBoundingBox().inflate(2.5D, 2.5D, 2.5D), EntityPredicates.ENTITY_STILL_ALIVE);
			return !list.isEmpty() && list.stream().anyMatch(itemEntity -> HopshelEntity.this.canTakeItem(itemEntity.getItem()));
		}

		@Override
		public boolean canContinueToUse() {
			return false;
		}

		@Override
		public void start() {
			if (level != null && !level.isClientSide) {
				if (HopshelEntity.this.isOnTransferCooldown()) {
					return;
				}
				//get all items entity can pickUp
				List<ItemEntity> list = level.getEntitiesOfClass(ItemEntity.class, HopshelEntity.this.getBoundingBox().inflate(2.5D, 2.5D, 2.5D), EntityPredicates.ENTITY_STILL_ALIVE);
				for (ItemEntity itemEntity : list) {
					///create a simulated stack of what to insert
					ItemStack pickupStack = itemEntity.getItem().copy().split(1);
					for (int i = 0; i < items.getSlots(); i++) {
						//is the item accepted into the slot ?
						if (items.isItemValid(i, pickupStack) && items.insertItem(i, pickupStack, true).getCount() != pickupStack.getCount()) {
							//actually split the picked up stack
							ItemStack actualPickupStack = itemEntity.getItem().split(1);
							//insert the stack
							ItemStack remaining = items.insertItem(i, actualPickupStack, false);
							//if leftover, spawn them in the world
							if (!remaining.isEmpty()) {
								ItemEntity item = new ItemEntity(EntityType.ITEM, level);
								item.setItem(remaining);
								item.setPos(HopshelEntity.this.getX() + 0.5f, HopshelEntity.this.getY() + 0.5f, HopshelEntity.this.getZ() + 0.5f);
								item.lifespan = remaining.getEntityLifespan(level);
								level.addFreshEntity(item);
							}
							HopshelEntity.this.setTransferCooldown(8);
							return;
						}
					}
				}
			}
		}

	}

}
