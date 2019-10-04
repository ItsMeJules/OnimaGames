package net.onima.onimagames.commands.citadel.arguments.staff;

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
import net.onima.onimaapi.zone.type.utils.Capable;
import net.onima.onimagames.game.Game;
import net.onima.onimagames.game.GameType;
import net.onima.onimagames.game.citadel.Citadel;

public class CitadelSetCapTimeArgument extends BasicCommandArgument {

	public CitadelSetCapTimeArgument() {
		super("setcaptime", OnimaPerm.CITADEL_SETCAPTIME_ARGUMENT);
		usage = new JSONMessage("§7/citadel " + name + " <name> <time>", "§d§oDéfini le temps de cap du " + GameType.CITADEL.getName() + '.');
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 3) {
			usage.send(sender, "§7Utilisation : ");
			return false;
		}
		
		Game game = null;
		
		if ((game = Game.getGame(args[1])) == null) {
			sender.sendMessage("§cL'event " + args[1] + " n'existe pas !");
			return false;
		}
		
		if (!(game instanceof Citadel)) {
			sender.sendMessage("§cL'event " + game.getName() + " n'est pas un " + GameType.CITADEL.getName() + " mais un " + game.getGameType().getName() + '.');
			return false;
		}
		
		long time = TimeUtils.timeToMillis(args[2]);
		
		if (time == -1) {
			sender.sendMessage("§cLa valeur " + args[2] + " n'est pas un nombre !");
			return false;
		} else if (time == -2) {
			sender.sendMessage("§cMauvais format pour : " + args[2] + " il faut écrire les deux premières lettres du temps. Exemple : §o/koth setcaptime §c15mi pour 15 minutes.");
			return false; 
		}
		
		((Capable) game).setCapTime(time);
		sender.sendMessage("§d§oVous §7avez défini le temps de cap pour la §d§o" + GameType.CITADEL.getName() + ' ' + game.getName() + " §7sur : §d§o" + LongTime.setYMDWHMSFormat(time));
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 2)
			return Game.getGames().parallelStream().filter(game -> game instanceof Citadel).map(Game::getName).filter(name -> StringUtil.startsWithIgnoreCase(name, args[1])).collect(Collectors.toList());
		else return Collections.emptyList();
	}

}
