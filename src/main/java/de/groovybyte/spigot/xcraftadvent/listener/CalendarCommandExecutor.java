package de.groovybyte.spigot.xcraftadvent.listener;

import de.groovybyte.spigot.xcraftadvent.IServiceProvider;
import de.groovybyte.spigot.xcraftadvent.Messages;
import de.groovybyte.spigot.xcraftadvent.Permissions;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CalendarCommandExecutor implements CommandExecutor {

	private final Logger logger;

	public CalendarCommandExecutor(IServiceProvider inject) {
		this.logger = inject.getService(Logger.class);
	}

//    public BukkitTask runAsync(Runnable task) {
//        return getServer().getScheduler().runTaskAsynchronously(this, task);
//    }
//    
//    public BukkitTask runSynced(Runnable task) {
//        return getServer().getScheduler().runTask(this, task);
//    }
//	
//	public <T> CompletableFuture<T> executeAsync(Supplier<T> task) {
//		CompletableFuture<T>.
//	}
	
	protected boolean opCommand(Player p, String[] args) {
		if (args[0].equalsIgnoreCase("edit")) {
			if (args.length == 1) {
				runSynced(() -> this.man.EDITOR.startEditing(p));
			} else {
				try {
					int day = Integer.parseInt(args[1]);
					if (day < 1 || day > 24) {
						throw new NumberFormatException("not a day from the calendar");
					}
					switch (args[2].toLowerCase()) {
						case "clear":
							this.man.EDITOR.clearCommands(day);
							break;
						case "add":
							this.man.EDITOR.addCommand(day, Arrays.asList(args).subList(3, args.length).stream().collect(Collectors.joining(" ")));
							break;
						case "test":
							this.man.getDoor(day).reward(p);
							break;
						default:
							throw new NumberFormatException("wrong usage");
					}
				} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
					this.message(p, "Error: " + e.getMessage());
					this.message(p, "use \"edit [day] clear/add/test [command]\"");
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private void reloadCalendarSettings() {

	}
	
	private void openCalendarView() {
		
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("reload")) {
				reloadCalendarSettings();
				return true;
			}
		}
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length > 0 && player.isOp()) {
				if (this.opCommand(player, args)) {
					return true;
				}
			}

			if (player.hasPermission(Permissions.OWN_AND_SHOW_CALENDAR)) {
				if (!man.isActive(player)) {
					this.message(player, Messages.MSG_WRONG_TIME);
				} else if (player.getGameMode().equals(GameMode.SURVIVAL)) {
					runSynced(() -> player.openInventory(man.get(player).getCalendar()));
				} else {
					this.message(player, Messages.MSG_WRONG_GAMEMODE);
				}
			} else {
				this.message(player, Messages.MSG_MISSING_PERM);
			}
			return true;
		} else {
			sender.sendMessage("not supported");
		}
		return false;
	}
}
