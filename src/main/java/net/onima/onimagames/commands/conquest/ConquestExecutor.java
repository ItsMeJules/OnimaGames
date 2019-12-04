package net.onima.onimagames.commands.conquest;

import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.commands.ArgumentExecutor;
import net.onima.onimagames.commands.conquest.arguments.staff.ConquestCapZoneArgument;
import net.onima.onimagames.commands.conquest.arguments.staff.ConquestSetCapTimeArgument;
import net.onima.onimagames.commands.conquest.arguments.staff.ConquestSetPointsPerCap;
import net.onima.onimagames.commands.conquest.arguments.staff.ConquestSetPointsToWinArgument;

public class ConquestExecutor extends ArgumentExecutor {

	public ConquestExecutor() {
		super("conquest", OnimaPerm.CONQUEST_COMMAND);
		
		addArgument(new ConquestCapZoneArgument());
		addArgument(new ConquestSetCapTimeArgument());
		addArgument(new ConquestSetPointsPerCap());
		addArgument(new ConquestSetPointsToWinArgument());
	}

}
