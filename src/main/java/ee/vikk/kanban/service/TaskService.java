package ee.vikk.kanban.service;

import ee.vikk.kanban.model.Task;
import ee.vikk.kanban.model.TaskDAO;
import ee.vikk.kanban.model.Column;
import ee.vikk.kanban.model.ColumnDAO;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for Task business logic
 */
public class TaskService {
    private final TaskDAO taskDAO;
    private final ColumnDAO columnDAO;

    /**
     * Constructor with DAO dependencies
     * @param taskDAO Task data access object
     * @param columnDAO Column data access object
     */
    public TaskService(TaskDAO taskDAO, ColumnDAO columnDAO) {
        this.taskDAO = taskDAO;
        this.columnDAO = columnDAO;
    }

    /**
     * Default constructor with default DAOs
     */
    public TaskService() {
        this(new TaskDAO(), new ColumnDAO());
    }

    /**
     * Create a new task in a column
     * @param columnId Column ID where task should be added
     * @param title Task title
     * @param description Task description (optional)
     * @return Created task
     * @throws SQLException if database operation fails
     * @throws ValidationException if validation fails
     */
    public Task createTask(Integer columnId, String title, String description) throws SQLException, ValidationException {
        validateTaskTitle(title);
        validateColumnExists(columnId);
        
        // Get next position in the column
        int nextPosition = taskDAO.getNextPosition(columnId);
        
        // Create task
        Task task = new Task(columnId, title, nextPosition);
        task.setDescription(description);
        task.setCreatedAt(LocalDateTime.now());
        task.setPriority(Task.Priority.MEDIUM); // Default priority
        
        return taskDAO.save(task);
    }

    /**
     * Create a new task with only title
     * @param columnId Column ID where task should be added
     * @param title Task title
     * @return Created task
     * @throws SQLException if database operation fails
     * @throws ValidationException if validation fails
     */
    public Task createTask(Integer columnId, String title) throws SQLException, ValidationException {
        return createTask(columnId, title, null);
    }

    /**
     * Get task by ID
     * @param taskId Task ID
     * @return Task or null if not found
     * @throws SQLException if database operation fails
     */
    public Task getTask(Integer taskId) throws SQLException {
        return taskDAO.findById(taskId);
    }

    /**
     * Get all tasks for a column
     * @param columnId Column ID
     * @return List of tasks ordered by position
     * @throws SQLException if database operation fails
     */
    public List<Task> getTasksByColumn(Integer columnId) throws SQLException {
        return taskDAO.findByColumnId(columnId);
    }

    /**
     * Update task title and description
     * @param taskId Task ID
     * @param newTitle New task title
     * @param newDescription New task description
     * @throws SQLException if database operation fails
     * @throws ValidationException if validation fails
     */
    public void updateTask(Integer taskId, String newTitle, String newDescription) throws SQLException, ValidationException {
        validateTaskTitle(newTitle);
        
        Task task = taskDAO.findById(taskId);
        if (task == null) {
            throw new ValidationException("Task not found with ID: " + taskId);
        }
        
        task.setTitle(newTitle);
        task.setDescription(newDescription);
        taskDAO.update(task);
    }

    /**
     * Move task to another column
     * @param taskId Task ID
     * @param targetColumnId Target column ID
     * @throws SQLException if database operation fails
     * @throws ValidationException if validation fails
     */
    public void moveTask(Integer taskId, Integer targetColumnId) throws SQLException, ValidationException {
        validateColumnExists(targetColumnId);
        
        Task task = taskDAO.findById(taskId);
        if (task == null) {
            throw new ValidationException("Task not found with ID: " + taskId);
        }
        
        // Get next position in target column
        int nextPosition = taskDAO.getNextPosition(targetColumnId);
        
        task.setColumnId(targetColumnId);
        task.setPosition(nextPosition);
        taskDAO.update(task);
    }

    /**
     * Delete task
     * @param taskId Task ID
     * @throws SQLException if database operation fails
     * @throws ValidationException if validation fails
     */
    public void deleteTask(Integer taskId) throws SQLException, ValidationException {
        Task task = taskDAO.findById(taskId);
        if (task == null) {
            throw new ValidationException("Task not found with ID: " + taskId);
        }
        
        taskDAO.deleteById(taskId);
    }

    /**
     * Set task priority
     * @param taskId Task ID
     * @param priority New priority
     * @throws SQLException if database operation fails
     * @throws ValidationException if validation fails
     */
    public void setTaskPriority(Integer taskId, Task.Priority priority) throws SQLException, ValidationException {
        Task task = taskDAO.findById(taskId);
        if (task == null) {
            throw new ValidationException("Task not found with ID: " + taskId);
        }
        
        task.setPriority(priority);
        taskDAO.update(task);
    }

    /**
     * Validate task title
     * @param title Task title to validate
     * @throws ValidationException if validation fails
     */
    private void validateTaskTitle(String title) throws ValidationException {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Task title cannot be empty");
        }
        if (title.length() > 200) {
            throw new ValidationException("Task title cannot be longer than 200 characters");
        }
    }

    /**
     * Validate that column exists
     * @param columnId Column ID to validate
     * @throws ValidationException if validation fails
     * @throws SQLException if database operation fails
     */
    private void validateColumnExists(Integer columnId) throws ValidationException, SQLException {
        Column column = columnDAO.findById(columnId);
        if (column == null) {
            throw new ValidationException("Column not found with ID: " + columnId);
        }
    }
}
