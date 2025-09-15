package ee.vikk.kanban.service;

import ee.vikk.kanban.database.DatabaseConnection;
import ee.vikk.kanban.model.Board;
import ee.vikk.kanban.model.Column;
import ee.vikk.kanban.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for TaskService - User Story #2: Task Creation
 */
class TaskServiceTest {

    private TaskService taskService;
    private BoardService boardService;
    private Board testBoard;
    private Column testColumn;

    @BeforeEach
    void setUp() throws SQLException, ValidationException {
        // Initialize test database
        DatabaseConnection.initializeDatabase();
        taskService = new TaskService();
        boardService = new BoardService();
        
        // Create test board with columns
        testBoard = boardService.createBoard("Test Board for Tasks");
        testColumn = testBoard.getColumns().get(0); // Get first column (TODO)
    }

    @AfterEach
    void tearDown() {
        DatabaseConnection.closeConnection();
    }

    /**
     * Test User Story #2: Task Creation
     * Acceptance Criteria:
     * ✓ User can enter task title
     * ✓ User can add optional description
     * ✓ Task appears in selected column
     * ✓ Task is saved to database
     */
    @Test
    void testCreateTask_WithTitleAndDescription_ShouldCreateTaskInColumn() throws SQLException, ValidationException {
        // Given
        String taskTitle = "Implement user authentication";
        String taskDescription = "Add login and registration functionality with JWT tokens";

        // When
        Task createdTask = taskService.createTask(testColumn.getId(), taskTitle, taskDescription);

        // Then
        assertNotNull(createdTask, "Created task should not be null");
        assertNotNull(createdTask.getId(), "Task should have an ID after creation");
        assertEquals(taskTitle, createdTask.getTitle(), "Task title should match input");
        assertEquals(taskDescription, createdTask.getDescription(), "Task description should match input");
        assertEquals(testColumn.getId(), createdTask.getColumnId(), "Task should belong to the specified column");
        assertNotNull(createdTask.getCreatedAt(), "Task should have creation timestamp");
        assertEquals(Task.Priority.MEDIUM, createdTask.getPriority(), "Task should have default MEDIUM priority");
        assertEquals(1, createdTask.getPosition(), "First task should be at position 1");

        // Verify task is saved to database
        Task retrievedTask = taskService.getTask(createdTask.getId());
        assertNotNull(retrievedTask, "Task should be retrievable from database");
        assertEquals(createdTask.getId(), retrievedTask.getId(), "Retrieved task should have same ID");
        assertEquals(taskTitle, retrievedTask.getTitle(), "Retrieved task should have same title");
        assertEquals(taskDescription, retrievedTask.getDescription(), "Retrieved task should have same description");
    }

    @Test
    void testCreateTask_WithTitleOnly_ShouldCreateTaskWithoutDescription() throws SQLException, ValidationException {
        // Given
        String taskTitle = "Fix bug in navigation";

        // When
        Task createdTask = taskService.createTask(testColumn.getId(), taskTitle);

        // Then
        assertNotNull(createdTask, "Created task should not be null");
        assertEquals(taskTitle, createdTask.getTitle(), "Task title should match input");
        assertNull(createdTask.getDescription(), "Task description should be null when not provided");
        assertEquals(testColumn.getId(), createdTask.getColumnId(), "Task should belong to the specified column");
    }

    @Test
    void testCreateTask_MultipleTasksInSameColumn_ShouldHaveCorrectPositions() throws SQLException, ValidationException {
        // Given
        String task1Title = "First task";
        String task2Title = "Second task";
        String task3Title = "Third task";

        // When
        Task task1 = taskService.createTask(testColumn.getId(), task1Title);
        Task task2 = taskService.createTask(testColumn.getId(), task2Title);
        Task task3 = taskService.createTask(testColumn.getId(), task3Title);

        // Then
        assertEquals(1, task1.getPosition(), "First task should be at position 1");
        assertEquals(2, task2.getPosition(), "Second task should be at position 2");
        assertEquals(3, task3.getPosition(), "Third task should be at position 3");

        // Verify tasks appear in column
        List<Task> columnTasks = taskService.getTasksByColumn(testColumn.getId());
        assertEquals(3, columnTasks.size(), "Column should contain 3 tasks");
        assertEquals(task1Title, columnTasks.get(0).getTitle(), "First task should be first in list");
        assertEquals(task2Title, columnTasks.get(1).getTitle(), "Second task should be second in list");
        assertEquals(task3Title, columnTasks.get(2).getTitle(), "Third task should be third in list");
    }

