package net.onima.onimagames.commands.game.arguments.staff;

import java.time.Month;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import net.onima.onimaapi.OnimaAPI;
import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.CasualFormatDate;
import net.onima.onimaapi.utils.JSONMessage;
import net.onima.onimaapi.utils.Methods;
import net.onima.onimaapi.utils.commands.BasicCommandArgument;
import net.onima.onimagames.game.Game;

public class GameFirstScheduleArgument extends BasicCommandArgument {

	private Pattern pattern;
	
	{
		pattern = Pattern.compile("[0-9]+");
	}
	
	public GameFirstScheduleArgument() {
		super("firstschedule", OnimaPerm.GAME_FIRSTSCHEDULE_ARGUMENT, new String[] {"fs"});
		usage = new JSONMessage("§7/game " + name + " <name> <dd-mm:HH-MM>", "§d§oDéfinit le jour où l'event commencera pour la première fois. Exemple : \n §d§o- /game firstschedule KoTH 14-06:16-00.");
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
		
	    Matcher matcher = pattern.matcher(args[2]);
		Integer[] date = {0, 0, 0, 0};
	    
	    for (int i = 0; matcher.find() && i < 4; i++) {
	    	String group = matcher.group();
	    	Integer digits = Methods.toInteger(group);
	    	
	    	if (digits == null) {
	    		sender.sendMessage("§c" + group + " n'est pas un nombre !");
	    		return false;
	    	}
	    	
	    	date[i] = digits;
	    }
	    
	    game.startTime(Month.of(date[1]), date[0], date[2], date[3]);
		OnimaAPI.broadcast("§d§o" + Methods.getRealName(sender) + " §7a définit la date du premier lancement de l'event §d§o" + game.getGameType().getName() + ' ' + game.getName() + " §7pour §d§o" + new CasualFormatDate("d u z h:i").toNormalDate(game.getWhenItStarts()) + "§7.", OnimaPerm.GAME_FIRSTSCHEDULE_ARGUMENT);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return Game.getGames().parallelStream().map(Game::getName).filter(name -> StringUtil.startsWithIgnoreCase(name, args[1])).collect(Collectors.toList());
	}
	
}
