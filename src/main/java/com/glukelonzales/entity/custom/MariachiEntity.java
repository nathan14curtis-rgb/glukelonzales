package com.glukelonzales.entity.custom;

import com.glukelonzales.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Also doubles as a performer for {@link MariachiRitual}: right-clicking one while holding the
 * vihuela, trumpet, or violin (and it isn't already holding an instrument) equips it, which is
 * how three of these standing together get set up to summon the Taco Boss.
 */
public class MariachiEntity extends AnimalEntity {
	private static final Set<Item> INSTRUMENTS = Set.of(ModItems.VIHUELA, ModItems.TRUMPET, ModItems.VIOLIN);

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

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		return false;
	}

	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		ItemStack held = player.getStackInHand(hand);
		if (this.getMainHandStack().isEmpty() && INSTRUMENTS.contains(held.getItem())) {
			if (!this.getWorld().isClient) {
				this.equipStack(EquipmentSlot.MAINHAND, held.copyWithCount(1));
				if (!player.isCreative()) {
					held.decrement(1);
				}
				this.playSound(SoundEvents.ENTITY_VILLAGER_YES, 1.0F, 1.0F);
			}
			return ActionResult.SUCCESS;
		}
		return super.interactMob(player, hand);
	}
}
