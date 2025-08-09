package io.github.mcengine.common.essential.database.postgresql;

import io.github.mcengine.api.essential.database.IMCEngineEssentialDB;
import org.bukkit.plugin.Plugin;

import java.sql.*;

/**
 * PostgreSQL implementation for the Essential module database.
 * <p>
 * Establishes a persistent connection and ensures a placeholder table exists
 * for future module data.
 */
public class MCEngineEssentialPostgreSQL implements IMCEngineEssentialDB {

    /** The Bukkit plugin instance providing config and logging. */
    private final Plugin plugin;

    /** Persistent PostgreSQL connection. */
    private final Connection conn;

    /**
     * Builds the PostgreSQL connection from config keys:
     * <ul>
     *     <li>{@code database.postgresql.host} (default: {@code localhost})</li>
     *     <li>{@code database.postgresql.port} (default: {@code 5432})</li>
     *     <li>{@code database.postgresql.name} (default: {@code mcengine_essential})</li>
     *     <li>{@code database.postgresql.user} (default: {@code postgres})</li>
     *     <li>{@code database.postgresql.password} (default: empty)</li>
     * </ul>
     *
     * @param plugin Bukkit plugin instance
     */
    public MCEngineEssentialPostgreSQL(Plugin plugin) {
        this.plugin = plugin;

        String host = plugin.getConfig().getString("database.postgresql.host", "localhost");
        String port = plugin.getConfig().getString("database.postgresql.port", "5432");
        String dbName = plugin.getConfig().getString("database.postgresql.name", "mcengine_essential");
        String user = plugin.getConfig().getString("database.postgresql.user", "postgres");
        String pass = plugin.getConfig().getString("database.postgresql.password", "");

        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;

        Connection tmp = null;
        try {
            tmp = DriverManager.getConnection(jdbcUrl, user, pass);
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to connect to PostgreSQL: " + e.getMessage());
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
