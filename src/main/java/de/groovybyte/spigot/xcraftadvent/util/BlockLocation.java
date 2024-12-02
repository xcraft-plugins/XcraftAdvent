package de.groovybyte.spigot.xcraftadvent.util;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public record BlockLocation(
    World world,
    int x, int y, int z
) {

    public static BlockLocation fromLocation(Location location) {
        return new BlockLocation(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public Location toLocation() {
        return new Location(world, x, y, z);
    }

    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(o instanceof BlockLocation(World world1, int x1, int y1, int z1)) {
            return x == x1 && y == y1 && z == z1 && world.equals(world1);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(world, x, y, z);
    }

    public String toString() {
        return "BlockLocation{world=%s, x=%d, y=%d, z=%d}".formatted(world, x, y, z);
    }
}
