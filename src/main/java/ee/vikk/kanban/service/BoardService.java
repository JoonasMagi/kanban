package ee.vikk.kanban.service;

import ee.vikk.kanban.model.Board;
import ee.vikk.kanban.model.BoardDAO;
import ee.vikk.kanban.model.Column;
import ee.vikk.kanban.model.ColumnDAO;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for Board business logic
 */
public class BoardService {
    private final BoardDAO boardDAO;
    private final ColumnDAO columnDAO;

    /**
     * Constructor with DAO dependencies
     * @param boardDAO Board data access object
     * @param columnDAO Column data access object
     */
    public BoardService(BoardDAO boardDAO, ColumnDAO columnDAO) {
        this.boardDAO = boardDAO;
        this.columnDAO = columnDAO;
    }

    /**
     * Default constructor with default DAOs
     */
    public BoardService() {
        this(new BoardDAO(), new ColumnDAO());
    }

    /**
     * Create a new board with default columns (TODO, IN PROGRESS, DONE)
     * @param name Board name
     * @return Created board with default columns
     * @throws SQLException if database operation fails
     * @throws ValidationException if validation fails
     */
    public Board createBoard(String name) throws SQLException, ValidationException {
        validateBoardName(name);
        
        // Create board
        Board board = new Board(name);
        board.setCreatedAt(LocalDateTime.now());
        board = boardDAO.save(board);
        
        // Create default columns
        createDefaultColumns(board.getId());
        
        // Load board with columns
        return getBoardWithColumns(board.getId());
    }

    /**
     * Get board by ID with its columns
     * @param boardId Board ID
     * @return Board with columns or null if not found
     * @throws SQLException if database operation fails
     */
    public Board getBoardWithColumns(Integer boardId) throws SQLException {
        Board board = boardDAO.findById(boardId);
        if (board != null) {
            List<Column> columns = columnDAO.findByBoardId(boardId);
            board.setColumns(columns);
        }
        return board;
    }

    /**
     * Get all boards
     * @return List of all boards
     * @throws SQLException if database operation fails
     */
    public List<Board> getAllBoards() throws SQLException {
        return boardDAO.findAll();
    }

    /**
     * Update board name
     * @param boardId Board ID
     * @param newName New board name
     * @throws SQLException if database operation fails
     * @throws ValidationException if validation fails
     */
    public void updateBoardName(Integer boardId, String newName) throws SQLException, ValidationException {
        validateBoardName(newName);
        
        Board board = boardDAO.findById(boardId);
        if (board == null) {
            throw new ValidationException("Board not found with ID: " + boardId);
        }
        
        board.setName(newName);
        boardDAO.update(board);
    }

    /**
     * Delete board and all its data
     * @param boardId Board ID
     * @throws SQLException if database operation fails
     */
    public void deleteBoard(Integer boardId) throws SQLException {
        boardDAO.deleteById(boardId);
    }

    /**
     * Create default columns for a board
     * @param boardId Board ID
     * @throws SQLException if database operation fails
     */
    private void createDefaultColumns(Integer boardId) throws SQLException {
        Column todoColumn = new Column(boardId, "TODO", 1);
        todoColumn.setColor("#FF6B6B"); // Red
        columnDAO.save(todoColumn);
        
        Column inProgressColumn = new Column(boardId, "IN PROGRESS", 2);
        inProgressColumn.setColor("#4ECDC4"); // Teal
        columnDAO.save(inProgressColumn);
        
        Column doneColumn = new Column(boardId, "DONE", 3);
        doneColumn.setColor("#45B7D1"); // Blue
        columnDAO.save(doneColumn);
    }

    /**
     * Validate board name
     * @param name Board name to validate
     * @throws ValidationException if validation fails
     */
    private void validateBoardName(String name) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Board name cannot be empty");
        }
        if (name.length() > 100) {
            throw new ValidationException("Board name cannot be longer than 100 characters");
        }
    }
}
