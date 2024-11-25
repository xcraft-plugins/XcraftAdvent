package io.github.salami555.ardania.xcraftadvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
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
    protected final CalendarManager man;
    protected Player player = null;
    protected final Inventory inv;
    protected int editingDay;

    public CalendarEditor(XcraftAdvent plugin, CalendarManager man) {
        this.plugin = plugin;
        this.man = man;
        this.inv = Bukkit.createInventory(null, InventoryType.CHEST, Messages.INV_FILL);
    }

    protected final Door getDoor(int day) {
        return this.man.doors[day - 1];
    }

    protected final boolean inSelectionInv() {
        return editingDay == 0;
    }

    protected final void setSelectionInv() {
        final ItemStack[] content = new ItemStack[24];
        ItemStack item;
        ItemMeta meta;
        Door door;
        for (int i = 1; i <= 24; i++) {
            door = this.getDoor(i);

            item = new ItemStack(door.isEmpty() ? Material.WEB : Material.CHEST, 1);
            meta = item.getItemMeta();

            List<String> l = new ArrayList<>();
            if (door.isEmpty()) {
                l.add("leer");
            } else {
                if (door.hasItems()) {
                    l.add("enthaelt: " + Arrays
                            .stream(door.getItems())
                            .limit(5)
                            .map((ItemStack is) -> is.getType().toString())
                            .collect(Collectors.joining(", ")));
                }
                if (door.hasCommands()) {
                    l.add("fuehrt " + door.getCommands().length + " Commands aus");
                }
            }
            meta.setDisplayName(String.format(Messages.ITEM_EDIT_DAY, Integer.toString(i)));
            meta.setLore(l);

            item.setItemMeta(meta);
            content[i - 1] = item;
        }
        this.inv.setContents(content);
        this.editingDay = 0;
    }

    protected final void setDayInv(int day) {
        Door d = this.getDoor(day);
        this.inv.setContents(d.getItems());
        this.editingDay = day;
    }

    public final void clearCommands(int day) {
        this.man.config.set("rewards.door" + day + ".commands", (List<String>) new ArrayList<String>());
        this.man.doors[day - 1].update(this.man.config);
        try {
            this.man.config.save(this.man.cfgFile);
        } catch (IOException ex) {
            this.plugin.log(Level.WARNING, "config io error");
        }
    }

    public final void addCommand(int day, String cmd) {
        cmd = cmd.trim();
        if (cmd.isEmpty()) {
            return;
        }
        List<String> tmp = this.man.config.getStringList("rewards.door" + day + ".commands");
        tmp.add(cmd);
        this.man.config.set("rewards.door" + day + ".commands", tmp);
        this.man.doors[day - 1].update(this.man.config);
        try {
            this.man.config.save(this.man.cfgFile);
        } catch (IOException ex) {
            this.plugin.log(Level.WARNING, "config io error");
        }
    }

    public final void startEditing(Player player) {
        if (this.player != null) {
            player.sendMessage("editing in progress");
            return;
        }
        this.player = player;
        this.setSelectionInv();
        this.player.openInventory(this.inv);
    }

    public final void edit(InventoryClickEvent ev) {
        if (this.inSelectionInv()) {
            ev.setCancelled(true);
            this.setDayInv(ev.getRawSlot() + 1);
        } else {

        }
    }

    public final boolean isEditing(Player player) {
        return (this.player == null) ? false : this.player.equals(player);
    }

    public final void stopEditing() {
        if (!this.inSelectionInv()) {
            List<ItemStack> items = Arrays
                    .asList(this.inv.getContents())
                    .stream()
                    .filter((ItemStack is) -> (is != null))
                    .filter((ItemStack is) -> !PlayerCalendar.isAir(is))
                    .collect(Collectors.toList());
            for (int i = 0; i < items.size(); i++) {
                this.man.config.set("rewards.door" + this.editingDay + ".item" + (i + 1), items.get(i));
            }
            this.man.doors[this.editingDay - 1].update(this.man.config);

            try {
                this.man.config.save(this.man.cfgFile);
            } catch (IOException ex) {
                this.plugin.log(Level.WARNING, "config io error");
            }
        }
        this.player = null;
    }
}
