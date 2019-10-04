package net.onima.onimagames.event;

import org.bukkit.event.Cancellable;

import net.onima.onimagames.game.Game;

public class GameStopEvent extends GameEvent implements Cancellable {
	
	private boolean cancelled;

	public GameStopEvent(Game game) {
		super(game);
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
