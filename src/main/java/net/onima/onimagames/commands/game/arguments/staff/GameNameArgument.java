package net.onima.onimagames.commands.game.arguments.staff;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.JSONMessage;
import net.onima.onimaapi.utils.commands.BasicCommandArgument;
import net.onima.onimafaction.faction.Faction;
import net.onima.onimagames.game.Game;

public class GameNameArgument extends BasicCommandArgument {

	public GameNameArgument() {
		super("name", OnimaPerm.GAME_NAME_ARGUMENT, new String[] {"rename"});
		usage = new JSONMessage("§7/game " + name + " <old-name> <new-name>", "§d§oRenomme un event.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 3) {
			usage.send(sender, "§7Utilisation : ");
			return false;
		}
		
		Game game = null;
		
		if (Faction.getFaction(args[1]) != null) {
			sender.sendMessage("§cUne faction s'appelle déjà " + args[1] + ", changez de nom.");
			return false;
		}
		
		if ((game = Game.getGame(args[1])) == null) {
			sender.sendMessage("§cL'event " + args[1] + " n'existe pas !");
			return false;
		}
		
		if (Game.getGame(args[2]) != null) {
			sender.sendMessage("§cUn event s'appelle déjà " + args[2] + ", changez de nom.");
			return false;
		}
		
		
		sender.sendMessage("§d§oVous §7avez renommé l'event §d§o" + game.getName() + " §7par §d§o" + args[2]);
		game.setName(args[2]);
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length != 2)
			return Collections.emptyList();

		return Game.getGames().parallelStream().map(Game::getName).filter(name -> StringUtil.startsWithIgnoreCase(name, args[1])).collect(Collectors.toList());
	}

}
