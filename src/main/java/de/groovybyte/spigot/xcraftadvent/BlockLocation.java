package de.groovybyte.spigot.xcraftadvent;

import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.World;

public class BlockLocation {
   World world;
   int x;
   int y;
   int z;

   BlockLocation(World world, int x, int y, int z) {
      this.world = world;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   static BlockLocation fromLocation(Location location) {
      return new BlockLocation(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
   }

   Location toLocation() {
      return new Location(this.world, (double)this.x, (double)this.y, (double)this.z);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof BlockLocation)) {
         return false;
      } else {
         BlockLocation that = (BlockLocation)o;
         return this.x == that.x && this.y == that.y && this.z == that.z && this.world.equals(that.world);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.world, this.x, this.y, this.z});
   }

   public String toString() {
      return "BlockLocation{world=" + this.world + ", x=" + this.x + ", y=" + this.y + ", z=" + this.z + "}";
   }
}
