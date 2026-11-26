package de.groovybyte.spigot.xcraftadvent.listener;

import de.groovybyte.spigot.xcraftadvent.CalendarManager;
import de.groovybyte.spigot.xcraftadvent.Messages;
import de.groovybyte.spigot.xcraftadvent.Permissions;
import de.groovybyte.spigot.xcraftadvent.entity.BlockLocation;
import de.groovybyte.spigot.xcraftadvent.view.InventoryCalendarView;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.BlockState;
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
						case "addcmd":
							this.calendarManager.EDITOR.addCommand(day,
									Arrays.asList(args).subList(3, args.length).stream().collect(Collectors.joining(" ")));
							break;
						case "clearcmds":
							this.calendarManager.EDITOR.clearCommands(day);
							break;
						case "add":
							this.calendarManager.EDITOR.addItem(day, player.getInventory().getItemInMainHand());
							break;
						case "clear":
							this.calendarManager.EDITOR.clearItems(day);
							break;
						case "addlocation":
							handleAddLocation(player, day);
							break;
						case "clearlocations":
							this.calendarManager.EDITOR.clearDoorInventoryHolder(day);
							break;
						case "test":
							this.calendarManager.getDoor(day).reward(player);
							break;
						default:
							throw new NumberFormatException("wrong usage");
					}
				} catch (ArrayIndexOutOfBoundsException | NumberFormatException exception) {
					this.message(player, "Error: " + exception.getMessage());
					this.message(player, "use \"edit [day] addcmd/clearcmds/add/clear/addlocation/clearlocations/test ([command])\"");
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Handles {@code edit <day> addlocation} — validates that the player
	 * targets a container block, checks for duplicate registration, and
	 * registers the location.
	 */
	private void handleAddLocation(Player player, int day) {
		Block target = player.getTargetBlockExact(15);
		if (target == null) {
			this.message(player, "Error: please target a valid storage container");
			return;
		}
		BlockState state = target.getState();
		if (!(state instanceof Container)) {
			this.message(player, "Error: please target a valid storage container");
			return;
		}
		Location location = target.getLocation();
		// Check if this container is already linked to another door
		de.groovybyte.spigot.xcraftadvent.entity.Door existing =
				this.calendarManager.getDoorByLocation(BlockLocation.fromLocation(location));
		if (existing != null) {
			this.message(player, "Error: this storage container is already linked to calendar door " + existing.getDay());
			return;
		}
		this.calendarManager.EDITOR.addDoorInventoryHolder(day, location);
		this.message(player, "Added storage container at x=" + location.getBlockX()
				+ ", y=" + location.getBlockY() + ", z=" + location.getBlockZ()
				+ " to calendar door " + day);
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

			// Backward compatibility: accepts either the combined "calendar.ownAndShow"
			// permission (2.0.0-alpha2 JAR) OR both "calendar.own" AND "calendar.show"
			// (split permissions for migrated servers).
			if (Permissions.hasCalendarAccess(player)) {
				if (!this.calendarManager.isActive(player)) {
					this.message(player, Messages.MSG_WRONG_TIME);
				} else if (player.getGameMode().equals(GameMode.SURVIVAL)) {
					runSynced(() -> new InventoryCalendarView(this.calendarManager.get(player)).show());
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
