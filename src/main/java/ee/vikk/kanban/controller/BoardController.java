package ee.vikk.kanban.controller;

import ee.vikk.kanban.model.Board;
import ee.vikk.kanban.model.Column;
import ee.vikk.kanban.model.Task;
import ee.vikk.kanban.service.BoardService;
import ee.vikk.kanban.service.TaskService;
import ee.vikk.kanban.service.ColumnService;
import ee.vikk.kanban.service.ValidationException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for board view with columns and tasks
 */
public class BoardController implements Initializable {

    @FXML
    private Label boardTitleLabel;
    
    @FXML
    private HBox columnsContainer;
    
    @FXML
    private Label statusLabel;

    private BoardService boardService;
    private TaskService taskService;
    private ColumnService columnService;
    private Board currentBoard;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        boardService = new BoardService();
        taskService = new TaskService();
        columnService = new ColumnService();
    }

    /**
     * Load and display a board
     * @param boardId Board ID to load
     */
    public void loadBoard(Integer boardId) {
        try {
            currentBoard = boardService.getBoardWithColumns(boardId);
            if (currentBoard == null) {
                showError("Board not found");
                return;
            }
            
            boardTitleLabel.setText(currentBoard.getName());
            displayColumns();
            setStatusMessage("Loaded board: " + currentBoard.getName());
            
        } catch (SQLException e) {
            showError("Failed to load board: " + e.getMessage());
        }
    }

    /**
     * Display columns with their tasks
     */
    private void displayColumns() {
        columnsContainer.getChildren().clear();
        
        for (Column column : currentBoard.getColumns()) {
            VBox columnBox = createColumnBox(column);
            columnsContainer.getChildren().add(columnBox);
        }

        // Add "Add Column" button at the end
        VBox addColumnBox = createAddColumnBox();
        columnsContainer.getChildren().add(addColumnBox);
    }

    /**
     * Create a column box with tasks
     * @param column Column to create box for
     * @return VBox representing the column
     */
    private VBox createColumnBox(Column column) {
        VBox columnBox = new VBox(10);
        columnBox.getStyleClass().add("column-box");
        columnBox.setPrefWidth(300);
        columnBox.setMaxWidth(300);
        columnBox.setPadding(new Insets(10));
        
        // Column header
        Label columnHeader = new Label(column.getName());
        columnHeader.getStyleClass().add("column-header");
        columnHeader.setStyle("-fx-background-color: " + column.getColor() + "; -fx-text-fill: white; -fx-padding: 8px; -fx-font-weight: bold;");
        
        // Add task button
        Button addTaskButton = new Button("+ Add Task");
        addTaskButton.getStyleClass().add("add-task-button");
        addTaskButton.setOnAction(e -> showAddTaskDialog(column));
        
        // Tasks container
        VBox tasksContainer = new VBox(5);
        tasksContainer.getStyleClass().add("tasks-container");
        
        // Load tasks for this column
        try {
            List<Task> tasks = taskService.getTasksByColumn(column.getId());
            for (Task task : tasks) {
                VBox taskBox = createTaskBox(task);
                tasksContainer.getChildren().add(taskBox);
            }
        } catch (SQLException e) {
            showError("Failed to load tasks for column: " + column.getName());
        }
        
        columnBox.getChildren().addAll(columnHeader, addTaskButton, tasksContainer);
        VBox.setVgrow(tasksContainer, Priority.ALWAYS);

        // Setup drop target for the tasks container
        setupDropTarget(tasksContainer, column);

        return columnBox;
    }

    /**
     * Create a task box
     * @param task Task to create box for
     * @return VBox representing the task
     */
    private VBox createTaskBox(Task task) {
        VBox taskBox = new VBox(5);
        taskBox.getStyleClass().add("task-box");
        taskBox.setPadding(new Insets(8));
        
        // Task title
        Label titleLabel = new Label(task.getTitle());
        titleLabel.getStyleClass().add("task-title");
        titleLabel.setWrapText(true);
        
        // Task description (if exists)
        if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) {
            Label descLabel = new Label(task.getDescription());
            descLabel.getStyleClass().add("task-description");
            descLabel.setWrapText(true);
            taskBox.getChildren().add(descLabel);
        }
        
        // Priority indicator
        if (task.getPriority() != null) {
            Label priorityLabel = new Label(task.getPriority().name());
            priorityLabel.getStyleClass().add("task-priority");
            priorityLabel.getStyleClass().add("priority-" + task.getPriority().name().toLowerCase());
            taskBox.getChildren().add(priorityLabel);
        }
        
        taskBox.getChildren().add(0, titleLabel);
        
        // Add click handler for editing
        taskBox.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                showEditTaskDialog(task);
            }
        });

        // Add drag and drop functionality
        setupDragAndDrop(taskBox, task);

        return taskBox;
    }

    /**
     * Show dialog to add new task
     * @param column Column to add task to
     */
    private void showAddTaskDialog(Column column) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Add New Task");
        dialog.setHeaderText("Add task to column: " + column.getName());

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        TextField titleField = new TextField();
        titleField.setPromptText("Task title (required)");
        
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Task description (optional)");
        descriptionArea.setPrefRowCount(3);

        content.getChildren().addAll(
            new Label("Title:"), titleField,
            new Label("Description:"), descriptionArea
        );

        dialog.getDialogPane().setContent(content);

        // Enable/disable add button based on title input
        Button addButton = (Button) dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            addButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    return taskService.createTask(column.getId(), titleField.getText().trim(), 
                                                descriptionArea.getText().trim().isEmpty() ? null : descriptionArea.getText().trim());
                } catch (SQLException | ValidationException e) {
                    showError("Failed to create task: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Task> result = dialog.showAndWait();
        if (result.isPresent()) {
            setStatusMessage("Task created: " + result.get().getTitle());
            displayColumns(); // Refresh the view
        }
    }

    /**
     * Show dialog to edit task
     * @param task Task to edit
     */
    private void showEditTaskDialog(Task task) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit Task");
        dialog.setHeaderText("Edit task: " + task.getTitle());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, deleteButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        TextField titleField = new TextField(task.getTitle());
        TextArea descriptionArea = new TextArea(task.getDescription() != null ? task.getDescription() : "");
        descriptionArea.setPrefRowCount(3);

        content.getChildren().addAll(
            new Label("Title:"), titleField,
            new Label("Description:"), descriptionArea
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    taskService.updateTask(task.getId(), titleField.getText().trim(), 
                                         descriptionArea.getText().trim().isEmpty() ? null : descriptionArea.getText().trim());
                    return true;
                } catch (SQLException | ValidationException e) {
                    showError("Failed to update task: " + e.getMessage());
                    return false;
                }
            } else if (dialogButton == deleteButtonType) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Delete Task");
                confirmAlert.setHeaderText("Are you sure you want to delete this task?");
                confirmAlert.setContentText(task.getTitle());
                
                Optional<ButtonType> confirmResult = confirmAlert.showAndWait();
                if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                    try {
                        taskService.deleteTask(task.getId());
                        return true;
                    } catch (SQLException | ValidationException e) {
                        showError("Failed to delete task: " + e.getMessage());
                        return false;
                    }
                }
            }
            return false;
        });

        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get()) {
            setStatusMessage("Task updated");
            displayColumns(); // Refresh the view
        }
    }

    /**
     * Set status message
     * @param message Status message
     */
    private void setStatusMessage(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("error-message");
        statusLabel.getStyleClass().add("status-message");
    }

    /**
     * Show error message
     * @param message Error message
     */
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("status-message");
        statusLabel.getStyleClass().add("error-message");

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Setup drag functionality for a task box
     * @param taskBox Task box to make draggable
     * @param task Task data
     */
    private void setupDragAndDrop(VBox taskBox, Task task) {
        taskBox.setOnDragDetected(event -> {
            Dragboard dragboard = taskBox.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(task.getId().toString());
            dragboard.setContent(content);

            taskBox.getStyleClass().add("task-dragging");
            event.consume();
        });

        taskBox.setOnDragDone(event -> {
            taskBox.getStyleClass().remove("task-dragging");
            event.consume();
        });
    }

    /**
     * Setup drop target for a tasks container
     * @param tasksContainer Container to accept drops
     * @param targetColumn Target column
     */
    private void setupDropTarget(VBox tasksContainer, Column targetColumn) {
        tasksContainer.setOnDragOver(event -> {
            if (event.getGestureSource() != tasksContainer &&
                event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        tasksContainer.setOnDragEntered(event -> {
            if (event.getGestureSource() != tasksContainer &&
                event.getDragboard().hasString()) {
                tasksContainer.getStyleClass().add("drop-target");
            }
            event.consume();
        });

        tasksContainer.setOnDragExited(event -> {
            tasksContainer.getStyleClass().remove("drop-target");
            event.consume();
        });

        tasksContainer.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;

            if (dragboard.hasString()) {
                try {
                    Integer taskId = Integer.parseInt(dragboard.getString());
                    taskService.moveTask(taskId, targetColumn.getId());
                    success = true;
                    setStatusMessage("Task moved to " + targetColumn.getName());
                    displayColumns(); // Refresh the view
                } catch (SQLException | ValidationException e) {
                    showError("Failed to move task: " + e.getMessage());
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    /**
     * Create "Add Column" box
     * @return VBox with add column button
     */
    private VBox createAddColumnBox() {
        VBox addColumnBox = new VBox(10);
        addColumnBox.getStyleClass().add("add-column-box");
        addColumnBox.setPrefWidth(250);
        addColumnBox.setMinWidth(250);
        addColumnBox.setMaxWidth(250);

        Button addColumnButton = new Button("+ Add Column");
        addColumnButton.getStyleClass().add("add-column-button");
        addColumnButton.setPrefWidth(230);
        addColumnButton.setOnAction(e -> showAddColumnDialog());

        addColumnBox.getChildren().add(addColumnButton);
        VBox.setMargin(addColumnButton, new Insets(10));

        return addColumnBox;
    }

    /**
     * Show add column dialog
     */
    private void showAddColumnDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Column");
        dialog.setHeaderText("Add New Column");
        dialog.setContentText("Column name:");

        dialog.showAndWait().ifPresent(columnName -> {
            if (!columnName.trim().isEmpty()) {
                try {
                    columnService.addColumn(currentBoard.getId(), columnName.trim());
                    setStatusMessage("Column '" + columnName + "' added successfully");

                    // Refresh board data and display
                    currentBoard = boardService.getBoardWithColumns(currentBoard.getId());
                    displayColumns();
                } catch (SQLException | ValidationException e) {
                    showError("Failed to add column: " + e.getMessage());
                }
            }
        });
    }
}
