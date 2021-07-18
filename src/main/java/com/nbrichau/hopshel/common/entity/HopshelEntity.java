package com.nbrichau.hopshel.common.entity;

import com.nbrichau.hopshel.common.tileentity.BurrowTileEntity;
import com.nbrichau.hopshel.core.registry.HopshelBlocks;
import com.nbrichau.hopshel.core.registry.HopshelItems;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HopshelEntity extends TameableEntity {

	private static final DataParameter<Optional<BlockPos>> BURROW_POS = EntityDataManager.defineId(HopshelEntity.class, DataSerializers.OPTIONAL_BLOCK_POS);
	private ItemStackHandler items = new ItemStackHandler(8);
	private int cooldownTime;
	private int tickOutOfBurrow;

	public HopshelEntity(EntityType<? extends TameableEntity> type, World worldIn) {
		super(type, worldIn);
		this.cooldownTime = 20;
		this.tickOutOfBurrow = 0;
		this.setBurrowPos(null);
		this.setCanPickUpLoot(false);
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
		if (this.getBurrowPos().isPresent()) {
			compound.put("BurrowPos", NBTUtil.writeBlockPos(this.getBurrowPos().get()));
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundNBT compound) {
		super.readAdditionalSaveData(compound);
		items.deserializeNBT(compound.getCompound("Inventory"));
		if (compound.contains("BurrowPos")) {
			this.setBurrowPos(NBTUtil.readBlockPos(compound.getCompound("BurrowPos")));
		}
	}

	@Nullable
	@Override
	public AgeableEntity getBreedOffspring(ServerWorld world, AgeableEntity entity) {
		return null;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
	}

	@Override
	public ActionResultType mobInteract(PlayerEntity player, Hand handIn) {//onRightCLick
		ItemStack heldStack = player.getItemInHand(handIn);
		Item heldItem = heldStack.getItem();
		if (level.isClientSide) {
			boolean flag = this.isOwnedBy(player) || this.isTame() || (!this.isTame() && heldItem.equals(Items.CHORUS_FRUIT));
			return flag ? ActionResultType.CONSUME : ActionResultType.PASS;
		}
		if (heldItem.equals(Items.DEBUG_STICK)) {
			for (int i = 0; i < items.getSlots(); i++) {
				System.out.println("Item " + i + " : " + items.getStackInSlot(i));
			}
			System.out.println("is Tamed " + isTame());
			return ActionResultType.SUCCESS;
		} else if (heldItem.equals(Items.CHORUS_FRUIT)) {
			if (this.isTame()) {
				if (this.isFood(heldStack) && this.getHealth() < this.getMaxHealth()) {
					if (!player.abilities.instabuild) {
						heldStack.shrink(1);
					}
					this.heal((float)heldItem.getFoodProperties().getNutrition());
					return ActionResultType.SUCCESS;
				}
			} else {
				if (!player.abilities.instabuild) {
					heldStack.shrink(1);
				}
				if (this.random.nextInt(4) <= 1 && !ForgeEventFactory.onAnimalTame(this, player)) {
					this.tame(player);
					this.navigation.stop();
				}
				return ActionResultType.SUCCESS;
			}
		}
		return super.mobInteract(player, handIn);
	}

	@Override
	public ItemStack getPickedResult(RayTraceResult target) {
		return new ItemStack(HopshelItems.HOPSHEL_SPAWN_EGG.get());
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(BURROW_POS, Optional.empty());
	}

	@Override
	public void aiStep() {
		if (!this.level.isClientSide && this.isAlive()) {
			cooldownTime--;
			tickOutOfBurrow++;
			if (cooldownTime <= 0 && !isInventoryFull()) {
				this.tryAbsorbItems();
			}
			if (!this.getBurrowPos().isPresent()) {
				if (this.level.getGameTime() % 20 == 0 || (tickOutOfBurrow > 6000 || this.isInventoryFull())) {
					//find burrow
					System.out.println("searching for burrow");
					BlockPos pos = this.findNearbyBurrow();
					if (pos != null) {
						this.setBurrowPos(pos);
					}
				}
			} else {
				if (this.getBurrow() == null) {
					//burrow block have been removed, but burrow pos was not updated
					this.setBurrowPos(null);
				} else {
					if (tickOutOfBurrow > 6000 || this.isInventoryFull()) {//every 5 minutes
						tickOutOfBurrow = 0;
						this.getBurrow().tryEnterBurrow(this);
						System.out.println("going in burrow");
					}
				}
			}
		}
		super.aiStep();
	}

	public void resetCountdowns() {
		this.tickOutOfBurrow = 0;
		this.cooldownTime = 20;
	}

	/*-------------------- Go In Burrow --------------------*/

	public Optional<BlockPos> getBurrowPos() {
		return this.getEntityData().get(BURROW_POS);
	}

	public void setBurrowPos(@Nullable BlockPos pos) {
		this.getEntityData().set(BURROW_POS, Optional.ofNullable(pos));
	}

	@Nullable
	public BurrowTileEntity getBurrow() {
		Optional<BlockPos> pos = this.getBurrowPos();
		if (pos.isPresent()) {
			TileEntity tile = this.level.getBlockEntity(pos.get());
			if (tile instanceof BurrowTileEntity) {
				return ((BurrowTileEntity) tile);
			}
		}
		return null;
	}

	@Nullable
	public BlockPos findNearbyBurrow() {
		BlockPos pos = this.blockPosition();
		for (BlockPos blockPos : BlockPos.betweenClosed(pos.offset(-16, -8, -16), pos.offset(16, 8, 16))) {
			if (this.level.getBlockState(blockPos).getBlock().equals(HopshelBlocks.HOPSHEL_BURROW.get()) && level.getBlockEntity(blockPos) instanceof BurrowTileEntity) {
				BurrowTileEntity burrow = ((BurrowTileEntity) level.getBlockEntity(blockPos));
				if (burrow != null && !burrow.isFull() && this.getBurrow() == null) {
					System.out.println("found burrow at " + blockPos);
					return blockPos;
				}
			}
		}
		return null;
	}

	/*-------------------- Pick Up Items --------------------*/

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
			if (stackInSlot.isEmpty() || stackInSlot.getCount() < stackInSlot.getMaxStackSize()) {
				return false;
			}
		}
		return true;
	}

	private void tryAbsorbItems() {
		if (level != null && !level.isClientSide) {
			List<ItemEntity> list = level.getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(5.0D, 5.0D, 5.0D), EntityPredicates.ENTITY_STILL_ALIVE);
			for (ItemEntity itemEntity : list) {
				if (this.isAlive() && cooldownTime <= 0) {
					ItemStack remainingStack = itemEntity.getItem().copy();
					for (int i = 0; i < items.getSlots() && !remainingStack.isEmpty(); i++) {
						remainingStack = this.addItem(remainingStack, i);
					}
					if (remainingStack.isEmpty()) {
						itemEntity.remove();
					} else {
						itemEntity.setItem(remainingStack);
					}
					cooldownTime = 20;
				}
			}
		}
	}

	private ItemStack addItem(ItemStack originStack, int index) {
		ItemStack destinationStack = items.getStackInSlot(index);
		if (destinationStack.isEmpty()) {
			items.setStackInSlot(index, originStack);
			originStack = ItemStack.EMPTY;
		} else if (canMergeItems(destinationStack, originStack)) {
			int i = destinationStack.getMaxStackSize() - destinationStack.getCount();
			int j = Math.min(originStack.getCount(), i);
			originStack.shrink(j);
			destinationStack.grow(j);
		}
		return originStack;
	}

	private boolean canMergeItems(ItemStack destination, ItemStack origin) {
		if (destination.getItem() != origin.getItem()) {
			return false;
		} else if (destination.getDamageValue() != origin.getDamageValue()) {
			return false;
		} else if (destination.getCount() > destination.getMaxStackSize()) {
			return false;
		} else {
			return ItemStack.tagMatches(destination, origin);
		}
	}

}
