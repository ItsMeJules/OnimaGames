package net.onima.onimagames.commands.game.arguments.staff;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.JSONMessage;
import net.onima.onimaapi.utils.commands.BasicCommandArgument;
import net.onima.onimagames.game.Game;

public class GameRemoveArgument extends BasicCommandArgument {

	public GameRemoveArgument() {
		super("remove", OnimaPerm.GAME_REMOVE_ARGUMENT, new String[] {"delete", "del"});
		usage = new JSONMessage("§7/game " + name + " <name>", "§d§oSupprime un event.");
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
		
		game.remove();
		sender.sendMessage("§eVous §7avez supprimé l'event §e" + game.getName() + "§7.");
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return Game.getGames().parallelStream().map(Game::getName).filter(name -> StringUtil.startsWithIgnoreCase(name, args[1])).collect(Collectors.toList());
	}

}
