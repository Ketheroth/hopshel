package com.nbrichau.hopshel.entity;

import com.google.common.collect.ImmutableSet;
import com.nbrichau.hopshel.block.ModBlocks;
import com.nbrichau.hopshel.inventory.container.HopshelContainer;
import com.nbrichau.hopshel.tileentity.BurrowTileEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.village.PointOfInterest;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO: 01/03/2021 Make the entity go in the burrow
public class HopshelEntity extends AnimalEntity {
	private static final DataParameter<Optional<BlockPos>> BURROW_POS = EntityDataManager.createKey(HopshelEntity.class, DataSerializers.OPTIONAL_BLOCK_POS);
	private ItemStackHandler itemHandler = createHandler();//internal
	private LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);//external capability
	private int remainingCooldownBeforeLocatingNewBurrow = 0;
	private FindBurrowGoal findBurrowGoal;
	private boolean inventoryOpen = false;

	public HopshelEntity(EntityType<? extends AnimalEntity> type, World worldIn) {
		super(type, worldIn);
	}

	public static AttributeModifierMap.MutableAttribute registerAttributes() {
		return MobEntity.func_233666_p_().createMutableAttribute(Attributes.MAX_HEALTH, 24.0D).createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.3F);
	}

	@Override
	public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
		System.out.println("I'm spawning in world");
		return super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
	}

	@Override
	public void onRemovedFromWorld() {
		System.out.println("I'm removed from world");
		super.onRemovedFromWorld();
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.put("BurrowPos", NBTUtil.writeBlockPos(this.getBurrowPos()));
		compound.put("Inventory", itemHandler.serializeNBT());
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		this.setBurrowPos(NBTUtil.readBlockPos(compound.getCompound("BurrowPos")));
		itemHandler.deserializeNBT(compound.getCompound("Inventory"));
	}

	@Nullable
	@Override
	public AgeableEntity func_241840_a(ServerWorld p_241840_1_, AgeableEntity p_241840_2_) {
		return null;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(1, new EnterBurrowGoal());
		goalSelector.addGoal(1, new SwimGoal(this));
		this.goalSelector.addGoal(3, new UpdateBurrowGoal());//5
		this.findBurrowGoal = new FindBurrowGoal();
		goalSelector.addGoal(3, this.findBurrowGoal);//5
		goalSelector.addGoal(6, new WaterAvoidingRandomWalkingGoal(this, 0.7D) {
			@Override
			public boolean shouldExecute() {
				return !HopshelEntity.this.inventoryOpen && super.shouldExecute();
			}

			@Override
			public boolean shouldContinueExecuting() {
				return !HopshelEntity.this.inventoryOpen && super.shouldContinueExecuting();
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
	public ActionResultType func_230254_b_(PlayerEntity playerEntity, Hand handIn) {//onRightCLick
		if (!world.isRemote()) {
			INamedContainerProvider containerProvider = new INamedContainerProvider() {
				@Override
				public ITextComponent getDisplayName() {
					return new TranslationTextComponent("screen.hopshel.hopshel_inventory");
				}

				@Nullable
				@Override
				public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity player) {
					return new HopshelContainer(id, world, playerInventory, player, HopshelEntity.this.getEntityId());
				}
			};
			inventoryOpen = true;
			navigator.clearPath();// TODO: 01/03/2021 stop entity moving when inventory is open
			NetworkHooks.openGui((ServerPlayerEntity) playerEntity, containerProvider, buf -> buf.writeInt(HopshelEntity.this.getEntityId()));
		}
		return super.func_230254_b_(playerEntity, handIn);
	}

	@Override
	protected void registerData() {
		super.registerData();
		dataManager.register(BURROW_POS, Optional.empty());
	}

	@Override
	public void livingTick() {
		if (this.remainingCooldownBeforeLocatingNewBurrow > 0) {
			this.remainingCooldownBeforeLocatingNewBurrow--;
		}
		super.livingTick();
	}

	private boolean canEnterBurrow() {
		// TODO: 06/02/2021 change canEnterBurrow according to data (timeOutOfBurrow or other things)
		return true;
	}

	@Nullable
	private BlockPos getBurrowPos() {
		return this.getDataManager().get(BURROW_POS).orElse(null);
	}

	public void setBurrowPos(@Nullable BlockPos pos) {
		this.dataManager.set(BURROW_POS, Optional.ofNullable(pos));
	}

	private boolean hasBurrow() {
		return this.dataManager.get(BURROW_POS).isPresent();
	}

	private boolean isWithinDistance(BlockPos pos, int distance) {
		return pos.withinDistance(this.getPosition(), distance);
	}

	private boolean isTooFar(BlockPos pos) {
		return !this.isWithinDistance(pos, 32);
	}

	private void startMovingTo(BlockPos pos) {
		Vector3d vector3d = Vector3d.copyCenteredHorizontally(pos);
		int i = 0;
		BlockPos blockpos = this.getPosition();
		int j = (int) vector3d.y - blockpos.getY();
		if (j > 2) {
			i = 4;
		} else if (j < -2) {
			i = -4;
		}

		int k = 6;
		int l = 8;
		int i1 = blockpos.manhattanDistance(pos);
		if (i1 < 15) {
			k = i1 / 2;
			l = i1 / 2;
		}

		Vector3d vector3d1 = RandomPositionGenerator.func_226344_b_(this, k, l, i, vector3d, (double) ((float) Math.PI / 10F));
		if (vector3d1 != null) {
			this.navigator.setRangeMultiplier(0.5F);
			this.navigator.tryMoveToXYZ(vector3d1.x, vector3d1.y, vector3d1.z, 1.0D);
		}
	}

	private boolean doesBurrowHaveSpace(BlockPos pos) {
		TileEntity tileentity = this.world.getTileEntity(pos);
		if (tileentity instanceof BurrowTileEntity) {
			return !((BurrowTileEntity) tileentity).isFull();
		} else {
			return false;
		}
	}

	public void setInventoryClosed() {
		this.inventoryOpen = false;
	}

	class EnterBurrowGoal extends Goal {
		@Override
		public boolean shouldExecute() {
			if (HopshelEntity.this.hasBurrow() && HopshelEntity.this.canEnterBurrow() && HopshelEntity.this.getBurrowPos().withinDistance(HopshelEntity.this.getPositionVec(), 2.0D)) {
				TileEntity tileentity = HopshelEntity.this.world.getTileEntity(HopshelEntity.this.getBurrowPos());
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
		public boolean shouldContinueExecuting() {
			return false;
		}

		@Override
		public void startExecuting() {
			TileEntity tileentity = HopshelEntity.this.world.getTileEntity(HopshelEntity.this.getBurrowPos());
			if (tileentity instanceof BurrowTileEntity) {
				BurrowTileEntity burrowTileEntity = (BurrowTileEntity) tileentity;
				burrowTileEntity.tryEnterBurrow(HopshelEntity.this, 50);//with random ?
			}
		}
	}

	class FindBurrowGoal extends Goal {
		private int ticks = HopshelEntity.this.world.rand.nextInt(10);
		private List<BlockPos> possibleBurrows = new ArrayList<>();
		@Nullable
		private Path path = null;
		private int counter;

		FindBurrowGoal() {
			this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean shouldExecute() {
			return HopshelEntity.this.hasBurrow() && !HopshelEntity.this.detachHome() && HopshelEntity.this.canEnterBurrow() && !this.isCloseEnough(HopshelEntity.this.getBurrowPos()) && HopshelEntity.this.world.getBlockState(HopshelEntity.this.getBurrowPos()).isIn(ModBlocks.hopshel_burrow.get());
		}

		@Override
		public boolean shouldContinueExecuting() {
			return this.shouldExecute();
		}

		@Override
		public void startExecuting() {
			this.ticks = 0;
			this.counter = 0;
			super.startExecuting();
		}

		public void resetTask() {
			this.ticks = 0;
			this.counter = 0;
			HopshelEntity.this.navigator.clearPath();
			HopshelEntity.this.navigator.resetRangeMultiplier();
		}

		public void tick() {
			if (HopshelEntity.this.hasBurrow()) {
				++this.ticks;
				if (this.ticks > 600) {
					this.makeChosenBurrowPossibleBurrow();
				} else if (!HopshelEntity.this.navigator.hasPath()) {
					if (!HopshelEntity.this.isWithinDistance(HopshelEntity.this.getBurrowPos(), 16)) {
						if (HopshelEntity.this.isTooFar(HopshelEntity.this.getBurrowPos())) {
							this.reset();
						} else {
							HopshelEntity.this.startMovingTo(HopshelEntity.this.getBurrowPos());
						}
					} else {
						if (!this.startMovingToFar(HopshelEntity.this.getBurrowPos())) {
							this.makeChosenBurrowPossibleBurrow();
						} else if (this.path != null && HopshelEntity.this.navigator.getPath().isSamePath(this.path)) {
							++this.counter;
							if (this.counter > 60) {
								this.reset();
								this.counter = 0;
							}
						} else {
							this.path = HopshelEntity.this.navigator.getPath();
						}
					}
				}
			}
		}

		private boolean startMovingToFar(BlockPos pos) {
			HopshelEntity.this.navigator.setRangeMultiplier(10.0F);
			HopshelEntity.this.navigator.tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), 1.0D);
			return HopshelEntity.this.navigator.getPath() != null && HopshelEntity.this.navigator.getPath().reachesTarget();
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
				Path path = HopshelEntity.this.navigator.getPath();
				return path != null && path.getTarget().equals(pos) && path.reachesTarget() && path.isFinished();
			}
		}
	}

	class UpdateBurrowGoal extends Goal {
		private UpdateBurrowGoal() {
		}

		@Override
		public boolean shouldExecute() {
			return HopshelEntity.this.remainingCooldownBeforeLocatingNewBurrow == 0 && !HopshelEntity.this.hasBurrow() && HopshelEntity.this.canEnterBurrow();
		}

		@Override
		public boolean shouldContinueExecuting() {
			return false;
		}

		@Override
		public void startExecuting() {
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
			BlockPos blockpos = HopshelEntity.this.getPosition();
			PointOfInterestManager pointofinterestmanager = ((ServerWorld) HopshelEntity.this.world).getPointOfInterestManager();
			ImmutableSet.copyOf(ModBlocks.hopshel_burrow.get().getStateContainer().getValidStates());
			PointOfInterestType poit = new PointOfInterestType("hopshel_burrow", ImmutableSet.copyOf(ModBlocks.hopshel_burrow.get().getStateContainer().getValidStates()), 0, 1);
			Stream<PointOfInterest> stream = pointofinterestmanager.func_219146_b((pointOfInterest) -> pointOfInterest == poit, blockpos, 20, PointOfInterestManager.Status.ANY);
			return stream.map(PointOfInterest::getPos)
					.filter(HopshelEntity.this::doesBurrowHaveSpace)
					.sorted(Comparator.comparingDouble((pos) -> pos.distanceSq(blockpos)))
					.collect(Collectors.toList());
		}
	}

}
