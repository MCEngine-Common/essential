package io.github.mcengine.common.essential.database.postgresql;

import io.github.mcengine.common.essential.database.IMCEngineEssentialDB;
import org.bukkit.plugin.Plugin;

import java.sql.*;

/**
 * PostgreSQL implementation for the Essential module database.
 * <p>
 * Establishes a persistent connection and provides simple SQL helpers.
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
        }
        this.conn = tmp;
    }

    /** {@inheritDoc} */
    @Override
    public void executeQuery(String sql) {
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("PostgreSQL executeQuery failed: " + e.getMessage(), e);
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
            throw new RuntimeException("PostgreSQL getValue failed: " + e.getMessage(), e);
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
