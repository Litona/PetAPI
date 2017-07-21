package de.litona.pet.api.type;

import de.litona.pet.api.Pet;
import de.litona.pet.api.WalkingPet;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.SpigotTimings;

import java.util.Iterator;
import java.util.List;

public class GuardianPet extends EntityGuardian implements WalkingPet {

	private float rideSpeed;
	private float jumpHeight;

	public GuardianPet(World world) {
		super(world);
	}

	@Override
	public void g(float sideMot, float forMot) { // Differs from other pets
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
	public void m() {
		if(passenger == null)
			super.m();
		else {
			if(this.bc > 0) {
				double d0 = this.locX + (this.bd - this.locX) / (double) this.bc;
				double d1 = this.locY + (this.be - this.locY) / (double) this.bc;
				double d2 = this.locZ + (this.bf - this.locZ) / (double) this.bc;
				double d3 = MathHelper.g(this.bg - (double) this.yaw);
				this.yaw = (float) ((double) this.yaw + d3 / (double) this.bc);
				this.pitch = (float) ((double) this.pitch + (this.bh - (double) this.pitch) / (double) this.bc);
				--this.bc;
				this.setPosition(d0, d1, d2);
				this.setYawPitch(this.yaw, this.pitch);
			} else if(!this.bM()) {
				this.motX *= 0.98D;
				this.motY *= 0.98D;
				this.motZ *= 0.98D;
			}
			if(Math.abs(this.motX) < 0.005D)
				this.motX = 0.0D;
			if(Math.abs(this.motY) < 0.005D)
				this.motY = 0.0D;
			if(Math.abs(this.motZ) < 0.005D)
				this.motZ = 0.0D;
			this.world.methodProfiler.a("ai");
			SpigotTimings.timerEntityAI.startTiming();
			if(this.bD()) {
				this.aY = false;
				this.aZ = 0.0F;
				this.ba = 0.0F;
				this.bb = 0.0F;
			} else if(this.bM()) {
				this.world.methodProfiler.a("newAi");
				this.doTick();
				this.world.methodProfiler.b();
			}
			SpigotTimings.timerEntityAI.stopTiming();
			this.world.methodProfiler.b();
			this.world.methodProfiler.a("jump");
			if(this.aY) {
				if(this.V())
					this.bG();
				else if(this.ab())
					this.bH();
				else if(this.onGround)
					this.bF();
			}
			this.world.methodProfiler.b();
			this.world.methodProfiler.a("travel");
			this.aZ *= 0.98F;
			this.ba *= 0.98F;
			this.bb *= 0.9F;
			SpigotTimings.timerEntityAIMove.startTiming();
			this.g(this.aZ, this.ba);
			SpigotTimings.timerEntityAIMove.stopTiming();
			this.world.methodProfiler.b();
			this.world.methodProfiler.a("push");
			if(!this.world.isClientSide) {
				SpigotTimings.timerEntityAICollision.startTiming();
				this.bL();
				SpigotTimings.timerEntityAICollision.stopTiming();
			}
			this.world.methodProfiler.b();
			this.world.methodProfiler.a("looting");
			if(!this.world.isClientSide && this.bY() && !this.aP && this.world.getGameRules().getBoolean("mobGriefing")) {
				List list = this.world.a(EntityItem.class, this.getBoundingBox().grow(1.0D, 0.0D, 1.0D));
				Iterator iterator = list.iterator();
				while(iterator.hasNext()) {
					EntityItem entityitem = (EntityItem) iterator.next();
					if(!entityitem.dead && entityitem.getItemStack() != null && !entityitem.s())
						this.a(entityitem);
				}
			}
			this.world.methodProfiler.b();
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