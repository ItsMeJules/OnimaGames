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
import net.onima.onimaapi.utils.commands.BasicCommandArgument;
import net.onima.onimaapi.utils.time.Time.LongTime;
import net.onima.onimaapi.utils.time.TimeUtils;
import net.onima.onimagames.game.Game;
import net.onima.onimagames.game.GameType;
import net.onima.onimagames.game.conquest.Conquest;
import net.onima.onimagames.game.conquest.ConquestType;
import net.onima.onimagames.game.conquest.ConquestZone;

public class ConquestSetCapTimeArgument extends BasicCommandArgument {

	public ConquestSetCapTimeArgument() {
		super("setcaptime", OnimaPerm.CONQUEST_SETCAPTIME_ARGUMENT);
		usage = new JSONMessage("§7/conquest " + name + " <conquest> <color> <time>", "§d§oDéfini le temps de cap de la " + GameType.CONQUEST.getName() + '.');
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
		
		long time = TimeUtils.timeToMillis(args[3]);
		
		if (time == -1) {
			sender.sendMessage("§cLa valeur " + args[3] + " n'est pas un nombre !");
			return false;
		} else if (time == -2) {
			sender.sendMessage("§cMauvais format pour : " + args[3] + " il faut écrire les deux premières lettres du temps. Exemple : §o/koth setcaptime §c15mi pour 15 minutes.");
			return false; 
		}
		
		zone.setCapTime(time);
		sender.sendMessage("§d§oVous §7avez défini le temps de cap pour " + type.getName() + " §7de la §d§o" + GameType.CONQUEST.getName() + ' ' + game.getName() + " §7sur : §d§o" + LongTime.setYMDWHMSFormat(time));
		return false;
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
