package net.onima.onimagames.game.citadel;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import net.onima.onimaapi.players.APIPlayer;
import net.onima.onimaapi.utils.ConfigurationService;
import net.onima.onimaapi.utils.Methods;
import net.onima.onimaapi.utils.time.Time.LongTime;
import net.onima.onimaapi.zone.Cuboid;
import net.onima.onimaapi.zone.struct.Flag;
import net.onima.onimafaction.cooldowns.PvPTimerCooldown;
import net.onima.onimagames.OnimaGames;
import net.onima.onimagames.event.GameStartEvent;
import net.onima.onimagames.event.GameStopEvent;
import net.onima.onimagames.event.capable.CapableCapEvent;
import net.onima.onimagames.event.capable.CapableKnockEvent;
import net.onima.onimagames.event.capable.CapableWinEvent;
import net.onima.onimagames.game.Game;
import net.onima.onimagames.game.GameType;
import net.onima.onimagames.game.koth.Koth;
import net.onima.onimagames.task.GameTask;

public class Citadel extends Koth {

	private static boolean refreshed;
	
	public Citadel(String name, long capTime, APIPlayer capper, String creator) {
		super(name, capTime, capper, creator, GameType.CITADEL);
	}
	
	public Citadel(String name, long capTime, String creator) {
		this(name, capTime, null, creator);
	}
	
	public Citadel(String name, String creator) {
		this(name, 0, null, creator);
	}
	
	public Citadel(String name) {
		this(name, 0, null, null);
	}
	