    @Test
    void testCreateTask_WithEmptyTitle_ShouldThrowValidationException() {
        // Given
        String emptyTitle = "";

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> taskService.createTask(testColumn.getId(), emptyTitle));
        
        assertEquals("Task title cannot be empty", exception.getMessage());
    }

    @Test
    void testCreateTask_WithNullTitle_ShouldThrowValidationException() {
        // Given
        String nullTitle = null;

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> taskService.createTask(testColumn.getId(), nullTitle));
        
        assertEquals("Task title cannot be empty", exception.getMessage());
    }

    @Test
    void testCreateTask_WithWhitespaceOnlyTitle_ShouldThrowValidationException() {
        // Given
        String whitespaceTitle = "   ";

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> taskService.createTask(testColumn.getId(), whitespaceTitle));
        
        assertEquals("Task title cannot be empty", exception.getMessage());
    }

    @Test
    void testCreateTask_WithTooLongTitle_ShouldThrowValidationException() {
        // Given
        String longTitle = "a".repeat(201); // 201 characters

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> taskService.createTask(testColumn.getId(), longTitle));
        
        assertEquals("Task title cannot be longer than 200 characters", exception.getMessage());
    }

    @Test
    void testCreateTask_WithNonExistentColumn_ShouldThrowValidationException() {
        // Given
        Integer nonExistentColumnId = 99999;
        String taskTitle = "Test task";

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> taskService.createTask(nonExistentColumnId, taskTitle));
        
        assertEquals("Column not found with ID: " + nonExistentColumnId, exception.getMessage());
    }

    @Test
    void testGetTasksByColumn_WithEmptyColumn_ShouldReturnEmptyList() throws SQLException {
        // When
        List<Task> tasks = taskService.getTasksByColumn(testColumn.getId());

        // Then
        assertNotNull(tasks, "Task list should not be null");
        assertTrue(tasks.isEmpty(), "Empty column should return empty task list");
    }

    @Test
    void testGetTasksByColumn_WithTasks_ShouldReturnTasksInOrder() throws SQLException, ValidationException {
        // Given
        taskService.createTask(testColumn.getId(), "Task A");
        taskService.createTask(testColumn.getId(), "Task B");
        taskService.createTask(testColumn.getId(), "Task C");

        // When
        List<Task> tasks = taskService.getTasksByColumn(testColumn.getId());

        // Then
        assertEquals(3, tasks.size(), "Column should contain 3 tasks");
        assertEquals("Task A", tasks.get(0).getTitle(), "Tasks should be ordered by position");
        assertEquals("Task B", tasks.get(1).getTitle(), "Tasks should be ordered by position");
        assertEquals("Task C", tasks.get(2).getTitle(), "Tasks should be ordered by position");
    }

    @Test
    void testUpdateTask_ShouldUpdateTitleAndDescription() throws SQLException, ValidationException {
        // Given
        Task originalTask = taskService.createTask(testColumn.getId(), "Original Title", "Original Description");
        String newTitle = "Updated Title";
        String newDescription = "Updated Description";

        // When
        taskService.updateTask(originalTask.getId(), newTitle, newDescription);

        // Then
        Task updatedTask = taskService.getTask(originalTask.getId());
        assertEquals(newTitle, updatedTask.getTitle(), "Task title should be updated");
        assertEquals(newDescription, updatedTask.getDescription(), "Task description should be updated");
        assertEquals(originalTask.getId(), updatedTask.getId(), "Task ID should remain the same");
        assertEquals(originalTask.getColumnId(), updatedTask.getColumnId(), "Task column should remain the same");
    }

    @Test
    void testDeleteTask_ShouldRemoveTaskFromDatabase() throws SQLException, ValidationException {
        // Given
        Task task = taskService.createTask(testColumn.getId(), "Task to delete");

        // When
        taskService.deleteTask(task.getId());

        // Then
        Task deletedTask = taskService.getTask(task.getId());
        assertNull(deletedTask, "Deleted task should not be found in database");
        
        List<Task> columnTasks = taskService.getTasksByColumn(testColumn.getId());
        assertTrue(columnTasks.isEmpty(), "Column should be empty after task deletion");
    }
}
