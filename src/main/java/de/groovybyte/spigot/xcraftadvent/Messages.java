package de.groovybyte.spigot.xcraftadvent;

import de.groovybyte.spigot.xcraftadvent.entity.ICalendar;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.OptionalInt;

public final class Messages {

	public final static String CHAT_PREFIX = ChatColor.WHITE + "[" + ChatColor.DARK_GREEN + "Adventskalender" + ChatColor.WHITE + "]" + ChatColor.DARK_AQUA + " ";
	private final static BaseComponent PREFIX = TextComponent.fromLegacy(CHAT_PREFIX);
	public final static String INV_TITLE = "Adventskalender";
	public final static String INV_FILL = "Türchenfüllen";
	public final static String ITEM_NAME = "Tag %s";
	public final static String ITEM_EDIT_DAY = "Türchen %s";
	public final static String ITEM_OPENED = "Bereits geöffnet";
	public final static String ITEM_CLOSED = "Klicke zum Öffnen";
	public final static String ITEM_FUTURE = "Überraschung in %s Tag%s";
	public final static String ITEM_24 = "Weihnachten in %s Tag%s";
	public final static String ITEM_24_READY = "Frohe Weihnachten!";
	public final static String MSG_OPEN = "Du hast das Türchen Nummer %d bereits geöffnet!";
	public final static String MSG_CLOSED = "Du darfst das Türchen Nummer %d noch nicht öffnen!";
	public final static String MSG_OPENING = "Du hast Türchen Nummer %d geöffnet!";
	public final static String MSG_OVERFLOW = "Dein Inventar ist voll. Der Türcheninhalt hat sich um dich herum verteilt.";
	public final static String MSG_MISSING_PERM = "Du darfst das leider nicht tun! (Versuche es nochmal in der Hauptwelt)";
	public final static String MSG_MISSING_CONTENT = "Die faulen Admins haben das Türchen noch nicht gefüllt ;)";
	public final static String MSG_WRONG_TIME = "Du kannst den Adventskalender nicht mehr öffnen.";
	public final static String MSG_WRONG_GAMEMODE = "Du solltest die Türchen nur im Survival-Modus öffnen.";
	public final static String MSG_UNOPENED_DOORS = "Du hast noch ungeöffnete Kalendertürchen.";
	public final static String MSG_NO_UNOPENED_DOORS = "Du hast keine ungeöffneten Kalendertürchen.";
	public final static String MSG_COMMAND_INFO = "Du kannst den Adventskalender mit /advent öffnen.";
	public final static String MSG_UNOPENED_DOORS_COUNT = "Das älteste ungeöffnete Türchen ist Nummer %d. Du hast noch %d Türchen zu öffnen.";

	private static BaseComponent prefixedMessage(BaseComponent message) {
		BaseComponent prefixedMessage = new TextComponent(PREFIX);
		prefixedMessage.addExtra(message);
		return prefixedMessage;
	}

	public static void send(CommandSender receiver, String templateMessage, Object... values) {
		send(receiver, new TextComponent(String.format(templateMessage, values)));
	}

	public static void send(CommandSender receiver, BaseComponent message) {
		receiver.spigot().sendMessage(prefixedMessage(message));
	}

	/**
	 * @return true if notification has been send (= hasUnopenedDoors)
	 */
	public static boolean notifyAboutUnopenedDoors(CommandSender receiver, ICalendar calendar) {
		OptionalInt hasUnopenedDoors = calendar.getFirstUnopenedDay();
		hasUnopenedDoors.ifPresent(firstDay -> {
			StringBuilder sb = new StringBuilder();
			sb.append(MSG_UNOPENED_DOORS)
					.append("\n")
					.append(String.format(MSG_UNOPENED_DOORS_COUNT, firstDay, calendar.getNumberOfOpenableDays()));

			send(receiver, new TextComponent(sb.toString()));
		});
		return hasUnopenedDoors.isPresent();
	}
}
