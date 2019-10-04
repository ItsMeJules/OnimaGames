package net.onima.onimagames.commands.dragon.arguments.staff;

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
import net.onima.onimagames.game.dragon.Dragon;

public class DragonNameArgument extends BasicCommandArgument {

	public DragonNameArgument() {
		super("name", OnimaPerm.DRAGON_NAME_ARGUMENT, new String[] {"setname"});
		usage = new JSONMessage("§7/dragon " + name + " <dragon> <name>", "§d§oDéfini le nom de la boss bar.");
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
		
		if (!(game instanceof Dragon)) {
			sender.sendMessage("§cL'event " + game.getName() + " n'est pas un " + GameType.DRAGON_EVENT.getName() + " mais un " + game.getGameType().getName() + '.');
			return false;
		}
		
		Dragon dragon = (Dragon) game;
		String name = args[2];
		
		dragon.setDragonName(name);
		sender.sendMessage("§7Le nom du §d§odragon §7est maintenant §r " + Methods.colors(name));
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 2)
			return Game.getGames().parallelStream().filter(game -> game instanceof Dragon).map(Game::getName).filter(name -> StringUtil.startsWithIgnoreCase(name, args[1])).collect(Collectors.toList());
		else return Collections.emptyList();
	}

}
