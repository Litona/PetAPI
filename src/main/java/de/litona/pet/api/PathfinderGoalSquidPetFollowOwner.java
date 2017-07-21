package de.litona.pet.api;

public class PathfinderGoalSquidPetFollowOwner extends PathfinderGoalPetFollowOwner {

	public PathfinderGoalSquidPetFollowOwner(Pet pet, float speed) {
		super(pet, speed);
	}

	@Override
	public boolean a() {
		pet.entity.setRemainingAir(300);
		if(!(pet.isRidden() || pet.owner.isFlying())) {
			double distance = pet.entity.getLocation().distanceSquared(pet.owner.getLocation());
			if(distance > 100)
				pet.entity.teleport(pet.owner);
			else if(distance > 7) {
				c();
				return true;
			}
		}
		return false;
	}

	@Override
	public void c() {
		pet.entity.setVelocity(pet.entity.getLocation().toVector().subtract(pet.owner.getLocation().toVector()).normalize().multiply(-0.4));
	}
}