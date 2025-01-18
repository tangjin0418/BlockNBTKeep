package org.tjdev.custom.blocknbtkeep;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.tjdev.util.tjpluginutil.spigot.PluginUtil;
import org.tjdev.util.tjpluginutil.spigot.nbt.customblockdata.CustomBlockData;
import org.tjdev.util.tjpluginutil.spigot.nbt.morepersistentdatatypes.DataType;
import org.tjdev.util.tjpluginutil.spigot.object.IncludedListener;
import org.tjdev.util.tjpluginutil.spigot.object.TJPlugin;

import java.util.HashMap;
import java.util.Map;

import static org.tjdev.util.tjpluginutil.object.TJPluginExtension.requester;

public final class BlockNBTKeep extends TJPlugin implements IncludedListener {
    @Override
    public void enable() {
        requester = "myself";
        requestType = PluginUtil.PLUGIN_TYPE.FREE;
        CustomBlockData.registerListener(plugin);
        ITEM = new NamespacedKey(plugin, "ITEM");
    }

    private static NamespacedKey ITEM;

    public static NamespacedKey getKey() {
        return ITEM;
    }

    @EventHandler
    public void place(BlockPlaceEvent e) {
        var i = e.getItemInHand().clone();
        i.setItemMeta(null);
        if (i.equals(e.getItemInHand())) return; // we don't process normal item

        new CustomBlockData(e.getBlock(), plugin).set(ITEM, DataType.ITEM_STACK, e.getItemInHand());
    }

    private final Map<Block, ItemStack> BLOCK_TO_ITEM = new HashMap<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void breakItem(BlockBreakEvent e) {
        var data = new CustomBlockData(e.getBlock(), plugin);
        if (data.has(ITEM)) BLOCK_TO_ITEM.put(e.getBlock(), data.get(ITEM, DataType.ITEM_STACK));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void drop(BlockDropItemEvent e) {
        var drop = BLOCK_TO_ITEM.remove(e.getBlock());
        if (drop == null) return;
        if (e.getItems().isEmpty()) return;
        drop.setAmount(1);
        e.getItems().clear();
        e.getItems().add(e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), drop));
    }
}
