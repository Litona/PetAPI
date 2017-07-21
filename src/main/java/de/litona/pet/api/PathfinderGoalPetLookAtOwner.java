package de.litona.pet.api;

import net.minecraft.server.v1_8_R3.EntitySnowball;
import net.minecraft.server.v1_8_R3.PathfinderGoal;
import org.bukkit.Location;

public class PathfinderGoalPetLookAtOwner extends PathfinderGoal {

	private final Pet pet;

	public PathfinderGoalPetLookAtOwner(Pet pet) {
		this.pet = pet;
	}

	@Override
	public boolean a() {
		if(!pet.isRidden() && pet.entity.getLocation().distanceSquared(pet.owner.getLocation()) <= 20) {
			c();
			return true;
		}
		return false;
	}

	@Override
	public void c() {
		Location ploc = pet.owner.getEyeLocation();
		pet.nmsEntity.getControllerLook().a(ploc.getX(), ploc.getY(), ploc.getZ(), 10F, 80F);
	}
}
