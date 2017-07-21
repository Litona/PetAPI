package de.litona.pet.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.litona.pet.api.type.*;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Stream;

public class Pet implements Serializable {

	private static Field FIELD_GOALS;
	private static Field FIELD_GOAL_SELECTOR;
	private static Field FIELD_FIRE_PROOF;

	static {
		Optional<Field> ofield = Stream.of(PathfinderGoalSelector.class.getFields()).filter(f -> f.getType() == List.class).findFirst();
		if(ofield.isPresent()) {
			FIELD_GOALS = ofield.get();
			FIELD_GOALS.setAccessible(true);
		}
		ofield = Stream.of(EntityInsentient.class.getFields()).filter(f -> f.getType() == PathfinderGoalSelector.class).findFirst();
		if(ofield.isPresent()) {
			FIELD_GOAL_SELECTOR = ofield.get();
			FIELD_GOAL_SELECTOR.setAccessible(true);
		}
		try {
			FIELD_FIRE_PROOF = Entity.class.getDeclaredField("fireProof");
			FIELD_FIRE_PROOF.setAccessible(true);
		} catch(NoSuchFieldException e) {
			e.printStackTrace();
		}
	}

	private static final Map<Player, Pet> pets = new HashMap<>();

	/**
	 * Returns the player's pet
	 *
	 * @param p player
	 * @return player's pet; default null
	 */
	public static Pet getPetByPlayer(Player p) {
		return pets.getOrDefault(p, null);
	}

	/**
	 * pet's owner
	 */
	public final Player owner;
	/**
	 * underlying Bukkit entity
	 */
	public final LivingEntity entity;
	/**
	 * underlying NMS entity
	 */
	public final EntityInsentient nmsEntity;
	/**
	 * pet's type
	 */
	public final Type petType;
	private final Collection<PetData> data = new ArrayList<>();
	private boolean ridden;

	/**
	 * Create a new Pet object
	 * <br>spawns entity at owner's location
	 *
	 * @param owner player who owns the pet
	 * @param type  pet type
	 */
	public Pet(Player owner, Type type) {
		this(owner, type, type.followSpeed, type.rideSpeed, type.jumpHeight);
	}

	/**
	 * Create a new Pet object
	 * <br>spawns entity at owner's location
	 *
	 * @param owner       player who owns the pet
	 * @param type        pet type
	 * @param followSpeed follow speed
	 * @param rideSpeed   ride speed
	 * @param jumpHeight  jump height
	 */
	public Pet(Player owner, Type type, float followSpeed, float rideSpeed, float jumpHeight) {
		this(owner, type, followSpeed, rideSpeed, jumpHeight, type.flightAcceleration, type.fallAcceleration);
	}

