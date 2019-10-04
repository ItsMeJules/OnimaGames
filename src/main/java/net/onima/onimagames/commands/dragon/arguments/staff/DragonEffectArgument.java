package net.onima.onimagames.commands.dragon.arguments.staff;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.StringUtil;

import net.onima.onimaapi.rank.OnimaPerm;
import net.onima.onimaapi.utils.ConfigurationService;
import net.onima.onimaapi.utils.JSONMessage;
import net.onima.onimaapi.utils.Methods;
import net.onima.onimaapi.utils.commands.BasicCommandArgument;
import net.onima.onimaapi.utils.time.Time.IntegerTime;
import net.onima.onimagames.game.Game;
import net.onima.onimagames.game.GameType;
import net.onima.onimagames.game.dragon.Dragon;

public class DragonEffectArgument extends BasicCommandArgument {

	public DragonEffectArgument() {
		super("effect", OnimaPerm.DRAGON_EFFECT_ARGUMENT);
		usage = new JSONMessage("§7/dragon " + name + " <add | remove | check> <dragon> (effect) (duration) (amplifier)", "§d§oGère les effets du dragon.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 3) {
			usage.send(sender, "§7Utilisation : ");
			return false;
		}
		
		Game game = null;
		
		if ((game = Game.getGame(args[1])) == null) {
			sender.sendMessage("§cL'event " + args[1] + " n'existe pas !");
			return false;
		}
		
		if (!(game instanceof Dragon)) {
			sender.sendMessage("§cL'event " + game.getName() + " n'est pas un " + GameType.DRAGON_EVENT.getName() + " mais un " + game.getGameType().getName() + '.');
			return false;
		}
		
		Dragon dragon = (Dragon) game;
		
		if (args.length > 3) {
			PotionEffectType effect = PotionEffectType.getByName(args[3]);
			
			if (effect == null) {
				sender.sendMessage("§cL'effet " + args[3] + " n'existe pas.");
				return false;
			}
			
			if (args[1].equalsIgnoreCase("remove"))
				return removeArgument(dragon, sender, effect);
			else if (args[1].equalsIgnoreCase("check"))
				return checkArgument(dragon, sender);
			else
				usage.send(sender, "§7Utilisation : ");
				
			if (args.length > 5) {
				if (args[1].equalsIgnoreCase("add")) {
					Integer duration = Methods.toInteger(args[4]);
					Integer amplifier = Methods.toInteger(args[5]);
					
					if (duration == null || amplifier == null) {
						sender.sendMessage("§cLa valeur " + duration == null ? args[4] : args[5] + " n'est pas un nombre.");
						return false;
					}
					
					dragon.removeEffect(effect);
					dragon.getDragonEffects().add(new PotionEffect(effect, duration, amplifier + 1));
					sender.sendMessage("§d§oVous §7avez ajouté l'effet §d§o" + effect.getName() + ' ' + (amplifier + 1) + " §7pour §d§o" + IntegerTime.setYMDWHMSFormat(duration / 20 * 1000) + " §7à l'event §d§o" + GameType.DRAGON_EVENT.getName() + ' ' + game.getName() + "§7.");
					return true;
				} else if( args[1].equalsIgnoreCase("remove"))
					return removeArgument(dragon, sender, effect);
				else if (args[1].equalsIgnoreCase("check"))
					return checkArgument(dragon, sender);
				else
					usage.send(sender, "§7Utilisation : ");
			}
		} else if (args[1].equalsIgnoreCase("check"))
			return checkArgument(dragon, sender);
		else
			usage.send(sender, "§7Utilisation : ");
		
		return false;
	}
	
	private boolean removeArgument(Dragon dragon, CommandSender sender, PotionEffectType effect) {
		if (!dragon.hasEffect(effect)) {
			sender.sendMessage("§cL'event dragon ne contient pas l'effet §c" + effect.getName() + '.');
			return false;
		} else {
			sender.sendMessage("§d§oVous §7avez retiré l'effet §d§o " + effect.getName() + " §7à l'event §d§o" + GameType.DRAGON_EVENT.getName() + ' ' + dragon.getName() + "§7.");
			dragon.removeEffect(effect);
			return true;
		}
	}
	
	private boolean checkArgument(Dragon dragon, CommandSender sender) {
		if (dragon.getDragonEffects().isEmpty()) {
			sender.sendMessage("§cL'event " + GameType.DRAGON_EVENT.getName() + ' ' + dragon.getName() + " n'a pas d'effets.");
			return false;
		}
		
		sender.sendMessage("§7" + ConfigurationService.STAIGHT_LINE);
		for (PotionEffect effect : dragon.getDragonEffects())
			sender.sendMessage("  §7 - §d§o" + effect.getType().getName() + ' ' + (effect.getAmplifier() + 1) + " §7pour §d§o" + IntegerTime.setYMDWHMSFormat(effect.getDuration() / 20 * 1000));
		sender.sendMessage("§7" + ConfigurationService.STAIGHT_LINE);
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> completions = new ArrayList<>();
		
		if (args.length == 2) {
			if (StringUtil.startsWithIgnoreCase("add", args[1]))
				completions.add("add");
			
			if (StringUtil.startsWithIgnoreCase("remove", args[1]))
				completions.add("remove");
			
			if (StringUtil.startsWithIgnoreCase("check", args[1]))
				completions.add("check");
		} else if (args.length == 3)
			return Game.getGames().parallelStream().filter(game -> game instanceof Dragon).map(Game::getName).filter(name -> StringUtil.startsWithIgnoreCase(name, args[1])).collect(Collectors.toList());
		else if (args.length == 4) {
			Game game = Game.getGame(args[2]);
			
			if (game == null || !(game instanceof Dragon))
				return completions;
			
			List<PotionEffectType> effects = ((Dragon) game).getDragonEffects().parallelStream().map(PotionEffect::getType).collect(Collectors.toCollection(() -> new ArrayList<>(20)));
			
			if (args[1].equalsIgnoreCase("add")) {
				for (PotionEffectType type : PotionEffectType.values()) {
					if (!effects.contains(type) && StringUtil.startsWithIgnoreCase(type.getName(), args[3]))
						completions.add(type.getName());
				}
			} else if (args[1].equalsIgnoreCase("remove")) {
				for (PotionEffectType type : PotionEffectType.values()) {
					if (effects.contains(type) && StringUtil.startsWithIgnoreCase(type.getName(), args[3]))
						completions.add(type.getName());
				}
			}
		}
		
		return completions;
	}

}
