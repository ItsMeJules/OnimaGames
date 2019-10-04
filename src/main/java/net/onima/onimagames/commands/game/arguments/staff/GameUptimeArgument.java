package net.onima.onimagames.commands.game.arguments.staff;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.ConfigurationService;
import net.onima.onimaapi.utils.JSONMessage;
import net.onima.onimaapi.utils.Methods;
import net.onima.onimaapi.utils.commands.BasicCommandArgument;
import net.onima.onimaapi.utils.time.Time.LongTime;
import net.onima.onimagames.game.Game;

public class GameUptimeArgument extends BasicCommandArgument {

	public GameUptimeArgument() {
		super("uptime", OnimaPerm.GAME_UPTIME_ARGUMENT);
		usage = new JSONMessage("§7/game " + name, "§d§oAffiche la durée de l'event.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (Game.getStartedGame() == null) {
			sender.sendMessage("§cAucun event n'est en cours.");
			return false;
		}
		
		Game started = Game.getStartedGame();
		sender.sendMessage("§7L'event §d§o" + started.getGameType().getName() + ' ' + started.getName() + " §7est commencé depuis §d§o" + LongTime.setYMDWHMSFormat(System.currentTimeMillis() - started.getStartedTime()) + ", §7il a été lancé le §d§o" + Methods.toFormatDate(started.getStartedTime(), ConfigurationService.DATE_FORMAT_HOURS + "§7."));
		return false;
	}
	
}
