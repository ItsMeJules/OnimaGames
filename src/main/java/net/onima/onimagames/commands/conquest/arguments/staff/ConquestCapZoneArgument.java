package net.onima.onimagames.commands.conquest.arguments.staff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import net.onima.onimaapi.items.Wand;
import net.onima.onimaapi.players.APIPlayer;
import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.JSONMessage;
import net.onima.onimaapi.utils.commands.BasicCommandArgument;
import net.onima.onimaapi.zone.Cuboid;
import net.onima.onimagames.game.Game;
import net.onima.onimagames.game.GameType;
import net.onima.onimagames.game.conquest.Conquest;
import net.onima.onimagames.game.conquest.ConquestType;
import net.onima.onimagames.game.conquest.ConquestZone;

public class ConquestCapZoneArgument extends BasicCommandArgument {

	public ConquestCapZoneArgument() {
		super("capzone", OnimaPerm.CONQUEST_CAPZONE_ARGUMENT);
		usage = new JSONMessage("§7/conquest " + name + " <conquest> <color> <set | remove>", "§d§oDéfini la capzone d'une zone de " + GameType.CONQUEST.getName() + '.');
		playerOnly = true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§cSeulement les joueurs peuvent définir une zone de cap.");
			return false;	
		}
		
		if (args.length < 4) {
			usage.send(sender, "§7Utilisation : ");
			return false;
		}
		
		Game game = null;
		
		if ((game = Game.getGame(args[1])) == null) {
			sender.sendMessage("§cL'event " + args[1] + " n'existe pas !");
			return false;
		}
		
		if (!(game instanceof Conquest)) {
			sender.sendMessage("§cL'event " + game.getName() + " n'est pas une " + GameType.CONQUEST.getName() + " mais un " + game.getGameType().getName() + '.');
			return false;
		}
		
		Conquest conquest = (Conquest) game;
		ConquestType type = null;
		
		if ((type = ConquestType.fromString(args[2])) == null) {
			sender.sendMessage("§cLa couleur " + args[2] + " n'existe pas !");
			return false;
		}
		
		if (args[3].equalsIgnoreCase("set")) {
			Wand wand = APIPlayer.getPlayer((Player) sender).getWand();

			if (!wand.hasAllLocationsSet()) {
				sender.sendMessage("§cVous devez sélecionner une zone !");
				sender.sendMessage("  §d§oLocation §7manquante : §d§on°" + wand.getLocation1() == null ? "1" : "2");
				return false;
			}
			
			Location loc1 = wand.getLocation1();
			Location loc2 = wand.getLocation2();

			if (!Wand.validWorlds((Player) sender, loc1, loc2)) return false;
			
			Cuboid cuboid = new Cuboid(loc1, loc2, true);
			ConquestZone zone = new ConquestZone(conquest, type, label);
			
			zone.setCapZone(cuboid);
			conquest.addConquestZone(type, new ConquestZone(conquest, type, label));
			sender.sendMessage("§d§oVous §7avez défini la zone de cap " + type.getName() + " §7du §d§o" + GameType.CONQUEST.getName() + ' ' + game.getName() + "§7.");
			return true;
		} else if (args[3].equalsIgnoreCase("remove")) {
			if (conquest.getZone(type).getCapZone() == null) {
				sender.sendMessage("§c" + type.getName() + " n'a pas de zone de cap.");
				return false;
			}
			
			conquest.removeZone(type);
			sender.sendMessage("§d§oVous §7avez §d§osupprimé §7la zone de cap " + type.getName() + " §7du §d§o" + GameType.CONQUEST.getName() + ' ' + game.getName() + "§7.");
			return true;
		}
		
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 2)
			return Game.getGames().parallelStream().filter(game -> game instanceof Conquest).map(Game::getName).filter(name -> StringUtil.startsWithIgnoreCase(name, args[1])).collect(Collectors.toList());
		else if (args.length > 2) {
			List<String> completions = new ArrayList<>();
			
			if (args.length == 3) {
				for (ConquestType type : ConquestType.values()) {
					if (StringUtil.startsWithIgnoreCase(type.name(), args[2]))
						completions.add(type.name());
				}
			} else if (args.length == 4) {
				if (StringUtil.startsWithIgnoreCase("remove", args[2]))
					completions.add("remove");
				else if (StringUtil.startsWithIgnoreCase("set", args[2]))
					completions.add("set");
			}
			
			return completions;
		}
		
		return Collections.emptyList();
	}

}