	@Override
	public void start() {
		if (!isStarted()) {
			GameStartEvent event = new GameStartEvent(this);		
			
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) return;
			
			Bukkit.broadcastMessage("§7\u2588\u2588\u2588\u2588\u2588\u2588\u2588");
			Bukkit.broadcastMessage("§7\u2588\u2588§5\u2588\u2588\u2588\u2588§7\u2588");
			Bukkit.broadcastMessage("§7\u2588§5\u2588§7\u2588\u2588\u2588\u2588\u2588");
			Bukkit.broadcastMessage("§7\u2588§5\u2588§7\u2588\u2588\u2588\u2588\u2588 §6[Citadel]");
			Bukkit.broadcastMessage("§7\u2588§5\u2588§7\u2588\u2588\u2588\u2588\u2588   §a§l" + name);
			Bukkit.broadcastMessage("§7\u2588§5\u2588§7\u2588\u2588\u2588\u2588\u2588 §7a commencé. §a(" + LongTime.setHMSFormat(capTime) + ')');
			Bukkit.broadcastMessage("§7\u2588\u2588§5\u2588\u2588\u2588\u2588§7\u2588");
			Bukkit.broadcastMessage("§7\u2588\u2588\u2588\u2588\u2588\u2588\u2588");
		
			region.setDeathban(false);
            region.addFlag(Flag.DENY_ENDERPEARL);
            region.addFlag(Flag.PVP_TIMER_DENY_ENTRY);
            
            Cuboid cuboid = region.toCuboid();
            Location loc = cuboid.getWorld().getHighestBlockAt(cuboid.getMinimumLocation().add(-0.5, 0, -0.5)).getLocation();
            
            for (Player player : cuboid.getPlayers()) {
            	if (APIPlayer.getPlayer(player).getTimeLeft(PvPTimerCooldown.class) > 0L)
            		player.teleport(loc);
            }
            
            capZone.setDeathban(false);
            capZone.addFlag(Flag.DENY_ENDERPEARL);
            capZone.addFlag(Flag.PVP_TIMER_DENY_ENTRY);
            
			startedGame = this;	
			capTimeLeft = capTime;
			startedTime = System.currentTimeMillis();
			(gameTask = new GameTask()).runTaskTimerAsynchronously(OnimaGames.getInstance(), 0L, 20L);
			update();
		}
	}
	
	@Override
	public void stop() {
		if (isStarted()) {
			GameStopEvent event = new GameStopEvent(this);		
			
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) return;
			
			Bukkit.broadcastMessage("§7La citadel §e" + name + " §7a été arrêté.");
			
            region.setDeathban(true);
            region.removeFlag(Flag.DENY_ENDERPEARL);
            region.removeFlag(Flag.PVP_TIMER_DENY_ENTRY);
            
            capZone.setDeathban(true);
            capZone.removeFlag(Flag.DENY_ENDERPEARL);
            capZone.removeFlag(Flag.PVP_TIMER_DENY_ENTRY);
            
            if (capper != null)
            	capper.setCapping(null);
			capper = null;
			timeAtCap = -1L;
			capTimeLeft = -1L;
			startedTime = 0;
			gameTask.cancel();
			gameTask = null;
			startedGame = null;
		}
	}
	
	@Override
	public void win(APIPlayer winner) {
		CapableWinEvent event = new CapableWinEvent(this, winner);
		Bukkit.getPluginManager().callEvent(event);
		
		if (event.isCancelled())
			return;
		
		Bukkit.broadcastMessage("");
		Bukkit.broadcastMessage("");
		Bukkit.broadcastMessage("§7La Citadel §e" + name + " §7a été capturé par §e" + winner.getName() + "§7. Cet event a duré §e" + LongTime.setYMDWHMSFormat(System.currentTimeMillis() - startedTime));
		award(winner.toPlayer());

        region.setDeathban(true);
        region.removeFlag(Flag.DENY_ENDERPEARL);
        region.removeFlag(Flag.PVP_TIMER_DENY_ENTRY);
        
        capZone.setDeathban(true);
        capZone.removeFlag(Flag.DENY_ENDERPEARL);
        capZone.removeFlag(Flag.PVP_TIMER_DENY_ENTRY);
        
        capper.setCapping(null);
		capper = null;
		timeAtCap = -1L;
		capTimeLeft = -1L;
		startedTime = 0;
		gameTask.cancel();
		gameTask = null;
		startedGame = null;
	}
	
	@Override
	public void tick() {
		if (!isCapped())
			return;
		
		decreaseTime();
		if (capTimeLeft <= 0L) {
			CapableWinEvent event = new CapableWinEvent(this, capper);
			Bukkit.getPluginManager().callEvent(event);
			
			if (!event.isCancelled())
				win(capper);
		} else if ((capTimeLeft / 1000) % 30 == 0) { 
			Bukkit.broadcastMessage("§eQuelqu'un §7est entrain de contrôler la Citadel §e" + name + ". §c(" + LongTime.setHMSFormat(capTimeLeft) + ')');
			capper.sendMessage("§eVous §7êtes entrain de contrôler la Citadel.");
		}
	}
	
	@Override
	public List<String> getServerListLines() {
		return Methods.replacePlaceholder(ConfigurationService.CITADEL_SERVER_LIST_LINE, "%name%", name, "%time%", LongTime.setHMSFormat(getCapTimeLeft()));
	}
	
	@Override
	public void onCap(APIPlayer capper) {
		CapableCapEvent event = new CapableCapEvent(this, capper);
		Bukkit.getPluginManager().callEvent(event);
		
		if (event.isCancelled())
			return;
		
		Bukkit.broadcastMessage("§7La Citadel §e" + name + " §7est entrain d'être capturé !");
		capper.sendMessage("§eVous §7êtes entrain de capturer la Citadel !");
		capper.setCapping(this);
		
		timeAtCap = System.currentTimeMillis();
		this.capper = capper;
	}
	
	@Override
	public void onKnock(APIPlayer capper, APIPlayer knocker) {
		CapableKnockEvent event = new CapableKnockEvent(this, knocker, capper);
		Bukkit.getPluginManager().callEvent(event);
		
		if (event.isCancelled())
			return;
		
		capper.sendMessage("§eVous §7n'êtes plus entrain de capturer la Citadel !");
		if ((capTime - capTimeLeft) > ConfigurationService.KOTH_KNOCK_ANNOUNCE_DELAY)
			Bukkit.broadcastMessage("§e" + capper.getDisplayName() + " §7a été knock de la Citadel (" + LongTime.setHMSFormatOnlySeconds(capTimeLeft) + ") !");

		capper.setCapping(null);
		
		timeAtCap = -1L;
		capTimeLeft = capTime;
		this.capper = null;
	}
	
	@Override
	public void serialize() {
		if (!refreshed)
			refreshFile();
		
		config.set(path+"cap-time", capTime);
		config.set(path+"creator", creator);
		config.set(path+"created", created);
		
		super.serialize();
	}
	
	@Override
	public void refreshFile() {
		ConfigurationSection section = config.getConfigurationSection("GAMES.CITADEL");
		
		if (section != null) {
			List<String> gamesName = games.stream().filter(game -> game instanceof Citadel).map(Game::getName).collect(Collectors.toList());
			for (String name : section.getKeys(false)) {
				if (!gamesName.contains(name))
					gameSerialConfig.remove("GAMES.CITADEL."+name, false);
			}
		}
		refreshed = true;
	}
	
}
