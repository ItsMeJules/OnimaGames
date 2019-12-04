package net.onima.onimagames.game;

import java.time.Instant;
import java.time.Month;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import net.onima.onimaapi.OnimaAPI;
import net.onima.onimaapi.crates.booster.NoBooster;
import net.onima.onimaapi.crates.openers.Key;
import net.onima.onimaapi.crates.openers.PhysicalKey;
import net.onima.onimaapi.manager.ConfigManager;
import net.onima.onimaapi.players.APIPlayer;
import net.onima.onimaapi.saver.FileSaver;
import net.onima.onimaapi.utils.Config;
import net.onima.onimaapi.utils.ConfigurationService;
import net.onima.onimaapi.utils.Methods;
import net.onima.onimaapi.utils.Scheduler;
import net.onima.onimaapi.zone.type.Region;
import net.onima.onimagames.game.citadel.Citadel;
import net.onima.onimagames.game.conquest.Conquest;
import net.onima.onimagames.game.conquest.ConquestType;
import net.onima.onimagames.game.conquest.ConquestZone;
import net.onima.onimagames.game.dragon.Dragon;
import net.onima.onimagames.game.dtc.DTC;
import net.onima.onimagames.game.koth.Koth;
import net.onima.onimagames.task.GameTask;

public abstract class Game implements FileSaver, Scheduler {
	
	protected static List<Game> games;
	protected static Game startedGame;
	protected static Config gameSerialConfig;
	protected static FileConfiguration config;
	protected static GameTask gameTask;
	
	static {
		games = new ArrayList<>();
		gameSerialConfig = ConfigManager.getGameSerialConfig();
		config = gameSerialConfig.getConfig();
	}
	
	protected GameType type;
	protected String name, creator, path;
	protected Temporal temporal;
	protected long created, startedTime, timeRestart;
	protected boolean schedulerEnabled;
	protected Key key;
	protected Region region;
	protected Location location;
	
	public Game(GameType type, String name, String creator) {
		this.type = type;
		this.name = name;
		this.creator = creator;
		path = "GAMES." + type.name() + '.' + name + '.';
		created = System.currentTimeMillis();
		key = new PhysicalKey(name, new NoBooster()); //Générer une clef au hasard parmis toutes les crates quand qqun win ?
		save();
	}

