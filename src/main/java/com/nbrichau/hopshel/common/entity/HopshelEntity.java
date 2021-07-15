package com.nbrichau.hopshel.common.entity;

import com.google.common.collect.ImmutableSet;
import com.nbrichau.hopshel.common.tileentity.BurrowTileEntity;
import com.nbrichau.hopshel.core.registry.HopshelBlocks;
import com.nbrichau.hopshel.core.registry.HopshelItems;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.village.PointOfInterest;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO: 01/03/2021 Make the entity go in the burrow
public class HopshelEntity extends AnimalEntity {

	private static final DataParameter<Optional<BlockPos>> BURROW_POS = EntityDataManager.defineId(HopshelEntity.class, DataSerializers.OPTIONAL_BLOCK_POS);
	private ItemStackHandler itemHandler = this.createHandler();//internal
	private LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);//external capability
	private int remainingCooldownBeforeLocatingNewBurrow = 0;
	private FindBurrowGoal findBurrowGoal;
	private boolean inventoryOpen = false;
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
		if (this.getBurrowPos() != null) {
			compound.put("BurrowPos", NBTUtil.writeBlockPos(this.getBurrowPos()));
		}
		compound.put("Inventory", itemHandler.serializeNBT());
	}

	@Override
	public void readAdditionalSaveData(CompoundNBT compound) {
		super.readAdditionalSaveData(compound);
		this.setBurrowPos(NBTUtil.readBlockPos(compound.getCompound("BurrowPos")));
		itemHandler.deserializeNBT(compound.getCompound("Inventory"));
	}

	@Nullable
	@Override
	public AgeableEntity getBreedOffspring(ServerWorld p_241840_1_, AgeableEntity p_241840_2_) {
		return null;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		goalSelector.addGoal(1, new EnterBurrowGoal());
		goalSelector.addGoal(1, new SwimGoal(this));
		goalSelector.addGoal(2, new SuckUpItemGoal());
		goalSelector.addGoal(5, new UpdateBurrowGoal());
		findBurrowGoal = new FindBurrowGoal();
		goalSelector.addGoal(5, this.findBurrowGoal);
		goalSelector.addGoal(6, new WaterAvoidingRandomWalkingGoal(this, 0.7D) {
			@Override
			public boolean canUse() {
				return !HopshelEntity.this.inventoryOpen && !isInventoryFull() && super.canUse();
			}

			@Override
			public boolean canContinueToUse() {
				return !HopshelEntity.this.inventoryOpen && !isInventoryFull() && super.canContinueToUse();
			}
		});
		goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 6.0F));
		goalSelector.addGoal(8, new LookRandomlyGoal(this));
	}

	private ItemStackHandler createHandler() {
		return new ItemStackHandler(8) {
			@Override
			protected void onContentsChanged(int slot) {
//				markDirty();
				// TODO: 01/03/2021 markDirty
			}
		};
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (capability.equals(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)) {
			return handler.cast();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public ActionResultType mobInteract(PlayerEntity playerEntity, Hand handIn) {//onRightCLick
		if (playerEntity.getItemInHand(handIn).getItem().equals(Items.STICK)) {
			for (int i = 0; i < itemHandler.getSlots(); i++) {
				System.out.println("Item " + i + " : " + itemHandler.getStackInSlot(i).toString());
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
		entityData.define(BURROW_POS, Optional.empty());
	}

	@Override
	public void aiStep() {
		if (this.remainingCooldownBeforeLocatingNewBurrow > 0) {
			this.remainingCooldownBeforeLocatingNewBurrow--;
		}
		transferCooldown--;
		super.aiStep();
	}

	@Override
	protected void dropEquipment() {
		super.dropEquipment();
		if (itemHandler != null) {
			for (int i = 0; i < itemHandler.getSlots(); i++) {
				ItemStack itemstack = itemHandler.getStackInSlot(i);
				if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack)) {
					this.spawnAtLocation(itemstack);
				}
			}
		}
	}

	private boolean canEnterBurrow() {
		// TODO: 06/02/2021 change canEnterBurrow according to data (timeOutOfBurrow or other things)
		return true;
	}

	@Nullable
	private BlockPos getBurrowPos() {
		return this.getEntityData().get(BURROW_POS).orElse(null);
	}

	public void setBurrowPos(@Nullable BlockPos pos) {
		this.entityData.set(BURROW_POS, Optional.ofNullable(pos));
	}

	private boolean hasBurrow() {
		return this.entityData.get(BURROW_POS).isPresent();
	}

	private boolean isWithinDistance(BlockPos pos, int distance) {
		return pos.closerThan(this.blockPosition(), distance);
	}

	private boolean isTooFar(BlockPos pos) {
		return !this.isWithinDistance(pos, 32);
	}

	private void startMovingTo(BlockPos pos) {
		Vector3d vector3d = Vector3d.atBottomCenterOf(pos);
		int i = 0;
		BlockPos blockpos = this.blockPosition();
		int j = (int) vector3d.y - blockpos.getY();
		if (j > 2) {
			i = 4;
		} else if (j < -2) {
			i = -4;
		}

		int k = 6;
		int l = 8;
		int i1 = blockpos.distManhattan(pos);
		if (i1 < 15) {
			k = i1 / 2;
			l = i1 / 2;
		}

		Vector3d vector3d1 = RandomPositionGenerator.getAirPosTowards(this, k, l, i, vector3d, (double) ((float) Math.PI / 10F));
		if (vector3d1 != null) {
			this.navigation.setMaxVisitedNodesMultiplier(0.5F);
			this.navigation.moveTo(vector3d1.x, vector3d1.y, vector3d1.z, 1.0D);
		}
	}

	private boolean doesBurrowHaveSpace(BlockPos pos) {
		TileEntity tileentity = this.level.getBlockEntity(pos);
		if (tileentity instanceof BurrowTileEntity) {
			return !((BurrowTileEntity) tileentity).isFull();
		} else {
			return false;
		}
	}

	private boolean haveToFindBurrrow() {
		return !hasBurrow() && isInventoryFull();
	}

	private boolean isInventoryFull() {
		for (int i = 0; i < itemHandler.getSlots(); i++) {
			ItemStack stackInSlot = itemHandler.getStackInSlot(i);
			if (stackInSlot.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public void setInventoryClosed() {
		this.inventoryOpen = false;
	}

	public boolean canTakeItem(ItemStack itemStack) {
		for (int i = 0; i < itemHandler.getSlots(); i++) {
			ItemStack stackInSlot = itemHandler.getStackInSlot(i);
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

	class EnterBurrowGoal extends Goal {

		@Override
		public boolean canUse() {
			if (HopshelEntity.this.hasBurrow() && HopshelEntity.this.canEnterBurrow() && HopshelEntity.this.getBurrowPos().closerThan(HopshelEntity.this.position(), 2.0D)) {
				TileEntity tileentity = HopshelEntity.this.level.getBlockEntity(HopshelEntity.this.getBurrowPos());
				if (tileentity instanceof BurrowTileEntity) {
					BurrowTileEntity burrowTileEntity = (BurrowTileEntity) tileentity;
					if (!burrowTileEntity.isFull()) {
						return true;
					}
					HopshelEntity.this.setBurrowPos(null);//chosen burrow was full
				}
			}
			return false;
		}

		@Override
		public boolean canContinueToUse() {
			return false;
		}

		@Override
		public void start() {
			TileEntity tileentity = HopshelEntity.this.level.getBlockEntity(HopshelEntity.this.getBurrowPos());
			if (tileentity instanceof BurrowTileEntity) {
				BurrowTileEntity burrowTileEntity = (BurrowTileEntity) tileentity;
				burrowTileEntity.tryEnterBurrow(HopshelEntity.this, 50);//with random ?
			}
		}

	}

	class FindBurrowGoal extends Goal {

		private int ticks = HopshelEntity.this.level.random.nextInt(10);
		private List<BlockPos> possibleBurrows = new ArrayList<>();
		@Nullable
		private Path path = null;
		private int counter;

		FindBurrowGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			return HopshelEntity.this.hasBurrow() && !HopshelEntity.this.hasRestriction() && HopshelEntity.this.canEnterBurrow() && !this.isCloseEnough(HopshelEntity.this.getBurrowPos()) && HopshelEntity.this.level.getBlockState(HopshelEntity.this.getBurrowPos()).is(HopshelBlocks.HOPSHEL_BURROW.get());
		}

		@Override
		public boolean canContinueToUse() {
			return this.canUse();
		}

		@Override
		public void start() {
			this.ticks = 0;
			this.counter = 0;
			super.start();
		}

		public void stop() {
			this.ticks = 0;
			this.counter = 0;
			HopshelEntity.this.navigation.stop();
			HopshelEntity.this.navigation.resetMaxVisitedNodesMultiplier();
		}

		public void tick() {
			if (HopshelEntity.this.hasBurrow()) {
				++this.ticks;
				if (this.ticks > 600) {
					this.makeChosenBurrowPossibleBurrow();
				} else if (!HopshelEntity.this.navigation.isInProgress()) {
					if (!HopshelEntity.this.isWithinDistance(HopshelEntity.this.getBurrowPos(), 16)) {
						if (HopshelEntity.this.isTooFar(HopshelEntity.this.getBurrowPos())) {
							this.reset();
						} else {
							HopshelEntity.this.startMovingTo(HopshelEntity.this.getBurrowPos());
						}
					} else {
						if (!this.startMovingToFar(HopshelEntity.this.getBurrowPos())) {
							this.makeChosenBurrowPossibleBurrow();
						} else if (this.path != null && HopshelEntity.this.navigation.getPath().sameAs(this.path)) {
							++this.counter;
							if (this.counter > 60) {
								this.reset();
								this.counter = 0;
							}
						} else {
							this.path = HopshelEntity.this.navigation.getPath();
						}
					}
				}
			}
		}

		private boolean startMovingToFar(BlockPos pos) {
			HopshelEntity.this.navigation.setMaxVisitedNodesMultiplier(10.0F);
			HopshelEntity.this.navigation.moveTo(pos.getX(), pos.getY(), pos.getZ(), 1.0D);
			return HopshelEntity.this.navigation.getPath() != null && HopshelEntity.this.navigation.getPath().canReach();
		}

		private boolean isPossibleBurrow(BlockPos pos) {
			return this.possibleBurrows.contains(pos);
		}

		private void addPossibleBurrows(BlockPos pos) {
			this.possibleBurrows.add(pos);
			while (this.possibleBurrows.size() > 3) {
				this.possibleBurrows.remove(0);
			}
		}

		private void clearPossibleHives() {
			this.possibleBurrows.clear();
		}

		private void makeChosenBurrowPossibleBurrow() {
			if (HopshelEntity.this.getBurrowPos() != null) {
				this.addPossibleBurrows(HopshelEntity.this.getBurrowPos());
			}
			this.reset();
		}

		private void reset() {
			HopshelEntity.this.setBurrowPos(null);
			HopshelEntity.this.remainingCooldownBeforeLocatingNewBurrow = 200;
		}

		private boolean isCloseEnough(BlockPos pos) {
			if (HopshelEntity.this.isWithinDistance(pos, 1)) {
				return true;
			} else {
				Path path = HopshelEntity.this.navigation.getPath();
				return path != null && path.getTarget().equals(pos) && path.canReach() && path.isDone();
			}
		}

	}

	class UpdateBurrowGoal extends Goal {

		private UpdateBurrowGoal() {
		}

		@Override
		public boolean canUse() {
			return HopshelEntity.this.remainingCooldownBeforeLocatingNewBurrow == 0 && !HopshelEntity.this.hasBurrow() && HopshelEntity.this.canEnterBurrow();
		}

		@Override
		public boolean canContinueToUse() {
			return false;
		}

		@Override
		public void start() {
			HopshelEntity.this.remainingCooldownBeforeLocatingNewBurrow = 200;
			List<BlockPos> list = this.getNearbyFreeBurrow();
			if (!list.isEmpty()) {
				for (BlockPos blockpos : list) {
					if (!HopshelEntity.this.findBurrowGoal.isPossibleBurrow(blockpos)) {
						HopshelEntity.this.setBurrowPos(blockpos);
						return;
					}
				}
				HopshelEntity.this.findBurrowGoal.clearPossibleHives();
				HopshelEntity.this.setBurrowPos(list.get(0));
			}
		}

		private List<BlockPos> getNearbyFreeBurrow() {
			BlockPos blockpos = HopshelEntity.this.blockPosition();
			PointOfInterestManager pointofinterestmanager = ((ServerWorld) HopshelEntity.this.level).getPoiManager();
			ImmutableSet.copyOf(HopshelBlocks.HOPSHEL_BURROW.get().getStateDefinition().getPossibleStates());
			PointOfInterestType poit = new PointOfInterestType("hopshel_burrow", ImmutableSet.copyOf(HopshelBlocks.HOPSHEL_BURROW.get().getStateDefinition().getPossibleStates()), 0, 1);
			Stream<PointOfInterest> stream = pointofinterestmanager.getInRange((pointOfInterest) -> pointOfInterest == poit, blockpos, 20, PointOfInterestManager.Status.ANY);
			return stream.map(PointOfInterest::getPos)
					.filter(HopshelEntity.this::doesBurrowHaveSpace)
					.sorted(Comparator.comparingDouble((pos) -> pos.distSqr(blockpos)))
					.collect(Collectors.toList());
		}

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
					for (int i = 0; i < itemHandler.getSlots(); i++) {
						//is the item accepted into the slot ?
						if (itemHandler.isItemValid(i, pickupStack) && itemHandler.insertItem(i, pickupStack, true).getCount() != pickupStack.getCount()) {
							//actually split the picked up stack
							ItemStack actualPickupStack = itemEntity.getItem().split(1);
							//insert the stack
							ItemStack remaining = itemHandler.insertItem(i, actualPickupStack, false);
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
