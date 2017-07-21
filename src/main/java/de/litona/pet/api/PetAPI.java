package de.litona.pet.api;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class PetAPI extends JavaPlugin {

	private static boolean debugMode = true;

	@Override
	public void onEnable() {
		// register PetActionsListener
		Bukkit.getPluginManager().registerEvents(new Pet.PetActionsListener(), this);
		//noinspection unused
		Bukkit.getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
				String[] command = event.getMessage().replaceAll("/", "").split(" ");
				if(command[0].equalsIgnoreCase("petdebug"))
					event.getPlayer().sendMessage("Pet debug mode set to " + toggleDebugMode());
				if(isDebugMode() && command[0].equalsIgnoreCase("pet")) {
					Player p = event.getPlayer();
					if(command.length == 2)
						if(command[1].equalsIgnoreCase("remove")) {
							Pet pet = Pet.getPetByPlayer(p);
							if(pet != null)
								pet.remove();
						} else if(command[1].equalsIgnoreCase("list"))
							for(Pet.Type type : Pet.Type.values())
								p.sendMessage(type.toString());
						else if(command[1].equalsIgnoreCase("keys"))
							for(String pd : PetData.getPetDataKeys())
								p.sendMessage(pd);
						else
							try {
								Pet pet = Pet.getPetByPlayer(p);
								if(pet != null)
									pet.remove();
								new Pet(p, Pet.Type.valueOf(command[1].toUpperCase()));
							} catch(Exception e) {
								p.sendMessage("err.. try again!");
								e.printStackTrace();
							}
					else if(command.length > 2) {
						if(command[1].equalsIgnoreCase("getdata"))
							try {
								for(PetData pd : PetData
									.getPetDataApplicableFor((Class<? extends LivingEntity>) Class.forName("org.bukkit.entity." + command[2])))
									p.sendMessage(pd.toString());
							} catch(ClassNotFoundException e) {
								p.sendMessage("err.. try again!");
								e.printStackTrace();
							}
						else if(command[1].equalsIgnoreCase("key"))
							for(PetData pd : PetData.getPetDataForKey(command[2]))
								p.spigot().sendMessage(
									new ComponentBuilder(pd.toString()).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pet d " + pd.toString()))
										.create());
						else if(command[1].equalsIgnoreCase("d")) {
							Pet pet = Pet.getPetByPlayer(p);
							if(pet != null)
								pet.addPetData(PetData.valueOf(command[2]));
						}
					}
					event.setCancelled(true);
				}
			}
		}, this);
	}

	public static boolean isDebugMode() {
		return debugMode;
	}

	private static boolean toggleDebugMode() {
		return (debugMode = !debugMode);
	}
}