	public GameType getGameType() {
		return type;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getCreator() {
		return creator;
	}
	
	public void setCreator(String creator) {
		this.creator = creator;
	}
	
	public long getCreated() {
		return created;
	}
	
	public void setCreated(long created) {
		this.created = created;
	}
	
	public long getStartedTime() {
		return startedTime;
	}
	
	public abstract void start();
	public abstract void stop();
	public abstract void win(APIPlayer winner);
	public abstract void tick();
	public abstract boolean isReadyToStart();
	public abstract List<String> getServerListLines();
	public abstract void update();
	public abstract void sendShow(CommandSender sender);
	
	public boolean isStarted() {
		return equals(startedGame);
	}
	
	public Key getKey() {
		return key;
	}
	
	public Region getRegion() {
		return region;
	}
	
	public void setRegion(Region region) {
		this.region = region;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	@Override
	public Temporal getTemporal() {
		return temporal;
	}
	
	@Override
	public void setTemporal(Temporal temporal) {
		this.temporal = temporal;
	}
	
	@Override
	public void scheduleEvery(long timeRestart) {
		this.timeRestart = timeRestart;
	}
	
	@Override
	public long getResetTimeCycle() {
		return timeRestart;
	}
	
	@Override
	public void startTime(Month month, int day, int hour, int minute) {
		temporal = ZonedDateTime.now().withMonth(month.getValue()).withDayOfMonth(day).withHour(hour).withMinute(minute);
	}
	
	@Override
	public void action(boolean started) {
		if (started && isReadyToStart())
			start();
	}
	
	@Override
	public boolean isSchedulerEnabled() {
		return schedulerEnabled;
	}
	
	@Override
	public void setSchedulerEnabled(boolean schedulerEnabled) {
		this.schedulerEnabled = schedulerEnabled;
	}
	
	@Override
	public boolean isSchedulerSet() {
		return temporal != null;
	}
	
	@Override
	public long getStartTimeLeft() {
		if (temporal == null) return -1;
		
		return ZonedDateTime.now().until(temporal, ChronoUnit.MILLIS);
	}
	
	@Override
	public long getWhenItStarts() {
		if (temporal == null) return -1;
		
		return Instant.from(temporal).toEpochMilli();
	}
	
	@Override
	public void serialize() {
		config.set(path + "created", created);
		config.set(path + "creator", creator);
		config.set(path + "scheduler-enabled", schedulerEnabled);
		
		if (temporal != null) {
			config.set(path + "scheduler", 
				temporal.get(ChronoField.MONTH_OF_YEAR) + ';' +
				temporal.get(ChronoField.DAY_OF_MONTH) + ';' +
				temporal.get(ChronoField.HOUR_OF_DAY) + ';' +
				temporal.get(ChronoField.MINUTE_OF_HOUR));
		}
	}
	
	@Override
	public void save() {
		games.add(this);
		OnimaAPI.getScheduled().add(this);
		OnimaAPI.getShutdownSavers().add(this);
	}
	
	@Override
	public void remove() {
		games.remove(this);
		OnimaAPI.getScheduled().remove(this);
		OnimaAPI.getShutdownSavers().remove(this);
		
		region.remove();
		
		if (isStarted())
			startedGame = null;
		
		if (games.size() == 0)
			gameSerialConfig.remove("GAMES", false);
	}
	
	@Override
	public boolean isSaved() {
		return games.contains(this);
	}
	
	public void award(Player player) {
		player.sendMessage("§dVous §eavez reçu une clef pour avoir remporté l'event. Allez au spawn pour ouvrir votre crate !");
		key.give(APIPlayer.getPlayer(player), false);
	}
	
	private static void initGamesStuff(Game game, String path, int rewardSize) {
		game.setCreator(config.getString(path+"creator"));
		game.setCreated(config.getLong(path+"created"));
		game.setSchedulerEnabled(config.getBoolean(path+"scheduler-enabled"));
		
		for (Region region : Region.getRegions()) {
			if (region.getName().equalsIgnoreCase(game.name + "_area"))
				game.setRegion(region);
		}
		
		if (game.schedulerEnabled) {
			String[] scheduler = config.getString(path+"scheduler").split(";");
			game.startTime(Month.of(Methods.toInteger(scheduler[0])), Methods.toInteger(scheduler[1]), Methods.toInteger(scheduler[2]), Methods.toInteger(scheduler[3]));
		}
	}
	
	public static void deserialize() {
		ConfigurationSection kothSection = config.getConfigurationSection("GAMES.KOTH"), dtcSection = config.getConfigurationSection("GAMES.DTC"), conquestSection = config.getConfigurationSection("GAMES.CONQUEST"),
				dragonSection = config.getConfigurationSection("GAMES.DRAGON_EVENT"), citadelSection = config.getConfigurationSection("GAMES.CITADEL");
		
		int koths = 0, dtcs = 0, conquests = 0, dragons = 0, citadels = 0, games = 0;
		
		if (kothSection != null) {
			for(String name : kothSection.getKeys(false)) {
				String path = "GAMES.KOTH."+name+'.';
				Koth koth = new Koth(name);
				
				initGamesStuff(koth, path, 9);
				
				koth.setCapTime(config.getLong(path+"cap-time"));
				
				for (Region region : Region.getRegions()) {
					if (region.getName().equalsIgnoreCase(name + "_capzone"))
						koth.setCapZone(region);
				}
				
				koths++;
				games++;
			}
		}
		
		if (dtcSection != null) {
			for(String name : dtcSection.getKeys(false)) {
				String path = "GAME.DTC."+name+'.';
				DTC dtc = new DTC(name);
				
				initGamesStuff(dtc, path, 9);
				
				dtc.setPoint(config.getInt(path+"points"));
				dtc.setBlock(Methods.deserializeLocation(config.getString(path+"location"), false).getBlock());
				
				dtcs++;
				games++;
			}
		}
		
		if (conquestSection != null) {
			for(String name : conquestSection.getKeys(false)) {
				String path = "GAMES.CONQUEST."+name+'.';
				ConfigurationSection sectionZone = config.getConfigurationSection(path+"zones");
				
				Conquest conquest = new Conquest(name);

				initGamesStuff(conquest, path, 9);
				
				conquest.setPointsToWin(config.getInt(path+"points-to-win"));
				if(sectionZone != null) {
					for (String color : sectionZone.getKeys(false)) {
						String path2 = path+"zones."+color+".", zoneName = config.getString(path2+"name");
						ConquestType type = ConquestType.fromString(color.toUpperCase());
						conquest.addConquestZone(type, new ConquestZone(conquest, type, config.getInt(path2+"points-per-cap"), config.getLong(path2+"cap-time"), null, zoneName));
					}
					
					for (Region region : Region.getRegions()) {
						if (region.getName().startsWith(name + '_') && region.getName().endsWith("_color"))
							conquest.getZone(ConquestType.fromString(region.getName().split("_")[1])).setCapZone(region);
					}
				}
				conquests++;
				games++;
			}
		}
		
		if (dragonSection != null) {
			for (String name : dragonSection.getKeys(false)) {
				String path = "GAMES.DRAGON_EVENT."+name+'.';
				Dragon dragon = new Dragon(name);
				
				initGamesStuff(dragon, path, 0);
				
				dragon.getDragonEffects().addAll(Methods.deserializePotionEffects(config.getString(path+"effects")));
				dragon.setDragonHealth(config.getDouble(path+"health"));
				dragon.setDragonName(config.getString(path+"name"));
				
				dragons++;
				games++;
			}
		}
		
		if (citadelSection != null) {
			for (String name : citadelSection.getKeys(false)) {
				String path = "GAMES.CITADEL." + name + '.';
				Citadel citadel = new Citadel(name);
				
				initGamesStuff(citadel, path, 9);
				
				citadel.setCapTime(config.getLong(path+"cap-time"));
				
				for (Region region : Region.getRegions()) {
					if (region.getName().equalsIgnoreCase(name + "_capzone"))
						citadel.setCapZone(region);
				}
				
				citadels++;
				games++;
			}
		}
		
		OnimaAPI.sendConsoleMessage("§aNous avons chargé "+games+" game"+(games > 1 ? "s" : "")+" (KOTH: "+koths+") (DTC: "+dtcs+") (CONQUEST: "+conquests+") (DRAGON: "+dragons+") (CITADEL: "+citadels+")", ConfigurationService.ONIMAAPI_PREFIX);
	}
	
	public static List<Game> getGames() {
		return games;
	}

	public static Game getStartedGame() {
		return startedGame;
	}
	
	public static void setStartedGame(Game startedGame) {
		Game.startedGame = startedGame;
	}
	
	public static Game getGame(String name) {
		for (Game game : games) {
			if (game.getName().equalsIgnoreCase(name))
				return game;
		}
		return null;
	}
	
	public static Game getGameByLoc(Location location) {
		for (Game game : games) {
			if (game instanceof DTC) {
				if (Methods.locationEquals(location, ((DTC) game).getBlock().getLocation()))
					return game;
			} else if (game instanceof Koth) {
				if (((Koth) game).getRegion().toCuboid().contains(location))
					return game;
			} else if (game instanceof Conquest) {
				for (ConquestZone zone : ((Conquest) game).getZones()) {
					if (zone.getCapZone().toCuboid().contains(location))
						return game;
				}
			}
		}
		return null;
	}
	
	public static List<Entry<Game, Long>> getNextGames() {
		Map<Game, Long> games = new HashMap<>();
		
		for (Game game : Game.getGames()) {
			if (game.isSchedulerEnabled() && game.isSchedulerSet() && !game.isStarted()) 
				games.put(game, game.getStartTimeLeft());
		}
		
		return games.entrySet().stream()
				.sorted((a, b) -> Long.compare(a.getValue(), b.getValue())).collect(Collectors.toList());
	}
	
	public static Game getNextGame() {
		List<Game> games = new ArrayList<>();
		
		for (Game game : Game.getGames()) {
			if (game.isSchedulerEnabled() && game.isSchedulerSet() && !game.isStarted()) 
				games.add(game);
		}
		
		if (games.isEmpty()) return null;
		
		return games.stream()
				.sorted((a, b) -> Long.compare(a.getStartTimeLeft(), b.getStartTimeLeft())).limit(1).findFirst().orElse(null);
	}

}
