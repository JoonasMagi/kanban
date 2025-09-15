package ee.vikk.kanban.controller;

import ee.vikk.kanban.model.Board;
import ee.vikk.kanban.service.BoardService;
import ee.vikk.kanban.service.ValidationException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Main controller for the KanBan application
 */
public class MainController implements Initializable {

    @FXML
    private VBox boardListContainer;
    
    @FXML
    private Button createBoardButton;
    
    @FXML
    private Label statusLabel;

    private BoardService boardService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        boardService = new BoardService();
        loadBoards();
        
        createBoardButton.setOnAction(e -> createNewBoard());
    }

    /**
     * Load and display all boards
     */
    private void loadBoards() {
        try {
            boardListContainer.getChildren().clear();
            List<Board> boards = boardService.getAllBoards();
            
            if (boards.isEmpty()) {
                Label emptyLabel = new Label("No boards found. Create your first board!");
                emptyLabel.getStyleClass().add("empty-message");
                boardListContainer.getChildren().add(emptyLabel);
            } else {
                for (Board board : boards) {
                    Button boardButton = createBoardButton(board);
                    boardListContainer.getChildren().add(boardButton);
                }
            }
            
            setStatusMessage("Loaded " + boards.size() + " boards");
        } catch (SQLException e) {
            showError("Failed to load boards: " + e.getMessage());
        }
    }

    /**
     * Create a button for a board
     * @param board Board to create button for
     * @return Button representing the board
     */
    private Button createBoardButton(Board board) {
        Button button = new Button(board.getName());
        button.getStyleClass().add("board-button");
        button.setPrefWidth(300);
        button.setPrefHeight(60);
        
        button.setOnAction(e -> openBoard(board));
        
        return button;
    }

    /**
     * Create a new board
     */
    private void createNewBoard() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Board");
        dialog.setHeaderText("Enter board name:");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            try {
                Board newBoard = boardService.createBoard(result.get().trim());
                setStatusMessage("Created board: " + newBoard.getName());
                loadBoards(); // Refresh the board list
                openBoard(newBoard); // Open the newly created board
            } catch (SQLException e) {
                showError("Failed to create board: " + e.getMessage());
            } catch (ValidationException e) {
                showError("Validation error: " + e.getMessage());
            }
        }
    }

    /**
     * Open a board in new window
     * @param board Board to open
     */
    private void openBoard(Board board) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/board.fxml"));
            Parent root = loader.load();

            BoardController boardController = loader.getController();
            boardController.loadBoard(board.getId());

            Stage boardStage = new Stage();
            boardStage.setTitle("KanBan Board - " + board.getName());
            boardStage.setScene(new Scene(root, 1200, 800));
            boardStage.setMinWidth(800);
            boardStage.setMinHeight(600);
            boardStage.show();

            setStatusMessage("Opened board: " + board.getName());

        } catch (Exception e) {
            showError("Failed to open board: " + e.getMessage());
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
}
