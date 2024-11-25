package de.groovybyte.spigot.xcraftadvent;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class ConnectionListenerTests {
	
	private Player mockPlayer() {
		Player p = mock(Player.class);
		return p;
	}
	
	private PlayerJoinEvent mockJoinEvent(Player joinedPlayer) {
		PlayerJoinEvent pje = mock(PlayerJoinEvent.class);
		when(pje.getPlayer()).thenReturn(joinedPlayer);
		return pje;
	}
	
	// --- Player Joined ---------
	@Test
	public void showReminderMessage() {
		Assert.assertTrue(false);
	}
	
	
	// --- Player Quit ---------
	@Test
	public void playerQuitTest() {
		Assert.assertTrue(false);
	}
}
