package net.onima.onimagames.game.koth;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.minecraft.util.com.google.common.collect.Iterables;
import net.onima.onimaapi.players.APIPlayer;
import net.onima.onimaapi.players.utils.PlayerOption;
import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.CasualFormatDate;
import net.onima.onimaapi.utils.ConfigurationService;
import net.onima.onimaapi.utils.Methods;
import net.onima.onimaapi.utils.time.Time.LongTime;
import net.onima.onimaapi.zone.Cuboid;
import net.onima.onimaapi.zone.struct.Flag;
import net.onima.onimaapi.zone.type.utils.Capable;
import net.onima.onimafaction.cooldowns.PvPTimerCooldown;
import net.onima.onimagames.OnimaGames;
import net.onima.onimagames.event.GameStartEvent;
import net.onima.onimagames.event.GameStopEvent;
import net.onima.onimagames.event.capable.CapableCapEvent;
import net.onima.onimagames.event.capable.CapableKnockEvent;
import net.onima.onimagames.event.capable.CapableWinEvent;
import net.onima.onimagames.game.Game;
import net.onima.onimagames.game.GameType;
import net.onima.onimagames.task.GameTask;

public class Koth extends Game implements Capable {
	
	private static boolean refreshed;
	
	protected APIPlayer capper;
	protected long capTime, capTimeLeft, timeAtCap = -1L;
	protected Cuboid capZone;
	
	public Koth(String name, long capTime, APIPlayer capper, String creator, GameType gameType) {
		super(gameType, name, creator);
		
		this.capTime = capTime;
		this.capper = capper;
	}
	
	public Koth(String name, long capTime, APIPlayer capper, String creator) {
		this(name, capTime, capper, creator, GameType.KOTH);
	}
	
	public Koth(String name, long capTime, String creator) {
		this(name, capTime, null, creator);
	}
	
	public Koth(String name, String creator) {
		this(name, 0, null, creator);
	}
	
	public Koth(String name) {
		this(name, 0, null, null);
	}
	
	@Override
	public Cuboid getCapZone() {
		return capZone;
	}

	@Override
	public void setCapZone(Cuboid capZone) {
		this.capZone = capZone;
	}

	@Override
	public void decreaseTime() {
		capTimeLeft = timeAtCap + capTime - System.currentTimeMillis();
	}

	@Override
	public long getCapTime() {
		return capTime;
	}

	@Override
	public void setCapTime(long capTime) {
		this.capTime = capTime;
		capTimeLeft = capTime;
	}

	@Override
	public long getCapTimeLeft() {
		return capTimeLeft;
	}

	@Override
	public void setCapTimeLeft(long capTimeLeft) {
		this.capTimeLeft = capTimeLeft;
	}

	@Override
	public long getTimeAtCap() {
		return timeAtCap;
	}

	@Override
	public void setTimeAtCap(long timeAtCap) {
		this.timeAtCap = timeAtCap;
	}

	@Override
	public APIPlayer getCapper() {
		return capper;
	}

	@Override
	public void setCapper(APIPlayer capper) {
		this.capper = capper;
	}

	@Override
	public boolean isCapped() {
		return capper != null;
	}

