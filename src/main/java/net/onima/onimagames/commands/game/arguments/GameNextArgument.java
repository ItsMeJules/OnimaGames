package net.onima.onimagames.commands.game.arguments;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.JSONMessage;
import net.onima.onimaapi.utils.Methods;
import net.onima.onimaapi.utils.commands.BasicCommandArgument;
import net.onima.onimaapi.utils.time.Time.LongTime;
import net.onima.onimagames.game.Game;

public class GameNextArgument extends BasicCommandArgument {

	public GameNextArgument() {
		super("next", OnimaPerm.GAME_NEXT_ARGUMENT);
		usage = new JSONMessage("§7/game " + name, "§d§oAffiche tous les events programmés.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		List<Entry<Game, Long>> nextGames = Game.getNextGames();
		
		if (nextGames == null || nextGames.isEmpty()) {
			sender.sendMessage("§cAucun event n'est programmé.");
			return false;
		}
		
		sender.sendMessage("§7Liste de tous les §d§oevents §7programmés :");
		for (Entry<Game, Long> entry : nextGames) {
			Game game = entry.getKey();
			
			Methods.sendJSON(sender, new ComponentBuilder("§7[" + game.getGameType().getName() + "] " + game.getName() + " - §d§o" + LongTime.setYMDWHMSFormat(entry.getValue()))
					.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/game show " + game.getName()))
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7§o/game show " + game.getName()).create())).create());
		}
		return true;
	}

	
	
}
