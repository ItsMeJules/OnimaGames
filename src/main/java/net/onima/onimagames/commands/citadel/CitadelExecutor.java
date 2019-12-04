package net.onima.onimagames.commands.citadel;

import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.commands.ArgumentExecutor;
import net.onima.onimagames.commands.citadel.arguments.staff.CitadelCapZoneArgument;
import net.onima.onimagames.commands.citadel.arguments.staff.CitadelSetCapTimeArgument;

public class CitadelExecutor extends ArgumentExecutor {

	public CitadelExecutor() {
		super("citadel", OnimaPerm.CITADEL_COMMAND);
		
		addArgument(new CitadelCapZoneArgument());
		addArgument(new CitadelSetCapTimeArgument());
	}

}
