package de.litona.pet.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.server.v1_8_R3.EntityWither;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

public enum PetData {

	ADULT((pet) -> {
		if(pet instanceof Ageable) {
			((Ageable) pet.entity).setAdult();
			((Ageable) pet.entity).setAgeLock(true);
		} else
			((Zombie) pet.entity).setBaby(true);
	}, "age", "adult", "Ausgewachsen", Ageable.class, Zombie.class),
	BABY((pet) -> {
		if(pet instanceof Ageable) {
			((Ageable) pet.entity).setBaby();
			((Ageable) pet.entity).setAgeLock(true);
		} else
			((Zombie) pet.entity).setBaby(true);
	}, "age", "baby", "Baby", Ageable.class, Zombie.class),
	CAT_TAMED((pet) -> ((Ocelot) pet.entity).setTamed(true), "tamed", "true", "Zahm", Ocelot.class),
	CAT_WILD((pet) -> {
		((Ocelot) pet.entity).setTamed(false);
		((Ocelot) pet.entity).setCatType(Ocelot.Type.WILD_OCELOT);
	}, "tamed", "false", "Wild", Ocelot.class),
	CAT_TYPE_BLACK((pet) -> ((Ocelot) pet.entity).setCatType(Ocelot.Type.BLACK_CAT), "catType", "black", "Schwarz", Ocelot.class),
	CAT_TYPE_RED((pet) -> ((Ocelot) pet.entity).setCatType(Ocelot.Type.RED_CAT), "catType", "red", "Rot", Ocelot.class),
	CAT_TYPE_SIAMESE((pet) -> ((Ocelot) pet.entity).setCatType(Ocelot.Type.SIAMESE_CAT), "catType", "siamese", "Siamesisch", Ocelot.class),
	COLOR_BLACK((pet) -> setColor(pet.entity, DyeColor.BLACK), "color", "black", "Schwarz", Sheep.class, Wolf.class),
	COLOR_BLUE((pet) -> setColor(pet.entity, DyeColor.BLUE), "color", "blue", "Blau", Sheep.class, Wolf.class),
	COLOR_BROWN((pet) -> setColor(pet.entity, DyeColor.BROWN), "color", "brown", "Braun", Sheep.class, Wolf.class),
	COLOR_CYAN((pet) -> setColor(pet.entity, DyeColor.CYAN), "color", "cyan", "Cyan", Sheep.class, Wolf.class),
	COLOR_GRAY((pet) -> setColor(pet.entity, DyeColor.GRAY), "color", "gray", "Grau", Sheep.class, Wolf.class),
	COLOR_GREEN((pet) -> setColor(pet.entity, DyeColor.GREEN), "color", "green", "Grün", Sheep.class, Wolf.class),
	COLOR_LIGHTBLUE((pet) -> setColor(pet.entity, DyeColor.LIGHT_BLUE), "color", "lightblue", "Hellblau", Sheep.class, Wolf.class),
	COLOR_LIME((pet) -> setColor(pet.entity, DyeColor.LIME), "color", "lime", "Hellgrün", Sheep.class, Wolf.class),
	COLOR_MAGENTA((pet) -> setColor(pet.entity, DyeColor.MAGENTA), "color", "magenta", "Magenta", Sheep.class, Wolf.class),
	COLOR_ORANGE((pet) -> setColor(pet.entity, DyeColor.ORANGE), "color", "orange", "Orange", Sheep.class, Wolf.class),
	COLOR_PINK((pet) -> setColor(pet.entity, DyeColor.PINK), "color", "pink", "Pink", Sheep.class, Wolf.class),
	COLOR_PURPLE((pet) -> setColor(pet.entity, DyeColor.PURPLE), "color", "purple", "Lila", Sheep.class, Wolf.class),
	COLOR_RED((pet) -> setColor(pet.entity, DyeColor.RED), "color", "red", "Rot", Sheep.class, Wolf.class),
	COLOR_SILVER((pet) -> setColor(pet.entity, DyeColor.SILVER), "color", "silver", "Silver", Sheep.class, Wolf.class),
	COLOR_WHITE((pet) -> setColor(pet.entity, DyeColor.WHITE), "color", "white", "Weiß", Sheep.class, Wolf.class),
	COLOR_YELLOW((pet) -> setColor(pet.entity, DyeColor.YELLOW), "color", "yellow", "Gelb", Sheep.class, Wolf.class),
	CREEPER_POWERED((pet) -> ((Creeper) pet.entity).setPowered(true), "powered", "true", "Geladen", Creeper.class),
	CREEPER_NONPOWERED((pet) -> ((Creeper) pet.entity).setPowered(false), "powered", "false", "Normal", Creeper.class),
	GUARDIAN_ELDER((pet) -> ((Guardian) pet.entity).setElder(true), "elder", "true", "Groß", Guardian.class),
	GUARDIAN_YOUNGER((pet) -> ((Guardian) pet.entity).setElder(false), "elder", "false", "Klein", Guardian.class),
	HORSE_ARMOR_NONE((pet) -> ((Horse) pet.entity).getInventory().setArmor(new ItemStack(Material.AIR)), "horseArmor", "none", "Keine Rüstung",
		Horse.class),
	HORSE_ARMOR_GOLD((pet) -> ((Horse) pet.entity).getInventory().setArmor(new ItemStack(Material.GOLD_BARDING)), "horseArmor", "gold", "Goldene Rüstung",
		Horse.class),
	HORSE_ARMOR_IRON((pet) -> ((Horse) pet.entity).getInventory().setArmor(new ItemStack(Material.IRON_BARDING)), "horseArmor", "iron", "Eiserne Rüstung",
		Horse.class),
	HORSE_ARMOR_DIAMOND((pet) -> ((Horse) pet.entity).getInventory().setArmor(new ItemStack(Material.DIAMOND_BARDING)), "horseArmor", "diamond",
		"Diamant Rüstung", Horse.class),
	HORSE_CHEST((pet) -> ((Horse) pet.entity).setCarryingChest(true), "horseChest", "true", "Mit Kiste", Horse.class),
	HORSE_NOCHEST((pet) -> ((Horse) pet.entity).setCarryingChest(false), "horseChest", "false", "Ohne Kiste", Horse.class),
	HORSE_COLOR_BLACK((pet) -> ((Horse) pet.entity).setColor(Horse.Color.BLACK), "horseColor", "black", "Schwarz", Horse.class),
	HORSE_COLOR_BROWN((pet) -> ((Horse) pet.entity).setColor(Horse.Color.BROWN), "horseColor", "brown", "Braun", Horse.class),
	HORSE_COLOR_CHESTNUT((pet) -> ((Horse) pet.entity).setColor(Horse.Color.CHESTNUT), "horseColor", "chestnut", "Kastanienbraun", Horse.class),
	HORSE_COLOR_CREAMY((pet) -> ((Horse) pet.entity).setColor(Horse.Color.CREAMY), "horseColor", "creamy", "Cremefarben", Horse.class),
	HORSE_COLOR_DARKBROWN((pet) -> ((Horse) pet.entity).setColor(Horse.Color.DARK_BROWN), "horseColor", "Dunkelbraun", "Schwarz", Horse.class),
	HORSE_COLOR_GRAY((pet) -> ((Horse) pet.entity).setColor(Horse.Color.GRAY), "horseColor", "gray", "Grau", Horse.class),
	HORSE_COLOR_WHITE((pet) -> ((Horse) pet.entity).setColor(Horse.Color.WHITE), "horseColor", "white", "Weiß", Horse.class),
	HORSE_STYLE_BLACKDOTS((pet) -> ((Horse) pet.entity).setStyle(Horse.Style.BLACK_DOTS), "horseStyle", "blackDots", "Schwarze Punkte", Horse.class),
	HORSE_STYLE_NONE((pet) -> ((Horse) pet.entity).setStyle(Horse.Style.NONE), "horseStyle", "none", "Keine Markierungen", Horse.class),
	HORSE_STYLE_WHITE((pet) -> ((Horse) pet.entity).setStyle(Horse.Style.WHITE), "horseStyle", "white", "Weiß", Horse.class),
	HORSE_STYLE_WHITEDOTS((pet) -> ((Horse) pet.entity).setStyle(Horse.Style.WHITE_DOTS), "horseStyle", "whiteDots", "Weiße Punkte", Horse.class),
	HORSE_STYLE_WHITEFIELD((pet) -> ((Horse) pet.entity).setStyle(Horse.Style.WHITEFIELD), "horseStyle", "whitefield", "Weiße Flecken", Horse.class),
	HORSE_VARIANT_NORMAL((pet) -> ((Horse) pet.entity).setVariant(Horse.Variant.HORSE), "horseVariant", "normal", "Pferd", Horse.class),
	HORSE_VARIANT_DONKEY((pet) -> ((Horse) pet.entity).setVariant(Horse.Variant.DONKEY), "horseVariant", "donkey", "Esel", Horse.class),
	HORSE_VARIANT_MULE((pet) -> ((Horse) pet.entity).setVariant(Horse.Variant.MULE), "horseVariant", "mule", "Maultier", Horse.class),
	HORSE_VARIANT_SKELETON((pet) -> ((Horse) pet.entity).setVariant(Horse.Variant.SKELETON_HORSE), "horseVariant", "skeleton", "Skelett Pferd",
		Horse.class),
	HORSE_VARIANT_ZOMBIE((pet) -> ((Horse) pet.entity).setVariant(Horse.Variant.UNDEAD_HORSE), "horseVariant", "zombie", "Zombie Pferd", Horse.class),
	NPC_PROFESSION_BLACKSMITH((pet) -> ((Villager) pet.entity).setProfession(Villager.Profession.BLACKSMITH), "npcProfession", "blacksmith", "Schmied",
		Villager.class),
	NPC_PROFESSION_BUTCHER((pet) -> ((Villager) pet.entity).setProfession(Villager.Profession.BUTCHER), "npcProfession", "butcher", "Fleischer",
		Villager.class),
	NPC_PROFESSION_FARMER((pet) -> ((Villager) pet.entity).setProfession(Villager.Profession.FARMER), "npcProfession", "farmer", "Bauer", Villager.class),
	NPC_PROFESSION_LIBRARIAN((pet) -> ((Villager) pet.entity).setProfession(Villager.Profession.LIBRARIAN), "npcProfession", "librarian", "Bibliothekar",
		Villager.class),
	NPC_PROFESSION_PRIEST((pet) -> ((Villager) pet.entity).setProfession(Villager.Profession.PRIEST), "npcProfession", "priest", "Priester",
		Villager.class),
	SADDLE((pet) -> ((Pig) pet.entity).setSaddle(true), "saddle", "true", "Sattel", Pig.class),
	NOSADDLE((pet) -> ((Pig) pet.entity).setSaddle(false), "saddle", "false", "Kein Sattel", Pig.class),
	SHEEP_SHEARED((pet) -> ((Sheep) pet.entity).setSheared(true), "sheepSheared", "true", "Gescheert", Sheep.class),
	SHEEP_NONSHEARED((pet) -> ((Sheep) pet.entity).setSheared(false), "sheepSheared", "false", "Nicht Gescheert", Sheep.class),
	SLIME_SIZE_SMALL((pet) -> ((Slime) pet.entity).setSize(1), "slimeSize", "small", "Klein", Slime.class),
	SLIME_SIZE_MEDIUM((pet) -> ((Slime) pet.entity).setSize(2), "slimeSize", "medium", "Mittel", Slime.class),
	SLIME_SIZE_LARGE((pet) -> ((Slime) pet.entity).setSize(3), "slimeSize", "large", "Groß", Slime.class),
	RABBIT_TYPE_BLACK((pet) -> ((Rabbit) pet.entity).setRabbitType(Rabbit.Type.BLACK), "rabbitType", "black", "Schwarz", Rabbit.class),
	RABBIT_TYPE_BLACKANDWHITE((pet) -> ((Rabbit) pet.entity).setRabbitType(Rabbit.Type.BLACK_AND_WHITE), "rabbitType", "blackAndWhite", "Schwarz/Weiß",
		Rabbit.class),
	RABBIT_TYPE_BROWN((pet) -> ((Rabbit) pet.entity).setRabbitType(Rabbit.Type.BROWN), "rabbitType", "brown", "Braun", Rabbit.class),
	RABBIT_TYPE_GOLD((pet) -> ((Rabbit) pet.entity).setRabbitType(Rabbit.Type.GOLD), "rabbitType", "gold", "Gold", Rabbit.class),
	RABBIT_TYPE_SALTANDPEPPER((pet) -> ((Rabbit) pet.entity).setRabbitType(Rabbit.Type.SALT_AND_PEPPER), "rabbitType", "saltAndPepper", "Salz und Pfeffer",
		Rabbit.class),
	RABBIT_TYPE_KILLERBUNNY((pet) -> ((Rabbit) pet.entity).setRabbitType(Rabbit.Type.THE_KILLER_BUNNY), "rabbitType", "killerBunny", "Der Killerhase",
		Rabbit.class),
	RABBIT_TYPE_WHITE((pet) -> ((Rabbit) pet.entity).setRabbitType(Rabbit.Type.WHITE), "rabbitType", "white", "Weiß", Rabbit.class),
	WITHER_SMALL((pet) -> {
		if(!contains(pet))
			put(pet, Bukkit.getScheduler().runTaskTimer(PetAPI.getPlugin(PetAPI.class), () -> ((EntityWither) pet.nmsEntity).r(400), 5, 1));
	}, "witherSize", "small", "Klein", Wither.class),
	WITHER_NORMAL((pet) -> {
		cancel(pet);
		((EntityWither) pet.nmsEntity).r(0);
	}, "witherSize", "normal", "Normal", Wither.class);

