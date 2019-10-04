package net.onima.onimagames.event.dtc;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import net.onima.onimagames.game.dtc.DTC;

public class DTCBreakEvent extends DTCEvent implements Cancellable {

	private Player player;
	private boolean cancelled;

	public DTCBreakEvent(DTC dtc, Player player) {
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

	public Player getPlayer() {
		return player;
	}

}
