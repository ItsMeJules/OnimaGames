package net.onima.onimagames.listener;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderDragonPart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import net.onima.onimaapi.utils.Methods;
import net.onima.onimafaction.players.FPlayer;
import net.onima.onimagames.game.Game;
import net.onima.onimagames.game.dragon.Dragon;

public class DragonListener implements Listener {
	
	@EventHandler
	public void onDamageTaken(EntityDamageByEntityEvent event) {
		Game game = Game.getStartedGame();
		
		if (game != null && game instanceof Dragon) {
			Dragon dragon = (Dragon) game;
			Entity damaged = event.getEntity();
			Entity damager = event.getDamager();
			
			if (damaged instanceof Player && damager instanceof EnderDragon) {
				FPlayer fPlayer = FPlayer.getByUuid(damaged.getUniqueId());
				
				dragon.addDamageTakenToPlayer(fPlayer.getApiPlayer().getName(), event.getDamage());
				if (fPlayer.hasFaction())
					dragon.addDamageTakenToFaction(fPlayer.getFaction().getName(), event.getDamage());
				
			} else if (damaged instanceof EnderDragon) {
				Player finalAttacker = Methods.getLastAttacker(event);
				
				if (finalAttacker != null) {
					FPlayer fPlayer = FPlayer.getByUuid(finalAttacker.getUniqueId());
						
					dragon.addDamageDealtToPlayer(fPlayer.getApiPlayer().getName(), event.getDamage());
					if (fPlayer.hasFaction())
						dragon.addDamageDealtToFaction(fPlayer.getFaction().getName(), event.getDamage());
				}
				
			}
		}
	}
	
	@EventHandler
	public void onArrowHit(ProjectileHitEvent event) {
		Game game = Game.getStartedGame();
		
		if (game != null && game instanceof Dragon) {
			Dragon dragon = (Dragon) game;
			Projectile projectile = event.getEntity();
			
			if (projectile instanceof Arrow && projectile.getLocation().getWorld().equals(dragon.getLocation().getWorld())) {
				Entity shot = event.getHitEntity();
				
				dragon.addArrowShot();
					
				if (shot == null || (shot != null && !(shot instanceof EnderDragonPart)))
					dragon.addArrowMissed();
			}
		}
		
	}
	
	@EventHandler
	public void onDragonDeath(EntityDeathEvent event) {
		Game game = Game.getStartedGame();
		
		if (game != null && game instanceof Dragon) {
			Entity entity = event.getEntity();
			
			if (entity instanceof EnderDragon) {
				Dragon dragon = (Dragon) game;
				
				if (entity.equals(dragon.getEntity()))
					dragon.win(null);
			}
		}
		
	}

}
