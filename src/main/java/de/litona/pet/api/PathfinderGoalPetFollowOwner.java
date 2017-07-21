package de.litona.pet.api;

import net.minecraft.server.v1_8_R3.PathfinderGoal;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

public class PathfinderGoalPetFollowOwner extends PathfinderGoal {

	protected final Pet pet;
	protected final float speed;

	public PathfinderGoalPetFollowOwner(Pet pet, float speed) {
		this.pet = pet;
		this.speed = speed;
	}

	@Override
	public boolean a() {
		if(!pet.isRidden())
			if(pet.entity.getLocation().distanceSquared(pet.owner.getLocation()) > 324) {
				if(!pet.owner.isFlying())
					pet.entity.teleport(pet.owner);
			} else {
				c();
				return true;
			}
		return false;
	}

	@Override
	public void c() {
		pet.nmsEntity.getNavigation().a(((CraftPlayer) pet.owner).getHandle(), speed);
	}
}
