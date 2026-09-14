package com.glukelonzales.entity.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** Example custom creature. Copy this class as the starting point for new mobs. */
public class MariachiEntity extends AnimalEntity {
	public MariachiEntity(EntityType<? extends AnimalEntity> entityType, World world) {
		super(entityType, world);
	}

	public static DefaultAttributeContainer.Builder createMariachiAttributes() {
		return AnimalEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		this.goalSelector.add(1, new WanderAroundFarGoal(this, 1.0));
		this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
		this.goalSelector.add(3, new LookAroundGoal(this));
	}

	@Nullable
	@Override
	public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
		return null;
	}
}
