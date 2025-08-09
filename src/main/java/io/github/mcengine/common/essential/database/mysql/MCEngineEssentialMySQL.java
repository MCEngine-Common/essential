package io.github.mcengine.common.essential.database.mysql;

import io.github.mcengine.api.essential.database.IMCEngineEssentialDB;
import org.bukkit.plugin.Plugin;

import java.sql.*;

/**
 * MySQL implementation for the Essential module database.
 * <p>
 * Establishes a persistent connection and ensures a placeholder table exists
 * for future module data.
 */
public class MCEngineEssentialMySQL implements IMCEngineEssentialDB {

    /** The Bukkit plugin instance providing config and logging. */
    private final Plugin plugin;

    /** Persistent MySQL connection. */
    private final Connection conn;

    /**
     * Builds the MySQL database connection from config keys:
     * <ul>
     *     <li>{@code database.mysql.host} (default: {@code localhost})</li>
     *     <li>{@code database.mysql.port} (default: {@code 3306})</li>
     *     <li>{@code database.mysql.name} (default: {@code mcengine_essential})</li>
     *     <li>{@code database.mysql.user} (default: {@code root})</li>
     *     <li>{@code database.mysql.password} (default: empty)</li>
     * </ul>
     *
     * @param plugin Bukkit plugin instance
     */
    public MCEngineEssentialMySQL(Plugin plugin) {
        this.plugin = plugin;

        String host = plugin.getConfig().getString("database.mysql.host", "localhost");
        String port = plugin.getConfig().getString("database.mysql.port", "3306");
        String dbName = plugin.getConfig().getString("database.mysql.name", "mcengine_essential");
        String user = plugin.getConfig().getString("database.mysql.user", "root");
        String pass = plugin.getConfig().getString("database.mysql.password", "");

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&autoReconnect=true&characterEncoding=utf8";

        Connection tmp = null;
        try {
            tmp = DriverManager.getConnection(jdbcUrl, user, pass);
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to connect to MySQL: " + e.getMessage());
            e.printStackTrace();
        }
        this.conn = tmp;
    }

    /** {@inheritDoc} */
    @Override
    public Connection getDBConnection() {
        return conn;
    }
}
