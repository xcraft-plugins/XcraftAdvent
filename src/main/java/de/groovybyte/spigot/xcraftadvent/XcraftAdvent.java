package de.groovybyte.spigot.xcraftadvent;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class XcraftAdvent implements Closeable {
    public final IDateChecker dateChecker;
    public final Logger logger;
    public final Consumer<Runnable> scheduleTaskNext;
    public final Consumer<Runnable> scheduleTaskSoon;
    public final CalendarManager calendarManager;
    public final Commands commands;


    XcraftAdvent(
        IDateChecker dateChecker,
        Logger logger,
        Consumer<Runnable> scheduleTaskNext,
        Consumer<Runnable> scheduleTaskSoon,
        File pluginFolder
    ) {
        this.dateChecker = dateChecker;
        this.logger = logger;
        this.scheduleTaskNext = scheduleTaskNext;
        this.scheduleTaskSoon = scheduleTaskSoon;

        this.calendarManager = new CalendarManager(this,
            new File(pluginFolder, "calendar.cfg"),
            new File(pluginFolder, "players.data")
        );

        logger.info(dateChecker.toString());
        try {
            calendarManager.load();
        } catch(IOException e) {
            logger.log(Level.WARNING, "IO error with save-file while loading: " + e.getLocalizedMessage());
        }

        this.commands = new Commands(this);
    }

    @Override
    public void close() {
        try {
            calendarManager.save();
        } catch(IOException e) {
            logger.log(Level.WARNING, "IO error with save-file while saving: " + e.getLocalizedMessage());
        }
    }


    public void message(Player p, String s) {
        p.sendMessage(Messages.CHAT_PREFIX + s);
    }

    public void onPlayerJoin(Player player) {
        if (player.hasPermission(Permissions.OWN_AND_SHOW)) {
            calendarManager.playerJoin(player).ifPresent((calendar) -> {
                if (calendar.hasClosedDoors()) {
                    scheduleTaskSoon.accept(() -> {
                        message(player, Messages.MSG_COMMAND_INFO);
                        if (player.getGameMode().equals(GameMode.SURVIVAL)) {
                            Bukkit.dispatchCommand(player, "advent");
                        }
                    });
                }
            });
        }
    }

    public void onPlayerQuit(Player player) {
        calendarManager.playerQuit(player);
    }

    /**
     * @return true if the clicked block was a calendar door
     */
    public boolean onPlayerClickedDoorBlock(
        Player player, Block block
    ) {
        Door door = calendarManager.getDoorByLocation(BlockLocation.fromLocation(block.getLocation()));
        if(door != null) {
            onCalendarDoorOpen(player, door);
            return true;
        }
        return false;
    }

    /**
     * @return true if the opened container was a calendar door
     */
    public boolean onPlayerOpenedDoorContainer(
        Player player, Location location
    ) {
        Door door = calendarManager.getDoorByLocation(BlockLocation.fromLocation(location));
        if(door != null) {
            onCalendarDoorOpen(player, door);
            return true;
        }
        return false;
    }

    private void onCalendarDoorOpen(Player player, Door door) {
        if(player.hasPermission(Permissions.OWN_AND_SHOW)) {
            if(!calendarManager.isActive(player)) {
                message(player, Messages.MSG_WRONG_TIME);
            } else if(player.getGameMode().equals(GameMode.SURVIVAL)) {
                tryOpenDoor(player, calendarManager.get(player), door.day);
            } else {
                message(player, Messages.MSG_WRONG_GAMEMODE);
            }
        } else {
            message(player, Messages.MSG_MISSING_PERM);
        }
    }

    void tryOpenDoor(Player player, PlayerCalendar calendar, int day) {
        if(!player.hasPermission(Permissions.OPEN_DOORS)) {
            message(player, Messages.MSG_MISSING_PERM);
        } else {
            scheduleTaskNext.accept(() -> {
                boolean isOpen = calendar.isOpen(day);
                boolean canOpen = calendar.canOpen(day);
                logger.log(Level.INFO, player.getName() + " tried opening door " + day + " [canOpen=" + canOpen + ", isOpen=" + isOpen + ", calendarGenerationDay=" + calendar.genDay + "]");

                if(isOpen) {
                    message(player, String.format(Messages.MSG_OPEN, day));
                } else if(!canOpen) {
                    message(player, String.format(Messages.MSG_CLOSED, day));
                } else {
                    calendar.open(day);
                }
            });
        }
    }
}