	@Override
	public void start() {
		if (!isStarted()) {
			GameStartEvent event = new GameStartEvent(this);		
			
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) return;
			
            Bukkit.broadcastMessage("§8" + ConfigurationService.STAIGHT_LINE);
            Bukkit.broadcastMessage("§8\u2588§e\u2588\u2588\u2588\u2588\u2588\u2588\u2588§8\u2588");
            Bukkit.broadcastMessage("§e\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588");
            Bukkit.broadcastMessage("§e\u2588§6\u2588§e\u2588§6\u2588§e\u2588§6\u2588§e\u2588§6\u2588§e\u2588 §6[KoTH]" );
            Bukkit.broadcastMessage("§e\u2588§6\u2588\u2588\u2588\u2588\u2588\u2588\u2588§e\u2588   §a§l" + name);
            Bukkit.broadcastMessage("§e\u2588§6\u2588§b\u2588§6\u2588§b\u2588§6\u2588§b\u2588§6\u2588§e\u2588 §7a commencé. §a(" + LongTime.setHMSFormat(capTime) + ')');
            Bukkit.broadcastMessage("§e\u2588§6\u2588\u2588\u2588\u2588\u2588\u2588\u2588§e\u2588");
            Bukkit.broadcastMessage("§e\u2588\u2588\u2588§7\u2588\u2588\u2588§e\u2588\u2588\u2588");
            Bukkit.broadcastMessage("§e\u2588\u2588\u2588\u2588§7\u2588§e\u2588\u2588\u2588\u2588");
            Bukkit.broadcastMessage("§8\u2588§e\u2588\u2588\u2588§7\u2588§e\u2588\u2588\u2588§8\u2588");
			
            region.setDeathban(false);
            region.addFlag(Flag.DENY_ENDERPEARL);
            region.addFlag(Flag.PVP_TIMER_DENY_ENTRY);
            
            Cuboid cuboid = region.toCuboid();
            Location loc;
            
            loc = cuboid.getWorld().getHighestBlockAt(cuboid.getMinimumLocation().add(-0.5, 0, -0.5)).getLocation();
            
            for (Player player : cuboid.getPlayers()) {
            	if (APIPlayer.getPlayer(player).getTimeLeft(PvPTimerCooldown.class) > 0L)
            		player.teleport(loc);
            }
            
			startedGame = this;	
			capTimeLeft = capTime;
			startedTime = System.currentTimeMillis();
			(gameTask = new GameTask()).runTaskTimerAsynchronously(OnimaGames.getInstance(), 0L, 20L);
			update();
		}
	}
	
	@Override
	public boolean isReadyToStart() {
		return capTime != 0 && region != null && capZone != null;
	}

	@Override
	public void stop() {
		if (isStarted()) {
			GameStopEvent event = new GameStopEvent(this);		
			
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) return;
			
			Bukkit.broadcastMessage("§7Le koth §e" + name + " §7a été arrêté.");
			
            region.setDeathban(true);
            region.removeFlag(Flag.DENY_ENDERPEARL);
            region.removeFlag(Flag.PVP_TIMER_DENY_ENTRY);
            
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
		Bukkit.broadcastMessage("§7Le KoTH §e" + name + " §7a été capturé par §e" + winner.getName() + "§7. Cet event a duré §e" + LongTime.setYMDWHMSFormat(System.currentTimeMillis() - startedTime));
		award(winner.toPlayer());
		
        region.setDeathban(true);
        region.removeFlag(Flag.DENY_ENDERPEARL);
        region.removeFlag(Flag.PVP_TIMER_DENY_ENTRY);
        
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
		} else if ((capTimeLeft / 1000) % 30 == 0 && capTimeLeft > 1000) {
			Bukkit.broadcastMessage("§eQuelqu'un §7est entrain de contrôler le KoTH §e" + name + ". §c(" + LongTime.setHMSFormatOnlySeconds(capTimeLeft) + ')');
			capper.sendMessage("§eVous §7êtes entrain de contrôler le KoTH.");
		}
	}
	
	@Override
	public boolean tryCapping(APIPlayer capper) {
		return capper.getTimeLeft(PvPTimerCooldown.class) <= 0L;
	}
	
	@Override
	public void onCap(APIPlayer capper) {
		CapableCapEvent event = new CapableCapEvent(this, capper);
		Bukkit.getPluginManager().callEvent(event);
		
		if (event.isCancelled())
			return;
		
		for (APIPlayer apiPlayer : APIPlayer.getOnlineAPIPlayers()) {
			if (apiPlayer.getOptions().getBoolean(PlayerOption.GlobalOptions.CAPZONE_MESSAGES))
				apiPlayer.sendMessage("§7Le KoTH §e" + name + " §7est entrain d'être capturé !");
		}
		
		capper.sendMessage("§eVous §7êtes entrain de capturer le KoTH !");
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
		
		capper.sendMessage("§eVous §7n'êtes plus entrain de capturer le KoTH !");
		
		if ((capTime - capTimeLeft) > ConfigurationService.KOTH_KNOCK_ANNOUNCE_DELAY) {
			for (APIPlayer apiPlayer : APIPlayer.getOnlineAPIPlayers()) {
				if (apiPlayer.getOptions().getBoolean(PlayerOption.GlobalOptions.CAPZONE_MESSAGES))
					apiPlayer.sendMessage("§e" + capper.getDisplayName() + " §7a été knock du KoTH (" + LongTime.setHMSFormatOnlySeconds(capTimeLeft) + ") !");
			}
		}
		
		capper.setCapping(null);
		
		timeAtCap = -1L;
		capTimeLeft = capTime;
		this.capper = null;
	}
	
	@Override
	public List<String> getServerListLines() {
		return Methods.replacePlaceholder(ConfigurationService.KOTH_SERVER_LIST_LINE, "%name%", name, "%time%", LongTime.setHMSFormat(getCapTimeLeft()));
	}

	@Override
	public void update() {
		if (isStarted()) {
			Player capper = Iterables.getFirst(capZone.getPlayers(), null);
			
			if (capper != null)
				onCap(APIPlayer.getPlayer(capper));
		}
	}
	
	@Override
	public void sendShow(CommandSender sender) {
		boolean hasPerm = sender instanceof ConsoleCommandSender ? true : OnimaPerm.GAME_SHOW_MOD.has(sender);
		
		sender.sendMessage(ConfigurationService.STAIGHT_LINE);
		sender.sendMessage("§7Event : §d§o" + type.getName() + ' ' + name + " §7- Créateur : §d§o" + creator + " §7- Monde : §d§o" + (region == null ? "§cAucun" : "§a" + region.getLocation1().getWorld().getName()));
		if (hasPerm) {
			sender.sendMessage("§7Créé le : §d§o" + Methods.toFormatDate(created, ConfigurationService.DATE_FORMAT_HOURS));
			sender.sendMessage("§7Temps de cap : §d§o" + LongTime.setHMSFormat(capTime));
			
			if (sender instanceof Player && location != null) {
				Location location = this.location.getWorld().getHighestBlockAt(this.location).getLocation();
				
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
			
			sender.sendMessage("§7Zone de cap : " + (capZone != null ? "§acréée" : "§cnon-créée") + "§7.");
			if (capZone != null) {
				Vector loc1 = capZone.getMinimum();
				Vector loc2 = capZone.getMaximum();
				sender.sendMessage(" §7- Location n°1 : §d§o" + loc1.getBlockX() + ' ' + loc1.getBlockY() + ' ' + loc1.getBlockZ());
				sender.sendMessage(" §7- Location n°2 : §d§o" + loc2.getBlockX() + ' ' + loc2.getBlockY() + ' ' + loc2.getBlockZ());
			}
		}
		
		if (sender.hasPermission(OnimaPerm.GAME_NEXT_ARGUMENT.getPermission()) && isSchedulerEnabled() && isSchedulerSet())
			sender.sendMessage("§7Programmé pour : §d§o" + new CasualFormatDate("d hi").toNormalDate(getWhenItStarts()));
		
		if (isStarted()) {
			sender.sendMessage("§7Commencé le : §d§o" + Methods.toFormatDate(startedTime, ConfigurationService.DATE_FORMAT_HOURS));
			sender.sendMessage("§7Commencé depuis : §d§o" + LongTime.setHMSFormat(System.currentTimeMillis() - startedTime));
			
			if (capper != null)
				sender.sendMessage("§7Cappeur : §d§o" + (hasPerm ? capper.getDisplayName(true) : "?????"));
			sender.sendMessage("§7Temps de cap restant : §d§o" + LongTime.setHMSFormat(capTimeLeft));
		}
		sender.sendMessage("§7Récompense : §d§oClé " + type.getName() + " x1");
		sender.sendMessage(ConfigurationService.STAIGHT_LINE);
	}
	
	@Override
	public void serialize() {
		if (!refreshed)
			refreshFile();
		
		config.set(path + "cap-time", capTime);
		
		if (capZone != null) {
			config.set(path + "cap-zone-loc1", Methods.serializeLocation(capZone.getMinimumLocation(), false));
			config.set(path + "cap-zone-loc2", Methods.serializeLocation(capZone.getMaximumLocation(), false));
		}
		
		super.serialize();
	}

	@Override
	public void refreshFile() {
		ConfigurationSection section = config.getConfigurationSection("GAMES.KOTH");
		
		if (section != null) {
			List<String> gamesName = games.stream().filter(game -> game instanceof Koth).map(Game::getName).collect(Collectors.toList());
			for (String name : section.getKeys(false)) {
				if (!gamesName.contains(name))
					gameSerialConfig.remove("GAMES.KOTH."+name, false);
			}
		}
		refreshed = true;
	}

}
