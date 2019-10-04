package net.onima.onimagames.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import net.onima.onimagames.game.Game;

public class GameMotdListener implements Listener {

	@EventHandler
	public void onServerPing(ServerListPingEvent event) {
		if (Game.getStartedGame() != null) {
			Game game = Game.getStartedGame();
			StringBuilder motd = new StringBuilder();
			
			for (String line : game.getServerListLines())
				motd.append(line).append('\n');
			
			event.setMotd(Bukkit.getMotd().split("\n")[0] + '\n' + motd);
		}
	}
	
}
