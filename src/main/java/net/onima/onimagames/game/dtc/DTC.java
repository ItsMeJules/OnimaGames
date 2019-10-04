package net.onima.onimagames.game.dtc;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.onima.onimaapi.players.APIPlayer;
import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.CasualFormatDate;
import net.onima.onimaapi.utils.ConfigurationService;
import net.onima.onimaapi.utils.Methods;
import net.onima.onimaapi.utils.time.Time.LongTime;
import net.onima.onimaapi.zone.Cuboid;
import net.onima.onimaapi.zone.struct.Flag;
import net.onima.onimafaction.cooldowns.PvPTimerCooldown;
import net.onima.onimafaction.players.FPlayer;
import net.onima.onimagames.event.GameStartEvent;
import net.onima.onimagames.event.GameStopEvent;
import net.onima.onimagames.event.dtc.DTCWinEvent;
import net.onima.onimagames.game.Game;
import net.onima.onimagames.game.GameType;

public class DTC extends Game {

	private int points, initialPoints;
	private Block block;
	private APIPlayer lastBreaker;
	
	private static boolean refreshed;
	
	public DTC(String name, int points, String creator, Block block) {
		super(GameType.DTC, name, creator);
		this.points = points;
		this.block = block;
		initialPoints = points;
	}
	
	public DTC(String name, String creator) {
		this(name, 0, creator, null);
	}
	
	public DTC(String name) {
		this(name, 0, null, null);
	}
	
	public int getPoints() {
		return points;
	}
	
	public void setPoint(int points) {
		this.points = points;
		initialPoints = points;
	}
	
	public void removePoint(int point) {
		if(point > points)
			points = 0;
		else
			points -= point;
	}
	
	public int getInitialPoints() {
		return initialPoints;
	}

	public Block getBlock() {
		return block;
	}
	
	public void setBlock(Block block) {
		this.block = block;
	}
	
	public APIPlayer getLastBreaker() {
		return lastBreaker;
	}
	
	public void setLastBreaker(APIPlayer lastBreaker) {
		this.lastBreaker = lastBreaker;
	}

	@Override
	public void serialize() {
		if (!refreshed)
			refreshFile();
		
		config.set(path+"points", initialPoints);
		config.set(path+"location", Methods.serializeLocation(block.getLocation(), false));
		config.set(path+"creator", creator);
		config.set(path+"created", created);
		
		super.serialize();
	}

	@Override
	public void refreshFile() {
		ConfigurationSection section = gameSerialConfig.getConfig().getConfigurationSection("GAMES.DTC");
		
		if (section != null) {
			List<String> gamesName = games.stream().filter(game -> game instanceof DTC).map(Game::getName).collect(Collectors.toList());
			for (String name : section.getKeys(false)) {
				if (!gamesName.contains(name))
					gameSerialConfig.remove("GAMES.DTC."+name, false);
			}
		}
		refreshed = true;
	}

