package ee.vikk.kanban.service;

import ee.vikk.kanban.model.Board;
import ee.vikk.kanban.model.Column;
import ee.vikk.kanban.database.DatabaseConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ColumnService
 * Tests User Story #4: Column Addition
 */
class ColumnServiceTest {

    private ColumnService columnService;
    private BoardService boardService;
    private Board testBoard;

    @BeforeEach
    void setUp() throws SQLException, ValidationException {
        // Initialize services
        columnService = new ColumnService();
        boardService = new BoardService();

        // Create test board
        testBoard = boardService.createBoard("Test Board for Columns");
        
        // Clear any existing columns (keep only the default 3)
        // The board should have 3 default columns: TODO, IN PROGRESS, DONE
        assertEquals(3, testBoard.getColumns().size(), "Test board should start with 3 default columns");
    }

    /**
     * Test User Story #4: Column Addition
     * Acceptance Criteria:
     * ✓ User can enter column name
     * ✓ New column appears on board
     * ✓ Column is saved to database
     * ✓ Column can be positioned relative to others
     */
    @Test
    void testAddColumn_WithValidName_ShouldCreateColumn() throws SQLException, ValidationException {
        // Given
        String columnName = "TESTING";

        // When
        Column newColumn = columnService.addColumn(testBoard.getId(), columnName);

        // Then
        assertNotNull(newColumn, "Created column should not be null");
        assertNotNull(newColumn.getId(), "Created column should have an ID");
        assertEquals(columnName, newColumn.getName(), "Column name should match input");
        assertEquals(testBoard.getId(), newColumn.getBoardId(), "Column should belong to test board");
        assertEquals(4, newColumn.getPosition(), "New column should be positioned after existing columns");
        assertEquals("#808080", newColumn.getColor(), "Column should have default gray color");

        // Verify column is saved to database
        List<Column> boardColumns = columnService.getColumnsByBoard(testBoard.getId());
        assertEquals(4, boardColumns.size(), "Board should now have 4 columns");
        
        Column lastColumn = boardColumns.get(3);
        assertEquals(columnName, lastColumn.getName(), "Last column should be the new column");
    }

    @Test
    void testAddColumn_WithCustomColor_ShouldCreateColumnWithColor() throws SQLException, ValidationException {
        // Given
        String columnName = "REVIEW";
        String customColor = "#ff5722";

        // When
        Column newColumn = columnService.addColumn(testBoard.getId(), columnName, customColor);

        // Then
        assertNotNull(newColumn, "Created column should not be null");
        assertEquals(columnName, newColumn.getName(), "Column name should match input");
        assertEquals(customColor, newColumn.getColor(), "Column should have custom color");
        assertEquals(4, newColumn.getPosition(), "New column should be positioned after existing columns");
    }

    @Test
    void testAddColumn_MultipleColumns_ShouldMaintainCorrectPositions() throws SQLException, ValidationException {
        // Given
        String column1Name = "TESTING";
        String column2Name = "REVIEW";
        String column3Name = "DEPLOYMENT";

        // When
        Column column1 = columnService.addColumn(testBoard.getId(), column1Name);
        Column column2 = columnService.addColumn(testBoard.getId(), column2Name);
        Column column3 = columnService.addColumn(testBoard.getId(), column3Name);

        // Then
        assertEquals(4, column1.getPosition(), "First new column should be at position 4");
        assertEquals(5, column2.getPosition(), "Second new column should be at position 5");
        assertEquals(6, column3.getPosition(), "Third new column should be at position 6");

        // Verify all columns are in correct order
        List<Column> boardColumns = columnService.getColumnsByBoard(testBoard.getId());
        assertEquals(6, boardColumns.size(), "Board should have 6 columns total");
        assertEquals("TODO", boardColumns.get(0).getName(), "First column should be TODO");
        assertEquals("IN PROGRESS", boardColumns.get(1).getName(), "Second column should be IN PROGRESS");
        assertEquals("DONE", boardColumns.get(2).getName(), "Third column should be DONE");
        assertEquals(column1Name, boardColumns.get(3).getName(), "Fourth column should be first new column");
        assertEquals(column2Name, boardColumns.get(4).getName(), "Fifth column should be second new column");
        assertEquals(column3Name, boardColumns.get(5).getName(), "Sixth column should be third new column");
    }

