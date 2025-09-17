package ee.vikk.kanban.service;

import ee.vikk.kanban.model.Column;
import ee.vikk.kanban.model.ColumnDAO;
import ee.vikk.kanban.model.Board;
import ee.vikk.kanban.model.BoardDAO;

import java.sql.SQLException;
import java.util.List;

/**
 * Service class for Column business logic
 */
public class ColumnService {
    private final ColumnDAO columnDAO;
    private final BoardDAO boardDAO;

    /**
     * Constructor with DAO dependencies
     * @param columnDAO Column data access object
     * @param boardDAO Board data access object
     */
    public ColumnService(ColumnDAO columnDAO, BoardDAO boardDAO) {
        this.columnDAO = columnDAO;
        this.boardDAO = boardDAO;
    }

    /**
     * Default constructor with default DAOs
     */
    public ColumnService() {
        this(new ColumnDAO(), new BoardDAO());
    }

    /**
     * Add a new column to a board
     * @param boardId Board ID to add column to
     * @param columnName Column name
     * @return Created column
     * @throws SQLException if database operation fails
     * @throws ValidationException if validation fails
     */
    public Column addColumn(Integer boardId, String columnName) throws SQLException, ValidationException {
        validateColumnName(columnName);
        validateBoardExists(boardId);
        
        // Get next position for the column
        int nextPosition = getNextColumnPosition(boardId);
        
        // Create column with default color
        Column column = new Column(boardId, columnName, nextPosition);
        column.setColor("#808080"); // Default gray color
        
        return columnDAO.save(column);
    }

    /**
     * Add a new column with custom color
     * @param boardId Board ID to add column to
     * @param columnName Column name
     * @param color Column color (hex format)
     * @return Created column
     * @throws SQLException if database operation fails
     * @throws ValidationException if validation fails
     */
    public Column addColumn(Integer boardId, String columnName, String color) throws SQLException, ValidationException {
        validateColumnName(columnName);
        validateBoardExists(boardId);
        validateColor(color);
        
        // Get next position for the column
        int nextPosition = getNextColumnPosition(boardId);
        
        // Create column
        Column column = new Column(boardId, columnName, nextPosition);
        column.setColor(color);
        
        return columnDAO.save(column);
    }

    /**
     * Get all columns for a board
     * @param boardId Board ID
     * @return List of columns ordered by position
     * @throws SQLException if database operation fails
     */
    public List<Column> getColumnsByBoard(Integer boardId) throws SQLException {
        return columnDAO.findByBoardId(boardId);
    }

    /**
     * Update column name
     * @param columnId Column ID
     * @param newName New column name
     * @throws SQLException if database operation fails
     * @throws ValidationException if validation fails
     */
    public void updateColumnName(Integer columnId, String newName) throws SQLException, ValidationException {
        validateColumnName(newName);
        
        Column column = columnDAO.findById(columnId);
        if (column == null) {
            throw new ValidationException("Column not found with ID: " + columnId);
        }
        
        column.setName(newName);
        columnDAO.update(column);
    }

    /**
     * Update column color
     * @param columnId Column ID
     * @param newColor New column color (hex format)
     * @throws SQLException if database operation fails
     * @throws ValidationException if validation fails
     */
    public void updateColumnColor(Integer columnId, String newColor) throws SQLException, ValidationException {
        validateColor(newColor);
        
        Column column = columnDAO.findById(columnId);
        if (column == null) {
            throw new ValidationException("Column not found with ID: " + columnId);
        }
        
        column.setColor(newColor);
        columnDAO.update(column);
    }

    /**
     * Delete column (only if empty)
     * @param columnId Column ID
     * @throws SQLException if database operation fails
     * @throws ValidationException if validation fails
     */
    public void deleteColumn(Integer columnId) throws SQLException, ValidationException {
        Column column = columnDAO.findById(columnId);
        if (column == null) {
            throw new ValidationException("Column not found with ID: " + columnId);
        }
        
        // Check if column has tasks (this would require TaskDAO, but for now we'll allow deletion)
        columnDAO.deleteById(columnId);
    }

    /**
     * Get next position for a column in a board
     * @param boardId Board ID
     * @return Next available position
     * @throws SQLException if database operation fails
     */
    private int getNextColumnPosition(Integer boardId) throws SQLException {
        List<Column> columns = columnDAO.findByBoardId(boardId);
        return columns.size() + 1;
    }

    /**
     * Validate column name
     * @param name Column name to validate
     * @throws ValidationException if validation fails
     */
    private void validateColumnName(String name) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Column name cannot be empty");
        }
        if (name.length() > 50) {
            throw new ValidationException("Column name cannot be longer than 50 characters");
        }
    }

    /**
     * Validate that board exists
     * @param boardId Board ID to validate
     * @throws ValidationException if validation fails
     * @throws SQLException if database operation fails
     */
    private void validateBoardExists(Integer boardId) throws ValidationException, SQLException {
        Board board = boardDAO.findById(boardId);
        if (board == null) {
            throw new ValidationException("Board not found with ID: " + boardId);
        }
    }

    /**
     * Validate color format
     * @param color Color to validate (hex format)
     * @throws ValidationException if validation fails
     */
    private void validateColor(String color) throws ValidationException {
        if (color == null || !color.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new ValidationException("Color must be in hex format (#RRGGBB)");
        }
    }
}
