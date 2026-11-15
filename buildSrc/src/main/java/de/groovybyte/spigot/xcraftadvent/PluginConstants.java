package de.groovybyte.spigot.xcraftadvent;

/**
 * Build-time copy of plugin constants used by build.gradle.kts to generate
 * plugin.yml. The runtime copy lives in src/main/java (same package).
 * Keep both files in sync — they contain identical constants.
 */
public final class PluginConstants {

    public static final String COMMAND_LABEL = "adventskalender";
    public static final String[] COMMAND_ALIASES = {"xmas", "advent"};
    public static final String COMMAND_DESCRIPTION = "Öffnet den Adventskalender";
    public static final String COMMAND_USAGE = "/advent";

    /**
     * Combined permission used in plugin.yml for the command permission node.
     * Kept as "calendar.ownAndShow" for backward compatibility with servers
     * running the 2.0.0-alpha2 JAR, where permission managers (e.g., LuckPerms)
     * may have granted this exact node to players.
     *
     * <p>At runtime, the plugin checks both {@link #PERMISSION_OWN_CALENDAR}
     * and {@link #PERMISSION_SHOW_CALENDAR} individually so that servers
     * migrated to the split permissions also work.
     */
    public static final String PERMISSION_OWN_AND_SHOW = "calendar.ownAndShow";

    public static final String PERMISSION_OWN_CALENDAR = "calendar.own";
    public static final String PERMISSION_OWN_CALENDAR_DESCRIPTION = "Benötigt, um einen Adventskalender zu besitzen";

    public static final String PERMISSION_SHOW_CALENDAR = "calendar.show";
    public static final String PERMISSION_SHOW_CALENDAR_DESCRIPTION = "Benötigt, um den Adventskalender anzuzeigen";

    public static final String PERMISSION_OPEN_CALENDAR_DOORS = "calendar.doors.open";
    public static final String PERMISSION_OPEN_CALENDAR_DOORS_DESCRIPTION = "Ermöglicht das Öffnen von Türchen";

    private PluginConstants() {
    }
}
