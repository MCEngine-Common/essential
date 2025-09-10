package io.github.mcengine.common.essential.database.sqlite;

import io.github.mcengine.common.essential.database.IMCEngineEssentialDB;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;

/**
 * SQLite implementation for the Essential module database.
 * <p>
 * This implementation establishes a persistent connection and provides
 * simple SQL helpers.
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
            }
        }

        this.databaseUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        Connection tmp = null;
        try {
            tmp = DriverManager.getConnection(databaseUrl);
            try (Statement pragma = tmp.createStatement()) {
                pragma.execute("PRAGMA foreign_keys = ON");
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to open SQLite connection: " + e.getMessage());
        }
        this.conn = tmp;
    }

    /** {@inheritDoc} */
    @Override
    public void executeQuery(String sql) {
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("SQLite executeQuery failed: " + e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public <T> T getValue(String sql, Class<T> type) {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (!rs.next()) return null;
            Object raw = rs.getObject(1);
            return coerce(raw, type);

        } catch (SQLException e) {
            throw new RuntimeException("SQLite getValue failed: " + e.getMessage(), e);
        }
    }

    /** Coerces a JDBC scalar to the requested type. */
    @SuppressWarnings("unchecked")
    private static <T> T coerce(Object raw, Class<T> type) {
        if (raw == null) return null;
        if (type == String.class)  return (T) String.valueOf(raw);
        if (type == Integer.class) return (T) Integer.valueOf(raw instanceof Number ? ((Number) raw).intValue() : Integer.parseInt(raw.toString()));
        if (type == Long.class)    return (T) Long.valueOf(raw instanceof Number ? ((Number) raw).longValue() : Long.parseLong(raw.toString()));
        if (type == Double.class)  return (T) Double.valueOf(raw instanceof Number ? ((Number) raw).doubleValue() : Double.parseDouble(raw.toString()));
        if (type == Float.class)   return (T) Float.valueOf(raw instanceof Number ? ((Number) raw).floatValue() : Float.parseFloat(raw.toString()));
        if (type == Boolean.class) {
            if (raw instanceof Boolean b) return (T) b;
            String s = raw.toString().trim().toLowerCase();
            if ("1".equals(s) || "true".equals(s))  return (T) Boolean.TRUE;
            if ("0".equals(s) || "false".equals(s)) return (T) Boolean.FALSE;
        }
        throw new IllegalArgumentException("Unsupported scalar type: " + type.getName());
    }
}
