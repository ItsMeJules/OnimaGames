package net.onima.onimagames.event.capable;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.onima.onimaapi.zone.type.utils.Capable;

public class CapableEvent extends Event {
	
	private Capable capable;
		
	private static HandlerList handlers = new HandlerList();
	
	public CapableEvent(Capable capable) {
		this.capable = capable;
	}
	
	public Capable getCapable() {
		return capable;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
}
