package net.onima.onimagames.commands.citadel.arguments.staff;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import net.onima.onimaapi.items.Wand;
import net.onima.onimaapi.players.APIPlayer;
import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.JSONMessage;
import net.onima.onimaapi.utils.commands.BasicCommandArgument;
import net.onima.onimaapi.zone.Cuboid;
import net.onima.onimaapi.zone.type.Region;
import net.onima.onimagames.game.Game;
import net.onima.onimagames.game.GameType;
import net.onima.onimagames.game.citadel.Citadel;

public class CitadelCapZoneArgument extends BasicCommandArgument {

	public CitadelCapZoneArgument() {
		super("capzone", OnimaPerm.CITADEL_CAPZONE_ARGUMENT);
		usage = new JSONMessage("§7/conquest " + name + " <name> <set | remove>", "§d§oDéfini la zone de cap de la " + GameType.CITADEL.getName() + '.');
		playerOnly = true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§cSeulement les joueurs peuvent définir une zone de cap.");
			return false;	
		}
		
		if (args.length < 3) {
			usage.send(sender, "§7Utilisation : ");
			return false;
		}
		
		Game game = null;
		
		if ((game = Game.getGame(args[1])) == null) {
			sender.sendMessage("§cL'event " + args[1] + " n'existe pas !");
			return false;
		}
		
		if (!(game instanceof Citadel)) {
			sender.sendMessage("§cL'event " + game.getName() + " n'est pas un " + GameType.CITADEL.getName() + " mais un " + game.getGameType().getName() + '.');
			return false;
		}
		
		Citadel citadel = (Citadel) game;
		
		if (args[2].equalsIgnoreCase("set")) {
			Wand wand = APIPlayer.getByPlayer((Player) sender).getWand();

			if (!wand.hasAllLocationsSet()) {
				sender.sendMessage("§cVous devez sélecionner une zone !");
				sender.sendMessage("  §d§oLocation §7manquante : §d§on°" + wand.getLocation1() == null ? "1" : "2");
				return false;
			}
			
			Location loc1 = wand.getLocation1();
			Location loc2 = wand.getLocation2();

			if (!Wand.validWorlds((Player) sender, loc1, loc2)) return false;
			
			Region region = new Region(game.getName() + "_capzone", game.getName() + " CapZone", sender.getName(), loc1, loc2);
			Cuboid cuboid = region.toCuboid();
			
			cuboid.expandVertical();
			sender.sendMessage("§d§oVous §7avez §d§odéfini §7la zone de cap pour la §d§o" + GameType.CITADEL.getName() + ' ' + game.getName() + "§7.");
			citadel.setCapZone(region);
			citadel.setLocation(cuboid.getCenterLocation());
			return true;
		} else if (args[2].equalsIgnoreCase("remove")) {
			sender.sendMessage("§d§oVous §7avez §d§osupprimé §7la zone pour la §d§o" + GameType.CITADEL.getName() + ' ' + game.getName() + "§7.");
			citadel.getCapZone().remove();
			citadel.setCapZone(null);
			citadel.setLocation(null);
			return true;
		}
		
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> completions = new ArrayList<>();
		
		if (args.length == 2)
			return Game.getGames().parallelStream().filter(game -> game instanceof Citadel).map(Game::getName).filter(name -> StringUtil.startsWithIgnoreCase(name, args[1])).collect(Collectors.toList());
		else if (args.length == 3) {
			if (StringUtil.startsWithIgnoreCase("remove", args[2]))
				completions.add("remove");
			if (StringUtil.startsWithIgnoreCase("set", args[2]))
				completions.add("set");
		}
		
		return completions;
	}

}
