package net.onima.onimagames.commands.game.arguments.staff;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.JSONMessage;
import net.onima.onimaapi.utils.Methods;
import net.onima.onimaapi.utils.commands.BasicCommandArgument;
import net.onima.onimagames.game.Game;
import net.onima.onimagames.game.GameType;

public class GameListArgument extends BasicCommandArgument {
	
	private static final int MAX_GAME_PER_PAGE = 10;

	public GameListArgument() {
		super("list", OnimaPerm.GAME_LIST_ARGUMENT);
		usage = new JSONMessage("§7/game " + name + " (type)", "§d§oAffiche la liste des events.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (checks(sender, args, 1, true))
			return false;
		
		GameType type = args.length > 1 ? GameType.getType(args[1]) : null;
		
		if (args.length < 3) {
			showPage(sender, 1, type);
			return true;
		}
		
		Integer page = Methods.toInteger(args[2]);
		
		if (page != null)
			showPage(sender, page, type);
		else
			showPage(sender, 1, type);
		
		return true;
	}

	private void showPage(CommandSender sender, int pageNumber, GameType type) {
		Multimap<Integer, BaseComponent[]> pages = ArrayListMultimap.create();
		int helps = 0;
		int index = 1;
		
		List<Game> games;
		
		if (type != null)
			games = Game.getGames().stream().filter(game -> game.getGameType() == type).collect(Collectors.toCollection(() -> new ArrayList<>(8)));
		else
			games = Game.getGames();
		
		for (Game game : games) {
			pages.get(index).add(new ComponentBuilder(" §7- §e" + game.getGameType() + " §7Nom : §e" + game.getName() + "")
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§a/game show " + game.getName()).create()))
					.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/game show " + game.getName())).create());
			helps++;
			
			if (helps == MAX_GAME_PER_PAGE) {
				index++;
				helps = 0;
			}
		}
		
		if (!pages.containsKey(pageNumber)) {
			sender.sendMessage("§cLa page " + pageNumber + " n'existe pas !");
			return;
		}
		
		sender.sendMessage("§7Total de §e" + games.size() + ", §7page §e" + pageNumber + '/' + pages.keySet().size());
		for (BaseComponent[] components : pages.get(pageNumber)) 
			Methods.sendJSON(sender, components);
			
	}

}
