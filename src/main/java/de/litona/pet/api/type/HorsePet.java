package de.litona.pet.api.type;

import de.litona.pet.api.WalkingPet;
import net.minecraft.server.v1_8_R3.EntityHorse;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.World;

public class HorsePet extends EntityHorse implements WalkingPet {

	private float rideSpeed;
	private float jumpHeight;

	public HorsePet(World world) {
		super(world);
	}

	@Override
	public void g(float sideMot, float forMot) {
		super.g(sideMot, forMot);
		if(passenger != null && passenger instanceof EntityHuman) // check if player is riding the entity
			k(0.2f * rideSpeed); // Applying speed
	}

	@Override
	public void setRideSpeed(float val) {
		this.rideSpeed = val;
	}

	@Override
	public void setJumpHeight(float val) {
		this.jumpHeight = val;
	}

	@Override
	public float getRideSpeed() {
		return this.rideSpeed;
	}

	@Override
	public float getJumpHeight() {
		return this.jumpHeight;
	}
}