package net.onima.onimagames.commands.game.arguments.staff;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.JSONMessage;
import net.onima.onimaapi.utils.commands.BasicCommandArgument;
import net.onima.onimagames.game.Game;

public class GameStopArgument extends BasicCommandArgument {
	
	public GameStopArgument() {
		super("stop", OnimaPerm.GAME_STOP_ARGUMENT, new String[]{"cancel"});
		usage = new JSONMessage("§7/game " + name + " <game>", "§d§oArrête un event en cours.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (Game.getStartedGame() == null) {
			sender.sendMessage("§cAucun event n'est en cours.");
			return false;
		}
		
//		Bukkit.broadcastMessage("§e" + sender.getName() + " §7a arrêté l'event §e" + Game.getStartedGame().getGameType().getName() + " §7en cours !");
		Game.getStartedGame().stop();
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> completions = new ArrayList<>();
		
		if (args.length == 2 && Game.getStartedGame() != null)
			completions.add(Game.getStartedGame().getName());
		
		return completions;
	}

}
