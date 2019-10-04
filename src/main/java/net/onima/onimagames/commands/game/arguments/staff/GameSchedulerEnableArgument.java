package net.onima.onimagames.commands.game.arguments.staff;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.onima.onimaapi.OnimaAPI;
import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.JSONMessage;
import net.onima.onimaapi.utils.commands.BasicCommandArgument;
import net.onima.onimagames.game.Game;

public class GameSchedulerEnableArgument extends BasicCommandArgument {

	public GameSchedulerEnableArgument() {
		super("enablescheduler", OnimaPerm.GAME_ENABLESCHEDULER_ARGUMENT);
		usage = new JSONMessage("§7/game " + name + " <game>", "§d§oActive/Désactive un scheduler.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 2) {
			usage.send(sender, "§7Utilisation : ");
			return false;
		}
		
		Game game = null;
		
		if ((game = Game.getGame(args[1])) == null) {
			sender.sendMessage("§cL'event " + args[1] + " n'existe pas !");
			return false;
		}
		
		game.setSchedulerEnabled(!game.isSchedulerEnabled());
		OnimaAPI.broadcast("§d§o" + sender.getName() + " §7a " + (game.isSchedulerEnabled() ? "§aactivé" : "§cdésactivé") + "§7le scheduler de l'event §d§o" + game.getGameType().getName() + ' ' + game.getName() + "§7.", OnimaPerm.GAME_ENABLESCHEDULER_ARGUMENT);
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length != 2) 
			return Collections.emptyList();
		
		return Game.getGames().parallelStream().map(Game::getName).collect(Collectors.toList());
	}
	
}
