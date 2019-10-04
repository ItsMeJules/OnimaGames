package net.onima.onimagames.game.conquest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.minecraft.util.com.google.common.collect.Iterables;
import net.onima.onimaapi.players.APIPlayer;
import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.CasualFormatDate;
import net.onima.onimaapi.utils.ConfigurationService;
import net.onima.onimaapi.utils.Methods;
import net.onima.onimaapi.utils.time.Time.LongTime;
import net.onima.onimaapi.zone.Cuboid;
import net.onima.onimaapi.zone.struct.Flag;
import net.onima.onimaapi.zone.type.Region;
import net.onima.onimafaction.OnimaFaction;
import net.onima.onimafaction.cooldowns.PvPTimerCooldown;
import net.onima.onimafaction.faction.PlayerFaction;
import net.onima.onimafaction.players.FPlayer;
import net.onima.onimagames.OnimaGames;
import net.onima.onimagames.event.GameStartEvent;
import net.onima.onimagames.event.GameStopEvent;
import net.onima.onimagames.event.capable.CapableWinEvent;
import net.onima.onimagames.game.Game;
import net.onima.onimagames.game.GameType;
import net.onima.onimagames.task.GameTask;

public class Conquest extends Game {
	
	private int pointsToWin;
	private ConquestType[] types;
	private ConquestZone[] zones;
	private Map<String, Short> cappingFactions;
	
	private static boolean refreshed;
	
	{
		types = new ConquestType[5];
		zones = new ConquestZone[5];
	}
	
	public Conquest(String name, String creator) {
		super(GameType.CONQUEST, name, creator);
		cappingFactions = new HashMap<>();
	}

	public Conquest(String name) {
		this(name, null);
	}
	public int getPointsToWin() {
		return pointsToWin;
	}

	public void setPointsToWin(int pointsToWin) {
		this.pointsToWin = pointsToWin;
	}
	
	public Region getRegion(ConquestType type) {
		switch(type) {
		case BLUE:
			return zones[0].getCapZone();
		case GREEN:
			return zones[1].getCapZone();
		case RED:
			return zones[2].getCapZone();
		case YELLOW:
			return zones[3].getCapZone();
		case MAIN:
			return zones[4].getCapZone();
		default:
			return null;
		}
	}
	
	public ConquestZone[] getZones() {
		return zones;
	}
	
	public void addZone(ConquestType type, ConquestZone zone, Region capZone) {
		zone.setCapZone(capZone);
		switch(type) {
		case BLUE:
			types[0] = type;
			zones[0] = zone;
			break;
		case GREEN:
			types[1] = type;
			zones[1] = zone;
			break;
		case RED:
			types[2] = type;
			zones[2] = zone;
			break;
		case YELLOW:
			types[3] = type;
			zones[3] = zone;
			break;
		case MAIN:
			types[4] = type;
			zones[4] = zone;
		default:
			break;
		}
	}
	
	public void addConquestZone(ConquestType type, ConquestZone zone) {
		switch(type) {
		case BLUE:
			zones[0] = zone;
			break;
		case GREEN:
			zones[1] = zone;
			break;
		case RED:
			zones[2] = zone;
			break;
		case YELLOW:
			zones[3] = zone;
			break;
		case MAIN:
			zones[4] = zone;
		default:
			break;
		}
	}
	
	public void removeZone(ConquestType type) {
		switch(type) {
		case BLUE:
			types[0] = null;
			zones[0] = null;
			break;
		case GREEN:
			types[1] = null;
			zones[1] = null;
			break;
		case RED:
			types[2] = null;
			zones[2] = null;
			break;
		case YELLOW:
			types[3] = null;
			zones[3] = null;
			break;
		case MAIN:
			types[4] = null;
			zones[4] = null;
		default:
			break;
		}
	}
	
	public ConquestZone getZone(ConquestType type) {
		for (ConquestZone zone : zones) {
			if (zone == null) continue;
			if (zone.getType() == type)
				return zone;
		}
		return null;
	}
	
	public boolean hasZone(ConquestType type) {
		for (ConquestType color : types) {
			if (color == null) continue;
			if (color == type) return true;
		}
		return false;
	}
	
	
	public boolean hasAllZoneSet() {
		for (ConquestType type : ConquestType.values())
			return hasZone(type);

		return true;
	}
	
	public Map<String, Short> getCappingFactions() {
		return cappingFactions;
	}
	
