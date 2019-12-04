package net.onima.onimagames.commands.game.arguments.staff;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import net.onima.onimaapi.OnimaAPI;
import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.JSONMessage;
import net.onima.onimaapi.utils.Methods;
import net.onima.onimaapi.utils.commands.BasicCommandArgument;
import net.onima.onimaapi.utils.time.Time.LongTime;
import net.onima.onimaapi.utils.time.TimeUtils;
import net.onima.onimagames.game.Game;

public class GameScheduleArgument extends BasicCommandArgument {

	public GameScheduleArgument() {
		super("schedule", OnimaPerm.GAME_SCHEDULE_ARGUMENT);
		usage = new JSONMessage("§7/game " + name + " <name> <1we | 1da | 1mo>", "§d§oProgramme un event. Exemple : \n - /game schedule KoTH 1we.");
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
		
		long time = TimeUtils.timeToMillis(args[2]);
		
		if (time == -1) {
			sender.sendMessage("§cLa valeur " + args[2] + " n'est pas un nombre !");
			return false;
		} else if (time == -2) {
			sender.sendMessage("§cMauvais format pour : " + args[2] + " il faut écrire les deux premières lettres du temps. Exemple : §o/game schedule §c1we pour toutes les semaines.");
			return false; 
		}
		
		game.scheduleEvery(time);
		OnimaAPI.broadcast("§d§o" + Methods.getRealName(sender) + " §7a programmé l'event §d§o" + game.getGameType().getName() + ' ' + game.getName() + " §7pour chaque §d§o" + LongTime.setYMDWHMSFormat(time) + "§7.", OnimaPerm.GAME_SCHEDULE_ARGUMENT);
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 2)
			return Game.getGames().parallelStream().map(Game::getName).filter(name -> StringUtil.startsWithIgnoreCase(name, args[1])).collect(Collectors.toList());
			
		return Collections.emptyList();
	}

}
