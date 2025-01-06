package plugin.holybuyer;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public class Expansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "litebuyer";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Mangarin7z (aka PlayGem)";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        //%litebuyer_items-sold_{player}%
        //%litebuyer_points_{player}%
        //%litebuyer_multiplier_{player}%
        String[] args = params.split("_");

        if (args[0].equals("points")) {
            if (args.length == 1) {
                try {
                    return "" + LiteBuyer.getSQL().getPoints(player.getUniqueId());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else if (args.length == 2) {
                String placeholder = args[1].replace("-", "_");

                if (PlaceholderAPI.containsBracketPlaceholders(placeholder)) {
                    if (Bukkit.getOfflinePlayer(PlaceholderAPI.setBracketPlaceholders(player, placeholder)) != null) {
                        try {
                            return "" + LiteBuyer.getSQL().getPoints(Bukkit.getPlayer(PlaceholderAPI.setBracketPlaceholders(player, placeholder)).getUniqueId());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else {
                    if (Bukkit.getOfflinePlayer(args[1]) != null) {
                        try {
                            return "" + LiteBuyer.getSQL().getPoints(Bukkit.getPlayer(args[1]).getUniqueId());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                return "Данный игрок оффлайн";
            }
        } else if (args[0].equals("multiplier")) {
            if (args.length == 1) {
                try {
                    if (LiteBuyer.getSQL().getMultiplier(player.getUniqueId()) == 0) {
                        return "" + ((LiteBuyer.getSQL().getMultiplier(player.getUniqueId()) > 1.0) ? LiteBuyer.getSQL().getMultiplier(player.getUniqueId()) : 1.0);
                    } else {
                        return "" + LiteBuyer.getSQL().getMultiplier(player.getUniqueId());
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else if (args.length == 2) {
                String placeholder = args[1].replace("-", "_");

                if (PlaceholderAPI.containsBracketPlaceholders(placeholder)) {
                    try {
                        if (Bukkit.getOfflinePlayer(PlaceholderAPI.setPlaceholders(player, placeholder)) != null) {
                            return "" + ((LiteBuyer.getSQL().getMultiplier(Bukkit.getPlayer(PlaceholderAPI.setPlaceholders(player, placeholder)).getUniqueId()) > 1.0) ? LiteBuyer.getSQL().getMultiplier(Bukkit.getPlayer(PlaceholderAPI.setPlaceholders(player, placeholder)).getUniqueId()) : 1.0);
                        } else {
                            return "Данный игрок оффлайн";
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        if (Bukkit.getOfflinePlayer(args[1]) != null) {
                            return "" + ((LiteBuyer.getSQL().getMultiplier(Bukkit.getPlayer(args[1]).getUniqueId()) > 1.0) ? LiteBuyer.getSQL().getMultiplier(Bukkit.getPlayer(args[1]).getUniqueId()) : 1.0);
                        }

                        return "Данный игрок оффлайн";
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } else if (args[0].equals("solditems")) {
            if (args.length == 1) {
                try {
                    return "" + LiteBuyer.getSQL().getSoldItems(Bukkit.getOfflinePlayer(args[1]).getUniqueId());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else if (args.length == 2) {
                String placeholder = args[1].replace("-", "_");

                if (PlaceholderAPI.containsBracketPlaceholders(placeholder)) {
                    try {
                        if (Bukkit.getOfflinePlayer(PlaceholderAPI.setPlaceholders(player, placeholder)) != null) {
                            return "" + LiteBuyer.getSQL().getSoldItems(Bukkit.getOfflinePlayer(PlaceholderAPI.setPlaceholders(player, placeholder)).getUniqueId());
                        } else {
                            return "Данный игрок оффлайн";
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        if (Bukkit.getOfflinePlayer(args[1]) != null) {
                            return "" + LiteBuyer.getSQL().getSoldItems(Bukkit.getOfflinePlayer(args[1]).getUniqueId());
                        }

                        return "Данный игрок оффлайн";
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return params;
    }
}
