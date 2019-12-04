package net.onima.onimagames.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.onima.onimaapi.event.region.PlayerRegionChangeEvent;
import net.onima.onimaapi.players.APIPlayer;
import net.onima.onimaapi.utils.ConfigurationService;
import net.onima.onimaapi.utils.Methods;
import net.onima.onimaapi.zone.Cuboid;
import net.onima.onimaapi.zone.type.utils.Capable;
import net.onima.onimafaction.faction.PlayerFaction;
import net.onima.onimafaction.players.FPlayer;
import net.onima.onimagames.event.capable.CapableCapEvent;
import net.onima.onimagames.event.capable.CapableKnockEvent;
import net.onima.onimagames.game.Game;
import net.onima.onimagames.game.conquest.Conquest;
import net.onima.onimagames.game.dragon.Dragon;
import net.onima.onimagames.game.dtc.DTC;

public class CapableListener implements Listener { //TODO A améliorer
	
	@EventHandler
	public void onAreaEnter(PlayerRegionChangeEvent event) {
		Game game = Game.getStartedGame();
		
		if (game != null && !(game instanceof DTC) && !(game instanceof Dragon)) {
			Capable[] capableGames = null;
			
			if (game instanceof Conquest) {
				capableGames = new Capable[5];
				Conquest conquest = (Conquest) game;
				
				for (int i = 0; i < 5; i++)
					capableGames[i] = conquest.getZones()[i];
					
			} else if (game instanceof Capable) {
				capableGames = new Capable[1];
				capableGames[0] = (Capable) game;
			}
			
			APIPlayer apiPlayer = event.getAPIPlayer();
			
			for (Capable capable : capableGames) {
				Cuboid cuboid = capable.getCapZone().toCuboid();
				boolean isInside = cuboid.contains(event.getToLocation());
				
				if (isInside && !capable.tryCapping(apiPlayer)) continue;
				
				if (isInside && !capable.isCapped()) {
					CapableCapEvent capableEvent = new CapableCapEvent(capable, apiPlayer);
					Bukkit.getPluginManager().callEvent(capableEvent);
					if (capableEvent.isCancelled()) continue;

					capable.onCap(apiPlayer);
				}
				
				if (!isInside && capable.isCapped() && capable.getCapper().getUUID().equals(apiPlayer.getUUID())) {
					CapableKnockEvent knockEvent = null;
					
					if (Methods.hasGotLastDamageByPlayer(apiPlayer.toPlayer()))
						knockEvent = new CapableKnockEvent(capable, APIPlayer.getPlayer((Player) ((EntityDamageByEntityEvent) apiPlayer.toPlayer().getLastDamageCause()).getDamager()), apiPlayer);
					else
						knockEvent = new CapableKnockEvent(capable, null, apiPlayer);
						
					capable.onKnock(apiPlayer, knockEvent.getKnocker());
					Bukkit.getPluginManager().callEvent(knockEvent);
				}
			}
		}
	}
	
	@EventHandler
	public void onConquestDeath(PlayerDeathEvent event) {
		Game game = Game.getStartedGame();
		
		if (game instanceof Conquest) {
			Player player = event.getEntity();
			FPlayer fPlayer = FPlayer.getPlayer(player);
			PlayerFaction faction = fPlayer.getFaction();
			
			if (faction == null) return;
			
			Conquest conquest = (Conquest) game;
			String factionName = faction.getName();
			
			if (conquest.getPoints(factionName) > 0)
				Bukkit.broadcastMessage("§e" + factionName + " §7a perdu §e" + ConfigurationService.CONQUEST_POINTS_DEATH + " §7points parce que §e" + fPlayer.getApiPlayer().getName() + " §7est mort. §7Ils ont maintenant " + conquest.removePoints(factionName, ConfigurationService.CONQUEST_POINTS_DEATH) + '.');
		}
	}

}
