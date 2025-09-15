package ee.vikk.kanban.model;

import ee.vikk.kanban.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Task operations
 */
public class TaskDAO {

    /**
     * Save a new task to database
     * @param task Task to save
     * @return Task with generated ID
     * @throws SQLException if database operation fails
     */
    public Task save(Task task) throws SQLException {
        String sql = "INSERT INTO tasks (column_id, title, description, priority, position, created_at, due_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, task.getColumnId());
            stmt.setString(2, task.getTitle());
            stmt.setString(3, task.getDescription());
            stmt.setString(4, task.getPriority() != null ? task.getPriority().name() : null);
            stmt.setInt(5, task.getPosition());
            stmt.setTimestamp(6, Timestamp.valueOf(task.getCreatedAt()));
            
            if (task.getDueDate() != null) {
                stmt.setDate(7, Date.valueOf(task.getDueDate()));
            } else {
                stmt.setNull(7, Types.DATE);
            }
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating task failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    task.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating task failed, no ID obtained.");
                }
            }
        }
        
        return task;
    }

    /**
     * Find task by ID
     * @param id Task ID
     * @return Task or null if not found
     * @throws SQLException if database operation fails
     */
    public Task findById(Integer id) throws SQLException {
        String sql = "SELECT id, column_id, title, description, priority, position, created_at, due_date FROM tasks WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTask(rs);
                }
            }
        }
        
        return null;
    }

    /**
     * Find all tasks for a column
     * @param columnId Column ID
     * @return List of tasks ordered by position
     * @throws SQLException if database operation fails
     */
    public List<Task> findByColumnId(Integer columnId) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT id, column_id, title, description, priority, position, created_at, due_date FROM tasks WHERE column_id = ? ORDER BY position";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, columnId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
            }
        }
        
        return tasks;
    }

    /**
     * Get next position for a column
     * @param columnId Column ID
     * @return Next available position
     * @throws SQLException if database operation fails
     */
    public int getNextPosition(Integer columnId) throws SQLException {
        String sql = "SELECT COALESCE(MAX(position), 0) + 1 FROM tasks WHERE column_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, columnId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 1; // Default to position 1 if no tasks exist
    }

    /**
     * Update task
     * @param task Task to update
     * @throws SQLException if database operation fails
     */
    public void update(Task task) throws SQLException {
        String sql = "UPDATE tasks SET column_id = ?, title = ?, description = ?, priority = ?, position = ?, due_date = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, task.getColumnId());
            stmt.setString(2, task.getTitle());
            stmt.setString(3, task.getDescription());
            stmt.setString(4, task.getPriority() != null ? task.getPriority().name() : null);
            stmt.setInt(5, task.getPosition());
            
            if (task.getDueDate() != null) {
                stmt.setDate(6, Date.valueOf(task.getDueDate()));
            } else {
                stmt.setNull(6, Types.DATE);
            }
            
            stmt.setInt(7, task.getId());
            
            stmt.executeUpdate();
        }
    }

    /**
     * Delete task by ID
     * @param id Task ID
     * @throws SQLException if database operation fails
     */
    public void deleteById(Integer id) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Map ResultSet to Task object
     * @param rs ResultSet
     * @return Task object
     * @throws SQLException if database operation fails
     */
    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setColumnId(rs.getInt("column_id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        
        String priorityStr = rs.getString("priority");
        if (priorityStr != null) {
            task.setPriority(Task.Priority.valueOf(priorityStr));
        }
        
        task.setPosition(rs.getInt("position"));
        task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        Date dueDate = rs.getDate("due_date");
        if (dueDate != null) {
            task.setDueDate(dueDate.toLocalDate());
        }
        
        return task;
    }
}
