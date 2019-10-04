package net.onima.onimagames.listener;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import net.onima.onimaapi.utils.Methods;
import net.onima.onimafaction.faction.PlayerFaction;
import net.onima.onimafaction.players.FPlayer;
import net.onima.onimagames.event.dtc.DTCBreakEvent;
import net.onima.onimagames.game.Game;
import net.onima.onimagames.game.dtc.DTC;

public class DTCListener implements Listener {
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Game game = Game.getStartedGame();
		Player player = event.getPlayer();
		
		if (game != null && game instanceof DTC) {
			Block block = event.getBlock();
			DTC dtc = (DTC) game;
			
			if (Methods.locationEquals(dtc.getLocation(), block.getLocation())) {
				FPlayer fPlayer = FPlayer.getByPlayer(player);
				PlayerFaction faction = fPlayer.getFaction();
				
				event.setCancelled(true);
				
				if (faction == null) {
					player.sendMessage("§cVous avez besoin d'une faction pour détruire le cœur !");
					return;
				}
				
				DTCBreakEvent breakEvent = new DTCBreakEvent(dtc, player);
				Bukkit.getPluginManager().callEvent(breakEvent);
				if (breakEvent.isCancelled()) return;
				
				dtc.setLastBreaker(fPlayer.getApiPlayer());
				dtc.tick();
			}
		}
	}

}