	@Override
	public void serialize() {
		if (!refreshed)
			refreshFile();
		
		super.serialize();
		
		config.set(path+"points-to-win", pointsToWin);
		
		for (ConquestZone zone : zones) {
			String path2 = path+"zones."+zone.getType().getName()+".";
			
			config.set(path2+"name", zone.getName());
			config.set(path2+"points-per-cap", zone.getPointsPerCap());
			config.set(path2+"cap-time", zone.getCapTime());
		}
	}

	@Override
	public void refreshFile() {
		ConfigurationSection section = gameSerialConfig.getConfig().getConfigurationSection("GAMES.CONQUEST");
		
		if (section != null) {
			List<String> gamesName = games.stream().filter(game -> game instanceof Conquest).map(Game::getName).collect(Collectors.toList());
			for (String name : section.getKeys(false)) {
				if (!gamesName.contains(name))
					gameSerialConfig.remove("GAMES.CONQUEST."+name, false);
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
			
			Location blue = getRegion(ConquestType.BLUE).toCuboid().getCenterLocation();
			Location green = getRegion(ConquestType.GREEN).toCuboid().getCenterLocation();
			Location red = getRegion(ConquestType.RED).toCuboid().getCenterLocation();
			Location yellow = getRegion(ConquestType.YELLOW).toCuboid().getCenterLocation();
			Location main = getRegion(ConquestType.MAIN).toCuboid().getCenterLocation();
			
			Bukkit.broadcastMessage("§8" + ConfigurationService.STAIGHT_LINE);
            Bukkit.broadcastMessage("§7La conquest §9" + name + " §7a commencé aux locations suivantes :");
            Bukkit.broadcastMessage("  §7- §bBleu : §7" + blue.getBlockX() + ", " + blue.getBlockY() + ", " + blue.getBlockZ());
            Bukkit.broadcastMessage("  §7- §aVert : §7" + green.getBlockX() + ", " + green.getBlockY() + ", " + green.getBlockZ());
            Bukkit.broadcastMessage("  §7- §cRouge : §7" + red.getBlockX() + ", " + red.getBlockY() + ", " + red.getBlockZ());
            Bukkit.broadcastMessage("  §7- §eJaune : §7" + yellow.getBlockX() + ", " + yellow.getBlockY() + ", " + yellow.getBlockZ());
            Bukkit.broadcastMessage("  §7- §dPrincipal : §7" + main.getBlockX() + ", " + main.getBlockY() + ", " + main.getBlockZ());
			
            region.setDeathban(false);
            region.addFlag(Flag.DENY_ENDERPEARL);
            region.addFlag(Flag.PVP_TIMER_DENY_ENTRY);
            
            Cuboid cuboid = region.toCuboid();
            Location loc = cuboid.getWorld().getHighestBlockAt(cuboid.getMinimumLocation().add(-0.5, 0, -0.5)).getLocation();
            
            for (Player player : cuboid.getPlayers()) {
            	if (APIPlayer.getByPlayer(player).getTimeLeft(PvPTimerCooldown.class) > 0L)
            		player.teleport(loc);
            }
            
            for (ConquestZone zone : zones) {
            	zone.setCapTimeLeft(zone.getCapTime());
            	zone.getCapZone().setDeathban(false);
            	zone.getCapZone().addFlag(Flag.DENY_ENDERPEARL);
            	zone.getCapZone().addFlag(Flag.PVP_TIMER_DENY_ENTRY);
            }
            
			startedGame = this;
			startedTime = System.currentTimeMillis();
			(gameTask = new GameTask()).runTaskTimerAsynchronously(OnimaGames.getInstance(), 0L, 20L);
			update();
		}
	}

	@Override
	public boolean isReadyToStart() {
		boolean ready = false;
		
		if (region != null && hasAllZoneSet()) {
			for (ConquestType type : ConquestType.values()) {
				ConquestZone zone = getZone(type);
				
				if (zone.getPointsPerCap() == 0 || zone.getCapTime() == 0) return ready;
			}
			ready = true;
		}
		return ready;
	}
	
	@Override
	public void stop() {
		if (isStarted()) {
			GameStopEvent event = new GameStopEvent(this);		
			
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) return;
			
			Bukkit.broadcastMessage("§7La conquest §e" + name + " §7a été arrêté.");
			
            region.setDeathban(true);
            region.removeFlag(Flag.DENY_ENDERPEARL);
            region.removeFlag(Flag.PVP_TIMER_DENY_ENTRY);
			
			for (ConquestZone zone : zones) {
				if (zone.getCapper() != null) {
					zone.getCapper().setCapping(null);
					zone.setCapper(null);
				}
				
				zone.getCapZone().setDeathban(false);
            	zone.getCapZone().removeFlag(Flag.DENY_ENDERPEARL);
            	zone.getCapZone().removeFlag(Flag.PVP_TIMER_DENY_ENTRY);
				zone.setTimeAtCap(-1L);
				zone.setCapTimeLeft(-1L);
			}
			
			cappingFactions.clear();
			startedTime = 0;
			gameTask.cancel();
			gameTask = null;
			startedGame = null;
		}
	}
	
