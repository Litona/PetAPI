package de.litona.pet.api.type;

import de.litona.pet.api.Pet;
import de.litona.pet.api.WalkingPet;
import net.minecraft.server.v1_8_R3.EntityEndermite;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.World;

public class EndermitePet extends EntityEndermite implements WalkingPet {

	private float rideSpeed;
	private float jumpHeight;

	public EndermitePet(World world) {
		super(world);
	}

	@Override
	public void g(float sideMot, float forMot) {
		if(passenger != null && passenger instanceof EntityHuman) { // check if player is riding the entity
			EntityHuman pass = (EntityHuman) passenger;
			lastYaw = yaw = pass.yaw;
			pitch = pass.pitch * 0.5F;
			setYawPitch(yaw, pitch); // Setting HeadRotation
			aI = aG = yaw;
			S = 1.0F; // Step height = 1
			forMot = pass.ba;
			k(0.2f * rideSpeed); // Applying speed
			super.g(pass.aZ * 0.375F, forMot <= 0.0F ? forMot * 0.25F : forMot); // Applying motion
			try {
				if(onGround && (boolean) Pet.getPrivateField("aY", EntityLiving.class, pass)) // check if entity is onGround and player is jumping
					motY = 0.5 * jumpHeight; // Applying jumpHeightFactor (default jump = motionY 0.5)
			} catch(NoSuchFieldException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			super.g(sideMot, forMot);
			S = 0.5F; // Step height = 0.5
		}
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