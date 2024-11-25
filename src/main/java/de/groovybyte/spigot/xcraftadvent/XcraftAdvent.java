package de.groovybyte.spigot.xcraftadvent;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import de.groovybyte.spigot.xcraftadvent.listener.CalendarCommandExecutor;
import de.groovybyte.spigot.xcraftadvent.listener.ConnectionListener;
import de.groovybyte.spigot.xcraftadvent.listener.InventoryListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class XcraftAdvent extends JavaPlugin implements IServiceProvider {

    public final static boolean DEBUG = true;
	public final static String CALENDAR_COMMAND_LABEL = "adventskalender";

	private final ClassToInstanceMap<Object> services;
    protected InventoryListener inventoryListener;
    protected ConnectionListener connectionListener;
	protected CalendarCommandExecutor calendarCommandExecutor;
    protected CalendarManager man;

    public XcraftAdvent() {
		services = MutableClassToInstanceMap.create();
		services.putInstance(IDateChecker.class, DEBUG
            ? new DebugDateChecker()
            : new DefaultDateChecker()
        );
    }
	
	@Override
	public <S> S getService(Class<? extends S> serviceType) {
		return (S) services.getInstance(serviceType);
	}

    @Override
    public void onEnable() {
		services.putInstance(Logger.class, getLogger());
		
        File pluginFolder = this.getDataFolder();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }
        man = new CalendarManager(this,
                new File(pluginFolder, "calendar.cfg"),
                new File(pluginFolder, "players.data")
        );

//        if (!DateChecker.isDecember()) {
//            this.log(Level.WARNING, "this month is not december, the plugin will be disabled");
//            this.getPluginLoader().disablePlugin(this);
//            return;
//        }
        try {
            man.load();
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "IO error with save-file while loading: {0}", e.getLocalizedMessage());
        }

        inventoryListener = new InventoryListener(this);
        connectionListener = new ConnectionListener(this);
        getServer().getPluginManager().registerEvents(inventoryListener, this);
        getServer().getPluginManager().registerEvents(connectionListener, this);
		calendarCommandExecutor = new CalendarCommandExecutor();
        Objects.requireNonNull(this.getCommand(CALENDAR_COMMAND_LABEL))
            .setExecutor(calendarCommandExecutor);
    }

    @Override
    public void onDisable() {
        try {
            man.save();
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "IO error with save-file while saving: {0}", e.getLocalizedMessage());
        }
    }
}
