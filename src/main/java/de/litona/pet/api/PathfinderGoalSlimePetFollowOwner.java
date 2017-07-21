package de.litona.pet.api;

import org.bukkit.util.Vector;

public class PathfinderGoalSlimePetFollowOwner extends PathfinderGoalPetFollowOwner {

	public PathfinderGoalSlimePetFollowOwner(Pet pet, float speed) {
		super(pet, speed);
	}

	@Override
	public boolean a() {
		if(!(pet.isRidden() || pet.owner.isFlying())) {
			double distance = pet.entity.getLocation().distanceSquared(pet.owner.getLocation());
			if(distance > 20)
				pet.entity.teleport(pet.owner);
			else if(distance > 4) {
				c();
				return true;
			}
		}
		return false;
	}

	@Override
	public void c() {
		Vector vel = pet.entity.getLocation().toVector().subtract(pet.owner.getLocation().toVector());
		if(pet.nmsEntity.onGround)
			vel.setY(-0.8);
		pet.entity.setVelocity(vel.normalize().multiply(-0.4));
	}
}