package de.groovybyte.spigot.xcraftadvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Door {

    protected final XcraftAdvent plugin;
    protected final int day;
    protected final int position;

    protected ItemStack[] rewardItems = new ItemStack[0];
    protected String[] rewardCmds = new String[0];

    public Door(XcraftAdvent plugin, int day) {
        this.plugin = plugin;
        this.day = day--;
        this.position = 10 + day + 2 * (day / 7) + ((day > 20) ? 2 : 0);
    }

    public void update(Configuration cfg) {
        int nr = 1;
        String path = "rewards.door" + day + ".item" + nr;
        List<ItemStack> items = new ArrayList<>();
        while (cfg.contains(path)) {
            items.add(cfg.getItemStack(path));
            nr++;
            path = "rewards.door" + day + ".item" + nr;
        }
        this.rewardItems = items.toArray(new ItemStack[0]);
        this.rewardCmds = cfg.getStringList("rewards.door" + day + ".commands").toArray(new String[0]);
    }

    public int getPosition() {
        return this.position;
    }

    public boolean hasItems() {
        return this.rewardItems.length != 0;
    }

    public ItemStack[] getItems() {
        return this.rewardItems;
    }

    public boolean hasCommands() {
        return this.rewardCmds.length != 0;
    }

    public String[] getCommands() {
        return this.rewardCmds;
    }

    public boolean isEmpty() {
        return this.rewardItems.length == 0 && this.rewardCmds.length == 0;
    }

    public boolean reward(Player player) {
        if (this.isEmpty()) {
            this.plugin.log(Level.WARNING, "door " + this.day + " hasn\'t been filled yet");
            return false;
        }
        for (String cmd : this.rewardCmds) {
            cmd = cmd.replace("@p", player.getName());
            boolean result = this.plugin.getServer().dispatchCommand(player.getServer().getConsoleSender(), cmd);
            if (!result) {
                this.plugin.log(Level.WARNING, cmd + " failed");
            }
        }
        if (this.rewardItems.length != 0) {
            Collection<ItemStack> overflow = player
                    .getInventory()
                    .addItem(this.rewardItems)
                    .values();
            if (overflow.isEmpty()) {
                this.plugin.message(player, String.format(Messages.MSG_OPENING, this.day));
            } else {
                World pWorld = player.getWorld();
                Location pLoc = player.getLocation();
                overflow.forEach((ItemStack is) -> pWorld.dropItemNaturally(pLoc, is));
                this.plugin.message(player, Messages.MSG_OVERFLOW);
            }
        }
        return true;
    }
}
