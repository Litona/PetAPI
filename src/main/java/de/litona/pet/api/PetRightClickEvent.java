package de.litona.pet.api;

import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PetRightClickEvent extends PlayerEvent {

	private static final HandlerList handlers = new HandlerList();

	private final Pet clicked;

	PetRightClickEvent(Pet clicked) {
		super(clicked.owner);
		this.clicked = clicked;
	}

	/**
	 * Returns the clicked Pet
	 * @return pet
	 */
	public Pet getClicked() {
		return clicked;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
