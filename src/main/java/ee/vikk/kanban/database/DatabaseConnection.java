package ee.vikk.kanban.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database connection manager for SQLite database
 */
public class DatabaseConnection {
    private static final String DATABASE_URL = "jdbc:sqlite:kanban.db";
    private static final String TEST_DATABASE_URL = "jdbc:sqlite:test-kanban.db";
    private static Connection connection;

    /**
     * Get database connection instance
     * @return Connection to SQLite database
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = isTestEnvironment() ? TEST_DATABASE_URL : DATABASE_URL;
            connection = DriverManager.getConnection(url);
        }
        return connection;
    }

    /**
     * Check if we are running in test environment
     * @return true if running tests, false otherwise
     */
    private static boolean isTestEnvironment() {
        // Check if JUnit is in the stack trace
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (className.contains("junit") ||
                className.contains("Test") ||
                className.endsWith("Test")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Initialize database tables
     * @throws SQLException if table creation fails
     */
    public static void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create boards table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS boards (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Create columns table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS columns (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    board_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    position INTEGER NOT NULL,
                    color TEXT DEFAULT '#808080',
                    FOREIGN KEY (board_id) REFERENCES boards(id)
                )
            """);

            // Create tasks table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    column_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT,
                    priority TEXT CHECK(priority IN ('LOW', 'MEDIUM', 'HIGH')),
                    position INTEGER NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    due_date DATE,
                    FOREIGN KEY (column_id) REFERENCES columns(id)
                )
            """);

            // Create tags table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tags (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    color TEXT NOT NULL
                )
            """);

            // Create task_tags junction table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS task_tags (
                    task_id INTEGER NOT NULL,
                    tag_id INTEGER NOT NULL,
                    PRIMARY KEY (task_id, tag_id),
                    FOREIGN KEY (task_id) REFERENCES tasks(id),
                    FOREIGN KEY (tag_id) REFERENCES tags(id)
                )
            """);
        }
    }

    /**
     * Close database connection
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}
