package de.litona.pet.api.type;

import de.litona.pet.api.Pet;
import de.litona.pet.api.WalkingPet;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.SpigotTimings;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.List;

public class SquidPet extends EntitySquid implements WalkingPet {

	private float rideSpeed;
	private float jumpHeight;

	public SquidPet(World world) {
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
			gSUPER(pass.aZ * 0.375F, forMot <= 0.0F ? forMot * 0.25F : forMot); // Applying motion
			if(inWater && forMot > 0.5) { // check if entity is flying and player is moving forward
				Vector dir = pass.getBukkitEntity().getLocation().getDirection().normalize(); // Apply additional motion in players direction (falling overrides motion set in super.g(..))
				motX = dir.getX();
				motZ = dir.getZ();
			}
			try {
				if(onGround && (boolean) Pet.getPrivateField("aY", EntityLiving.class, pass)) // check if entity is onGround and player is jumping
					motY = 0.5 * jumpHeight; // Applying jumpHeightFactor (default jump = motionY 0.5)
			} catch(NoSuchFieldException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			gSUPER(sideMot, forMot);
			S = 0.5F; // Step height = 0.5
		}
	}

	@Override
	public void m() {
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
		if(this.inWater)
			this.motY = 0;
	}

	public void gSUPER(float f, float f1) { // super.g from EntityLiving as EntitySquid overrides this method
		double d0;
		float f2;
		if(this.bM()) {
			float f3;
			float f4;
			if(this.V()) {
				d0 = this.locY;
				f3 = 0.8F;
				f4 = 0.02F;
				f2 = (float) EnchantmentManager.b(this);
				if(f2 > 3.0F)
					f2 = 3.0F;
				if(!this.onGround)
					f2 *= 0.5F;
				if(f2 > 0.0F) {
					f3 += (0.54600006F - f3) * f2 / 3.0F;
					f4 += (this.bI() * 1.0F - f4) * f2 / 3.0F;
				}
				this.a(f, f1, f4);
				this.move(this.motX, this.motY, this.motZ);
				this.motX *= (double) f3;
				this.motY *= 0.800000011920929D;
				this.motZ *= (double) f3;
				this.motY -= 0.02D;
				if(this.positionChanged && this.c(this.motX, this.motY + 0.6000000238418579D - this.locY + d0, this.motZ))
					this.motY = 0.30000001192092896D;
			} else if(this.ab()) {
				d0 = this.locY;
				this.a(f, f1, 0.02F);
				this.move(this.motX, this.motY, this.motZ);
				this.motX *= 0.5D;
				this.motY *= 0.5D;
				this.motZ *= 0.5D;
				this.motY -= 0.02D;
				if(this.positionChanged && this.c(this.motX, this.motY + 0.6000000238418579D - this.locY + d0, this.motZ))
					this.motY = 0.30000001192092896D;
			} else {
				float f5 = 0.91F;
				if(this.onGround)
					f5 =
						this.world.getType(
							new BlockPosition(MathHelper.floor(this.locX), MathHelper.floor(this.getBoundingBox().b) - 1, MathHelper.floor(this.locZ)))
							.getBlock().frictionFactor * 0.91F;
				float f6 = 0.16277136F / (f5 * f5 * f5);
				f3 = this.onGround ? this.bI() * f6 : this.aM;
				this.a(f, f1, f3);
				f5 = 0.91F;
				if(this.onGround)
					f5 =
						this.world.getType(
							new BlockPosition(MathHelper.floor(this.locX), MathHelper.floor(this.getBoundingBox().b) - 1, MathHelper.floor(this.locZ)))
							.getBlock().frictionFactor * 0.91F;
				if(this.k_()) {
					f4 = 0.15F;
					this.motX = MathHelper.a(this.motX, (double) (-f4), (double) f4);
					this.motZ = MathHelper.a(this.motZ, (double) (-f4), (double) f4);
					this.fallDistance = 0.0F;
					if(this.motY < -0.15D)
						this.motY = -0.15D;
				}
				this.move(this.motX, this.motY, this.motZ);
				if(this.positionChanged && this.k_())
					this.motY = 0.2D;
				if(!this.world.isClientSide || this.world.isLoaded(new BlockPosition((int) this.locX, 0, (int) this.locZ)) && this.world
					.getChunkAtWorldCoords(new BlockPosition((int) this.locX, 0, (int) this.locZ)).o())
					this.motY -= 0.08D;
				else if(this.locY > 0.0D)
					this.motY = -0.1D;
				else
					this.motY = 0.0D;
				this.motY *= 0.9800000190734863D;
				this.motX *= (double) f5;
				this.motZ *= (double) f5;
			}
		}
		this.aA = this.aB;
		d0 = this.locX - this.lastX;
		double d1 = this.locZ - this.lastZ;
		f2 = MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0F;
		if(f2 > 1.0F)
			f2 = 1.0F;
		this.aB += (f2 - this.aB) * 0.4F;
		this.aC += this.aB;
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