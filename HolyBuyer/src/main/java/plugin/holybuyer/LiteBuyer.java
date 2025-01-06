package plugin.holybuyer;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class LiteBuyer extends JavaPlugin {
    private static LiteBuyer inst;
    private static Economy economy;
    private static SQLManager manager;

    @Override
    public void onLoad() {
        inst = this;
    }

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        File dbFile = new File(getDataFolder().getPath() + File.separator + "buyer-statistic.s3db");

        setupEconomy();
        saveDefaultConfig();
        saveConfig();

        try {
            manager = new SQLManager(dbFile);
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }

        if (new File(getDataFolder(), "database.yml").exists()) {
            Bukkit.getLogger().info("[LiteBuyer] >> у вас находится старая база данных");
            Bukkit.getLogger().info("[LiteBuyer] >> начинается миграция с yml на db...");

            try {
                manager.migrateTo();
                new File(getDataFolder(), "database.yml").delete();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            Bukkit.getLogger().info("[LiteBuyer] >> миграция была завершена!");
            Bukkit.getLogger().info("[LteBuyer] >> данные были перенесены в файл " + dbFile.getName());
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().warning("[LiteBuyer] >> у вас не установлен плагин PlaceholderAPI");
            getLogger().warning("[LiteBuyer] >> некоторые функции могут не работать!");
        }
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("[LiteBuyer] >> у вас не установлен плагин Vault");
            getLogger().warning("[LiteBuyer] >> некоторые функции могут не работать!");
        }

        getCommand("buyer").setExecutor(new CommandDispatcher());
        Bukkit.getPluginManager().registerEvents(new EventListener(), inst);
        new Expansion().register();
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(inst);
    }

    public static LiteBuyer inst() {
         return inst;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }

    public static Economy getEconomy() {
        return economy;
    }

    public static SQLManager getSQL() {
        return manager;
    }
}
