package de.groovybyte.spigot.xcraftadvent;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public final class Messages {
   public static final String CHAT_PREFIX;
   private static final BaseComponent[] PREFIX;
   public static final String INV_TITLE = "Adventskalender";
   public static final String INV_FILL = "Türchenfüllen";
   public static final String ITEM_NAME = "Tag %s";
   public static final String ITEM_EDIT_DAY = "Türchen %s";
   public static final String ITEM_OPENED = "Bereits geöffnet";
   public static final String ITEM_CLOSED = "Klicke zum Öffnen";
   public static final String ITEM_FUTURE = "Überraschung in %s Tag%s";
   public static final String ITEM_24 = "Weihnachten in %s Tag%s";
   public static final String ITEM_24_READY = "Frohe Weihnachten!";
   public static final String MSG_OPEN = "Du hast das Türchen Nummer %d bereits geöffnet!";
   public static final String MSG_CLOSED = "Du darfst das Türchen Nummer %d noch nicht öffnen!";
   public static final String MSG_OPENING = "Du hast Türchen Nummer %d geöffnet!";
   public static final String MSG_OVERFLOW = "Dein Inventar ist voll. Der Türcheninhalt hat sich um dich herum verteilt.";
   public static final String MSG_MISSING_PERM = "Du darfst das leider nicht tun! (Versuche es nochmal in der Hauptwelt)";
   public static final String MSG_MISSING_CONTENT = "Die faulen Admins haben das Türchen noch nicht gefüllt ;)";
   public static final String MSG_WRONG_TIME = "Du kannst den Adventskalender nicht mehr öffnen.";
   public static final String MSG_WRONG_GAMEMODE = "Du solltest die Türchen nur im Survival-Modus öffnen.";
   public static final String MSG_UNOPENED_DOORS = "Du hast noch ungeöffnete Kalendertürchen.";
   public static final String MSG_COMMAND_INFO = "Du kannst den Adventskalender mit /advent öffnen.";

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

   public static void notifyAboutUnopenedDoors(CommandSender receiver) {
      send(receiver, new TextComponent("Du hast noch ungeöffnete Kalendertürchen.\nDu kannst den Adventskalender mit /advent öffnen."));
   }

   static {
      CHAT_PREFIX = ChatColor.WHITE + "[" + ChatColor.DARK_GREEN + "Adventskalender" + ChatColor.WHITE + "]" + ChatColor.DARK_AQUA + " ";
      PREFIX = TextComponent.fromLegacyText(CHAT_PREFIX);
   }
}