	@Override
	public void tick() {
		for (ConquestZone zone : zones) {
			if (!zone.isCapped()) continue;
			
			zone.decreaseTime();
			
			if (zone.getCapTimeLeft() <= 0L) {
				APIPlayer apiPlayer = zone.getCapper();
				PlayerFaction faction = FPlayer.getByUuid(apiPlayer.getUUID()).getFaction();
				String factionName = faction.getName();
				int factionPoints = getPoints(factionName) + zone.getPointsPerCap();
				
				if (pointsToWin > factionPoints) {
					setPoints(factionName, factionPoints);
					zone.setCapTimeLeft(zone.getCapTime());
					zone.setTimeAtCap(System.currentTimeMillis());
				
					if (factionPoints % 10 == 0) {
						for (Player online : Bukkit.getOnlinePlayers()) 
							online.sendMessage("§7La faction §e" + faction.getRelation(online)+faction.getName() + " §7a §e" + factionPoints + " §7points !");
					}
				} else {
					CapableWinEvent event = new CapableWinEvent(zone, apiPlayer);
					Bukkit.getPluginManager().callEvent(event);
					
					if (event.isCancelled()) continue;
					
					win(apiPlayer);
					break;
				}
			} else if ((zone.getCapTimeLeft() / 1000) % 5 == 0)
				Bukkit.broadcastMessage("§eQuelqu'un §7est entrain de contrôler la zone " + zone.getType().getName() + ". §c(" + LongTime.setHMSFormat(zone.getCapTimeLeft()) + ')');
		}
	}
	
	@Override
	public void win(APIPlayer winner) {
		Bukkit.broadcastMessage("");
		Bukkit.broadcastMessage("");
		
		for (Player online : Bukkit.getOnlinePlayers()) {
		      online.sendMessage("§7\u2588\u2588\u2588\u2588\u2588\u2588\u2588");
		      online.sendMessage("§7\u2588\u2588\u2588§a\u2588§7\u2588\u2588\u2588");
		      online.sendMessage("§7\u2588\u2588§a\u2588§7\u2588§a\u2588§7\u2588\u2588 §7[§6" + name + "§7]");
		      online.sendMessage("§7\u2588\u2588§a\u2588§7\u2588§a\u2588§7\u2588\u2588 §ecapturée par");
		      online.sendMessage("§7\u2588§a\u2588\u2588\u2588\u2588\u2588§7\u2588 §7[" + OnimaFaction.getDisplay(FPlayer.getByUuid(winner.getUUID()), FPlayer.getByPlayer(online)) + "§7]§e" + winner.getName());
		      online.sendMessage("§7\u2588§a\u2588§7\u2588\u2588\u2588§a\u2588§7\u2588");
		      online.sendMessage("§7\u2588§a\u2588§7\u2588\u2588\u2588§a\u2588§7\u2588");
		      online.sendMessage("§7\u2588\u2588\u2588\u2588\u2588\u2588\u2588");
		      online.sendMessage("         §7Durée de l'event : " + LongTime.setHMSFormat(System.currentTimeMillis() - startedTime));
		}
			
		award(winner.toPlayer());
		
        region.setDeathban(true);
        region.removeFlag(Flag.DENY_ENDERPEARL);
        region.removeFlag(Flag.PVP_TIMER_DENY_ENTRY);
		
		for (ConquestZone zone : zones) {
			if (zone.getCapper() != null) {
				zone.getCapper().setCapping(null);
				zone.setCapper(null);
			}
			
			zone.getCapZone().setDeathban(false);
        	zone.getCapZone().removeFlag(Flag.DENY_ENDERPEARL);
        	zone.getCapZone().removeFlag(Flag.PVP_TIMER_DENY_ENTRY);
			zone.setTimeAtCap(-1L);
			zone.setCapTimeLeft(-1L);
		}
		
		cappingFactions.clear();
		startedTime = 0;
		gameTask.cancel();
		gameTask = null;
		startedGame = null;
	}
	
	@Override
	public List<String> getServerListLines() {
		return Methods.replacePlaceholder(ConfigurationService.CONQUEST_SERVER_LIST_LINE, "%name%", name, "%points-to-win%", pointsToWin);
	}
	
