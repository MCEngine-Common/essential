package io.github.mcengine.common.essential;

import io.github.mcengine.api.core.util.MCEngineCoreApiDispatcher;
import io.github.mcengine.api.essential.database.IMCEngineEssentialDB;
import io.github.mcengine.common.essential.database.mysql.MCEngineEssentialMySQL;
import io.github.mcengine.common.essential.database.postgresql.MCEngineEssentialPostgreSQL;
import io.github.mcengine.common.essential.database.sqlite.MCEngineEssentialSQLite;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;

/**
 * The {@code MCEngineEssentialCommon} class provides a lightweight facade for
 * shared "Essential" features. It wires a Bukkit {@link Plugin} instance to the
 * internal command {@link MCEngineCoreApiDispatcher}, so you can register command
 * namespaces, subcommands, and tab completers in a consistent way across the module.
 * <p>
 * Additionally, this class initializes and exposes the Essential database connection
 * via a minimal interface. The database backend is selected using {@code database.type}
 * with support for {@code sqlite} (default), {@code mysql}, and {@code postgresql}.
 *
 * <p>Usage pattern:
 * <pre>{@code
 *   MCEngineEssentialCommon essential = new MCEngineEssentialCommon(plugin);
 *   essential.registerNamespace("essential");
 *   essential.registerSubCommand("essential", "ping", new PingCommand());
 *   plugin.getCommand("essential").setExecutor(essential.getDispatcher("essential"));
 *
 *   // DB usage (connection only)
 *   try (Connection conn = essential.getDBConnection()) {
 *       // Perform your own queries/prepared statements...
 *   }
 * }</pre>
 */
public class MCEngineEssentialCommon {

    /**
     * Singleton instance of the Essentials common API.
     */
    private static MCEngineEssentialCommon instance;

    /**
     * The Bukkit plugin instance that owns and initializes this API.
     * <p>
     * Used for configuration access and logging.
     */
    private final Plugin plugin;

    /**
     * Internal command dispatcher used to register namespaces, subcommands,
     * and tab completers for this module.
     */
    private final MCEngineCoreApiDispatcher dispatcher;

    /**
     * Database interface used by the Essential module.
     * <p>
     * The concrete implementation is selected from config ({@code database.type})
     * and created during construction.
     */
    private final IMCEngineEssentialDB db;

    /**
     * Constructs the Essential API and prepares the internal dispatcher and database.
     *
     * @param plugin the Bukkit {@link Plugin} instance bootstrapping this API
     */
    public MCEngineEssentialCommon(Plugin plugin) {
        instance = this;
        this.plugin = plugin;
        this.dispatcher = new MCEngineCoreApiDispatcher();

        String dbType = plugin.getConfig().getString("database.type", "sqlite").toLowerCase();
        switch (dbType) {
            case "sqlite" -> this.db = new MCEngineEssentialSQLite(plugin);
            case "mysql" -> this.db = new MCEngineEssentialMySQL(plugin);
            case "postgresql" -> this.db = new MCEngineEssentialPostgreSQL(plugin);
            default -> throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }

    /**
     * Returns the global API singleton instance.
     *
     * @return the {@link MCEngineEssentialCommon} instance
     */
    public static MCEngineEssentialCommon getApi() {
        return instance;
    }

    /**
     * Returns the Bukkit plugin instance associated with this API.
     *
     * @return the plugin instance
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Registers a command namespace (e.g., {@code "essential"}) for this module.
     *
     * @param namespace unique namespace to register
     */
    public void registerNamespace(String namespace) {
        dispatcher.registerNamespace(namespace);
    }

    /**
     * Binds a Bukkit command (e.g., {@code /essential}) to the internal dispatcher.
     * <p>
     * Typical usage: {@code plugin.getCommand("essential").setExecutor(essential.getDispatcher("essential"))}
     *
     * @param namespace       the command namespace to bind
     * @param commandExecutor an optional fallback {@link CommandExecutor}
     */
    public void bindNamespaceToCommand(String namespace, CommandExecutor commandExecutor) {
        dispatcher.bindNamespaceToCommand(namespace, commandExecutor);
    }

    /**
     * Registers a subcommand under the given namespace.
     *
     * @param namespace the command namespace
     * @param name      the subcommand label
     * @param executor  logic to execute when the subcommand is invoked
     */
    public void registerSubCommand(String namespace, String name, CommandExecutor executor) {
        dispatcher.registerSubCommand(namespace, name, executor);
    }

    /**
     * Registers a tab completer for a subcommand under the given namespace.
     *
     * @param namespace    the command namespace
     * @param subcommand   the subcommand label
     * @param tabCompleter tab completion logic
     */
    public void registerSubTabCompleter(String namespace, String subcommand, TabCompleter tabCompleter) {
        dispatcher.registerSubTabCompleter(namespace, subcommand, tabCompleter);
    }

    /**
     * Obtains the dispatcher to assign as the command executor and tab completer
     * for a Bukkit command mapped to the provided namespace.
     *
     * @param namespace the command namespace
     * @return a {@link CommandExecutor} that routes to the internal dispatcher
     */
    public CommandExecutor getDispatcher(String namespace) {
        return dispatcher.getDispatcher(namespace);
    }

    // --------------------
    // Database conveniences
    // --------------------

    /**
     * Returns the database interface used by this module.
     *
     * @return the {@link IMCEngineEssentialDB} instance
     */
    public IMCEngineEssentialDB getDB() {
        return db;
    }

    /**
     * Retrieves the active SQL database connection being used by the Essential module.
     *
     * @return JDBC {@link Connection} or {@code null} if the connection couldn't be established
     */
    public Connection getDBConnection() {
        return db.getDBConnection();
    }
}
