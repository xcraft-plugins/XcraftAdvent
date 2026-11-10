package de.groovybyte.spigot.xcraftadvent.entity;

import de.groovybyte.spigot.xcraftadvent.Messages;
import de.groovybyte.spigot.xcraftadvent.XcraftAdvent;
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
        int itemNumber = 1;
        String path = "rewards.door" + day + ".item" + itemNumber;
        List<ItemStack> items = new ArrayList<>();
        while (cfg.contains(path)) {
            items.add(cfg.getItemStack(path));
            itemNumber++;
            path = "rewards.door" + day + ".item" + itemNumber;
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
            this.plugin.getLogger().log(Level.WARNING, "door " + this.day + " hasn\'t been filled yet");
            return false;
        }
        for (String command : this.rewardCmds) {
            command = command.replace("@p", player.getName());
            boolean result = this.plugin.getServer().dispatchCommand(player.getServer().getConsoleSender(), command);
            if (!result) {
                this.plugin.getLogger().log(Level.WARNING, command + " failed");
            }
        }
        if (this.rewardItems.length != 0) {
            Collection<ItemStack> overflow = player
                    .getInventory()
                    .addItem(this.rewardItems)
                    .values();
            if (overflow.isEmpty()) {
                Messages.send(player, String.format(Messages.MSG_OPENING, this.day));
            } else {
                World playerWorld = player.getWorld();
                Location playerLocation = player.getLocation();
                overflow.forEach((ItemStack itemStack) -> playerWorld.dropItemNaturally(playerLocation, itemStack));
                Messages.send(player, Messages.MSG_OVERFLOW);
            }
        }
        return true;
    }
}
