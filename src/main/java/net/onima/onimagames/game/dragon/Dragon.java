package net.onima.onimagames.game.dragon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.onima.onimaapi.players.APIPlayer;
import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.CasualFormatDate;
import net.onima.onimaapi.utils.ConfigurationService;
import net.onima.onimaapi.utils.Methods;
import net.onima.onimaapi.utils.time.Time.LongTime;
import net.onima.onimafaction.faction.PlayerFaction;
import net.onima.onimagames.event.GameStartEvent;
import net.onima.onimagames.event.GameStopEvent;
import net.onima.onimagames.game.Game;
import net.onima.onimagames.game.GameType;

public class Dragon extends Game { //TODO Event quand qqun passe premier dans les d�g�ts donn� par un joueur.
	
	private Map<String, Double> playerDamageDealt, factionDamageDealt, playerDamageTaken, factionDamageTaken;
	private int arrowsShot, arrowsMissed;
	private List<PotionEffect> effects;
	private double health;
	private String name;
	private EnderDragon dragon;
	
	private static boolean refreshed;
	
	{
		playerDamageDealt = new HashMap<>();
		factionDamageDealt = new HashMap<>();
		playerDamageTaken = new HashMap<>();
		factionDamageTaken = new HashMap<>();
		effects = new ArrayList<>();
	}
	
	public Dragon(String name, String creator) {
		super(GameType.DRAGON_EVENT, name, creator);
	}
	
	public Dragon(String name) {
		this(name, null);
	}

	@Override
	public void refreshFile() {
		ConfigurationSection section = gameSerialConfig.getConfig().getConfigurationSection("GAMES.DRAGON_EVENT");
		
		if (section != null) {
			List<String> gamesName = games.stream().filter(game -> game instanceof Dragon).map(Game::getName).collect(Collectors.toList());
			for (String name : section.getKeys(false)) {
				if (!gamesName.contains(name))
					gameSerialConfig.remove("GAMES.DRAGON_EVENT."+name, false);
			}
		}
		refreshed = true;
	}
	
	@Override
	public void serialize() {
		if (!refreshed)
			refreshFile();
		
		super.serialize();
		
		config.set(path+"effects", Methods.serializePotionEffects(effects));
		config.set(path+"health", health);
		config.set(path+"name", name);
	}
	

