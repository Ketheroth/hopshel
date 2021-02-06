package com.nbrichau.hopshel.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Optional;

public class HopshelEntity extends AnimalEntity {
	private static final DataParameter<Boolean> SHIELDING = EntityDataManager.createKey(HopshelEntity.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Optional<BlockPos>> BURROW_POS = EntityDataManager.createKey(HopshelEntity.class, DataSerializers.OPTIONAL_BLOCK_POS);

	public HopshelEntity(EntityType<? extends AnimalEntity> type, World worldIn) {
		super(type, worldIn);
	}

	@Override
	public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
		return super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
	}

	@Override
	public void onRemovedFromWorld() {
		super.onRemovedFromWorld();
	}

	public void setBurrowPos(@Nullable BlockPos pos) {
		this.dataManager.set(BURROW_POS, Optional.ofNullable(pos));
	}

	@Nullable
	@Override
	public AgeableEntity func_241840_a(ServerWorld p_241840_1_, AgeableEntity p_241840_2_) {
		return null;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		goalSelector.addGoal(1, new SwimGoal(this));
		goalSelector.addGoal(6, new WaterAvoidingRandomWalkingGoal(this, 0.7D));
		goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 6.0F));
		goalSelector.addGoal(8, new LookRandomlyGoal(this));
	}

	public static AttributeModifierMap.MutableAttribute registerAttributes() {
		return MobEntity.func_233666_p_().createMutableAttribute(Attributes.MAX_HEALTH, 24.0D).createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.3F);
	}

	public boolean isShielding() {
		return dataManager.get(SHIELDING);
	}

	public void changeShielding() {
		boolean shielding = !dataManager.get(SHIELDING);
		dataManager.set(SHIELDING, shielding);
	}

	//onRightCLick
	@Override
	public ActionResultType func_230254_b_(PlayerEntity playerEntity, Hand handIn) {
		ItemStack itemStack = playerEntity.getHeldItem(handIn);
		if (itemStack.getItem() == Items.STICK) {
			if (!world.isRemote()) {
				this.changeShielding();
			}
			return ActionResultType.SUCCESS;
		}
		return super.func_230254_b_(playerEntity, handIn);
	}

	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (!world.isRemote()) {
			this.changeShielding();
		}
		return super.attackEntityFrom(source, amount);
	}

	@Override
	protected void registerData() {
		super.registerData();
		dataManager.register(SHIELDING, false);
		dataManager.register(BURROW_POS, Optional.empty());
	}
}
