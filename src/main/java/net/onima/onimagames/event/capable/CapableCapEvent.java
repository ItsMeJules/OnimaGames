package net.onima.onimagames.event.capable;

import org.bukkit.event.Cancellable;

import net.onima.onimaapi.players.APIPlayer;
import net.onima.onimaapi.zone.type.utils.Capable;

public class CapableCapEvent extends CapableEvent implements Cancellable {
	
	private APIPlayer capper;
	private boolean cancelled;

	public CapableCapEvent(Capable capable, APIPlayer capper) {
		super(capable);
		this.capper = capper;
	}
	
	public APIPlayer getCapper() {
		return capper;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
}
