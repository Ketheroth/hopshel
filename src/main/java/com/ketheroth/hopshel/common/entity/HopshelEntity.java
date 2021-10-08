package com.ketheroth.hopshel.common.entity;

import com.ketheroth.hopshel.common.tileentity.BurrowTileEntity;
import com.ketheroth.hopshel.core.registry.HopshelBlocks;
import com.ketheroth.hopshel.core.registry.HopshelItems;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HopshelEntity extends TameableEntity {

	private static final DataParameter<Optional<BlockPos>> BURROW_POS = EntityDataManager.defineId(HopshelEntity.class, DataSerializers.OPTIONAL_BLOCK_POS);
	private final ItemStackHandler inventory = new ItemStackHandler(8);
	private final ItemStackHandler headSlot = new ItemStackHandler(1);
	private int cooldownTime;
	private int tickOutOfBurrow;
	private double vacuumDistance;

	public HopshelEntity(EntityType<? extends TameableEntity> type, World worldIn) {
		super(type, worldIn);
		this.cooldownTime = 20;
		this.tickOutOfBurrow = 0;
		this.vacuumDistance = 5.0D;
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
		compound.put("Inventory", inventory.serializeNBT());
		compound.put("HeadSlot", headSlot.serializeNBT());
		if (this.getBurrowPos().isPresent()) {
			compound.put("BurrowPos", NBTUtil.writeBlockPos(this.getBurrowPos().get()));
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundNBT compound) {
		super.readAdditionalSaveData(compound);
		inventory.deserializeNBT(compound.getCompound("Inventory"));
		headSlot.deserializeNBT(compound.getCompound("HeadSlot"));
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
	public boolean isFood(ItemStack stack) {
		return stack.getItem().equals(Items.CHORUS_FRUIT);
	}

	@Override
	public boolean canFallInLove() {
		return false;
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
			boolean flag = this.isOwnedBy(player) || this.isTame() || heldItem.equals(Items.CHORUS_FRUIT);
			return flag ? ActionResultType.CONSUME : ActionResultType.PASS;
		}
		if (heldItem.equals(Items.DEBUG_STICK)) {
			for (int i = 0; i < inventory.getSlots(); i++) {
				System.out.println("Item " + i + " : " + inventory.getStackInSlot(i));
			}
			System.out.println("is Tamed " + isTame());
			System.out.println("Helmet is " + headSlot.getStackInSlot(0));
			return ActionResultType.SUCCESS;
		} else if (heldItem.equals(Items.CHORUS_FRUIT)) {
			if (this.isTame()) {
				if (this.isFood(heldStack) && this.getHealth() < this.getMaxHealth()) {
					if (!player.abilities.instabuild) {
						heldStack.shrink(1);
					}
					//eat the chorus fruit if possible
					this.heal((float) heldItem.getFoodProperties().getNutrition());
					return ActionResultType.SUCCESS;
				}
				//get teleported by the chorus fruit
				heldStack.finishUsingItem(level, this);
			} else {
				if (!player.abilities.instabuild) {
					heldStack.shrink(1);
				}
				//try to tame the hopshel
				if (this.random.nextInt(4) <= 1 && !ForgeEventFactory.onAnimalTame(this, player)) {
					this.tame(player);
					this.navigation.stop();
					//spawn tame success particle
					// TODO: 12/09/2021 make the plates flash white
					this.level.broadcastEntityEvent(this, (byte) 7);
				} else {
					//get teleported by the chorus fruit
					heldStack.finishUsingItem(level, this);
					//spawn tame fail particle
					this.level.broadcastEntityEvent(this, (byte) 6);
				}
				return ActionResultType.SUCCESS;
			}
		} else if(isHelmet(heldItem) && headSlot.getStackInSlot(0).isEmpty()) {
			headSlot.insertItem(0, heldStack.copy(), false);
			heldStack.shrink(heldStack.getCount());
			this.changeHelmetValues(headSlot.getStackInSlot(0).getItem());
		} else if(heldStack.isEmpty()) {
			//if sneaking take the helmet
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
			if (cooldownTime <= 0) {
				if (!isInventoryFull()) {
					this.tryAbsorbItems();
				}
				this.reapplyEffects();
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
			this.reapplyEffects();
		}
		super.aiStep();
	}

	@Override
	public boolean isSensitiveToWater() {
		return !this.headSlot.getStackInSlot(0).isEmpty() && this.headSlot.getStackInSlot(0).getItem().equals(Items.TURTLE_HELMET);
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
		if (inventory != null) {
			for (int i = 0; i < inventory.getSlots(); i++) {
				ItemStack itemstack = inventory.getStackInSlot(i);
				if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack)) {
					this.spawnAtLocation(itemstack);
				}
			}
		}
		ItemStack helmet = headSlot.getStackInSlot(0);
		if (!helmet.isEmpty() && !EnchantmentHelper.hasVanishingCurse(helmet)) {
			this.spawnAtLocation(helmet);
		}
	}

	private boolean isInventoryFull() {
		for (int i = 0; i < inventory.getSlots(); i++) {
			ItemStack stackInSlot = inventory.getStackInSlot(i);
			if (stackInSlot.isEmpty() || stackInSlot.getCount() < stackInSlot.getMaxStackSize()) {
				return false;
			}
		}
		return true;
	}

	private void tryAbsorbItems() {
		if (level != null && !level.isClientSide) {
			List<ItemEntity> list = level.getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(this.vacuumDistance, this.vacuumDistance, this.vacuumDistance), EntityPredicates.ENTITY_STILL_ALIVE);
			for (ItemEntity itemEntity : list) {
				if (this.isAlive() && cooldownTime <= 0) {
					ItemStack remainingStack = itemEntity.getItem().copy();
					if (isHelmet(remainingStack.getItem())) {
						remainingStack = headSlot.insertItem(0, remainingStack, false);
						if (remainingStack.isEmpty()) {
							this.changeHelmetValues(itemEntity.getItem().getItem());
						}
					}
					for (int i = 0; i < inventory.getSlots() && !remainingStack.isEmpty(); i++) {
						remainingStack = inventory.insertItem(i, remainingStack, false);
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

	/*---------------------- Head Slot ----------------------*/

	@Override
	public void hurtArmor(DamageSource damageSource, float amount) {
		if (!(amount <= 0.0F)) {
			amount = amount / 4.0F;
			if (amount < 1.0F) {
				amount = 1.0F;
			}

			ItemStack headStack = this.headSlot.getStackInSlot(0);
			if ((!damageSource.isFire() || !headStack.getItem().isFireResistant()) && headStack.getItem() instanceof ArmorItem) {
				headStack.hurtAndBreak((int)amount, this, (entity) -> {
					entity.broadcastBreakEvent(EquipmentSlotType.byTypeAndIndex(EquipmentSlotType.Group.ARMOR, 3));
					changeHelmetValues(null);
				});
			}

		}
	}

	@Override
	public Iterable<ItemStack> getArmorSlots() {
		return Collections.singleton(this.headSlot.getStackInSlot(0));
	}

	private void changeHelmetValues(@Nullable Item helmet) {
		if (helmet == null || helmet.equals(Items.CHAINMAIL_HELMET)) {
			this.vacuumDistance = 5.0D;
			this.removeEffect(Effects.MOVEMENT_SPEED);
			this.removeEffect(Effects.MOVEMENT_SLOWDOWN);
			return;
		}
		this.vacuumDistance = 2.5D;
		if (helmet.equals(Items.BUCKET)) {
			this.vacuumDistance = 0D;
			this.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 200));
		} else if(helmet.equals(Items.GOLDEN_HELMET)) {
			this.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 200));
		}
	}

	private void reapplyEffects() {
		if (headSlot.getStackInSlot(0).isEmpty()) {
			return;
		}
		Item helmet = headSlot.getStackInSlot(0).getItem();
		if (helmet.equals(Items.BUCKET)) {
			this.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 200));
		} else if(helmet.equals(Items.GOLDEN_HELMET)) {
			this.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 200));
		}
	}

	private static boolean isHelmet(Item item) {
		if (item.equals(Items.BUCKET)) {
			return true;
		}
		if (!(item instanceof ArmorItem)) {
			return false;
		}
		EquipmentSlotType type = ((ArmorItem) item).getSlot();
		return type.getType().equals(EquipmentSlotType.Group.ARMOR) && type.getIndex() == 3;
	}

}
