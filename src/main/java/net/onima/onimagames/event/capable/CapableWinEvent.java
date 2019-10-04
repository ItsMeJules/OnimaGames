package net.onima.onimagames.event.capable;

import org.bukkit.event.Cancellable;

import net.onima.onimaapi.players.APIPlayer;
import net.onima.onimaapi.zone.type.utils.Capable;

public class CapableWinEvent extends CapableEvent implements Cancellable {

	private APIPlayer winner;
	private boolean cancelled;
	
	public CapableWinEvent(Capable capable, APIPlayer capper) {
		super(capable);
		this.winner = capper;
	}
	
	public APIPlayer getWinner() {
		return winner;
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
