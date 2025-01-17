package net.onima.onimagames.commands.koth;

import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.commands.ArgumentExecutor;
import net.onima.onimagames.commands.koth.arguments.staff.KothCapZoneArgument;
import net.onima.onimagames.commands.koth.arguments.staff.KothSetCapTimeArgument;

public class KothExecutor extends ArgumentExecutor {

	public KothExecutor() {
		super("koth", OnimaPerm.KOTH_COMMAND);
		
		addArgument(new KothCapZoneArgument());
		addArgument(new KothSetCapTimeArgument());
	}

}
