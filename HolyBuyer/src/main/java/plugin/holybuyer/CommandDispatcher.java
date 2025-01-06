package plugin.holybuyer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class CommandDispatcher implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            if (args[0].equals("reload")) {
                File config = new File(LiteBuyer.inst().getDataFolder(), "config.yml");

                if (!config.exists()) {
                    LiteBuyer.inst().saveDefaultConfig();
                    LiteBuyer.inst().saveConfig();

                    for (String s : LiteBuyer.inst().getConfig().getStringList("seller.messages.on-missing-config")) {
                        sender.sendMessage(HexUtil.translate(s));
                    }

                    return true;
                }

                try {
                    LiteBuyer.inst().getConfig().load(config);

                    for (String s : LiteBuyer.inst().getConfig().getStringList("seller.messages.on-reload")) {
                        sender.sendMessage(HexUtil.translate(s));
                    }
                } catch (IOException | InvalidConfigurationException e) {
                    throw new RuntimeException(e);
                }

                return true;
            }
            return true;
        }

        if (sender instanceof Player) {
            Player p = (Player) sender;

            try {
                p.openInventory(BuyerUtil.getBuyerInventory(p));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return true;
    }
}