	/**
	 * String PetData key
	 * <br>Some PetData overrides other, use the keys to find competing PetData
	 */
	public final String dataKey;
	/**
	 * (formal) String PetData value
	 * <br>Some PetData overrides other, use the keys to find competing PetData
	 * <br>While other PetData may have the same key, this value is unique for its key
	 */
	public final String dataValue;
	/**
	 * German display String
	 */
	public final String displayName;

	final Consumer<Pet> apply;
	private final Class<? extends LivingEntity>[] applicable;

	PetData(Consumer<Pet> apply, String dataKey, String dataValue, String displayName, Class<? extends LivingEntity>... applicable) {
		this.apply = apply;
		this.displayName = displayName;
		this.dataKey = dataKey;
		this.dataValue = dataValue;
		this.applicable = applicable;
	}

	private static final Multimap<String, PetData> petDataMap = HashMultimap.create(values().length, 5);
	private static final ConcurrentHashMap<Pet, BukkitTask> witherMap = new ConcurrentHashMap<>();

	static {
		for(PetData data : values())
			petDataMap.put(data.dataKey, data);
	}

	/**
	 * Returns true if the PetData may be applied to pets of the parameter class
	 *
	 * @param clazz Bukkit class of pet entity
	 * @return boolean PetData is applicable
	 */
	public boolean isApplicable(Class<? extends LivingEntity> clazz) {
		for(Class<? extends LivingEntity> c : applicable)
			if(c.isAssignableFrom(clazz))
				return true;
		return false;
	}

