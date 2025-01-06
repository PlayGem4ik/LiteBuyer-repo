package plugin.holybuyer;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.sqlite.JDBC;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;

public class SQLManager {
    private final File file;

    /**
     * @param file файл, куда будет записываться статистика игроков
     */
    public SQLManager(File file) throws IOException, SQLException {
        this.file = file;

        if (!file.exists()) {
            file.createNewFile();
        }

        DriverManager.registerDriver(new JDBC());

        Connection conn = DriverManager.getConnection(JDBC.PREFIX + LiteBuyer.inst().getDataFolder().getPath() + File.separator + file.getName());
        Statement statement = conn.createStatement();

        statement.execute("CREATE TABLE IF NOT EXISTS player_points ( UUID TEXT PRIMARY KEY ON CONFLICT IGNORE, Points INTEGER DEFAULT 1.0);");
        statement.execute("CREATE TABLE IF NOT EXISTS player_multipliers ( UUID TEXT PRIMARY KEY ON CONFLICT IGNORE, Multiplier REAL DEFAULT 0);");
        statement.execute("CREATE TABLE IF NOT EXISTS items_sold ( UUID TEXT PRIMARY KEY ON CONFLICT IGNORE, Items INTEGER DEFAULT 0);");

        statement.close();
        conn.close();
    }

    public void setPoints(UUID uuid, int points) throws SQLException {
        Connection conn = DriverManager.getConnection(JDBC.PREFIX + LiteBuyer.inst().getDataFolder().getPath() + File.separator + file.getName());
        PreparedStatement statement = conn.prepareStatement("INSERT INTO player_points (UUID, Points) VALUES (?, ?) ON CONFLICT(UUID) DO UPDATE SET Points = excluded.Points;");

        statement.setString(1, uuid.toString());
        statement.setInt(2, points);
        statement.executeUpdate();

        conn.close();
    }

    public int getPoints(UUID uuid) throws SQLException {
        Connection conn = DriverManager.getConnection(JDBC.PREFIX + LiteBuyer.inst().getDataFolder().getPath() + File.separator + file.getName());
        PreparedStatement statement = conn.prepareStatement("SELECT Points FROM player_points WHERE UUID = ?;");

        statement.setString(1, uuid.toString());
        statement.execute();
        ResultSet res = statement.executeQuery();

        int i = res.getInt(1);

        conn.close();

        return i;
    }

    public void setMultiplier(UUID uuid, float multiplier) throws SQLException {
        String sqlStatement = "INSERT INTO player_multipliers (UUID, Multiplier) VALUES (?, ?) ON CONFLICT(UUID) DO UPDATE SET Multiplier = excluded.Multiplier;";

        Connection conn = DriverManager.getConnection(JDBC.PREFIX + LiteBuyer.inst().getDataFolder().getPath() + File.separator + file.getName());
        PreparedStatement statement = conn.prepareStatement(sqlStatement);

        statement.setString(1, uuid.toString());
        statement.setFloat(2, multiplier);
        statement.executeUpdate();

        conn.close();
    }


    public float getMultiplier(UUID uuid) throws SQLException {
        Connection conn = DriverManager.getConnection(JDBC.PREFIX + LiteBuyer.inst().getDataFolder().getPath() + File.separator + file.getName());
        PreparedStatement statement = conn.prepareStatement("SELECT Multiplier FROM player_multipliers WHERE UUID = ?;");

        statement.setString(1, uuid.toString());
        statement.execute();

        ResultSet res = statement.executeQuery();

        float f = res.getFloat(1);

        res.close();
        conn.close();

        return (f > 0) ? f : 1.0f;
    }

    public void setSoldItems(UUID uuid, int items) throws SQLException {
        Connection conn = DriverManager.getConnection(JDBC.PREFIX + LiteBuyer.inst().getDataFolder().getPath() + File.separator + file.getName());
        PreparedStatement statement = conn.prepareStatement("INSERT INTO items_sold (UUID, Items) VALUES (?, ?) ON CONFLICT(UUID) DO UPDATE SET Items = excluded.Items;");

        statement.setString(1, uuid.toString());
        statement.setInt(2, items);
        statement.executeUpdate();

        conn.close();
        statement.close();
    }

    public int getSoldItems(UUID uuid) throws SQLException {
        Connection conn = DriverManager.getConnection(JDBC.PREFIX + LiteBuyer.inst().getDataFolder().getPath() + File.separator + file.getName());
        PreparedStatement statement = conn.prepareStatement("SELECT Items FROM items_sold WHERE UUID = ?;");

        statement.setString(1, uuid.toString());
        statement.execute();

        ResultSet res = statement.executeQuery();

        int i = res.getInt(1);

        res.close();
        statement.close();
        conn.close();

        return i;
    }

    public void migrateTo() throws SQLException {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new File(LiteBuyer.inst().getDataFolder(), "database.yml"));
        Connection conn = DriverManager.getConnection(JDBC.PREFIX + LiteBuyer.inst().getDataFolder().getPath() + File.separator + file.getName());

        for (String s : yaml.getConfigurationSection("points").getKeys(false)) {
            PreparedStatement stmnt = conn.prepareStatement("INSERT INTO player_points (UUID, Points) VALUES (?, ?);");

            if (Bukkit.getPlayer(s) != null) {
                stmnt.setString(1, Bukkit.getPlayer(s).getUniqueId().toString());
                stmnt.setInt(2, yaml.getInt("points." + s));
                stmnt.executeUpdate();
            }

            stmnt.close();
        }

        for (String s : yaml.getConfigurationSection("multipliers").getKeys(false)) {
            PreparedStatement stmnt = conn.prepareStatement("INSERT INTO player_multipliers (UUID, Multiplier) VALUES (?, ?);");

            if (Bukkit.getPlayer(s) != null) {
                stmnt.setString(1, Bukkit.getPlayer(s).getUniqueId().toString());
                stmnt.setFloat(2, yaml.getInt("multipliers." + s));
                stmnt.executeUpdate();
            }

            stmnt.close();
        }

        conn.close();
    }
}
