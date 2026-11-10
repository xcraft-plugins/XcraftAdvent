package de.groovybyte.spigot.xcraftadvent;

import de.groovybyte.spigot.xcraftadvent.entity.ICalendar;
import de.groovybyte.spigot.xcraftadvent.listener.ConnectionListener;
import de.groovybyte.spigot.xcraftadvent.repository.ICalendarRepository;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

public class ConnectionListenerTests {

	private static final class StubCalendarRepository implements ICalendarRepository {

		@Override
		public CompletableFuture<ICalendar> loadCalendar(Player player) {
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public ICalendar getCalendar(Player player) {
			return null;
		}

		@Override
		public CompletableFuture<ICalendar> unloadCalendar(Player player) {
			return CompletableFuture.completedFuture(null);
		}
	}

	private ConnectionListener newListener() {
		IServiceProvider services = new IServiceProvider() {
			@Override
			public <S> S getService(Class<? extends S> serviceType) {
				if (serviceType == ICalendarRepository.class) {
					return serviceType.cast(new StubCalendarRepository());
				}
				return null;
			}
		};
		return new ConnectionListener(services);
	}

	// --- Player Joined ---------

	@Test
	public void checkForUnopenedDoorsIgnoresMissingCalendar() {
		// no calendar (e.g. outside of advent time) must not throw
		newListener().checkForUnopenedDoors(null);
	}
}