	@Override
	public void start() {
		if (!isStarted()) {
			GameStartEvent event = new GameStartEvent(this);		
			
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) return;
			
			points = initialPoints;
			
			Bukkit.broadcastMessage("§8" + ConfigurationService.STAIGHT_LINE);
			Bukkit.broadcastMessage("§7\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588");
			Bukkit.broadcastMessage("§7\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588"); 
			Bukkit.broadcastMessage("§7\u2588\u2588§e\u2588\u2588\u2588§7\u2588\u2588\u2588");
			Bukkit.broadcastMessage("§7\u2588\u2588§e\u2588§7\u2588\u2588§e\u2588§7\u2588\u2588  §6[DTC] " + name); 
			Bukkit.broadcastMessage("§7\u2588\u2588§e\u2588§7\u2588\u2588§e\u2588§7\u2588\u2588  §da commencé"); 
			Bukkit.broadcastMessage("§7\u2588\u2588§e\u2588§7\u2588\u2588§e\u2588§7\u2588\u2588  §epoints : " + points);
			Bukkit.broadcastMessage("§7\u2588\u2588§e\u2588\u2588\u2588§7\u2588\u2588\u2588"); 
			Bukkit.broadcastMessage("§7\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588");
			Bukkit.broadcastMessage("§7\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588");
			
			region.setDeathban(false);
            region.addFlag(Flag.DENY_ENDERPEARL);
            region.addFlag(Flag.PVP_TIMER_DENY_ENTRY);
            
            Cuboid cuboid = region.toCuboid();
            Location loc = cuboid.getWorld().getHighestBlockAt(cuboid.getMinimumLocation().add(-0.5, 0, -0.5)).getLocation();
            
            for (Player player : cuboid.getPlayers()) {
            	if (APIPlayer.getByPlayer(player).getTimeLeft(PvPTimerCooldown.class) > 0L)
            		player.teleport(loc);
            }
	        
			startedGame = this;
			startedTime = System.currentTimeMillis();
		}
	}
	
	@Override
	public boolean isReadyToStart() {
		return initialPoints != 0 && block != null && region != null;
	}

	@Override
	public void stop() {
		if (isStarted()) {
			GameStopEvent event = new GameStopEvent(this);		
			
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) return;
			
			Bukkit.broadcastMessage("§7Le DTC §e" + name + " §7a été arrêté.");
			
			region.setDeathban(true);
			region.removeFlag(Flag.DENY_ENDERPEARL);
			region.removeFlag(Flag.PVP_TIMER_DENY_ENTRY);
			
			points = initialPoints;
			lastBreaker = null;
			startedGame = null;	
			startedTime = 0;
		}
	}
	
	@Override
	public void win(APIPlayer winner) {
		DTCWinEvent event = new DTCWinEvent(this, winner);
		Bukkit.getPluginManager().callEvent(event);
		
		if (event.isCancelled())
			return;
		
		Bukkit.broadcastMessage("");
		Bukkit.broadcastMessage("");
		Bukkit.broadcastMessage("§7La faction §e" + FPlayer.getByUuid(lastBreaker.getUUID()).getFaction().getName() + " §7a remporté le DTC §e" + name + " §7. Cet event a duré §e" + LongTime.setYMDWHMSFormat(System.currentTimeMillis() - startedTime) + '.');
		award(winner.toPlayer());
		
		region.setDeathban(true);
		region.removeFlag(Flag.DENY_ENDERPEARL);
		region.removeFlag(Flag.PVP_TIMER_DENY_ENTRY);
		
		points = initialPoints;
		lastBreaker = null;
		startedGame = null;	
		startedTime = 0;
	}
	
	@Override
	public void tick() {
		points--;
		
		if (points == 0) {
			win(lastBreaker);
			return;
		} else if (points % 10 == 0 || points < 4) {
			Methods.playServerSound(ConfigurationService.DTC_BREAK_SOUND);
			Bukkit.broadcastMessage("§7Le DTC §e" + name + " §7est entrain d'être détruit ! §7(§c" + points + "§7)");
		} else
			ConfigurationService.DTC_BREAK_SOUND.play(lastBreaker);
		
	}
	
	@Override
	public List<String> getServerListLines() {
		return Methods.replacePlaceholder(ConfigurationService.DTC_SERVER_LIST_LINE, "%name%", name, "%points%", points);
	}
	
	@Override
	public void update() {}
	
	@Override
	public void sendShow(CommandSender sender) {
		boolean hasPerm = sender instanceof ConsoleCommandSender ? true : APIPlayer.getByPlayer((Player) sender).getRank().getRankType().hasPermission(OnimaPerm.GAME_SHOW_MOD);
		
		sender.sendMessage(ConfigurationService.STAIGHT_LINE);
		sender.sendMessage("§7Event : §d§o" + type.getName() + ' ' + name + " §7- Créateur : §d§o" + creator + " §7- Monde : §d§o" + (region == null ? "§cAucun" : "§a" + region.getLocation1().getWorld().getName()));
		if (hasPerm) {
			sender.sendMessage("§7Créé le : §d§o" + Methods.toFormatDate(created, ConfigurationService.DATE_FORMAT_HOURS));
			sender.sendMessage("§7Points pour gagner : §d§o" + initialPoints);
			
			if (sender instanceof Player && block != null) {
				Location location = block.getLocation().getWorld().getHighestBlockAt(block.getLocation()).getLocation();
				
				((Player) sender).spigot().sendMessage(new ComponentBuilder("§7§oSe téléporter à l'event.")
						.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + location.getBlockX() + ' ' + location.getBlockY() + ' ' + location.getBlockZ()))
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7/tp " + location.getBlockX() + ' ' + location.getBlockY() + ' ' + location.getBlockZ()).create())).create());
			}
			
			sender.sendMessage("§7Zone de l'event : " + (region != null ? "§acréée" : "§cnon-créée") + "§7.");
			if (region != null) {
				Location loc1 = region.getLocation1();
				Location loc2 = region.getLocation2();
				sender.sendMessage(" §7- Location n°1 : §d§o" + loc1.getBlockX() + ' ' + loc1.getBlockY() + ' ' + loc1.getBlockZ());
				sender.sendMessage(" §7- Location n°2 : §d§o" + loc2.getBlockX() + ' ' + loc2.getBlockY() + ' ' + loc2.getBlockZ());
			}
			
			sender.sendMessage("§7Block : " + (block != null ? "§a" + block.getType().name() : "§cnon-créée") + "§7." + (block != null ? " Location : §d§o" + block.getLocation().getBlockX() + ' ' + block.getLocation().getBlockY() + ' ' + block.getLocation().getBlockZ() : ""));
		}
		
		if (sender.hasPermission(OnimaPerm.GAME_NEXT_ARGUMENT.getPermission()) && isSchedulerEnabled() && isSchedulerSet())
			sender.sendMessage("§7Programmé pour : §d§o" + new CasualFormatDate("d hi").toNormalDate(getWhenItStarts()));
		
		if (isStarted()) {
			sender.sendMessage("§7Commencé le : §d§o" + Methods.toFormatDate(startedTime, ConfigurationService.DATE_FORMAT_HOURS));
			sender.sendMessage("§7Commencé depuis : §d§o" + LongTime.setHMSFormat(System.currentTimeMillis() - startedTime));
			
			if (lastBreaker != null)
				sender.sendMessage("§7Cappeur : §d§o" + (hasPerm ? lastBreaker.getName() : "?????"));
			sender.sendMessage("§7Break restant" + (points > 1 ? "s" : "") + " : §d§o" + points);
		}
		sender.sendMessage("§7Récompense : §d§oClé " + type.getName() + " x1");
		sender.sendMessage(ConfigurationService.STAIGHT_LINE);
	}
	
	public static DTC getDTCByLoc(Location location) {
		for (Game game : games) {
			if (game instanceof DTC) {
				if (Methods.locationEquals(location, ((DTC) game).getBlock().getLocation()))
					return (DTC) game;
			}
		}
		return null;
	}
	
}