	/**
	 * Create a new Pet object
	 * <br>spawns entity at owner's location
	 *
	 * @param owner              player who owns the pet
	 * @param type               pet type
	 * @param followSpeed        follow speed
	 * @param rideSpeed          ride speed
	 * @param jumpHeight         jump height
	 * @param flightAcceleration flight acceleration
	 * @param fallAcceleration   fall acceleration
	 */
	public Pet(Player owner, Type type, float followSpeed, float rideSpeed, float jumpHeight, float flightAcceleration, float fallAcceleration) {
		this.owner = owner;
		this.entity = type.spawn(owner.getLocation(), rideSpeed, jumpHeight, flightAcceleration, fallAcceleration);
		this.nmsEntity = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
		this.petType = type;
		pets.put(owner, this);
		try { // remove all goals
			((List) getPrivateField("b", PathfinderGoalSelector.class, nmsEntity.goalSelector)).clear();
			((List) getPrivateField("c", PathfinderGoalSelector.class, nmsEntity.goalSelector)).clear();
			((List) getPrivateField("b", PathfinderGoalSelector.class, nmsEntity.targetSelector)).clear();
			((List) getPrivateField("c", PathfinderGoalSelector.class, nmsEntity.targetSelector)).clear();
		} catch(NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		if(nmsEntity instanceof EntityGuardian)
			nmsEntity.goalSelector.a(1, new PathfinderGoalGuardianPetFollowOwner(this, followSpeed));
		else {
			nmsEntity.goalSelector.a(0, new PathfinderGoalFloat(nmsEntity)); // apply custom goals
			if(nmsEntity instanceof EntitySquid)
				nmsEntity.goalSelector.a(1, new PathfinderGoalSquidPetFollowOwner(this, followSpeed));
			else if(entity instanceof Slime)
				nmsEntity.goalSelector.a(1, new PathfinderGoalSlimePetFollowOwner(this, followSpeed));
			else
				nmsEntity.goalSelector.a(1, new PathfinderGoalPetFollowOwner(this, followSpeed));
		}
		nmsEntity.goalSelector.a(2, new PathfinderGoalPetLookAtOwner(this));
		NBTTagCompound tag = new NBTTagCompound(); // make Pet silent
		nmsEntity.e(tag);
		tag.setInt("Silent", 1);
		nmsEntity.f(tag);
		try {
			FIELD_FIRE_PROOF.set(nmsEntity, true); // make Pet fireProof
		} catch(IllegalAccessException e) {
			e.printStackTrace();
		}
		entity.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 32767, 127, false, false), true); // unterwater breathing
		entity.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 32767, 127, false, false), true); // invincibility
		entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 32767, 127, false, false), true); // invincibility
		if(entity instanceof Horse) { // Horse settings
			Horse horse = (Horse) entity;
			horse.setOwner(owner);
			horse.setTamed(true);
			horse.setStyle(Horse.Style.values()[(int) (Math.random() * 5)]);
			horse.setColor(Horse.Color.values()[(int) (Math.random() * 7)]);
			horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
		}
	}

	/**
	 * Call to remove the pet
	 * <br>Dismounts owner before removing
	 */
	public void remove() {
		if(ridden)
			owner.eject();
		pets.remove(owner);
		if(entity.isValid())
			entity.remove();
	}

	/**
	 * Called in PetActionsListener
	 * <br>Used to control the ridden boolean field
	 */
	protected void dismounted() {
		ridden = false;
	}

	/**
	 * Makes the player ride the pet
	 * <br>Method call affects ridden boolean
	 */
	public void ride() {
		ridden = entity.setPassenger(owner);
	}

	/**
	 * Returns true if the pet is ridden
	 *
	 * @return boolean ridden
	 */
	public boolean isRidden() {
		return ridden;
	}

	/**
	 * Sets the pet's name
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		if(name != null) {
			entity.setCustomName(name);
			entity.setCustomNameVisible(true);
		} else
			entity.setCustomNameVisible(false);
	}

	/**
	 * Returns the pet's name
	 *
	 * @return String entity.getCustomName()
	 */
	public String getName() {
		return entity.getCustomName();
	}

	/**
	 * Returns true if the entity is able to fly
	 *
	 * @return boolean nmsEntity instanceof FlyingPet
	 */
	public boolean canFly() {
		return nmsEntity instanceof FlyingPet;
	}

	/**
	 * Sets the pet's speed when ridden
	 *
	 * @param val new ride speed
	 */
	public void setRideSpeed(float val) {
		((WalkingPet) nmsEntity).setRideSpeed(val);
	}

	/**
	 * Sets the pet's jump height
	 *
	 * @param val new jump height
	 */
	public void setJumpHeight(float val) {
		((WalkingPet) nmsEntity).setJumpHeight(val);
	}

	/**
	 * Returns the pet's speed when ridden
	 *
	 * @return float ride speed
	 */
	public float getRideSpeed() {
		return ((WalkingPet) nmsEntity).getRideSpeed();
	}

	/**
	 * Returns the pet's jump height
	 *
	 * @return float jump height
	 */
	public float getJumpHeight() {
		return ((WalkingPet) nmsEntity).getJumpHeight();
	}

	/**
	 * Sets the pet's flight acceleration
	 *
	 * @param val new flight acceleration
	 */
	public void setFlightAcceleration(float val) {
		if(canFly())
			((FlyingPet) nmsEntity).setFlightAcceleration(val);
	}

	/**
	 * Sets the pet's fall acceleration
	 *
	 * @param val new fall acceleration
	 */
	public void setFallAcceleration(float val) {
		if(canFly())
			((FlyingPet) nmsEntity).setFallAcceleration(val);
	}

	/**
	 * Returns the pet's flight acceleration
	 *
	 * @return float flight acceleration
	 */
	public float getFlightAcceleration() {
		return canFly() ? ((FlyingPet) nmsEntity).getFlightAcceleration() : 0f;
	}

	/**
	 * Returns the pet's fall acceleration
	 *
	 * @return float fall acceleration
	 */
	public float getFallAcceleration() {
		return canFly() ? ((FlyingPet) nmsEntity).getFallAcceleration() : 0f;
	}

	/**
	 * Activate Pet Data
	 *
	 * @param petData new data
	 * @return boolean no error occured
	 */
	public boolean addPetData(PetData petData) {
		if(!data.contains(petData) && petData.isApplicable(entity.getClass())) {
			data.stream().filter(pd -> pd.dataKey.equals(petData.dataKey)).findAny().ifPresent(data::remove);
			petData.apply.accept(this);
			return data.add(petData);
		}
		return false;
	}

	/**
	 * Returns true if the parameter pet data is already applied
	 *
	 * @param petData the pet data to check for
	 * @return boolean pet data is already applied
	 */
	public boolean hasPetData(PetData petData) {
		return data.contains(petData);
	}

	/**
	 * Serialize this Pet object.
	 * Save name and data
	 *
	 * @return JSON String
	 */
	public String serialize() {
		JsonObject object = new JsonObject();
		object.addProperty("type", petType.toString());
		object.addProperty("name", getName());
		JsonArray data = new JsonArray();
		for(PetData pd : this.data) {
			JsonObject entry = new JsonObject();
			entry.addProperty("data", pd.toString());
			data.add(entry);
		}
		object.add("data", data);
		return object.toString();
	}

	public static Pet deserialize(String json, Player owner) {
		JsonObject object = new JsonParser().parse(json).getAsJsonObject();
		Pet pet = new Pet(owner, Type.valueOf(object.get("type").getAsString()));
		pet.setName(object.get("name").getAsString());
		object.getAsJsonArray("data").forEach(je -> pet.addPetData(PetData.valueOf(je.getAsString())));
		return pet;
	}

	public enum Type {
		BAT(BatPet.class, "Bat", 65, 0.6f, 1, 0.7f, 1, 1),
		BLAZE(BlazePet.class, "Blaze", 61, 1.8f, 1, 1.2f, 1.35f, 1.5f),
		CAVE_SPIDER(CaveSpiderPet.class, "CaveSpider", 59),
		CHICKEN(ChickenPet.class, "Chicken", 93),
		COW(CowPet.class, "Cow", 92),
		CREEPER(CreeperPet.class, "Creeper", 50),
		ENDER_DRAGON(EnderDragonPet.class, "EnderDragon", 63, 0.6f, 1, 0.7f, 1, 1),
		ENDERMAN(EndermanPet.class, "Enderman", 58),
		ENDERMITE(EndermitePet.class, "Endermite", 67),
		GHAST(GhastPet.class, "Ghast", 56, 0.6f, 1, 0.7f, 1, 1),
		GUARDIAN(GuardianPet.class, "Guardian", 68),
		HORSE(HorsePet.class, "Horse", 100),
		IRON_GOLEM(IronGolemPet.class, "IronGolem", 99),
		MAGMA_CUBE(MagmaCubePet.class, "MagmaCube", 62),
		MUSHROOM_COW(MushroomCowPet.class, "MushroomCow", 96),
		NPC(NPCPet.class, "Villager", 120, 1.0f),
		OCELOT(OcelotPet.class, "Ocelot", 98),
		PIG(PigPet.class, "Pig", 90),
		PIG_ZOMBIE(PigZombiePet.class, "PigZombie", 57),
		RABBIT(RabbitPet.class, "Rabbit", 101),
		SHEEP(SheepPet.class, "Sheep", 91),
		SILVERFISH(SilverfishPet.class, "Silverfish", 60),
		SKELETON(SkeletonPet.class, "Skeleton", 51),
		SLIME(SlimePet.class, "Slime", 55),
		SNOWMAN(SnowmanPet.class, "Snowman", 97),
		SPIDER(SpiderPet.class, "Spider", 52),
		SQUID(SquidPet.class, "Squid", 94, 1.8F, 2F, 1F),
		WITCH(WitchPet.class, "Witch", 66),
		WITHER(WitherPet.class, "Wither", 64, 0.6f, 1.4f, 1.3f, 1.5f, 1.5f),
		WOLF(WolfPet.class, "Wolf", 95, 1.5f),
		ZOMBIE(ZombiePet.class, "Zombie", 54);

		private final Class clazz;
		private final float followSpeed;
		private final float rideSpeed;
		private final float jumpHeight;
		private final float flightAcceleration;
		private final float fallAcceleration;

		Type(Class<? extends EntityInsentient> clazz, String name, int id) {
			this(clazz, name, id, 1.8f);
		}

		Type(Class<? extends EntityInsentient> clazz, String name, int id, float followSpeed) {
			this(clazz, name, id, followSpeed, 1, 1);
		}

		Type(Class<? extends EntityInsentient> clazz, String name, int id, float followSpeed, float rideSpeed, float jumpHeight) {
			this(clazz, name, id, followSpeed, rideSpeed, jumpHeight, 0, 0);
		}

		Type(Class<? extends EntityInsentient> clazz, String name, int id, float followSpeed, float rideSpeed, float jumpHeight, float flightAcceleration,
			float fallAcceleration) {
			this.clazz = clazz;
			this.followSpeed = followSpeed;
			this.rideSpeed = rideSpeed;
			this.jumpHeight = jumpHeight;
			this.flightAcceleration = flightAcceleration;
			this.fallAcceleration = fallAcceleration;
			try {
				((Map) getPrivateField("c", net.minecraft.server.v1_8_R3.EntityTypes.class, null)).put(name, clazz);
				((Map) getPrivateField("d", net.minecraft.server.v1_8_R3.EntityTypes.class, null)).put(clazz, name);
				((Map) getPrivateField("f", net.minecraft.server.v1_8_R3.EntityTypes.class, null)).put(clazz, id);
			} catch(IllegalAccessException | NoSuchFieldException e) {
				e.printStackTrace();
			}
		}

		private LivingEntity spawn(Location loc, float rideSpeed, float jumpHeight, float flightAcceleration, float fallAcceleration) {
			try {
				World w = ((CraftWorld) loc.getWorld()).getHandle();
				EntityLiving ecreature = (EntityLiving) clazz.getConstructor(World.class).newInstance(w);
				ecreature.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
				w.addEntity(ecreature);
				if(ecreature instanceof WalkingPet) {
					WalkingPet wpet = (WalkingPet) ecreature;
					wpet.setJumpHeight(jumpHeight);
					wpet.setRideSpeed(rideSpeed);
					if(wpet instanceof FlyingPet) {
						FlyingPet fpet = (FlyingPet) wpet;
						fpet.setFallAcceleration(fallAcceleration);
						fpet.setFlightAcceleration(flightAcceleration);
					}
				}
				return (LivingEntity) ecreature.getBukkitEntity();
			} catch(InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public static final class PetActionsListener implements Listener {
		@EventHandler
		public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
			Pet pet = getPetByPlayer(event.getPlayer());
			if(pet != null && pet.entity == event.getRightClicked()) {
				Bukkit.getPluginManager().callEvent(new PetRightClickEvent(pet));
				// ride pet on right click (testing purposes)
				if(PetAPI.isDebugMode() && !pet.isRidden())
					pet.ride();
			}
		}

		@EventHandler
		public void onEntityDismount(EntityDismountEvent event) {
			if(event.getEntity() instanceof Player) {
				Pet pet = getPetByPlayer((Player) event.getEntity());
				if(pet != null && pet.entity == event.getDismounted())
					pet.dismounted();
			}
		}

		@EventHandler
		public void onPlayerQuit(PlayerQuitEvent event) {
			Pet pet = getPetByPlayer(event.getPlayer());
			if(pet != null)
				pet.remove();
		}

		@EventHandler
		public void onProjectileLaunch(ProjectileLaunchEvent event) {
			if(event.getEntity() instanceof WitherSkull)
				event.setCancelled(true);
		}

		@EventHandler
		public void onInventoryOpen(InventoryOpenEvent event) {
			if(event.getInventory().getType() == InventoryType.MERCHANT)
				event.setCancelled(true);
		}
	}

	public static Object getPrivateField(String fieldName, Class clazz, Object object) throws NoSuchFieldException, IllegalAccessException {
		Field field = clazz.getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(object);
	}
}