    @Test
    void testAddColumn_WithEmptyName_ShouldThrowValidationException() {
        // Given
        String emptyName = "";

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> columnService.addColumn(testBoard.getId(), emptyName));
        
        assertEquals("Column name cannot be empty", exception.getMessage());
    }

    @Test
    void testAddColumn_WithNullName_ShouldThrowValidationException() {
        // Given
        String nullName = null;

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> columnService.addColumn(testBoard.getId(), nullName));
        
        assertEquals("Column name cannot be empty", exception.getMessage());
    }

    @Test
    void testAddColumn_WithTooLongName_ShouldThrowValidationException() {
        // Given
        String longName = "A".repeat(51); // 51 characters, exceeds 50 character limit

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> columnService.addColumn(testBoard.getId(), longName));
        
        assertEquals("Column name cannot be longer than 50 characters", exception.getMessage());
    }

    @Test
    void testAddColumn_WithNonExistentBoard_ShouldThrowValidationException() {
        // Given
        Integer nonExistentBoardId = 99999;
        String columnName = "Test Column";

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> columnService.addColumn(nonExistentBoardId, columnName));
        
        assertEquals("Board not found with ID: " + nonExistentBoardId, exception.getMessage());
    }

    @Test
    void testAddColumn_WithInvalidColor_ShouldThrowValidationException() {
        // Given
        String columnName = "Test Column";
        String invalidColor = "red"; // Should be hex format

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> columnService.addColumn(testBoard.getId(), columnName, invalidColor));
        
        assertEquals("Color must be in hex format (#RRGGBB)", exception.getMessage());
    }

    @Test
    void testUpdateColumnName_WithValidName_ShouldUpdateColumn() throws SQLException, ValidationException {
        // Given
        Column newColumn = columnService.addColumn(testBoard.getId(), "Original Name");
        String updatedName = "Updated Name";

        // When
        columnService.updateColumnName(newColumn.getId(), updatedName);

        // Then
        List<Column> boardColumns = columnService.getColumnsByBoard(testBoard.getId());
        Column updatedColumn = boardColumns.stream()
            .filter(c -> c.getId().equals(newColumn.getId()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(updatedColumn, "Updated column should be found");
        assertEquals(updatedName, updatedColumn.getName(), "Column name should be updated");
    }

    @Test
    void testUpdateColumnColor_WithValidColor_ShouldUpdateColumn() throws SQLException, ValidationException {
        // Given
        Column newColumn = columnService.addColumn(testBoard.getId(), "Test Column");
        String newColor = "#ff5722";

        // When
        columnService.updateColumnColor(newColumn.getId(), newColor);

        // Then
        List<Column> boardColumns = columnService.getColumnsByBoard(testBoard.getId());
        Column updatedColumn = boardColumns.stream()
            .filter(c -> c.getId().equals(newColumn.getId()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(updatedColumn, "Updated column should be found");
        assertEquals(newColor, updatedColumn.getColor(), "Column color should be updated");
    }

    @Test
    void testDeleteColumn_WithValidId_ShouldRemoveColumn() throws SQLException, ValidationException {
        // Given
        Column newColumn = columnService.addColumn(testBoard.getId(), "Column to Delete");
        Integer columnId = newColumn.getId();

        // When
        columnService.deleteColumn(columnId);

        // Then
        List<Column> boardColumns = columnService.getColumnsByBoard(testBoard.getId());
        assertEquals(3, boardColumns.size(), "Board should be back to 3 columns after deletion");
        
        boolean columnExists = boardColumns.stream()
            .anyMatch(c -> c.getId().equals(columnId));
        assertFalse(columnExists, "Deleted column should not exist in board columns");
    }
}
