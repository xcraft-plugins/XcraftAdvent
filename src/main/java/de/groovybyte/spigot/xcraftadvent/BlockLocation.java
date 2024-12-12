package de.groovybyte.spigot.xcraftadvent;

import org.bukkit.Location;
import org.bukkit.World;

public record BlockLocation(
    World world,
    int x,
    int y,
    int z
) {
   public static BlockLocation fromLocation(Location location) {
      return new BlockLocation(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
   }

   public Location toLocation() {
      return new Location(this.world, this.x, this.y, this.z);
   }
}
