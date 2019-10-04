package net.onima.onimagames.event.capable;

import org.bukkit.event.Cancellable;

import net.onima.onimaapi.players.APIPlayer;
import net.onima.onimaapi.zone.type.utils.Capable;

public class CapableKnockEvent extends CapableEvent implements Cancellable {

	private APIPlayer knocked, knocker;
	private boolean cancelled;
	
	public CapableKnockEvent(Capable capable, APIPlayer knocker, APIPlayer knocked) {
		super(capable);
	}
	
	public APIPlayer getKnocked() {
		return knocked;
	}
	
	public APIPlayer getKnocker() {
		return knocker;
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
