package de.groovybyte.spigot.xcraftadvent.listener;

import de.groovybyte.spigot.xcraftadvent.CalendarManager;
import de.groovybyte.spigot.xcraftadvent.Messages;
import de.groovybyte.spigot.xcraftadvent.Permissions;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CalendarCommandExecutor implements CommandExecutor {

	protected final CalendarManager calendarManager;
	protected final JavaPlugin plugin;

	public CalendarCommandExecutor(CalendarManager calendarManager, JavaPlugin plugin) {
		this.calendarManager = calendarManager;
		this.plugin = plugin;
	}

	/**
	 * Schedules the task for the next server tick.
	 */
	protected void runSynced(Runnable task) {
		this.plugin.getServer().getScheduler().runTask(this.plugin, task);
	}

	/**
	 * Sends the player a chat message with the standard plugin prefix.
	 */
	protected void message(Player player, String text) {
		Messages.send(player, text);
	}

	protected boolean opCommand(Player player, String[] args) {
		if (args[0].equalsIgnoreCase("edit")) {
			if (args.length == 1) {
				runSynced(() -> this.calendarManager.EDITOR.startEditing(player));
			} else {
				try {
					int day = Integer.parseInt(args[1]);
					if (day < 1 || day > 24) {
						throw new NumberFormatException("not a day from the calendar");
					}
					switch (args[2].toLowerCase()) {
						case "clear":
							this.calendarManager.EDITOR.clearCommands(day);
							break;
						case "add":
							this.calendarManager.EDITOR.addCommand(day, Arrays.asList(args).subList(3, args.length).stream().collect(Collectors.joining(" ")));
							break;
						case "test":
							this.calendarManager.getDoor(day).reward(player);
							break;
						default:
							throw new NumberFormatException("wrong usage");
					}
				} catch (ArrayIndexOutOfBoundsException | NumberFormatException exception) {
					this.message(player, "Error: " + exception.getMessage());
					this.message(player, "use \"edit [day] clear/add/test [command]\"");
					return false;
				}
			}
			return true;
		} else if (args[0].equalsIgnoreCase("addlocation")) {
			// Adds the block the player is targeting as a physical door location
			if (args.length < 2) {
				this.message(player, "Error: missing day");
				this.message(player, "use \"addlocation <day>\" (target a block first)");
				return false;
			}
			try {
				int day = Integer.parseInt(args[1]);
				if (day < 1 || day > 24) {
					throw new NumberFormatException("not a day from the calendar");
				}
				Block targetBlock = player.getTargetBlockExact(5);
				if (targetBlock == null) {
					this.message(player, "Error: could not find target block, look at a block and try again");
					return false;
				}
				this.calendarManager.EDITOR.addDoorInventoryHolder(day, targetBlock.getLocation());
				this.message(player, "Added location for door " + day + " at " + targetBlock.getLocation());
			} catch (NumberFormatException exception) {
				this.message(player, "Error: " + exception.getMessage());
				this.message(player, "use \"addlocation <day>\"");
				return false;
			}
			return true;
		} else if (args[0].equalsIgnoreCase("clearlocations")) {
			// Clears all physical door locations for a given day
			if (args.length < 2) {
				this.message(player, "Error: missing day");
				this.message(player, "use \"clearlocations <day>\"");
				return false;
			}
			try {
				int day = Integer.parseInt(args[1]);
				if (day < 1 || day > 24) {
					throw new NumberFormatException("not a day from the calendar");
				}
				this.calendarManager.EDITOR.clearDoorInventoryHolder(day);
				this.message(player, "Cleared locations for door " + day);
			} catch (NumberFormatException exception) {
				this.message(player, "Error: " + exception.getMessage());
				this.message(player, "use \"clearlocations <day>\"");
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Re-reads calendar.cfg and re-applies the door rewards so admins do
	 * not have to restart the server after editing the file.
	 */
	private void reloadCalendarSettings() {
		try {
			this.calendarManager.load();
		} catch (IOException exception) {
			this.plugin.getLogger().log(Level.WARNING,
					"IO error with save-file while loading: " + exception.getLocalizedMessage());
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
			reloadCalendarSettings();
			return true;
		}
		if (sender instanceof Player player) {
			if (args.length > 0 && player.isOp()) {
				if (this.opCommand(player, args)) {
					return true;
				}
			}

			// Backward compatibility: plugin.yml declares the command permission as
			// "calendar.ownAndShow" (matching the 2.0.0-alpha2 JAR) so that existing
			// LuckPerms grants continue to work. However, at runtime we check both
			// calendar.own AND calendar.show individually so that servers migrated to
			// the split permissions also work.
			if (player.hasPermission(Permissions.OWN_CALENDAR)
					&& player.hasPermission(Permissions.SHOW_CALENDAR)) {
				if (!this.calendarManager.isActive(player)) {
					this.message(player, Messages.MSG_WRONG_TIME);
				} else if (player.getGameMode().equals(GameMode.SURVIVAL)) {
					runSynced(() -> player.openInventory(this.calendarManager.get(player).getCalendar()));
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
