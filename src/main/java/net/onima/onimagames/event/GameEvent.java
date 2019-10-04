package net.onima.onimagames.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.onima.onimagames.game.Game;

public class GameEvent extends Event {
	
	private Game game;
		
	private static HandlerList handlers = new HandlerList();
	
	public GameEvent(Game game) {
		this.game = game;
	}
	
	public Game getGame() {
		return game;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

}
