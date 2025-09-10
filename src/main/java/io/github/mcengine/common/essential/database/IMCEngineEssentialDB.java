package io.github.mcengine.common.essential.database;

/**
 * Minimal persistence contract for the Essential module.
 * <p>
 * Provides low-level helpers to run arbitrary SQL and fetch a single scalar value
 * <strong>without</strong> exposing a JDBC {@code Connection}. This keeps call sites
 * backend-agnostic while still enabling flexible SQL when needed.
 */
public interface IMCEngineEssentialDB {

    /**
     * Executes a SQL statement that does not return a result set
     * (e.g., {@code CREATE TABLE}, {@code INSERT}, {@code UPDATE}, {@code DELETE}, {@code ALTER}).
     *
     * @param sql a complete SQL statement
     * @throws RuntimeException if execution fails
     */
    void executeQuery(String sql);

    /**
     * Executes a SQL query expected to return a single scalar value (first column of the first row),
     * coercing the value to the requested Java type.
     *
     * <p>Supported target types include: {@code String}, {@code Integer}, {@code Long},
     * {@code Double}, {@code Float}, and {@code Boolean} (accepting 1/0 or true/false).</p>
     *
     * @param sql  a complete SQL query
     * @param type target scalar class
     * @param <T>  type parameter for the coerced result
     * @return coerced value, or {@code null} if no rows are returned
     * @throws IllegalArgumentException if {@code type} is unsupported
     * @throws RuntimeException         if execution fails
     */
    <T> T getValue(String sql, Class<T> type);
}
