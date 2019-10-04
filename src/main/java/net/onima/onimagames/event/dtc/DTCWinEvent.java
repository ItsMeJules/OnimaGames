package net.onima.onimagames.event.dtc;

import org.bukkit.event.Cancellable;

import net.onima.onimaapi.players.APIPlayer;
import net.onima.onimagames.game.dtc.DTC;

public class DTCWinEvent extends DTCEvent implements Cancellable {

	private APIPlayer player;
	private boolean cancelled;

	public DTCWinEvent(DTC dtc, APIPlayer player) {
		super(dtc);
		this.player = player;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public APIPlayer getApiPlayer() {
		return player;
	}

}
