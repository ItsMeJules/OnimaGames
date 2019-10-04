package net.onima.onimagames.commands.game.arguments.staff;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.JSONMessage;
import net.onima.onimaapi.utils.Methods;
import net.onima.onimaapi.utils.commands.BasicCommandArgument;
import net.onima.onimagames.game.Game;

public class GameStartArgument extends BasicCommandArgument {

	public GameStartArgument() {
		super("start", OnimaPerm.GAME_START_ARGUMENT, new String[] {"begin"});
		usage = new JSONMessage("§7/game " + name + " <game>", "§d§oLance un event.");
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
		
		if (Game.getStartedGame() != null) {
			Methods.sendJSON(sender, new ComponentBuilder("§cUn event est déjà en cours, cliquez ici pour l'annuler ou faites /game stop.")
			.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/game stop"))
			.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§c/game stop").create())).create());
			return false;
		}
		
		if (!game.isReadyToStart()) {
			sender.sendMessage("§cL'event n'est pas bien configuré !");
			return false;
		}
		
		game.start();
		sender.sendMessage("§d§oVous §7avez lancé l'event §d§o" + game.getGameType().getName() + ' ' + game.getName() + "§7.");
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length != 2)
			return Collections.emptyList();

		return Game.getGames().parallelStream().map(Game::getName).filter(name -> StringUtil.startsWithIgnoreCase(name, args[1])).collect(Collectors.toList());
	}

}
