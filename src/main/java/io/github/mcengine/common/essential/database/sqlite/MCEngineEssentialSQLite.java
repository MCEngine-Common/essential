package io.github.mcengine.common.essential.database.sqlite;

import io.github.mcengine.api.essential.database.IMCEngineEssentialDB;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;

/**
 * SQLite implementation for the Essential module database.
 * <p>
 * This implementation establishes a persistent connection and ensures
 * a basic table exists for future use.
 */
public class MCEngineEssentialSQLite implements IMCEngineEssentialDB {

    /** The Bukkit plugin instance providing config, paths, and logging. */
    private final Plugin plugin;

    /** JDBC SQLite database URL (file-based). */
    private final String databaseUrl;

    /** Persistent SQLite connection shared by the module. */
    private final Connection conn;

    /**
     * Builds the SQLite database from plugin config:
     * <ul>
     *     <li>{@code database.sqlite.path} → DB file name in plugin data folder (default: {@code essential.db})</li>
     * </ul>
     *
     * @param plugin Bukkit plugin instance
     */
    public MCEngineEssentialSQLite(Plugin plugin) {
        this.plugin = plugin;
        String fileName = plugin.getConfig().getString("database.sqlite.path", "essential.db");
        File dbFile = new File(plugin.getDataFolder(), fileName);

        if (!dbFile.exists()) {
            try {
                if (dbFile.getParentFile() != null) {
                    dbFile.getParentFile().mkdirs();
                }
                boolean created = dbFile.createNewFile();
                if (created) {
                    plugin.getLogger().info("SQLite database file created: " + dbFile.getAbsolutePath());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to create SQLite database file: " + e.getMessage());
                e.printStackTrace();
            }
        }

        this.databaseUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        Connection tmp = null;
        try {
            tmp = DriverManager.getConnection(databaseUrl);
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to open SQLite connection: " + e.getMessage());
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
