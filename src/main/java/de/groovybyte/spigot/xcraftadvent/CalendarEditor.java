package de.groovybyte.spigot.xcraftadvent;

import de.groovybyte.spigot.xcraftadvent.entity.Door;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CalendarEditor {

    public final static String META_KEY = "CalendarEditing";
    //p.setMetadata("calendarEditing", new FixedMetadataValue(this.plugin, true));

    protected final XcraftAdvent plugin;
    protected final CalendarManager calendarManager;
    protected Player player = null;
    protected final Inventory inventory;
    protected int editingDay;

    public CalendarEditor(XcraftAdvent plugin, CalendarManager calendarManager) {
        this.plugin = plugin;
        this.calendarManager = calendarManager;
        this.inventory = plugin.getServer().createInventory(null, InventoryType.CHEST, Messages.INV_FILL);
    }

    protected Door getDoor(int day) {
        return this.calendarManager.doors[day - 1];
    }

    protected boolean inSelectionInv() {
        return editingDay == 0;
    }

    protected void setSelectionInv() {
        ItemStack[] content = new ItemStack[24];
        ItemStack item;
        ItemMeta meta;
        Door door;
        for (int day = 1; day <= 24; day++) {
            door = this.getDoor(day);

            item = new ItemStack(door.isEmpty() ? Material.COBWEB : Material.CHEST, 1);
            meta = item.getItemMeta();

            List<String> lore = new ArrayList<>();
            if (door.isEmpty()) {
                lore.add("leer");
            } else {
                if (door.hasItems()) {
                    lore.add("enthält: " + Arrays
                            .stream(door.getItems())
                            .limit(5)
                            .map((ItemStack itemStack) -> itemStack.getType().toString())
                            .collect(Collectors.joining(", ")));
                }
                if (door.hasCommands()) {
                    lore.add("führt " + door.getCommands().length + " Commands aus");
                }
            }
            meta.setDisplayName(String.format(Messages.ITEM_EDIT_DAY, Integer.toString(day)));
            meta.setLore(lore);

            item.setItemMeta(meta);
            content[day - 1] = item;
        }
        this.inventory.setContents(content);
        this.editingDay = 0;
    }

    protected void setDayInv(int day) {
        Door door = this.getDoor(day);
        this.inventory.setContents(door.getItems());
        this.editingDay = day;
    }

    public void clearCommands(int day) {
        this.calendarManager.config.set("rewards.door" + day + ".commands", null);
        this.calendarManager.doors[day - 1].update(this.calendarManager.config);
        try {
            this.calendarManager.config.save(this.calendarManager.cfgFile);
        } catch (IOException exception) {
            this.plugin.getLogger().log(Level.WARNING, "config io error");
        }
    }

    public void addCommand(int day, String command) {
        command = command.trim();
        if (command.isEmpty()) {
            return;
        }
        List<String> commands = this.calendarManager.config.getStringList("rewards.door" + day + ".commands");
        commands.add(command);
        this.calendarManager.config.set("rewards.door" + day + ".commands", commands);
        this.calendarManager.doors[day - 1].update(this.calendarManager.config);
        try {
            this.calendarManager.config.save(this.calendarManager.cfgFile);
        } catch (IOException exception) {
            this.plugin.getLogger().log(Level.WARNING, "config io error");
        }
    }

    public void startEditing(Player player) {
        if (this.player != null) {
            player.sendMessage("editing in progress");
            return;
        }
        this.player = player;
        this.setSelectionInv();
        this.player.openInventory(this.inventory);
    }

    public void edit(InventoryClickEvent event) {
        if (this.inSelectionInv()) {
            event.setCancelled(true);
            this.setDayInv(event.getRawSlot() + 1);
        } else {

        }
    }

    public boolean isEditing(Player player) {
        return (this.player == null) ? false : this.player.equals(player);
    }

    public void stopEditing() {
        if (!this.inSelectionInv()) {
            List<ItemStack> items = Arrays
                    .asList(this.inventory.getContents())
                    .stream()
                    .filter((ItemStack itemStack) -> (itemStack != null))
                    .filter((ItemStack itemStack) -> !PlayerCalendar.isAir(itemStack))
                    .collect(Collectors.toList());
            for (int index = 0; index < items.size(); index++) {
                this.calendarManager.config.set("rewards.door" + this.editingDay + ".item" + (index + 1), items.get(index));
            }
            this.calendarManager.doors[this.editingDay - 1].update(this.calendarManager.config);

            try {
                this.calendarManager.config.save(this.calendarManager.cfgFile);
            } catch (IOException exception) {
                this.plugin.getLogger().log(Level.WARNING, "config io error");
            }
        }
        this.player = null;
    }
}
