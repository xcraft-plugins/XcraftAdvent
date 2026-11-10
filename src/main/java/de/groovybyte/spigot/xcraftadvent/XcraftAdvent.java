package de.groovybyte.spigot.xcraftadvent;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import de.groovybyte.spigot.xcraftadvent.listener.CalendarCommandExecutor;
import de.groovybyte.spigot.xcraftadvent.listener.ConnectionListener;
import de.groovybyte.spigot.xcraftadvent.listener.InventoryListener;
import de.groovybyte.spigot.xcraftadvent.repository.CalendarLoader;
import de.groovybyte.spigot.xcraftadvent.repository.ICalendarRepository;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class XcraftAdvent extends JavaPlugin implements IServiceProvider {

    public final static boolean DEBUG = true;
    public final static String CALENDAR_COMMAND_LABEL = PluginConstants.COMMAND_LABEL;

    private final ClassToInstanceMap<Object> services = MutableClassToInstanceMap.create();
    protected CalendarManager calendarManager;

    public XcraftAdvent() {
        registerService(IDateChecker.class, DEBUG
            ? new DebugDateChecker()
            : new DefaultDateChecker()
        );
    }

    @Override
    public <S> S getService(Class<? extends S> serviceType) {
        return (S) services.getInstance(serviceType);
    }

    /**
     * Registers a service instance under its type. Listeners and other
     * components resolve their dependencies via {@link #getService}.
     */
    public <S> void registerService(Class<S> serviceType, S service) {
        services.putInstance(serviceType, service);
    }

    @Override
    public void onEnable() {
        registerService(JavaPlugin.class, this);
        registerService(Logger.class, getLogger());

        File pluginFolder = this.getDataFolder();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }
        calendarManager = new CalendarManager(this,
                new File(pluginFolder, "calendar.cfg"),
                new File(pluginFolder, "players.data")
        );
        registerService(CalendarManager.class, calendarManager);
        registerService(ICalendarRepository.class, new CalendarLoader(this, calendarManager));

//        if (!DateChecker.isDecember()) {
//            this.log(Level.WARNING, "this month is not december, the plugin will be disabled");
//            this.getPluginLoader().disablePlugin(this);
//            return;
//        }
        try {
            calendarManager.load();
        } catch (IOException exception) {
            getLogger().log(Level.WARNING, "IO error with save-file while loading: {0}", exception.getLocalizedMessage());
        }

        registerEventListener(new InventoryListener(this));
        registerEventListener(new ConnectionListener(this));

        CalendarCommandExecutor commandExecutor = new CalendarCommandExecutor(calendarManager, this);
        Objects.requireNonNull(this.getCommand(CALENDAR_COMMAND_LABEL))
            .setExecutor(commandExecutor);
    }

    private <L extends Listener> void registerEventListener(L listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {
        try {
            calendarManager.save();
        } catch (IOException exception) {
            getLogger().log(Level.WARNING, "IO error with save-file while saving: {0}", exception.getLocalizedMessage());
        }
        services.clear();
    }
}