	@Override
	public void update() {
		if (isStarted()) {
			for (ConquestZone zone : zones) {
				Player capper = Iterables.getFirst(zone.getCapZone().toCuboid().getPlayers(), null);
				
				zone.setCapper(capper == null ? null : APIPlayer.getByPlayer(capper));
			}
		}
	}
	
	@Override
	public void sendShow(CommandSender sender) {
		boolean hasPerm = sender instanceof ConsoleCommandSender ? true : APIPlayer.getByPlayer((Player) sender).getRank().getRankType().hasPermission(OnimaPerm.GAME_SHOW_MOD);
		
		sender.sendMessage(ConfigurationService.STAIGHT_LINE);
		sender.sendMessage("§7Event : §d§o" + type.getName() + ' ' + name + " §7- Créateur : §d§o" + creator + " §7- Monde : §d§o" + (region == null ? "§cAucun" : "§a" + region.getLocation1().getWorld().getName()));
		if (hasPerm) {
			sender.sendMessage("§7Créé le : §d§o" + Methods.toFormatDate(created, ConfigurationService.DATE_FORMAT_HOURS));
			sender.sendMessage("§7Points pour gagner : §d§o" + pointsToWin);
			sender.sendMessage("§7Zone de l'event : " + (region != null ? "§acréée" : "§cnon-créée") + "§7.");
			if (region != null) {
				Location loc1 = region.getLocation1();
				Location loc2 = region.getLocation2();
				sender.sendMessage(" §7- Location n°1 : §d§o" + loc1.getBlockX() + ' ' + loc1.getBlockY() + ' ' + loc1.getBlockZ());
				sender.sendMessage(" §7- Location n°2 : §d§o" + loc2.getBlockX() + ' ' + loc2.getBlockY() + ' ' + loc2.getBlockZ());
			}
			
			
			sender.sendMessage("§7Zones de cap (§d§o" + zones.length + "/5§7) :");
			for (ConquestZone zone : zones) {
				ConquestType type = zone.getType();
				
				Methods.sendJSON(sender, new ComponentBuilder("§7Zone " + type.getName().toLowerCase())
						.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/conquest info " + type.name()))
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7§o/conquest info " + type.name()).create())).create());
			}
		}
		
		if (sender.hasPermission(OnimaPerm.GAME_NEXT_ARGUMENT.getPermission()) && isSchedulerEnabled() && isSchedulerSet())
			sender.sendMessage("§7Programmé pour : §d§o" + new CasualFormatDate("d hi").toNormalDate(getWhenItStarts()));
		
		if (isStarted()) {
			sender.sendMessage("§7Commencé le : §d§o" + Methods.toFormatDate(startedTime, ConfigurationService.DATE_FORMAT_HOURS));
			sender.sendMessage("§7Commencé depuis : §d§o" + LongTime.setHMSFormat(System.currentTimeMillis() - startedTime));
			sender.sendMessage("§7Zones de cap : ");
			for (ConquestZone zone : zones)
				sender.sendMessage("§7Zone " + zone.getType().getName().toLowerCase() + " §7- §d§o" + LongTime.setHMSFormat(zone.getCapTimeLeft()));
		}
		sender.sendMessage("§7Récompense : §d§oClé " + type.getName() + " x1");
		sender.sendMessage(ConfigurationService.STAIGHT_LINE);
	}
	 
	public void putIfAbsent(String factionName) {
		synchronized (cappingFactions) {
			cappingFactions.putIfAbsent(factionName, (short) 0);
		}
	}
	
	public boolean hasPoints(String factionName) {
		synchronized (cappingFactions) {
			return cappingFactions.containsKey(factionName);
		}
	}
	
	public short addPoints(String factionName, int points) {
		synchronized (cappingFactions) {
			short newPoints = (short) (cappingFactions.get(factionName)+points);
			
			cappingFactions.put(factionName, newPoints);
			
			return newPoints;
		}
	}
	
	public short removePoints(String factionName, int points) {
		synchronized (cappingFactions) {
			short newPoints = (short) (cappingFactions.get(factionName)-points);
			
			cappingFactions.put(factionName, newPoints);
			
			return newPoints;
		}
	}
	
	public short setPoints(String factionName, int points) {
		synchronized (cappingFactions) {
			short newPoints = (short) points;
			
			cappingFactions.put(factionName, newPoints);
			
			return newPoints;
		}
	}
	
	public short getPoints(String factionName) {
		synchronized (cappingFactions) {
			return cappingFactions.containsKey(factionName) ? cappingFactions.get(factionName) : 0;
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		
		for (ConquestZone zone : zones)
			zone.getCapZone().remove();
	}
	
}
