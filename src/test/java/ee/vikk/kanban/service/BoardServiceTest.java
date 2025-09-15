package ee.vikk.kanban.service;

import ee.vikk.kanban.database.DatabaseConnection;
import ee.vikk.kanban.model.Board;
import ee.vikk.kanban.model.Column;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for BoardService - User Story #1: Board Creation
 */
class BoardServiceTest {

    private BoardService boardService;

    @BeforeEach
    void setUp() throws SQLException {
        // Initialize test database
        DatabaseConnection.initializeDatabase();
        boardService = new BoardService();
    }

    @AfterEach
    void tearDown() {
        DatabaseConnection.closeConnection();
    }

    /**
     * Test User Story #1: Board Creation
     * Acceptance Criteria:
     * ✓ User can enter board name
     * ✓ Board is created with default 3 columns (TODO, IN PROGRESS, DONE)
     * ✓ Board is saved to database
     * ✓ New board opens immediately after creation
     */
    @Test
    void testCreateBoard_ShouldCreateBoardWithDefaultColumns() throws SQLException, ValidationException {
        // Given
        String boardName = "Test Project Board";

        // When
        Board createdBoard = boardService.createBoard(boardName);

        // Then
        assertNotNull(createdBoard, "Created board should not be null");
        assertNotNull(createdBoard.getId(), "Board should have an ID after creation");
        assertEquals(boardName, createdBoard.getName(), "Board name should match input");
        assertNotNull(createdBoard.getCreatedAt(), "Board should have creation timestamp");

        // Verify board has 3 default columns
        List<Column> columns = createdBoard.getColumns();
        assertEquals(3, columns.size(), "Board should have exactly 3 default columns");

        // Verify column names and order
        assertEquals("TODO", columns.get(0).getName(), "First column should be TODO");
        assertEquals("IN PROGRESS", columns.get(1).getName(), "Second column should be IN PROGRESS");
        assertEquals("DONE", columns.get(2).getName(), "Third column should be DONE");

        // Verify column positions
        assertEquals(1, columns.get(0).getPosition(), "TODO column should be at position 1");
        assertEquals(2, columns.get(1).getPosition(), "IN PROGRESS column should be at position 2");
        assertEquals(3, columns.get(2).getPosition(), "DONE column should be at position 3");

        // Verify columns belong to the board
        for (Column column : columns) {
            assertEquals(createdBoard.getId(), column.getBoardId(), 
                "Column should belong to the created board");
            assertNotNull(column.getId(), "Column should have an ID");
            assertNotNull(column.getColor(), "Column should have a color");
        }
    }

    @Test
    void testCreateBoard_WithEmptyName_ShouldThrowValidationException() {
        // Given
        String emptyName = "";

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> boardService.createBoard(emptyName));
        
        assertEquals("Board name cannot be empty", exception.getMessage());
    }

    @Test
    void testCreateBoard_WithNullName_ShouldThrowValidationException() {
        // Given
        String nullName = null;

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> boardService.createBoard(nullName));
        
        assertEquals("Board name cannot be empty", exception.getMessage());
    }

    @Test
    void testCreateBoard_WithWhitespaceOnlyName_ShouldThrowValidationException() {
        // Given
        String whitespaceName = "   ";

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> boardService.createBoard(whitespaceName));
        
        assertEquals("Board name cannot be empty", exception.getMessage());
    }

    @Test
    void testCreateBoard_WithTooLongName_ShouldThrowValidationException() {
        // Given
        String longName = "a".repeat(101); // 101 characters

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> boardService.createBoard(longName));
        
        assertEquals("Board name cannot be longer than 100 characters", exception.getMessage());
    }

    @Test
    void testGetBoardWithColumns_ShouldReturnBoardWithColumns() throws SQLException, ValidationException {
        // Given
        Board createdBoard = boardService.createBoard("Test Board");

        // When
        Board retrievedBoard = boardService.getBoardWithColumns(createdBoard.getId());

        // Then
        assertNotNull(retrievedBoard, "Retrieved board should not be null");
        assertEquals(createdBoard.getId(), retrievedBoard.getId(), "Board IDs should match");
        assertEquals(createdBoard.getName(), retrievedBoard.getName(), "Board names should match");
        assertEquals(3, retrievedBoard.getColumns().size(), "Board should have 3 columns");
    }

    @Test
    void testGetBoardWithColumns_WithNonExistentId_ShouldReturnNull() throws SQLException {
        // Given
        Integer nonExistentId = 99999;

        // When
        Board retrievedBoard = boardService.getBoardWithColumns(nonExistentId);

        // Then
        assertNull(retrievedBoard, "Non-existent board should return null");
    }
}