	/**
	 * Returns an array of all PetData that may be applied for pets of the parameter class
	 *
	 * @param clazz Bukkit class of pet entity
	 * @return PetData[]
	 */
	public static PetData[] getPetDataApplicableFor(Class<? extends LivingEntity> clazz) {
		Collection<PetData> petData = new ArrayList<>();
		for(PetData pd : PetData.values())
			if(pd.isApplicable(clazz))
				petData.add(pd);
		return petData.toArray(new PetData[0]);
	}

	/**
	 * Returns an array of all PetData keys
	 *
	 * @return String[] all PetData string keys
	 */
	public static String[] getPetDataKeys() {
		return petDataMap.keySet().toArray(new String[0]);
	}

	/**
	 * Returns an array of all keys of PetData that may be applied to pets of the parameter class
	 *
	 * @param clazz Bukkit class of pet entity
	 * @return String[] all PetData string keys for provided class
	 */
	public static String[] getPetDataKeysFor(Class<? extends LivingEntity> clazz) {
		return Stream.of(getPetDataApplicableFor(clazz)).map(pd -> pd.dataKey).distinct().toArray(String[]::new);
	}

	/**
	 * Returns an array of all PetData values for a String key
	 *
	 * @param dataKey String PetData key
	 * @return PetData[] all PetData values for the parameter key
	 */
	public static PetData[] getPetDataForKey(String dataKey) {
		return petDataMap.get(dataKey).toArray(new PetData[0]);
	}

	private static void put(Pet pet, BukkitTask task) {
		witherMap.put(pet, task);
	}

	private static void cancel(Pet pet) {
		witherMap.remove(pet).cancel();
	}

	private static boolean contains(Pet pet) {
		return witherMap.containsKey(pet);
	}

	private static void setColor(LivingEntity ent, DyeColor color) {
		if(ent instanceof Sheep)
			((Sheep) ent).setColor(color);
		else {
			((Wolf) ent).setTamed(true);
			((Wolf) ent).setCollarColor(color);
		}
	}
}