	@Override
	public void start() {
		if (!isStarted()) {
			GameStartEvent event = new GameStartEvent(this);		
			
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) return;
			
			Bukkit.broadcastMessage("§8" + ConfigurationService.STAIGHT_LINE);
            Bukkit.broadcastMessage("§0\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588");
            Bukkit.broadcastMessage("§0\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588");
            Bukkit.broadcastMessage("§0\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588");
            Bukkit.broadcastMessage("§0\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588  §7Dragon:");
            Bukkit.broadcastMessage("§0\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588  §e" + Methods.colors(name));
            Bukkit.broadcastMessage("§d\u2588§5\u2588§d\u2588§0\u2588\u2588§5\u2588§d\u2588§5\u2588 §7a spawné en");
            Bukkit.broadcastMessage("§0\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588 §e" + location.getWorld().getName() + ' ' + location.getBlockX() + ' ' + location.getBlockY() + ' ' + location.getBlockZ());
            Bukkit.broadcastMessage("§0\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588");
            Bukkit.broadcastMessage("§0\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588");
			
			startedGame = this;
			startedTime = System.currentTimeMillis();
		}
	}

	@Override
	public boolean isReadyToStart() {
		return name != null && location != null;
	}

	@Override
	public void stop() {
		if (isStarted()) {
			GameStopEvent event = new GameStopEvent(this);		
			
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) return;
			
			Bukkit.broadcastMessage("§7L'event dragon §e" + name + " §7a été stoppé.");
			
			playerDamageDealt.clear();
			playerDamageTaken.clear();
			factionDamageDealt.clear();
			factionDamageTaken.clear();
			arrowsShot = 0;
			arrowsMissed = 0;
			startedGame = null;
			startedTime = 0;
		}
	}
	
	@Override
	public void win(APIPlayer winner) {
		GameStopEvent event = new GameStopEvent(this);		
		
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return;
		
		ComponentBuilder playerDealtBuilder = new ComponentBuilder("§eTop 3 joueurs avec le plus de dégâts donné §7§o(passez votre souris dessus)");
		ComponentBuilder playerDealtHover = new ComponentBuilder("");
		
		ComponentBuilder playerTakenBuilder = new ComponentBuilder("§eTop 3 joueurs avec le plus de dégâts subit §7§o(passez votre souris dessus)");
		ComponentBuilder playerTakenHover = new ComponentBuilder("");

		ComponentBuilder factionDealtBuilder = new ComponentBuilder("§eTop 3 factions avec le plus de dégâts donné §7§o(passez votre souris dessus)");
		ComponentBuilder factionDealtHover = new ComponentBuilder("");
		
		ComponentBuilder factionTakenBuilder = new ComponentBuilder("§eTop 3 factions wavec le plus de dégâts subit §7§o(passez votre souris dessus)");
		ComponentBuilder factionTakenHover = new ComponentBuilder("");
		
		String topFacName = "", topPlayerName = "";
		
		int pos = 0;
		for (Entry<String, Double> top : Methods.getTop(playerDamageDealt, 3).collect(Collectors.toCollection(() -> new ArrayList<>(3)))) {
			pos++;
			playerDealtHover.append(pos + ". §e" + top.getKey() + " - §a" + top.getValue());
			if (pos == 3) pos = 0;
			else if (pos == 1) topPlayerName = top.getKey();
		}
		
		for (Entry<String, Double> top : Methods.getTop(playerDamageTaken, 3).collect(Collectors.toCollection(() -> new ArrayList<>(3)))) {
			pos++;
			playerTakenHover.append(pos + ". §e" + top.getKey() + " - §a" + top.getValue());
			if (pos == 3) pos = 0;
		}
		
		for (Entry<String, Double> top : Methods.getTop(factionDamageDealt, 3).collect(Collectors.toCollection(() -> new ArrayList<>(3)))) {
			pos++;
			factionDealtHover.append(pos + ". §e" + top.getKey() + " - §a" + top.getValue());
			if (pos == 3) pos = 0;
			else if (pos == 1) topFacName = top.getKey();
		}
		
		for (Entry<String, Double> top : Methods.getTop(factionDamageDealt, 3).collect(Collectors.toCollection(() -> new ArrayList<>(3)))) {
			pos++;
			factionTakenHover.append(pos + ". §e" + top.getKey() + " - §a" + top.getValue());
			if (pos == 3) pos = 0;
		}
		
		playerDealtBuilder.event(new HoverEvent(Action.SHOW_TEXT, playerDealtHover.create()));
		playerTakenBuilder.event(new HoverEvent(Action.SHOW_TEXT, playerTakenHover.create()));
		factionDealtBuilder.event(new HoverEvent(Action.SHOW_TEXT, factionDealtHover.create()));
		factionTakenBuilder.event(new HoverEvent(Action.SHOW_TEXT, factionTakenHover.create()));
		
		Bukkit.broadcastMessage("§6§m-----------[§e§oStat Event Dragon§6]-----------");
		Bukkit.spigot().broadcast(playerDealtBuilder.create());
		Bukkit.spigot().broadcast(playerTakenBuilder.create());
		Bukkit.spigot().broadcast(factionDealtBuilder.create());
		Bukkit.spigot().broadcast(factionTakenBuilder.create());
		Bukkit.broadcastMessage("§eFlèches (§7tirées/§cratées§7/§atouchées§e): §7" + arrowsShot + "/§c" + arrowsMissed + "§7/§a" + (arrowsShot - arrowsMissed));
		Bukkit.broadcastMessage("§6§m----------------------------------------");

		OfflinePlayer leader = PlayerFaction.getPlayersFaction().get(topFacName).getLeader();
		APIPlayer apiPlayer = APIPlayer.getPlayer(topPlayerName);
		
		if (apiPlayer.isOnline()) { //TODO
			return;
		}
		
		if (leader.isOnline()) { //TODO dragon crate
			
		}
		
		playerDamageDealt.clear();
		playerDamageTaken.clear();
		factionDamageDealt.clear();
		factionDamageTaken.clear();
		arrowsShot = 0;
		arrowsMissed = 0;
		startedGame = null;
		startedTime = 0;
	}

	@Override
	public void tick() {}
	
	@Override
	public List<String> getServerListLines() {
		return Methods.replacePlaceholder(ConfigurationService.DRAGON_SERVER_LIST_LINE, "%health%", getEntityHealthInPercentage());
	}
	
	@Override
	public void update() {}
	
	@Override
	public void sendShow(CommandSender sender) {
		boolean hasPerm = sender instanceof ConsoleCommandSender ? true : OnimaPerm.GAME_SHOW_MOD.has(sender);
		
		sender.sendMessage(ConfigurationService.STAIGHT_LINE);
		sender.sendMessage("§7Event : §d§o" + type.getName() + ' ' + super.name + " §7- Créateur : §d§o" + creator + " §7- Monde : §d§o" + (location == null ? "§cAucun" : "§a" + location.getWorld().getName()));
		if (hasPerm) {
			sender.sendMessage("§7Créé le : §d§o" + Methods.toFormatDate(created, ConfigurationService.DATE_FORMAT_HOURS));
			sender.sendMessage("§7Nom du boss bar : §r" + Methods.colors(name));
			sender.sendMessage("§7Vie max du dragon : §d§o" + health);
			if (!effects.isEmpty()) {
				sender.sendMessage("§7Effet" + (effects.size() > 1 ? "s" : "") + " de potion : ");
				for (String effect : Methods.setEffectsAsInInventory(effects, "§d§o", "§7§o"))
					sender.sendMessage(effect);
			}
			
			if (sender.hasPermission(OnimaPerm.GAME_NEXT_ARGUMENT.getPermission()) && isSchedulerEnabled() && isSchedulerSet())
				sender.sendMessage("§7Programmé pour : §d§o" + new CasualFormatDate("d hi").toNormalDate(getWhenItStarts()));
			
			if (sender instanceof Player && location != null) {
				Location location = this.location.getWorld().getHighestBlockAt(this.location).getLocation();
				
				((Player) sender).spigot().sendMessage(new ComponentBuilder("§7§oSe téléporter à l'event.")
						.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + location.getBlockX() + ' ' + location.getBlockY() + ' ' + location.getBlockZ()))
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7/tp " + location.getBlockX() + ' ' + location.getBlockY() + ' ' + location.getBlockZ()).create())).create());
			}
		}
		
		if (isStarted() && dragon != null) {
			sender.sendMessage("§7Commencé le : §d§o" + Methods.toFormatDate(startedTime, ConfigurationService.DATE_FORMAT_HOURS));
			sender.sendMessage("§7Commencé depuis : §d§o" + LongTime.setHMSFormat(System.currentTimeMillis() - startedTime));
			sender.sendMessage("§7Vie du dragon : " + getEntityHealthFormatted());
		}
		
		sender.sendMessage("§7Récompense : §d§oClé " + type.getName() + "§7§o(le nombre varie en fonction des stats.)");
		sender.sendMessage(ConfigurationService.STAIGHT_LINE);
	}
	
	public boolean spawnDragon(Location location) {
		World world = location.getWorld();

		if (ConfigurationService.ENDERDRAGON_EVENT_WORLDS.contains(location.getWorld().getName())) {
			super.location = location;
			dragon = (EnderDragon) world.spawnEntity(location, EntityType.ENDER_DRAGON);
			
			if (health != 0) {
				dragon.setHealth(health);
				dragon.setMaxHealth(health);
			}
			
			if (name != null) dragon.setCustomName(Methods.colors(name));
			dragon.addPotionEffects(effects);
			return true;
		} else
			return false;
	}
	
	public void despawnDragon() {
		if(dragon != null) dragon.remove();
	}
	
	public EnderDragon getEntity() {
		return dragon;
	}
	
	public double getEntityHealth() {
		return ((Damageable) dragon).getHealth();
	}
	
	public int getEntityHealthInPercentage() {
		return (int) (getEntityHealth() * 100 / getEntityMaxHealth());
	}
	
	public String getEntityHealthFormatted() {
		int health = getEntityHealthInPercentage();
		StringBuilder builder = new StringBuilder();
		
		if (health < 75)
			builder.append("§e");
		else if (health < 50)
			builder.append("§6");
		else if (health < 25)
			builder.append("§c");
		else if (health < 10)
			builder.append("§4");
		else
			builder.append("§a");
		
		return builder.append(health).append('%').toString();
	}
	
	public double getEntityMaxHealth() {
		return ((Damageable) dragon).getMaxHealth();
	}
	
	public void addDamageDealtToPlayer(String name, double damage) {
		if (playerDamageDealt.containsKey(name))
			playerDamageDealt.put(name, playerDamageDealt.get(name)+damage);
		else
			playerDamageDealt.put(name, damage);
	}
	
	public void addDamageDealtToFaction(String name, double damage) {
		if (factionDamageDealt.containsKey(name))
			factionDamageDealt.put(name, factionDamageDealt.get(name)+damage);
		else
			factionDamageDealt.put(name, damage);
	}
	
	public void addDamageTakenToPlayer(String name, double damage) {
		if (playerDamageTaken.containsKey(name))
			playerDamageTaken.put(name, playerDamageTaken.get(name)+damage);
		else
			playerDamageTaken.put(name, damage);
	}
	
	public void addDamageTakenToFaction(String name, double damage) {
		if (factionDamageTaken.containsKey(name))
			factionDamageTaken.put(name, factionDamageTaken.get(name)+damage);
		else
			factionDamageTaken.put(name, damage);
	}
	
	public Map<String, Double> getPlayerDamageDealt() {
		return playerDamageDealt;
	}

	public Map<String, Double> getFactionDamageDealt() {
		return factionDamageDealt;
	}

	public Map<String, Double> getPlayerDamageTaken() {
		return playerDamageTaken;
	}

	public Map<String, Double> getFactionDamageTaken() {
		return factionDamageTaken;
	}

	public int getArrowsShot() {
		return arrowsShot;
	}

	public int getArrowsMissed() {
		return arrowsMissed;
	}
	
	public void addArrowMissed() {
		arrowsMissed++;
	}
	
	public void addArrowShot() {
		arrowsShot++;
	}
	
	public double getDragonHealth() {
		return health;
	}
	
	public void setDragonHealth(double health) {
		this.health = health;
	}
	
	public String getDragonName() {
		return name;
	}
	
	public void setDragonName(String name) {
		this.name = name;
	}
	
	public List<PotionEffect> getDragonEffects() {
		return effects;
	}
	
	public Location getSpawnLocation() {
		return location;
	}
	
	public void setSpawnLocation(Location location) {
		this.location = location;
	}
	
	public boolean hasEffect(PotionEffectType type) {
		for (PotionEffect effect : effects) {
			if (effect.getType() == type)
				return true;
		}
		return false;
	}
	
	public boolean entityHasEffect(PotionEffectType type) {
		if (dragon != null) {
			for (PotionEffect effect : dragon.getActivePotionEffects()) {
				if (effect.getType() == type) return true;
			}
		}
		return false;
	}
	
	public void removeEffect(PotionEffectType type) {
		Iterator<PotionEffect> iterator = effects.iterator();
		
		while (iterator.hasNext()) {
			PotionEffectType effectType = iterator.next().getType();
			
			if (effectType == type) iterator.remove();
		}
	}
	
	public void removeEntityEffect(PotionEffectType type) {
		if (dragon != null)
			dragon.removePotionEffect(type);
	}

}
