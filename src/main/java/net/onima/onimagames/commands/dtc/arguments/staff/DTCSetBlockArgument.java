package net.onima.onimagames.commands.dtc.arguments.staff;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import net.onima.onimaapi.items.Wand;
import net.onima.onimaapi.players.APIPlayer;
import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.JSONMessage;
import net.onima.onimaapi.utils.commands.BasicCommandArgument;
import net.onima.onimagames.game.Game;
import net.onima.onimagames.game.GameType;
import net.onima.onimagames.game.dtc.DTC;

public class DTCSetBlockArgument extends BasicCommandArgument {

	public DTCSetBlockArgument() {
		super("setblock", OnimaPerm.DTC_SETBLOCK_ARGUMENT);
		usage = new JSONMessage("§7/dtc " + name + " <DTC>", "§d§oDéfini le block d'un " + GameType.DTC.getName() + '.');
		playerOnly = true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§cSeulement les joueurs peuvent définir une zone de cap.");
			return false;	
		}
		
		if (args.length < 2) {
			usage.send(sender, "§7Utilisation : ");
			return false;
		}
		
		Game game = null;
		
		if ((game = Game.getGame(args[1])) == null) {
			sender.sendMessage("§cL'event " + args[1] + " n'existe pas !");
			return false;
		}
		
		if (!(game instanceof DTC)) {
			sender.sendMessage("§cL'event " + game.getName() + " n'est pas un " + GameType.DTC.getName() + " mais un " + game.getGameType().getName() + '.');
			return false;
		}
		
		DTC dtc = (DTC) game;
		Wand wand = APIPlayer.getByPlayer((Player) sender).getWand();
		
		if (wand.getLocation1() == null && wand.getLocation2() == null) {
			sender.sendMessage("§cVous devez sélectionner un block avec la wand !");
			return false;
		}
		
		dtc.setBlock(wand.getLocation1() == null ? wand.getLocation2().getBlock() : wand.getLocation1().getBlock());
		Block block = dtc.getBlock();
		
		game.setLocation(block.getLocation());
		sender.sendMessage("§d§oVous §7avez défini le block du §d§o" + GameType.DTC.getName() + ' ' + game.getName() + " §7par un §d§o" + block.getType().name());
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return Game.getGames().parallelStream().filter(game -> game instanceof DTC).map(Game::getName).filter(name -> StringUtil.startsWithIgnoreCase(name, args[1])).collect(Collectors.toList());
	}

}
