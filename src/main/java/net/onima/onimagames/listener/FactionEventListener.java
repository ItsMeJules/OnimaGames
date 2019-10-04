package net.onima.onimagames.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.onima.onimaapi.OnimaAPI;
import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimafaction.events.server_event.FactionEventServerStartEvent;
import net.onima.onimagames.game.Game;

public class FactionEventListener implements Listener {
	
	@EventHandler
	public void onFactionEventStart(FactionEventServerStartEvent event) {
		if (Game.getStartedGame() != null) {
			event.setCancelled(true);
			OnimaAPI.broadcast("§cL'event faction n'a pas pu être lancé car une game est en cours !", OnimaPerm.RAIDABLE_COMMAND);
		}
	}

}
