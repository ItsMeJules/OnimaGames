package net.onima.onimagames.commands.dtc.arguments.staff;

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
import net.onima.onimagames.game.dtc.DTC;

public class DTCSetPointsArgument extends BasicCommandArgument {

	public DTCSetPointsArgument() {
		super("setpoints", OnimaPerm.DTC_SETPOINTS_ARGUMENTS);
		usage = new JSONMessage("§7/dtc " + name + " <DTC> <points>", "§d§oDéfini le nombre de break pour le " + GameType.DTC.getName());
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
		
		if (!(game instanceof DTC)) {
			sender.sendMessage("§cL'event " + game.getName() + " n'est pas un " + GameType.DTC.getName() + " mais un " + game.getGameType().getName() + '.');
			return false;
		}
		
		Integer points = Methods.toInteger(args[2]);
		
		if (points == null) {
			sender.sendMessage("§cLa valeur " + args[2] + " n'est pas un nombre.");
			return false;
		}
		
		((DTC) game).setPoint(points);
		sender.sendMessage("§d§oVous §7avez défini le nombre de break du §d§o" + GameType.DTC.getName() + ' ' + game.getName() + " §7sur : §d§o" + points);
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 2)
			return Game.getGames().parallelStream().filter(game -> game instanceof DTC).map(Game::getName).filter(name -> StringUtil.startsWithIgnoreCase(name, args[1])).collect(Collectors.toList());
		else return Collections.emptyList();	
	}

}
