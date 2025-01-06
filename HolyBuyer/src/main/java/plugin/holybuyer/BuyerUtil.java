package plugin.holybuyer;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BuyerUtil {

    /**
     * @param p player
     * @return array with size 2, array[0] - calculated money, array[1] - calculated points
     */
    public static float[] calculateAll(Player p) {
        float[] result = new float[2];
        float money = 0, points = 0;

        if (isBuyerInventory(p.getOpenInventory().getTopInventory())) {
            Inventory bInv = p.getOpenInventory().getTopInventory();

            for (int i = 0; i < bInv.getSize(); i++) {
                for (String s : LiteBuyer.inst().getConfig().getStringList("seller.items-to-sell")) {
                    try {
                        Material mat = Material.valueOf(s.split(";")[0].trim());
                        float x1Money = Float.parseFloat(s.split(";")[1].trim());
                        int x1Points = Integer.parseInt(s.split(";")[2].trim());

                        if (bInv.getItem(i) != null && !isBuyerItem(bInv.getItem(i))) {
                            if (bInv.getItem(i).getType() == mat) {
                                money += x1Money * LiteBuyer.getSQL().getMultiplier(p.getUniqueId()) * bInv.getItem(i).getAmount();
                                points += x1Points * bInv.getItem(i).getAmount();
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        p.sendMessage(HexUtil.translate("[Buyer] >> &cнеизвестный материал " + s.split(";")[0] + ", &Fсообщите об этом администрации"));
                        break;
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }

                result[0] = money;
                result[1] = points;
            }
        }

        return result;
    }

    /**
     * updates buyer multiplier
     *
     * @param p player, that need update multiplier
     */
    public static void updateMultiplier(Player p) throws SQLException {
        for (String s : LiteBuyer.inst().getConfig().getStringList("seller.multipliers")) {
            float targetMultiplier = Float.parseFloat(s.split(";")[0].trim());
            int targetPoints = Integer.parseInt(s.split(";")[1].trim());

            if (LiteBuyer.getSQL().getPoints(p.getUniqueId()) >= targetPoints) {
                LiteBuyer.getSQL().setMultiplier(p.getUniqueId(), targetMultiplier);
            }
        }
    }

    /**
     * @param player игрок, для которого был создан инвентарь скупщика
     * @return созданный инвентарь
     */
    public static Inventory getBuyerInventory(Player player) throws SQLException {
        Inventory buyerInventory = Bukkit.createInventory(null, 54, HexUtil.translate(LiteBuyer.inst().getConfig().getString("seller.title")));
        ItemStack sell = new ItemStack(Material.valueOf(LiteBuyer.inst().getConfig().getString("seller.items-in-menu.sell.material")));
        ItemStack info = new ItemStack(Material.valueOf(LiteBuyer.inst().getConfig().getString("seller.items-in-menu.info.material")));
        ItemStack divider = new ItemStack(Material.valueOf(LiteBuyer.inst().getConfig().getString("seller.items-in-menu.divider.material")));

        List<String> sellLore = new ArrayList<>();
        for (String s : LiteBuyer.inst().getConfig().getStringList("seller.items-in-menu.sell.lore")) {
            sellLore.add(PlaceholderAPI.setPlaceholders(player, HexUtil.translate(s
                    .replace("{multiplier}", "" + LiteBuyer.getSQL().getMultiplier(player.getUniqueId()))
                    .replace("{money}", "" + calculateAll(player)[0])
                    .replace("{receivedPoints}", "" + calculateAll(player)[1])
                    .replace("{points}", "" + LiteBuyer.getSQL().getPoints(player.getUniqueId())))));
        }

        ItemMeta sellMeta = sell.getItemMeta();
        sellMeta.setDisplayName(PlaceholderAPI.setPlaceholders(player, HexUtil.translate(LiteBuyer.inst().getConfig().getString("seller.items-in-menu.sell.name"))));
        sellMeta.setLore(sellLore);
        sellMeta.setCustomModelData(0);
        sellMeta.getPersistentDataContainer().set(new NamespacedKey(LiteBuyer.inst(), "isbuyeritem"), PersistentDataType.STRING, "Type 1");
        sell.setItemMeta(sellMeta);
        LiteBuyer.inst().getConfig().getIntegerList("seller.items-in-menu.sell.slot").forEach(integer -> {
            buyerInventory.setItem(integer, sell);
        });

        List<String> infoLore = new ArrayList<>();
        for (String s : LiteBuyer.inst().getConfig().getStringList("seller.items-in-menu.info.lore")) {
            infoLore.add(PlaceholderAPI.setPlaceholders(player, HexUtil.translate(s
                    .replace("{multiplier}", "" + LiteBuyer.getSQL().getMultiplier(player.getUniqueId()))
                    .replace("{money}", "" + calculateAll(player)[0])
                    .replace("{receivedPoints}", "" + calculateAll(player)[1])
                    .replace("{points}", "" + LiteBuyer.getSQL().getPoints(player.getUniqueId())))));
        }
        ItemMeta infoMeta = info.getItemMeta();

        infoMeta.setDisplayName(PlaceholderAPI.setPlaceholders(player, HexUtil.translate(LiteBuyer.inst().getConfig().getString("seller.items-in-menu.info.name"))));
        infoMeta.setLore(infoLore);
        infoMeta.setCustomModelData(1);
        infoMeta.getPersistentDataContainer().set(new NamespacedKey(LiteBuyer.inst(), "isbuyeritem"), PersistentDataType.STRING, "Type 1");
        info.setItemMeta(infoMeta);
        LiteBuyer.inst().getConfig().getIntegerList("seller.items-in-menu.info.slot").forEach(integer -> {
            buyerInventory.setItem(integer, info);
        });

        ItemMeta dividerMeta = divider.getItemMeta();

        dividerMeta.setDisplayName(" ");
        dividerMeta.setCustomModelData(2);
        dividerMeta.getPersistentDataContainer().set(new NamespacedKey(LiteBuyer.inst(), "isbuyeritem"), PersistentDataType.STRING, "Type 1");
        divider.setItemMeta(dividerMeta);
        LiteBuyer.inst().getConfig().getIntegerList("seller.items-in-menu.divider.slot").forEach(integer -> {
            buyerInventory.setItem(integer, divider);
        });

        return buyerInventory;
    }

    public static boolean isBuyerInventory(Inventory inventory) {
        for (ItemStack i : inventory.getContents()) {
            if (i != null && i.getItemMeta() != null) {
                if (i.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(LiteBuyer.inst(), "isbuyeritem"), PersistentDataType.STRING) && i.getItemMeta().hasDisplayName() && i.getItemMeta().hasLore() && i.getItemMeta().hasCustomModelData()) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isBuyerItem(ItemStack i) {
        if (i != null && i.getItemMeta() != null) {
            return i.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(LiteBuyer.inst(), "isbuyeritem"), PersistentDataType.STRING);
        }

        return false;
    }

    public static void sell(Player player) throws SQLException {
        if (isBuyerInventory(player.getOpenInventory().getTopInventory())) {
            if (calculateAll(player)[0] != 0 && calculateAll(player)[1] != 0) {
                int soldItems = 0;

                for (String s1 : LiteBuyer.inst().getConfig().getStringList("seller.message-on-sell")) {
                    player.sendMessage(HexUtil.translate(s1
                            .replace("{points}", "" + LiteBuyer.getSQL().getPoints(player.getUniqueId()))
                            .replace("{multiplier}", "" + LiteBuyer.getSQL().getMultiplier(player.getUniqueId()))
                            .replace("{receivedPoints}", "" + calculateAll(player)[1])
                            .replace("{money}", "" + calculateAll(player)[0])));
                }

                updateMultiplier(player);

                LiteBuyer.getSQL().setPoints(player.getUniqueId(), (int) (LiteBuyer.getSQL().getPoints(player.getUniqueId()) + calculateAll(player)[1]));
                LiteBuyer.getEconomy().depositPlayer(player, calculateAll(player)[0]);

                for (int i = 0; i < player.getOpenInventory().getTopInventory().getSize(); i++) {
                    for (String s : LiteBuyer.inst().getConfig().getStringList("seller.items-to-sell")) {
                        Material mat = Material.valueOf(s.split(";")[0].trim());

                        if (player.getOpenInventory().getTopInventory().getItem(i) != null && !isBuyerItem(player.getOpenInventory().getTopInventory().getItem(i))) {
                            if (player.getOpenInventory().getTopInventory().getItem(i).getType() == mat) {
                                soldItems += player.getOpenInventory().getTopInventory().getItem(i).getAmount();
                                player.getOpenInventory().getTopInventory().clear(i);
                            }
                        }

                    }
                }

                if (soldItems > 0) {
                    LiteBuyer.getSQL().setSoldItems(player.getUniqueId(), LiteBuyer.getSQL().getSoldItems(player.getUniqueId()) + soldItems);
                }
            }
        }
    }
