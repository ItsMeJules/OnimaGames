package net.onima.onimagames.commands.game;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.commands.ArgumentExecutor;
import net.onima.onimaapi.utils.commands.BasicCommandArgument;
import net.onima.onimagames.commands.game.arguments.GameHelpArgument;
import net.onima.onimagames.commands.game.arguments.GameNextArgument;
import net.onima.onimagames.commands.game.arguments.GameShowArgument;
import net.onima.onimagames.commands.game.arguments.staff.GameAreaArgument;
import net.onima.onimagames.commands.game.arguments.staff.GameCreateArgument;
import net.onima.onimagames.commands.game.arguments.staff.GameFirstScheduleArgument;
import net.onima.onimagames.commands.game.arguments.staff.GameListArgument;
import net.onima.onimagames.commands.game.arguments.staff.GameNameArgument;
import net.onima.onimagames.commands.game.arguments.staff.GameRemoveArgument;
import net.onima.onimagames.commands.game.arguments.staff.GameScheduleArgument;
import net.onima.onimagames.commands.game.arguments.staff.GameSchedulerEnableArgument;
import net.onima.onimagames.commands.game.arguments.staff.GameStartArgument;
import net.onima.onimagames.commands.game.arguments.staff.GameStopArgument;
import net.onima.onimagames.commands.game.arguments.staff.GameUptimeArgument;

public class GameExecutor extends ArgumentExecutor {
	
	private GameHelpArgument helpArgument;

	public GameExecutor() {
		super("game");
		
		addArgument(new GameAreaArgument());
		addArgument(new GameCreateArgument());
		addArgument(new GameFirstScheduleArgument());
		addArgument(helpArgument = new GameHelpArgument(this));
		addArgument(new GameListArgument());
		addArgument(new GameNameArgument());
		addArgument(new GameNextArgument());
		addArgument(new GameRemoveArgument());
		addArgument(new GameScheduleArgument());
		addArgument(new GameSchedulerEnableArgument());
		addArgument(new GameShowArgument());
		addArgument(new GameStartArgument());
		addArgument(new GameStopArgument());
		addArgument(new GameUptimeArgument());
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) {
			helpArgument.onCommand(sender, cmd, label, args);
			return true;
		}

		BasicCommandArgument argument = (BasicCommandArgument) getArgument(args[0]);
		if (argument != null) {
			OnimaPerm permission = argument.getPermission();

			if (permission == null || sender.hasPermission(permission.getPermission()))
				return argument.onCommand(sender, cmd, label, args);
		}

		return helpArgument.onCommand(sender, cmd, label, args);
	}
	
}
