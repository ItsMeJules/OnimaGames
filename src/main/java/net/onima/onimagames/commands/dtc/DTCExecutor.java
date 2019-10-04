package net.onima.onimagames.commands.dtc;

import net.onima.onimaapi.utils.commands.ArgumentExecutor;
import net.onima.onimagames.commands.dtc.arguments.staff.DTCSetBlockArgument;
import net.onima.onimagames.commands.dtc.arguments.staff.DTCSetPointsArgument;

public class DTCExecutor extends ArgumentExecutor {

	public DTCExecutor() {
		super("dtc");
	
		addArgument(new DTCSetBlockArgument());
		addArgument(new DTCSetPointsArgument());
	}

}
