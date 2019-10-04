package net.onima.onimagames.commands.dragon.arguments.staff;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.ConfigurationService;
import net.onima.onimaapi.utils.JSONMessage;
import net.onima.onimaapi.utils.commands.BasicCommandArgument;
import net.onima.onimagames.game.Game;
import net.onima.onimagames.game.GameType;
import net.onima.onimagames.game.dragon.Dragon;

public class DragonSpawnLocationArgument extends BasicCommandArgument {

	public DragonSpawnLocationArgument() {
		super("spawnlocation", OnimaPerm.DRAGON_SPAWNLOCATION_ARGUMENT);
		usage = new JSONMessage("§7/dragon " + name + " <dragon>", "§d§oDéfini la location de spawn.");
		playerOnly = true;
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
		
		if (!(game instanceof Dragon)) {
			sender.sendMessage("§cL'event " + game.getName() + " n'est pas un " + GameType.DRAGON_EVENT.getName() + " mais un " + game.getGameType().getName() + '.');
			return false;
		}
		
		Location location = ((Player) sender).getLocation();
		
		if (!ConfigurationService.ENDERDRAGON_EVENT_WORLDS.contains(location.getWorld().getName())) {
			sender.sendMessage("§cLe dragon ne peut pas spawn dans ce monde !");
			return false;
		}
		
		((Dragon) game).setLocation(location);
		sender.sendMessage("§d§oVous §7avez défini la location de spawn du dragon en §d§o" + location.getBlockX() + ' ' + location.getBlockY() + ' ' + location.getBlockZ() + "§7.");
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return Game.getGames().parallelStream().filter(game -> game instanceof Dragon).map(Game::getName).filter(name -> StringUtil.startsWithIgnoreCase(name, args[1])).collect(Collectors.toList());
	}

}
