package net.onima.onimagames.commands.dragon;

import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.commands.ArgumentExecutor;
import net.onima.onimagames.commands.dragon.arguments.staff.DragonEffectArgument;
import net.onima.onimagames.commands.dragon.arguments.staff.DragonHealthArgument;
import net.onima.onimagames.commands.dragon.arguments.staff.DragonNameArgument;
import net.onima.onimagames.commands.dragon.arguments.staff.DragonSpawnLocationArgument;

public class DragonExecutor extends ArgumentExecutor {
	
	public DragonExecutor() {
		super("dragon", OnimaPerm.DRAGON_COMMAND);
		
		addArgument(new DragonEffectArgument());
		addArgument(new DragonHealthArgument());
		addArgument(new DragonNameArgument());
		addArgument(new DragonSpawnLocationArgument());
	}

}
