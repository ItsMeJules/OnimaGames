package net.onima.onimagames.task;

import org.bukkit.scheduler.BukkitRunnable;

import net.onima.onimagames.game.Game;

public class GameTask extends BukkitRunnable {

	@Override
	public void run() {
//		if (Game.getStartedGame() != null)
			Game.getStartedGame().tick();
	}
	
}
