package de.groovybyte.spigot.xcraftadvent;

import de.groovybyte.spigot.xcraftadvent.entity.ICalendar;
import java.util.logging.Level;
import org.bukkit.entity.Player;

public final class PlayerCalendar implements ICalendar {

	protected final CalendarManager calendarManager;
	protected final Player player;
	protected int open;

	public PlayerCalendar(CalendarManager calendarManager, Player player, int openStatus) {
		this.calendarManager = calendarManager;
		this.player = player;
		this.open = openStatus;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof PlayerCalendar)) {
			return false;
		}
		PlayerCalendar other = (PlayerCalendar) obj;
		return this.player.getUniqueId().equals(other.getPlayer().getUniqueId());
	}

	@Override
	public int hashCode() {
		return this.player.getUniqueId().hashCode();
	}

	@Override
	public Player getOwner() {
		return this.player;
	}

	@Override
	public boolean open(int day) {
		if (day < 1 || day > 24 || !canOpen(day) || isOpen(day)
				|| !this.calendarManager.getDoor(day).reward(this.player)) {
			return false;
		}
		this.open |= 0b1 << (day - 1);
		this.calendarManager.plugin.getLogger().log(Level.INFO, this.player.getName() + " opened door " + day);
		return true;
	}

	@Override
	public boolean hasClosedDoors() {
		int day = 0;
		while (canOpen(++day)) {
			if (!isOpen(day)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isOpen(int day) {
		return day >= 1 && day <= 24 && ((this.open >> (day - 1)) & 0b1) == 0b1;
	}

	public boolean isFullyOpened() {
		return 0b1111_1111_1111_1111_1111_1111 == this.open;
	}

	@Override
	public boolean canOpen(int day) {
		return day >= 1 && day <= 24 && day <= getCurrentDay();
	}

	@Override
	public int getCurrentDay() {
		return this.calendarManager.getDateChecker().getDayOfMonth();
	}

	public Player getPlayer() {
		return this.player;
	}

	public int getOpenStatus() {
		return this.open;
	}

	@Override
	public String toString() {
		return String.format("Calendar[%s]=%b", this.player.getName(), this.open);
	}

}
