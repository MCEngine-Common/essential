package io.github.mcengine.common.essential.database.mysql;

import io.github.mcengine.common.essential.database.IMCEngineEssentialDB;
import org.bukkit.plugin.Plugin;

import java.sql.*;

/**
 * MySQL implementation for the Essential module database.
 * <p>
 * Establishes a persistent connection and provides simple SQL helpers.
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

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                + "?useSSL=false&autoReconnect=true&characterEncoding=utf8";

        Connection tmp = null;
        try {
            tmp = DriverManager.getConnection(jdbcUrl, user, pass);
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to connect to MySQL: " + e.getMessage());
        }
        this.conn = tmp;
    }

    /** {@inheritDoc} */
    @Override
    public void executeQuery(String sql) {
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("MySQL executeQuery failed: " + e.getMessage(), e);
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
            throw new RuntimeException("MySQL getValue failed: " + e.getMessage(), e);
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
