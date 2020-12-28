package com.nbrichau.hopshel.entity;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
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
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

public class HopshelEntity extends AnimalEntity {
	private static DataParameter<Boolean> SHIELDING = EntityDataManager.createKey(HopshelEntity.class, DataSerializers.BOOLEAN);

	public HopshelEntity(EntityType<? extends AnimalEntity> type, World worldIn) {
		super(type, worldIn);
	}

	@Nullable
	@Override
	public AgeableEntity func_241840_a(ServerWorld p_241840_1_, AgeableEntity p_241840_2_) {
		return null;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(1, new SwimGoal(this));
		this.goalSelector.addGoal(6, new WaterAvoidingRandomWalkingGoal(this, 0.7D));
		this.goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 6.0F));
		this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
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

	@Override
	public ActionResultType func_230254_b_(PlayerEntity playerEntity, Hand handIn) {
		ItemStack itemStack = playerEntity.getHeldItem(handIn);
		if (itemStack.getItem() == Items.STICK) {
			if (!world.isRemote()) {
//				System.out.println("before shield : " + isShielding());
				changeShielding();
//				System.out.println("current shield : " + isShielding());
			}
			return ActionResultType.SUCCESS;
		}
		return super.func_230254_b_(playerEntity, handIn);
	}

	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (!world.isRemote()) {
//			System.out.println("attacked shielding : " + isShielding());
			changeShielding();
//			System.out.println("attacked shielding after: " + isShielding());
		}
		return super.attackEntityFrom(source, amount);
	}

	@Override
	protected void registerData() {
		super.registerData();
		this.dataManager.register(SHIELDING, false);
	}
}
