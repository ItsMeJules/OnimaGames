package net.onima.onimagames.commands.conquest.arguments.staff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.JSONMessage;
import net.onima.onimaapi.utils.Methods;
import net.onima.onimaapi.utils.commands.BasicCommandArgument;
import net.onima.onimagames.game.Game;
import net.onima.onimagames.game.GameType;
import net.onima.onimagames.game.conquest.Conquest;
import net.onima.onimagames.game.conquest.ConquestType;
import net.onima.onimagames.game.conquest.ConquestZone;

public class ConquestSetPointsPerCap extends BasicCommandArgument {

	public ConquestSetPointsPerCap() {
		super("setpointspercap", OnimaPerm.CONQUEST_SETPOINTSPERCAP_ARGUMENT);
		usage = new JSONMessage("§7/conquest " + name + " <conquest> <color> <points>", "§d§oDéfini le nombre de points par cap.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
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
		
		ConquestType type = null;
		
		if ((type = ConquestType.fromString(args[2])) == null) {
			sender.sendMessage("§cLa couleur " + args[2] + " n'existe pas !");
			return false;
		}
		
		ConquestZone zone = ((Conquest) game).getZone(type);
		
		if (zone == null) {
			sender.sendMessage("§cVous devez d'abord définir la zone " + type.getName() + " §c.");
			return false;
		}
		
		Integer points = Methods.toInteger(args[3]);
		
		if (points == null) {
			sender.sendMessage("§cLa valeur " + args[3] + " n'est pas un nombre.");
			return false;
		}
		
		zone.setPointsPerCap(points);
		sender.sendMessage("§d§oVous §7avez défini le nombre de points par cap pour " + type.getName() + " §7de la §d§o" + GameType.CONQUEST.getName() + ' ' + game.getName() + " §7sur : §d§o" + points);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 2)
			return Game.getGames().parallelStream().filter(game -> game instanceof Conquest).map(Game::getName).filter(name -> StringUtil.startsWithIgnoreCase(name, args[1])).collect(Collectors.toList());
		else if (args.length == 3) {
			List<String> completions = new ArrayList<>();
			
			for (ConquestType type : ConquestType.values()) {
				if (StringUtil.startsWithIgnoreCase(type.name(), args[2]))
					completions.add(type.name());
			}
			
			return completions;
		}
		return Collections.emptyList();
	}
	
}
