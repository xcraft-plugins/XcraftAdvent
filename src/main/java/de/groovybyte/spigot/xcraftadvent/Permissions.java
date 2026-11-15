package de.groovybyte.spigot.xcraftadvent;

public class Permissions {

	/* fix: give item only in gm 0 */
	//public final String PERM_admin_peek = "calendar.admin.showAny";
	public final static String OWN_CALENDAR = PluginConstants.PERMISSION_OWN_CALENDAR;
	public final static String SHOW_CALENDAR = PluginConstants.PERMISSION_SHOW_CALENDAR;
	public final static String OPEN_CALENDAR_DOORS = PluginConstants.PERMISSION_OPEN_CALENDAR_DOORS;
	/**
	 * Combined permission that acts as an alternative to {@link #OWN_CALENDAR}
	 * AND {@link #SHOW_CALENDAR}. Granted via the command permission in plugin.yml
	 * for backward compatibility with the 2.0.0-alpha2 JAR.
	 */
	public final static String OWN_AND_SHOW = PluginConstants.PERMISSION_OWN_AND_SHOW;

	/**
	 * Checks whether a player has calendar access: either the combined
	 * {@link #OWN_AND_SHOW} permission OR both {@link #OWN_CALENDAR} and
	 * {@link #SHOW_CALENDAR} individually.
	 */
	public static boolean hasCalendarAccess(org.bukkit.permissions.Permissible p) {
		return p.hasPermission(OWN_AND_SHOW)
				|| (p.hasPermission(OWN_CALENDAR) && p.hasPermission(SHOW_CALENDAR));
	}

}
