package de.groovybyte.spigot.xcraftadvent.entity;

import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Lightweight representation of a block position in the world.
 * Used as a key in the door-locations map to associate physical
 * chest positions with advent calendar doors.
 */
public record BlockLocation(World world, int x, int y, int z) {

    /**
     * Creates a BlockLocation from a Bukkit Location, snapping to block coordinates.
     */
    public static BlockLocation fromLocation(Location location) {
        return new BlockLocation(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Converts this BlockLocation back to a Bukkit Location.
     */
    public Location toLocation() {
        return new Location(world, x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BlockLocation(World world1, int x1, int y1, int z1))) {
            return false;
        }
        return x == x1 && y == y1 && z == z1 && Objects.equals(world, world1);
    }

    @Override
    public String toString() {
        return "BlockLocation{world=" + world + ", x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}
