package de.litona.pet.api.type;

import de.litona.pet.api.FlyingPet;
import de.litona.pet.api.Pet;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntityWither;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class WitherPet extends EntityWither implements FlyingPet {

	private float rideSpeed;
	private float jumpHeight;
	private float flightAcceleration;
	private float fallAcceleration;

	public WitherPet(World world) {
		super(world);
	}

	@Override
	public void g(float sideMot, float forMot) {
		if(passenger != null && passenger instanceof EntityHuman) { // check if player is riding the entity
			EntityHuman pass = (EntityHuman) passenger;
			Player player = (Player) pass.getBukkitEntity();
			lastYaw = yaw = pass.yaw;
			pitch = pass.pitch * 0.5F;
			setYawPitch(yaw, pitch); // Setting HeadRotation
			aI = aG = yaw;
			S = 1.0F; // Step height = 1
			forMot = pass.ba;
			k(0.2f * rideSpeed); // Applying speed
			super.g(pass.aZ * 0.375F, forMot <= 0.0F ? forMot * 0.25F : forMot); // Applying motion
			if(!onGround && forMot > 0.5) { // check if entity is flying and player is moving forward
				Vector dir = player.getLocation().getDirection().normalize(); // Apply additional motion in players direction (falling overrides motion set in super.g(..))
				motX = dir.getX() * flightAcceleration;
				motZ = dir.getZ() * flightAcceleration;
			}
			try {
				if((boolean) Pet.getPrivateField("aY", EntityLiving.class, pass)) { // check if player is jumping
					motY = 0.9 * jumpHeight; // Applying jumpHeight = riseAcceleration (default rise = motionY 0.9)
					if(player.isFlying())
						player.setFlying(false);
				}
			} catch(NoSuchFieldException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			super.g(sideMot, forMot);
			S = 1F; // Step height = 1
		}
	}

	@Override
	public void E() {
		this.motY *= 0.5D * fallAcceleration; // Applying fall acceleration (default = motionY *= 0.5)
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

	@Override
	public void setFlightAcceleration(float val) {
		this.flightAcceleration = val;
	}

	@Override
	public void setFallAcceleration(float val) {
		this.fallAcceleration = val;
	}

	@Override
	public float getFlightAcceleration() {
		return this.flightAcceleration;
	}

	@Override
	public float getFallAcceleration() {
		return this.fallAcceleration;
	}
}