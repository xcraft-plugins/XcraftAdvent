package de.groovybyte.spigot.xcraftadvent;

/**
 * Single source of truth for the names and descriptions that go into
 * plugin.yml (declared in the root build.gradle.kts) and that are checked
 * at runtime (Permissions, XcraftAdvent).
 *
 * <p>Lives in buildSrc so the Gradle build classpath can reference it, and
 * is additionally compiled into the plugin jar: the root build script adds
 * this source directory to the main source set, so both sides compile the
 * exact same file.
 */
public final class PluginConstants {

    public static final String COMMAND_LABEL = "adventskalender";
    public static final String[] COMMAND_ALIASES = {"xmas", "advent"};
    public static final String COMMAND_DESCRIPTION = "Öffnet den Adventskalender";
    public static final String COMMAND_USAGE = "/advent";

    public static final String PERMISSION_OWN_CALENDAR = "calendar.own";
    public static final String PERMISSION_OWN_CALENDAR_DESCRIPTION = "Benötigt, um einen Adventskalender zu besitzen";

    public static final String PERMISSION_SHOW_CALENDAR = "calendar.show";
    public static final String PERMISSION_SHOW_CALENDAR_DESCRIPTION = "Benötigt, um den Adventskalender anzuzeigen";

    public static final String PERMISSION_OPEN_CALENDAR_DOORS = "calendar.doors.open";
    public static final String PERMISSION_OPEN_CALENDAR_DOORS_DESCRIPTION = "Ermöglicht das Öffnen von Türchen";

    private PluginConstants() {
    }
}
