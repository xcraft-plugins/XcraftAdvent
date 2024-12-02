package de.groovybyte.spigot.xcraftadvent;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import de.groovybyte.spigot.xcraftadvent.config.Config;
import de.groovybyte.spigot.xcraftadvent.datechecker.DebugDateChecker;
import de.groovybyte.spigot.xcraftadvent.datechecker.DefaultDateChecker;
import de.groovybyte.spigot.xcraftadvent.datechecker.IDateChecker;
import de.groovybyte.spigot.xcraftadvent.listener.CalendarCommandExecutor;
import de.groovybyte.spigot.xcraftadvent.listener.ConnectionListener;
import de.groovybyte.spigot.xcraftadvent.listener.InventoryListener;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class XcraftAdvent extends JavaPlugin implements IServiceProvider {

    public final static boolean DEBUG = true;
	public final static String CALENDAR_COMMAND_LABEL = "adventskalender";

	private final ClassToInstanceMap<Object> SERVICES = MutableClassToInstanceMap.create();

    @Override
	public <S> S getService(Class<? extends S> serviceType) {
		return (S) SERVICES.getInstance(serviceType);
	}

    public <S> void registerService(Class<S> clazz, S service) {
        SERVICES.putInstance(clazz, service);
    }

    @Override
    public void onEnable() {
        registerService(JavaPlugin.class, this);
        registerService(Logger.class, getLogger());
        registerService(Config.class, new Config(this.getDataFolder().toPath()));
        registerService(
            IDateChecker.class, DEBUG
            ? new DebugDateChecker()
            : new DefaultDateChecker()
        );

        man = new CalendarManager(this);

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

        registerEventListener(new InventoryListener(this));
        registerEventListener(new ConnectionListener(this));
        registerCommand(CALENDAR_COMMAND_LABEL, new CalendarCommandExecutor(this));
    }

    private <L extends Listener> void registerEventListener(L listener) {
        registerService((Class<L>) listener.getClass(), listener);
        Bukkit.getPluginManager().registerEvents(listener, this);
    }
    private <E extends CommandExecutor> void registerCommand(String command, E commandExecutor) {
        registerService((Class<E>) commandExecutor.getClass(), commandExecutor);
        Objects.requireNonNull(this.getCommand(command)).setExecutor(commandExecutor);
    }

    @Override
    public void onDisable() {
        try {
            man.save();
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "IO error with save-file while saving: {0}", e.getLocalizedMessage());
        }
        this.SERVICES.clear();
    }
}
