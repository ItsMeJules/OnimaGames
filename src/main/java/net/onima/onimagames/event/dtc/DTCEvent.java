package net.onima.onimagames.event.dtc;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.onima.onimagames.game.dtc.DTC;

public class DTCEvent extends Event {
	
	private static HandlerList handlers = new HandlerList();
	
	private DTC dtc;
		
	public DTCEvent(DTC dtc) {
		this.dtc = dtc;
	}
	
	public DTC getDTC() {
		return dtc;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

}
