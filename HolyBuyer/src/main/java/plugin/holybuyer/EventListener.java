package plugin.holybuyer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EventListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) throws SQLException {
        Player p = (Player) e.getWhoClicked();

        if (e.getClickedInventory() != null && BuyerUtil.isBuyerInventory(e.getClickedInventory())) {
            if (e.getCurrentItem() != null) {
                if (BuyerUtil.isBuyerItem(e.getCurrentItem())) {
                    if (LiteBuyer.inst().getConfig().getIntegerList("seller.items-in-menu.sell.slot").contains(e.getSlot())) {
                        e.setCancelled(true);
                        BuyerUtil.sell(p);
                    } else {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void updateMenu(InventoryOpenEvent e) {
        if (BuyerUtil.isBuyerInventory(e.getInventory())) {
            Player player = (Player) e.getPlayer();
            Inventory buyerInventory = e.getInventory();

            ItemStack sell = new ItemStack(Material.valueOf(LiteBuyer.inst().getConfig().getString("seller.items-in-menu.sell.material")));
            ItemStack info = new ItemStack(Material.valueOf(LiteBuyer.inst().getConfig().getString("seller.items-in-menu.info.material")));
            Bukkit.getScheduler().runTaskTimer(LiteBuyer.inst(), bukkitTask -> {
                List<String> sellLore = new ArrayList<>();
                for (String s : LiteBuyer.inst().getConfig().getStringList("seller.items-in-menu.sell.lore")) {
                    try {
                        sellLore.add(HexUtil.translate(s
                                .replace("{multiplier}", "" + LiteBuyer.getSQL().getMultiplier(player.getUniqueId()))
                                .replace("{money}", "" + BuyerUtil.calculateAll(player)[0])
                                .replace("{receivedPoints}", "" + BuyerUtil.calculateAll(player)[1])
                                .replace("{points}", "" + LiteBuyer.getSQL().getPoints(player.getUniqueId()))));
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                ItemMeta sellMeta = sell.getItemMeta();
                sellMeta.setDisplayName(HexUtil.translate(LiteBuyer.inst().getConfig().getString("seller.items-in-menu.sell.name")));
                sellMeta.setLore(sellLore);
                sellMeta.setCustomModelData(0);
                sellMeta.getPersistentDataContainer().set(new NamespacedKey(LiteBuyer.inst(), "isbuyeritem"), PersistentDataType.STRING, "Type 1");
                sellMeta.getPersistentDataContainer().set(new NamespacedKey(LiteBuyer.inst(), "taskid"), PersistentDataType.INTEGER, bukkitTask.getTaskId());
                sell.setItemMeta(sellMeta);
                LiteBuyer.inst().getConfig().getIntegerList("seller.items-in-menu.sell.slot").forEach(integer -> {
                    buyerInventory.getItem(integer).setItemMeta(sellMeta);
                });

                List<String> infoLore = new ArrayList<>();
                for (String s : LiteBuyer.inst().getConfig().getStringList("seller.items-in-menu.info.lore")) {
                    try {
                        infoLore.add(HexUtil.translate(s
                                .replace("{multiplier}", "" + LiteBuyer.getSQL().getMultiplier(player.getUniqueId()))
                                .replace("{money}", "" + BuyerUtil.calculateAll(player)[0])
                                .replace("{receivedPoints}", "" + BuyerUtil.calculateAll(player)[1])
                                .replace("{points}", "" + LiteBuyer.getSQL().getPoints(player.getUniqueId()))));
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                ItemMeta infoMeta = info.getItemMeta();
                infoMeta.setDisplayName(HexUtil.translate(LiteBuyer.inst().getConfig().getString("seller.items-in-menu.info.name")));
                infoMeta.setLore(infoLore);
                infoMeta.setCustomModelData(1);
                infoMeta.getPersistentDataContainer().set(new NamespacedKey(LiteBuyer.inst(), "isbuyeritem"), PersistentDataType.STRING, "Type 1");
                info.setItemMeta(infoMeta);
                LiteBuyer.inst().getConfig().getIntegerList("seller.items-in-menu.info.slot").forEach(integer -> {
                    buyerInventory.getItem(integer).setItemMeta(infoMeta);
                });
            }, 20, 20);
        }
    }

    @EventHandler
    public void giveItemsOnClose(InventoryCloseEvent e) {
        if (BuyerUtil.isBuyerInventory(e.getInventory())) {
            for (int i = 0; i < e.getInventory().getSize(); i++) {
                if (e.getInventory().getItem(i) != null && e.getInventory().getItem(i).getItemMeta() != null && !e.getInventory().getItem(i).getItemMeta().getPersistentDataContainer().has(new NamespacedKey(LiteBuyer.inst(), "isbuyeritem"), PersistentDataType.STRING)) {
                    if (e.getInventory().firstEmpty() != -1) {
                        Bukkit.getScheduler().cancelTask(e.getInventory().getItem(LiteBuyer.inst().getConfig().getIntegerList("seller.items-in-menu.sell.slot").get(0)).getItemMeta().getPersistentDataContainer().get(new NamespacedKey(LiteBuyer.inst(), "taskid"), PersistentDataType.INTEGER));
                        e.getPlayer().getInventory().addItem(e.getInventory().getItem(i));
                    } else {
                        Bukkit.getScheduler().cancelTask(e.getInventory().getItem(LiteBuyer.inst().getConfig().getIntegerList("seller.items-in-menu.sell.slot").get(0)).getItemMeta().getPersistentDataContainer().get(new NamespacedKey(LiteBuyer.inst(), "taskid"), PersistentDataType.INTEGER));
                        e.getPlayer().getWorld().dropItemNaturally(e.getPlayer().getLocation(), e.getInventory().getItem(i));
                    }
                }
            }
        }
    }
}
