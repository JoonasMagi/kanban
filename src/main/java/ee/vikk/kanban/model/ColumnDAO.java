package ee.vikk.kanban.model;

import ee.vikk.kanban.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Column operations
 */
public class ColumnDAO {

    /**
     * Save a new column to database
     * @param column Column to save
     * @return Column with generated ID
     * @throws SQLException if database operation fails
     */
    public Column save(Column column) throws SQLException {
        String sql = "INSERT INTO columns (board_id, name, position, color) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, column.getBoardId());
            stmt.setString(2, column.getName());
            stmt.setInt(3, column.getPosition());
            stmt.setString(4, column.getColor());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating column failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    column.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating column failed, no ID obtained.");
                }
            }
        }
        
        return column;
    }

    /**
     * Find column by ID
     * @param id Column ID
     * @return Column or null if not found
     * @throws SQLException if database operation fails
     */
    public Column findById(Integer id) throws SQLException {
        String sql = "SELECT id, board_id, name, position, color FROM columns WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Column(
                        rs.getInt("id"),
                        rs.getInt("board_id"),
                        rs.getString("name"),
                        rs.getInt("position"),
                        rs.getString("color")
                    );
                }
            }
        }
        
        return null;
    }

    /**
     * Find all columns for a board
     * @param boardId Board ID
     * @return List of columns ordered by position
     * @throws SQLException if database operation fails
     */
    public List<Column> findByBoardId(Integer boardId) throws SQLException {
        List<Column> columns = new ArrayList<>();
        String sql = "SELECT id, board_id, name, position, color FROM columns WHERE board_id = ? ORDER BY position";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, boardId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    columns.add(new Column(
                        rs.getInt("id"),
                        rs.getInt("board_id"),
                        rs.getString("name"),
                        rs.getInt("position"),
                        rs.getString("color")
                    ));
                }
            }
        }
        
        return columns;
    }

    /**
     * Update column
     * @param column Column to update
     * @throws SQLException if database operation fails
     */
    public void update(Column column) throws SQLException {
        String sql = "UPDATE columns SET name = ?, position = ?, color = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, column.getName());
            stmt.setInt(2, column.getPosition());
            stmt.setString(3, column.getColor());
            stmt.setInt(4, column.getId());
            
            stmt.executeUpdate();
        }
    }

    /**
     * Delete column by ID
     * @param id Column ID
     * @throws SQLException if database operation fails
     */
    public void deleteById(Integer id) throws SQLException {
        String sql = "DELETE FROM columns WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
