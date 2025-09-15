package ee.vikk.kanban.model;

import ee.vikk.kanban.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Board operations
 */
public class BoardDAO {

    /**
     * Save a new board to database
     * @param board Board to save
     * @return Board with generated ID
     * @throws SQLException if database operation fails
     */
    public Board save(Board board) throws SQLException {
        String sql = "INSERT INTO boards (name, created_at) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, board.getName());
            stmt.setTimestamp(2, Timestamp.valueOf(board.getCreatedAt()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating board failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    board.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating board failed, no ID obtained.");
                }
            }
        }
        
        return board;
    }

    /**
     * Find board by ID
     * @param id Board ID
     * @return Board or null if not found
     * @throws SQLException if database operation fails
     */
    public Board findById(Integer id) throws SQLException {
        String sql = "SELECT id, name, created_at FROM boards WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Board(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        }
        
        return null;
    }

    /**
     * Find all boards
     * @return List of all boards
     * @throws SQLException if database operation fails
     */
    public List<Board> findAll() throws SQLException {
        List<Board> boards = new ArrayList<>();
        String sql = "SELECT id, name, created_at FROM boards ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                boards.add(new Board(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        }
        
        return boards;
    }

    /**
     * Update board
     * @param board Board to update
     * @throws SQLException if database operation fails
     */
    public void update(Board board) throws SQLException {
        String sql = "UPDATE boards SET name = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, board.getName());
            stmt.setInt(2, board.getId());
            
            stmt.executeUpdate();
        }
    }

    /**
     * Delete board by ID
     * @param id Board ID
     * @throws SQLException if database operation fails
     */
    public void deleteById(Integer id) throws SQLException {
        String sql = "DELETE FROM boards WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
